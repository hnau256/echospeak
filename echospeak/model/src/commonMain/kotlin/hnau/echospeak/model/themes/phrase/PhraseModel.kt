@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.themes.phrase

import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.engine.ChosenVariant
import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.model.themes.dto.Phrase
import hnau.echospeak.model.themes.phrase.display.PhraseDisplayModel
import hnau.echospeak.model.utils.EchoSpeakConfig
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class PhraseModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val complete: suspend (newFactor: KnowFactor) -> Unit,
) {

    @Pipe
    interface Dependencies {

        val learnInfo: ChosenVariant.LearnInfo?

        val phrase: Phrase

        val config: EchoSpeakConfig

        fun recognize(): RecognizeModel.Dependencies

        fun display(): PhraseDisplayModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val state: MutableStateFlow<PhraseStateModel.Skeleton> =
            PhraseStateModel.Skeleton.WaitingForRecognizing.toMutableStateFlowAsInitial(),
        val display: PhraseDisplayModel.Skeleton = PhraseDisplayModel.Skeleton(),
    )

    val learnInfo: ChosenVariant.LearnInfo?
        get() = dependencies.learnInfo

    val display = PhraseDisplayModel(
        scope = scope,
        dependencies = dependencies.display(),
        skeleton = skeleton.display,
    )

    private fun updateState(
        state: PhraseStateModel.Skeleton,
    ) {
        skeleton.state.value = state
    }

    val state: StateFlow<PhraseStateModel> = skeleton
        .state
        .mapWithScope(scope) { scope, state ->
            when (state) {
                PhraseStateModel.Skeleton.WaitingForRecognizing -> PhraseStateModel.WaitingForRecognizing(
                    recognize = { updateState(PhraseStateModel.Skeleton.Recognizing) },
                )

                PhraseStateModel.Skeleton.Recognizing -> PhraseStateModel.Recognizing(
                    model = RecognizeModel(
                        scope = scope,
                        dependencies = dependencies.recognize(),
                        textToRecognize = dependencies.phrase.phrase,
                        onReady = { result ->
                            updateState(
                                PhraseStateModel.Skeleton.Recognized(
                                    result = result,
                                )
                            )
                        },
                        cancel = { updateState(PhraseStateModel.Skeleton.WaitingForRecognizing) },
                    ),
                )

                is PhraseStateModel.Skeleton.Recognized -> PhraseStateModel.Recognized(
                    similarity = state.result.similarity,
                    retry = { updateState(PhraseStateModel.Skeleton.Recognizing) },
                    complete = (state.result.similarity.satisfactory).ifTrue {
                        actionOrNullIfExecuting(scope) {
                            complete(
                                learnInfo
                                    ?.info
                                    ?.knowFactor
                                    ?.times(dependencies.config.correctAnswerKnowFactorFactor)
                                    ?: KnowFactor.initial
                            )
                        }
                    }
                )
            }
        }

}