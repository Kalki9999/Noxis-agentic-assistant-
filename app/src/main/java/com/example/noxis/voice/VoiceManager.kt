package com.example.noxis.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(context: Context) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
  private var isInitialized = false

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  var speechRate: Float = 1.0f
    set(value) {
      field = value
      tts?.setSpeechRate(value)
    }

  var speechPitch: Float = 1.0f
    set(value) {
      field = value
      tts?.setPitch(value)
    }

  var isVoiceEnabled: Boolean = true

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      tts?.let { engine ->
        val result = engine.setLanguage(Locale.US)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
          Log.w("VoiceManager", "Language US not supported, using default locale")
          engine.setLanguage(Locale.getDefault())
        }
        engine.setSpeechRate(speechRate)
        engine.setPitch(speechPitch)

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
          }

          override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
          }

          @Deprecated("Deprecated in Java")
          override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
          }

          override fun onError(utteranceId: String?, errorCode: Int) {
            _isSpeaking.value = false
          }
        })
        isInitialized = true
      }
    } else {
      Log.e("VoiceManager", "TextToSpeech init failed with status $status")
    }
  }

  fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
    if (!isVoiceEnabled || text.isBlank()) return
    if (!isInitialized || tts == null) {
      Log.w("VoiceManager", "TTS engine not yet ready")
      return
    }

    val params = Bundle().apply {
      putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "jarvis_speech_${System.currentTimeMillis()}")
    }
    tts?.speak(text, queueMode, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
  }

  fun stop() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun shutdown() {
    try {
      tts?.stop()
      tts?.shutdown()
      tts = null
      isInitialized = false
    } catch (e: Exception) {
      Log.w("VoiceManager", "Shutdown error", e)
    }
  }
}
