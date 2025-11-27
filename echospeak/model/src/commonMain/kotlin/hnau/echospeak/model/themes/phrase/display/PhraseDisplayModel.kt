package hnau.echospeak.model.themes.phrase.display

import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.echospeak.model.themes.DisplayMode
import hnau.echospeak.model.themes.dto.Phrase
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class PhraseDisplayModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val phrase: Phrase

        val displayMode: StateFlow<DisplayMode>

        fun text(): TextDisplayModel.Dependencies

        fun speech(): SpeechDisplayModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val overwrittenDisplayMode: MutableStateFlow<DisplayMode?> = null.toMutableStateFlowAsInitial(),
    )

    sealed interface State {

        data class Text(
            val model: TextDisplayModel,
        ) : State

        data class Speech(
            val model: SpeechDisplayModel,
        ) : State
    }

    val state: StateFlow<KeyValue<DisplayMode, State>> = skeleton
        .overwrittenDisplayMode
        .flatMapWithScope(scope) { scope, overwrittenOrNull ->
            overwrittenOrNull
                .foldNullable(
                    ifNull = { dependencies.displayMode },
                    ifNotNull = DisplayMode::toMutableStateFlowAsInitial,
                )
                .mapWithScope(scope) { scope, displayMode ->
                    val state = when (displayMode) {
                        DisplayMode.Text -> State.Text(
                            model = TextDisplayModel(
                                dependencies = dependencies.text(),
                            ),
                        )

                        DisplayMode.Speech -> State.Speech(
                            model = SpeechDisplayModel(
                                scope = scope,
                                dependencies = dependencies.speech(),
                            ),
                        )
                    }
                    KeyValue(
                        key = displayMode,
                        value = state,
                    )
                }
        }

    fun setDisplayMode(
        displayMode: DisplayMode,
    ) {
        skeleton.overwrittenDisplayMode.value = displayMode
    }
}