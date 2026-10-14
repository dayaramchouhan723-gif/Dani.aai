package com.example.voice

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onFinalText: (String) -> Unit,
    private val onSpeakingFinished: () -> Unit
) {
    private val tag = "MyraVoiceManager"

    // Speech Recognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    // TTS
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingSpeakText: String? = null

    // State flows for UI
    private val _isSpeechAvailable = MutableStateFlow(SpeechRecognizer.isRecognitionAvailable(context))
    val isSpeechAvailable: StateFlow<Boolean> = _isSpeechAvailable.asStateFlow()

    private val _liveRmsDb = MutableStateFlow(0f)
    val liveRmsDb: StateFlow<Float> = _liveRmsDb.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Tone & Haptics
    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 65)
    } catch (_: Exception) {
        null
    }

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                textToSpeech?.language = Locale.US
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeakingFinished()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })

                pendingSpeakText?.let {
                    speak(it)
                    pendingSpeakText = null
                }
            } else {
                Log.e(tag, "TTS Initialization failed with status: $status")
            }
        }
    }

    fun startListening(haptic: Boolean = true, chime: Boolean = true) {
        stopSpeaking()

        if (chime) playChime(isStart = true)
        if (haptic) triggerHaptic()

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            _partialTranscript.value = ""
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start speech recognition: ${e.message}")
            isListening = false
        }
    }

    fun stopListening(chime: Boolean = true) {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(tag, "Error stopping listening: ${e.message}")
            }
            isListening = false
            if (chime) playChime(isStart = false)
        }
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
        isListening = false
        _partialTranscript.value = ""
        _liveRmsDb.value = 0f
    }

    fun speak(text: String, rate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isTtsInitialized || textToSpeech == null) {
            pendingSpeakText = text
            return
        }

        stopSpeaking()
        textToSpeech?.setSpeechRate(rate)
        textToSpeech?.setPitch(pitch)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MYRA_SPEECH_${System.currentTimeMillis()}")
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "MYRA_SPEECH_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        try {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
            }
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _liveRmsDb.value = 0.1f
            }

            override fun onBeginningOfSpeech() {
                _liveRmsDb.value = 0.3f
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize rmsdB (-2 to 10+ dB typical) to 0.0 - 1.0
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                _liveRmsDb.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _liveRmsDb.value = 0f
                isListening = false
            }

            override fun onError(error: Int) {
                _liveRmsDb.value = 0f
                isListening = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue with voice recognition."
                    else -> "Speech recognition paused."
                }
                Log.d(tag, "Speech recognition error: $error ($errorMsg)")
            }

            override fun onResults(results: Bundle?) {
                _liveRmsDb.value = 0f
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull()?.trim()
                if (!recognized.isNullOrBlank()) {
                    _partialTranscript.value = recognized
                    onFinalText(recognized)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let {
                    _partialTranscript.value = it
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun playChime(isStart: Boolean) {
        try {
            toneGenerator?.startTone(
                if (isStart) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_PROMPT,
                150
            )
        } catch (_: Exception) {}
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (_: Exception) {}
    }

    fun release() {
        cancelListening()
        speechRecognizer?.destroy()
        speechRecognizer = null

        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null

        toneGenerator?.release()
        toneGenerator = null
    }
}
