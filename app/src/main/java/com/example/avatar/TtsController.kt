package com.example.avatar

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsController(context: Context) {

    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentPhonemeLevel = MutableStateFlow(0f)
    val currentPhonemeLevel: StateFlow<Float> = _currentPhonemeLevel.asStateFlow()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let {
                    val result = it.setLanguage(Locale("es", "ES"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        it.setLanguage(Locale.US)
                    }
                    it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            _currentPhonemeLevel.value = 0f
                        }

                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            _currentPhonemeLevel.value = 0f
                        }
                    })
                }
            } else {
                Log.w("TtsController", "TTS initialization failed: $status")
            }
        }
    }

    fun speak(text: String, pitch: Float = 1.0f, speed: Float = 1.0f) {
        if (text.isBlank()) return
        tts?.apply {
            setPitch(pitch)
            setSpeechRate(speed)
            speak(text, TextToSpeech.QUEUE_FLUSH, null, "CineGenAvatarUtterance")
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentPhonemeLevel.value = 0f
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
