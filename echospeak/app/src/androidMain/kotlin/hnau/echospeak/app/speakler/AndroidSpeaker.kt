package hnau.echospeak.app.speakler

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import arrow.core.NonEmptySet
import arrow.core.nonEmptySetOf
import hnau.echospeak.model.process.dto.Gender
import hnau.echospeak.model.process.dto.GenderValues
import hnau.echospeak.model.utils.Speaker
import hnau.echospeak.model.utils.Speaker.Config
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidSpeaker private constructor(
    private val textToSpeech: TextToSpeech,
    private val pitchFactors: GenderValues<Float>,
) : Speaker {

    private val accessCurrentSpeechJobMutex = Mutex()

    private var currentSpeechJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override suspend fun speak(
        gender: Gender,
        text: String,
    ): Boolean = coroutineScope {
        val deferredResult = accessCurrentSpeechJobMutex.withLock {
            async {
                textToSpeech.setPitch(pitchFactors[gender])
                speak(
                    text = text,
                )
            }.also { currentSpeechJob = it }
        }
        deferredResult.await()
    }

    private suspend fun speak(
        text: String,
    ): Boolean = suspendCancellableCoroutine { continuation ->
        textToSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {

                override fun onDone(utteranceId: String) {
                    continuation.resume(true)
                }

                override fun onError(utteranceId: String) {
                    continuation.resume(false)
                }

                override fun onStart(utteranceId: String) {}
            }
        )

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId")

        continuation.invokeOnCancellation {
            textToSpeech.stop()
        }
    }

    class Factory(
        private val context: Context,
    ) : Speaker.Factory {

        override suspend fun createSpeaker(
            config: Config,
            locale: Locale,
        ): Speaker? {

            val textToSpeech = createTextToSpeech(context) ?: return null
            textToSpeech.language = locale
            textToSpeech.setSpeechRate(config.speechRate)

            val voice = textToSpeech.selectBestVoice(
                locale = locale,
                tryUseNetworkVoice = config.tryUseNetworkVoice,
            ) ?: return null
            textToSpeech.voice = voice

            val voiceGender = voice.gender
            return AndroidSpeaker(
                textToSpeech = textToSpeech,
                pitchFactors = GenderValues.create { gender ->
                    calcPitchForGender(
                        voiceGender = voiceGender,
                        targetGender = gender,
                        pitchFactors = config.pitchFactors,
                    )
                }
            )
        }

        companion object {

            private suspend fun createTextToSpeech(
                context: Context,
            ): TextToSpeech? = suspendCoroutine { continuation ->
                var textToSpeech: TextToSpeech? = null
                textToSpeech = TextToSpeech(context) { status ->
                    val localTextToSpeechOrNull = when (status) {
                        TextToSpeech.SUCCESS -> textToSpeech!!
                        else -> null
                    }
                    continuation.resume(localTextToSpeechOrNull)
                }
            }

            private fun TextToSpeech.selectBestVoice(
                locale: Locale,
                tryUseNetworkVoice: Boolean,
            ): Voice? = voices
                .filter { voice ->
                    voice.locale.language.equals(
                        other = locale.language,
                        ignoreCase = true,
                    )
                }
                .sortedWith(
                    compareByDescending(Voice::getQuality)
                        .thenByDescending { tryUseNetworkVoice && it.isNetworkConnectionRequired }
                        .thenByDescending { voice ->
                            voice.locale.country.equals(
                                other = locale.country,
                                ignoreCase = true
                            )
                        }
                        .thenBy(Voice::getLatency)
                )
                .firstOrNull()

            private val Voice.gender: Gender?
                get() = Gender
                    .entries
                    .firstOrNull { gender ->
                        genderVoiceNameElements[gender].any { element ->
                            name.contains(
                                other = element,
                                ignoreCase = true,
                            )
                        }
                    }

            private fun calcPitchForGender(
                voiceGender: Gender?,
                targetGender: Gender,
                pitchFactors: GenderValues<Float>,
            ): Float {
                if (voiceGender == targetGender) {
                    return 1f
                }
                if (voiceGender == null) {
                    return pitchFactors[targetGender]
                }
                return pitchFactors[targetGender] / pitchFactors[voiceGender]
            }

            private val genderVoiceNameElements: GenderValues<NonEmptySet<String>> = GenderValues(
                male = nonEmptySetOf("male", "m1", "m_"),
                female = nonEmptySetOf("female", "f1", "f_"),
            )
        }
    }
}