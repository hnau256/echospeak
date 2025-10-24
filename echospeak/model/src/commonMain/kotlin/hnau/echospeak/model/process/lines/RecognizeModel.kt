package hnau.echospeak.model.process.lines

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.echospeak.model.utils.SpeechRecognizer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecognizeModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    val retry: () -> Unit,
    onReady: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val recognizer: SpeechRecognizer
    }

    private val _recognitionResult: MutableStateFlow<String> = "".toMutableStateFlowAsInitial()

    val recognitionResult: StateFlow<String>
        get() = _recognitionResult

    init {
        scope.launch {
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
}