package com.prexoft.prexocore

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

object EasyTts {
    private var tts: TextToSpeech? = null
    private var initialized = false
    private var initializing = false
    private val pendingQueue = CopyOnWriteArrayList<() -> Unit>()
    private var lastRequestedLocale: Locale = Locale.getDefault()

    private fun ensureInitialized(
        context: Context,
        locale: Locale,
        onReady: (success: Boolean) -> Unit
    ) {
        if (initialized) {
            onReady(true)
            return
        }

        lastRequestedLocale = locale
        if (initializing) {
            pendingQueue.add { onReady(initialized) }
            return
        }

        initializing = true
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(locale)
                val localeSupported = when (result) {
                    TextToSpeech.LANG_AVAILABLE,
                    TextToSpeech.LANG_COUNTRY_AVAILABLE,
                    TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
                    else -> false
                }
                initialized = localeSupported
                if (!localeSupported) {
                    tts?.language = Locale.getDefault()
                }
            } else {
                initialized = false
            }
            initializing = false
            val toRun = pendingQueue.toList()
            pendingQueue.clear()
            toRun.forEach { it() }
            onReady(initialized)
        }
    }

    fun speak(
        context: Context,
        text: String,
        rate: Float,
        pitch: Float,
        locale: Locale,
        onDone: (() -> Unit)?
    ) {
        ensureInitialized(context, locale) { success ->
            if (!success) {
                onDone?.invoke()
                return@ensureInitialized
            }

            val engine = tts ?: run {
                onDone?.invoke()
                return@ensureInitialized
            }

            engine.setSpeechRate(rate.coerceAtLeast(0.1f))
            engine.setPitch(pitch.coerceAtLeast(0.1f))
            engine.language = locale

            val utteranceId = UUID.randomUUID().toString()

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {  }

                override fun onDone(utteranceId: String) {
                    onDone?.invoke()
                }

                override fun onError(utteranceId: String) {
                    onDone?.invoke()
                }

                override fun onError(utteranceId: String, errorCode: Int) {
                    onDone?.invoke()
                }
            })

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
        initializing = false
        pendingQueue.clear()
    }
}