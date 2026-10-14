package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentActionDispatcher
import com.example.agent.GeminiAgentService
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.VoiceNoteEntity
import com.example.data.preferences.AssistantPreferences
import com.example.model.AssistantPersona
import com.example.model.AssistantState
import com.example.model.ChatMessage
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.voiceAssistantDao()
    val preferences = AssistantPreferences(application)
    private val actionDispatcher = AgentActionDispatcher(application)
    private val geminiService = GeminiAgentService()

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _statusText = MutableStateFlow("Tap the microphone or say a command")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    // Persistent Chat Messages
    val chatMessages: StateFlow<List<ChatMessage>> = dao.getAllMessages()
        .map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    text = entity.text,
                    isUser = entity.isUser,
                    timestamp = entity.timestamp,
                    actionType = entity.actionType,
                    actionDetail = entity.actionDetail,
                    isError = entity.isError
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Persistent Notes
    val voiceNotes: StateFlow<List<VoiceNoteEntity>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Voice Manager instance
    private var voiceManager: VoiceManager? = null

    val liveRmsDb: StateFlow<Float> get() = voiceManager?.liveRmsDb ?: MutableStateFlow(0f)
    val partialTranscript: StateFlow<String> get() = voiceManager?.partialTranscript ?: MutableStateFlow("")
    val isSpeaking: StateFlow<Boolean> get() = voiceManager?.isSpeaking ?: MutableStateFlow(false)

    init {
        initVoiceManager()
    }

    private fun initVoiceManager() {
        voiceManager = VoiceManager(
            context = getApplication(),
            onFinalText = { spokenText ->
                onUserSpoke(spokenText)
            },
            onSpeakingFinished = {
                _assistantState.value = AssistantState.IDLE
                _statusText.value = "Ready"
                if (preferences.continuousMode.value) {
                    // In continuous conversation mode, listen again after speaking!
                    startListening()
                }
            }
        )
    }

    fun startListening() {
        _assistantState.value = AssistantState.LISTENING
        _statusText.value = "Listening to you..."
        voiceManager?.startListening(
            haptic = preferences.hapticFeedback.value,
            chime = preferences.audioChimes.value
        )
    }

    fun stopListening() {
        voiceManager?.stopListening(chime = preferences.audioChimes.value)
        if (_assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.IDLE
            _statusText.value = "Ready"
        }
    }

    fun toggleListening() {
        if (_assistantState.value == AssistantState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun stopSpeaking() {
        voiceManager?.stopSpeaking()
        if (_assistantState.value == AssistantState.SPEAKING) {
            _assistantState.value = AssistantState.IDLE
            _statusText.value = "Ready"
        }
    }

    fun onUserSpoke(text: String) {
        if (text.isBlank()) {
            _assistantState.value = AssistantState.IDLE
            _statusText.value = "Ready"
            return
        }
        processUserInput(text)
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        processUserInput(text)
    }

    private fun processUserInput(input: String) {
        viewModelScope.launch {
            _assistantState.value = AssistantState.THINKING
            _statusText.value = "Thinking..."

            // 1. Save user message to database
            val userMsg = ChatMessage(text = input, isUser = true)
            dao.insertMessage(
                ChatMessageEntity(
                    id = userMsg.id,
                    text = userMsg.text,
                    isUser = true,
                    timestamp = userMsg.timestamp,
                    actionType = null,
                    actionDetail = null,
                    isError = false
                )
            )

            // 2. Query Agent Intelligence (Gemini or local engine)
            val history = chatMessages.value.takeLast(6).map { it.text to it.isUser }
            val currentPersona = preferences.persona.value
            val response = geminiService.queryAssistant(input, history, currentPersona)

            // 3. Execute action if triggered
            var actionSummary: String? = null
            var actionDetail: String? = null
            if (response.actionType != null) {
                _statusText.value = "Executing ${response.actionType}..."
                val actionResult = actionDispatcher.executeAction(
                    response.actionType,
                    response.actionParam ?: ""
                )
                actionSummary = actionResult.summary
                actionDetail = actionResult.detail
            }

            // 4. Save Assistant Response to database
            val assistantMsg = ChatMessage(
                text = response.spokenText,
                isUser = false,
                actionType = response.actionType,
                actionDetail = actionSummary ?: actionDetail,
                isError = false
            )
            dao.insertMessage(
                ChatMessageEntity(
                    id = assistantMsg.id,
                    text = assistantMsg.text,
                    isUser = false,
                    timestamp = assistantMsg.timestamp,
                    actionType = assistantMsg.actionType,
                    actionDetail = assistantMsg.actionDetail,
                    isError = false
                )
            )

            // 5. Speak response aloud if auto-speak is enabled
            if (preferences.autoSpeak.value && response.spokenText.isNotBlank()) {
                _assistantState.value = AssistantState.SPEAKING
                _statusText.value = "Speaking..."
                voiceManager?.speak(
                    text = response.spokenText,
                    rate = preferences.speechRate.value,
                    pitch = preferences.speechPitch.value
                )
            } else {
                _assistantState.value = AssistantState.IDLE
                _statusText.value = "Ready"
            }
        }
    }

    fun replayMessage(text: String) {
        _assistantState.value = AssistantState.SPEAKING
        _statusText.value = "Speaking..."
        voiceManager?.speak(
            text = text,
            rate = preferences.speechRate.value,
            pitch = preferences.speechPitch.value
        )
    }

    fun clearChat() {
        viewModelScope.launch {
            dao.clearMessages()
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            dao.deleteNote(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.release()
    }
}
