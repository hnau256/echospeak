@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.process.lines

import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.model.utils.Translator
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TranslateModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val textToTranslate: String,
) {

    @Pipe
    interface Dependencies {

        val translator: Translator
    }

    @Serializable
    data class Skeleton(
        val translation: MutableStateFlow<String?> =
            null.toMutableStateFlowAsInitial()
    )

    sealed interface State {

        data class Translate(
            val translate: () -> Unit,
        ) : State

        data object Translating : State

        data class Translated(
            val translation: String,
            val close: () -> Unit,
        ) : State
    }

    private fun setTranslation(
        translation: String?
    ) {
        skeleton.translation.value = translation
    }

    val state: StateFlow<State> = skeleton
        .translation
        .flatMapWithScope(scope) { scope, translationOrNull ->
            translationOrNull.foldNullable(
                ifNotNull = { translation ->
                    State
                        .Translated(
                            translation = translation,
                            close = { setTranslation(null) },

                            ).toMutableStateFlowAsInitial()
                },
                ifNull = {
                    val inProgressRegistry = InProgressRegistry()
                    inProgressRegistry
                        .inProgress
                        .mapState(scope) { isTranslating ->
                            isTranslating.foldBoolean(
                                ifTrue = { State.Translating },
                                ifFalse = {
                                    State.Translate(
                                        translate = {
                                            scope.launch {
                                                inProgressRegistry.executeRegistered {
                                                    dependencies
                                                        .translator
                                                        .translate(textToTranslate)
                                                        .foldNullable(
                                                            ifNull = { /*Show error*/ },
                                                            ifNotNull = ::setTranslation,
                                                        )

                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }
                }
            )
        }
}