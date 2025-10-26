@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.process.lines

import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.model.process.dto.Gender
import hnau.echospeak.model.utils.EchoSpeakConfig
import hnau.echospeak.model.utils.Speaker
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ActiveLineModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val gender: Gender,
    onReady: () -> Unit,
    val cancel: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val config: EchoSpeakConfig

        val speaker: Speaker

        fun recognize(): RecognizeModel.Dependencies

        fun translate(): TranslateModel.Dependencies
    }

    val translate = TranslateModel(
        scope = scope,
        dependencies = dependencies.translate(),
        skeleton = skeleton.translate,
        textToTranslate = skeleton.line.text,
    )

    private val recognizeModelVersion: MutableStateFlow<Int> = MutableStateFlow(0)

    val recognize: StateFlow<(() -> Unit)?> = skeleton
        .state
        .mapState(scope) { state ->
            val isWaitingForRecognize = when (state) {
                Skeleton.State.WaitingForRecognize -> true
                Skeleton.State.Speaking, Skeleton.State.Recognizing -> false
            }
            isWaitingForRecognize.ifTrue {
                { skeleton.state.value = Skeleton.State.Recognizing }
            }
        }

    val recognizing: StateFlow<RecognizeModel?> = skeleton
        .state
        .flatMapWithScope(scope) { scope, state ->
            val isRecognizing = when (state) {
                Skeleton.State.Recognizing -> true
                Skeleton.State.Speaking, Skeleton.State.WaitingForRecognize -> false
            }
            isRecognizing.foldBoolean(
                ifFalse = { null.toMutableStateFlowAsInitial() },
                ifTrue = {
                    recognizeModelVersion
                        .mapWithScope(scope) { scope, _ ->
                            RecognizeModel(
                                scope = scope,
                                dependencies = dependencies.recognize(),
                                retry = { recognizeModelVersion.update { it + 1 } },
                                onReady = {
                                    scope.launch {
                                        delay(dependencies.config.pauseAfterLine)
                                        onReady()
                                    }
                                },
                                originalText = skeleton.line.text,
                            )
                        }
                }
            )
        }

    init {
        scope.launch {
            skeleton
                .state
                .collectLatest { state ->
                    val isSpeaking = when (state) {
                        Skeleton.State.Speaking -> true
                        Skeleton.State.WaitingForRecognize, Skeleton.State.Recognizing -> false
                    }
                    if (!isSpeaking) {
                        return@collectLatest
                    }
                    dependencies
                        .speaker
                        .speak(
                            gender = gender,
                            text = skeleton.line.text,
                        )
                    skeleton.state.value = Skeleton.State.WaitingForRecognize
                }
        }
    }

    @Serializable
    data class Skeleton(
        val line: LineSkeleton,
        val state: MutableStateFlow<State> = State.Speaking.toMutableStateFlowAsInitial(),
        val translate: TranslateModel.Skeleton = TranslateModel.Skeleton(),
    ) {

        enum class State { Speaking, WaitingForRecognize, Recognizing }

    }

    val text: String
        get() = skeleton.line.text
}