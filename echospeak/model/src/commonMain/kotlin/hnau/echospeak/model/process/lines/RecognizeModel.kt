package hnau.echospeak.model.process.lines

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.echospeak.model.utils.EchoSpeakConfig
import hnau.echospeak.model.utils.SpeechRecognizer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apache.commons.text.similarity.LevenshteinDistance
import java.text.Normalizer
import kotlin.math.max

class RecognizeModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    originalText: String,
    val retry: () -> Unit,
    onReady: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val config: EchoSpeakConfig

        val recognizer: SpeechRecognizer
    }

    private val originalText: String = originalText.normalize()

    private val _correctFactor: MutableStateFlow<Pair<Float, Boolean>?> = null.toMutableStateFlowAsInitial()

    val correctFactor: StateFlow<Pair<Float, Boolean>?>
        get() = _correctFactor

    private val _recognitionResult: MutableStateFlow<String> = "".toMutableStateFlowAsInitial()

    val recognitionResult: StateFlow<String>
        get() = _recognitionResult

    init {
        scope.launch {
            val text = dependencies
                .recognizer
                .recognize()
                .let { launch ->
                    coroutineScope {
                        val collectCurrentJob = launch {
                            launch
                                .current
                                .collect { current ->
                                    _recognitionResult.value = current
                                }
                        }
                        val result = launch.result.await()
                        _recognitionResult.value = result
                        collectCurrentJob.cancel()
                        result
                    }
                }

            val factor = calcCorrectFactor(text)
            val success = factor > dependencies.config.minAllowedRecognitionSimilarity

            _correctFactor.value = factor to success

            if (success) {
                onReady()
            }
        }
    }

    private fun String.normalize(): String = Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .filter { ch ->
            when {
                Character.getType(ch) == Character.NON_SPACING_MARK.toInt() -> false
                ch == ' ' -> true
                ch.isLetterOrDigit() -> true
                else -> false
            }
        }
        .lowercase()
        .trim()

    private fun calcCorrectFactor(
        text: String,
    ): Float {

        val normalizedText = text.normalize()

        val maxLength = max(normalizedText.length, originalText.length)
            .takeIf { it > 0 }
            ?.toFloat()
            ?: return 1f

        val distance = LevenshteinDistance
            .getDefaultInstance()
            .apply(originalText, normalizedText)
            .toFloat()

        return 1f - (distance / maxLength)
    }
}