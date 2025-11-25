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
import hnau.echospeak.model.themes.dto.PhraseVariant
import hnau.echospeak.model.utils.EchoSpeakConfig
import hnau.echospeak.model.utils.Speaker
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class PhraseModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val phrase: ChosenVariant<PhraseVariant>,
    private val complete: suspend (newFactor: KnowFactor) -> Unit,
) {

    @Pipe
    interface Dependencies {

        val config: EchoSpeakConfig

        val speaker: Speaker

        fun recognize(): RecognizeModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val state: MutableStateFlow<PhraseStateModel.Skeleton> =
            PhraseStateModel.Skeleton.WaitingForRecognizing.toMutableStateFlowAsInitial(),
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
                        textToRecognize = phrase.variant.phrase.phrase,
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
                                phrase
                                    .learnInfo
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