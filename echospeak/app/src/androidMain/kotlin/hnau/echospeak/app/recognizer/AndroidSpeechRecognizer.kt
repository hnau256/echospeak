package hnau.echospeak.app.recognizer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import arrow.core.nonEmptySetOf
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.echospeak.app.permissions.PermissionRequester
import hnau.echospeak.model.utils.SpeechRecognizer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import android.speech.SpeechRecognizer as SystemSpeechRecognizer

class AndroidSpeechRecognizer(
    private val applicationContext: Context,
    private val intent: Intent,
) : SpeechRecognizer {

    class Launch(
        applicationContext: Context,
        intent: Intent,
    ) : SpeechRecognizer.Launch {

        override val current: MutableStateFlow<String> =
            "".toMutableStateFlowAsInitial()

        override val result: CompletableDeferred<String> = CompletableDeferred()

        private val recognizer: SystemSpeechRecognizer = SystemSpeechRecognizer
            .createSpeechRecognizer(applicationContext)
            .apply {
                setRecognitionListener(
                    object : RecognitionListener {

                        override fun onPartialResults(
                            partialResults: Bundle,
                        ) {
                            val currentText = partialResults
                                .getStringArrayList(SystemSpeechRecognizer.RESULTS_RECOGNITION)
                                ?.firstOrNull()
                                .orEmpty()
                            current.value = currentText
                        }

                        override fun onResults(
                            results: Bundle,
                        ) {
                            val resultText = results
                                .getStringArrayList(SystemSpeechRecognizer.RESULTS_RECOGNITION)
                                ?.firstOrNull()
                                .orEmpty()
                            result.complete(resultText)
                        }

                        override fun onBeginningOfSpeech() {}

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {}

                        override fun onError(error: Int) {
                            //TODO()
                            result.complete("<Error>")
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}

                        override fun onReadyForSpeech(params: Bundle?) {}

                        override fun onRmsChanged(rmsdB: Float) {}

                    }
                )
                startListening(intent)
            }

        fun cancel() {
            result.complete("")
            recognizer.cancel()
        }
    }

    private var lastLaunch: Launch? = null
        set(value) {
            field?.cancel()
            field = value
        }

    @Synchronized
    override fun recognize(): SpeechRecognizer.Launch = Launch(
        intent = intent,
        applicationContext = applicationContext,
    ).also(::lastLaunch::set)

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
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            return AndroidSpeechRecognizer(
                applicationContext = applicationContext,
                intent = intent,
            )
        }
    }
}