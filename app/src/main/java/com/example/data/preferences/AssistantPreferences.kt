package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AssistantPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AssistantPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dani_assistant_prefs", Context.MODE_PRIVATE)

    private val _persona = MutableStateFlow(
        try {
            AssistantPersona.valueOf(
                prefs.getString("persona", AssistantPersona.GF_MODE.name)
                    ?: AssistantPersona.GF_MODE.name
            )
        } catch (_: Exception) {
            AssistantPersona.GF_MODE
        }
    )
    val persona: StateFlow<AssistantPersona> = _persona.asStateFlow()

    private val _speechRate = MutableStateFlow(prefs.getFloat("speech_rate", 1.05f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(prefs.getFloat("speech_pitch", 1.15f))
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _autoSpeak = MutableStateFlow(prefs.getBoolean("auto_speak", true))
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean("haptic_feedback", true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _audioChimes = MutableStateFlow(prefs.getBoolean("audio_chimes", true))
    val audioChimes: StateFlow<Boolean> = _audioChimes.asStateFlow()

    private val _continuousMode = MutableStateFlow(prefs.getBoolean("continuous_mode", false))
    val continuousMode: StateFlow<Boolean> = _continuousMode.asStateFlow()

    private val _isVipUnlocked = MutableStateFlow(prefs.getBoolean("vip_unlocked", false))
    val isVipUnlocked: StateFlow<Boolean> = _isVipUnlocked.asStateFlow()

    private val _wakeWordServiceActive = MutableStateFlow(prefs.getBoolean("wake_word_service", false))
    val wakeWordServiceActive: StateFlow<Boolean> = _wakeWordServiceActive.asStateFlow()

    fun setPersona(persona: AssistantPersona) {
        prefs.edit().putString("persona", persona.name).apply()
        _persona.value = persona
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat("speech_rate", rate).apply()
        _speechRate.value = rate
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat("speech_pitch", pitch).apply()
        _speechPitch.value = pitch
    }

    fun setAutoSpeak(autoSpeak: Boolean) {
        prefs.edit().putBoolean("auto_speak", autoSpeak).apply()
        _autoSpeak.value = autoSpeak
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
        _hapticFeedback.value = enabled
    }

    fun setAudioChimes(enabled: Boolean) {
        prefs.edit().putBoolean("audio_chimes", enabled).apply()
        _audioChimes.value = enabled
    }

    fun setContinuousMode(enabled: Boolean) {
        prefs.edit().putBoolean("continuous_mode", enabled).apply()
        _continuousMode.value = enabled
    }

    fun unlockVip(code: String): Boolean {
        if (code.trim().equals("OMG", ignoreCase = true)) {
            prefs.edit().putBoolean("vip_unlocked", true).apply()
            _isVipUnlocked.value = true
            return true
        }
        return false
    }

    fun setWakeWordServiceActive(active: Boolean) {
        prefs.edit().putBoolean("wake_word_service", active).apply()
        _wakeWordServiceActive.value = active
    }
}
