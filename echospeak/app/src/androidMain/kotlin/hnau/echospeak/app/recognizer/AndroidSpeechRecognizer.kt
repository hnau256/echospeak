package hnau.echospeak.app.recognizer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import arrow.core.nonEmptySetOf
import hnau.echospeak.app.permissions.PermissionRequester
import hnau.echospeak.model.utils.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale
import android.speech.SpeechRecognizer as SystemSpeechRecognizer

class AndroidSpeechRecognizer(
    private val applicationContext: Context,
    private val intent: Intent,
) : SpeechRecognizer {

    @Synchronized
    override fun recognize(): Flow<SpeechRecognizer.State> = callbackFlow {

        val listener = object : RecognitionListener {

            override fun onPartialResults(
                partialResults: Bundle,
            ) {
                val currentText = partialResults
                    .getStringArrayList(SystemSpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                trySend(
                    SpeechRecognizer.State(
                        recognizedText = currentText,
                        stage = SpeechRecognizer.State.Stage.InProgress,
                    )
                )
            }

            override fun onResults(
                results: Bundle,
            ) {
                val resultText = results
                    .getStringArrayList(SystemSpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                trySend(
                    SpeechRecognizer.State(
                        recognizedText = resultText,
                        stage = SpeechRecognizer.State.Stage.Finished,
                    )
                )
            }

            override fun onBeginningOfSpeech() {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                //TODO()
                trySend(
                    SpeechRecognizer.State(
                        recognizedText = "<Error>",
                        stage = SpeechRecognizer.State.Stage.Finished,
                    )
                )
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onRmsChanged(rmsdB: Float) {}

        }

        val recognizer = SystemSpeechRecognizer
            .createSpeechRecognizer(applicationContext)

        recognizer.setRecognitionListener(listener)
        recognizer.startListening(intent)

        awaitClose {
            recognizer.setRecognitionListener(null)
            recognizer.cancel()
        }
    }
        .flowOn(Dispatchers.Main)
        .buffer(
            capacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    class Factory(
        private val applicationContext: Context,
        private val permissionRequester: PermissionRequester,
    ) : SpeechRecognizer.Factory {

        override suspend fun create(
            locale: Locale,
        ): SpeechRecognizer? {

            val available = SystemSpeechRecognizer.isRecognitionAvailable(applicationContext)
            if (!available) {
                return null
            }

            val recordAudioPermissionGranted = permissionRequester.request(
                nonEmptySetOf(
                    Manifest.permission.RECORD_AUDIO,
                )
            )
            if (!recordAudioPermissionGranted) {
                return null
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            }

            return AndroidSpeechRecognizer(
                applicationContext = applicationContext,
                intent = intent,
            )
        }
    }
}