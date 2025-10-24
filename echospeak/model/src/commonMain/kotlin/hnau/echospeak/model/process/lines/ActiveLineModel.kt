package hnau.echospeak.model.process.lines

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.echospeak.model.process.dto.Gender
import hnau.echospeak.model.utils.Speaker
import hnau.echospeak.model.utils.SpeechRecognizer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class ActiveLineModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val gender: Gender,
    onReady: () -> Unit,
    cancel: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val speaker: Speaker

        val recognizer: SpeechRecognizer
    }

    private val _recognitionResult: MutableStateFlow<String?> =
        null.toMutableStateFlowAsInitial()

    val recognitionResult: StateFlow<String?>
        get() = _recognitionResult

    init {
        scope.launch {

            dependencies
                .speaker
                .speak(
                    gender = gender,
                    text = skeleton.text.replace(';', '?'),
                )

            _recognitionResult.value = ""
            dependencies
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
                    }
                }

            onReady()
        }
    }

    @Serializable
    data class Skeleton(
        val text: String,
    )

    val text: String
        get() = skeleton.text
}