@file:UseSerializers(
    NonEmptyListSerializer::class,
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.themes

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.coroutines.Stickable
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.predeterminated
import hnau.common.kotlin.coroutines.stick
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.groupByToNonEmpty
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.mapSecond
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.echospeak.model.themes.dto.Phrase
import hnau.echospeak.model.themes.dto.ThemeId
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ChooseOrProcessThemesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val themes: Map<ThemeId, NonEmptyList<Phrase>>,
) {

    @Pipe
    interface Dependencies {

        fun process(): ProcessThemesModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val unselectedThemes: MutableStateFlow<Set<ThemeId>> =
            emptySet<ThemeId>().toMutableStateFlowAsInitial(),

        val launched: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),

        var process: ProcessThemesModel.Skeleton? = null,
    )

    sealed interface State {

        data class Launched(
            val themes: NonEmptyList<KeyValue<ThemeId, NonEmptyList<Phrase>>>,
        ) : State

        data class NotLaunched(
            val themes: List<Triple<ThemeId, NonEmptyList<Phrase>, Boolean>>,
        ) : State
    }

    val state: StateFlow<ChooseOrProcessStateModel> = combineState(
        scope = scope,
        a = skeleton.launched,
        b = skeleton.unselectedThemes,
    ) { launched, unselectedThemes ->
        themes
            .toList()
            .groupByToNonEmpty { (id, phrases) ->
                val selected = id !in unselectedThemes
                KeyValue(
                    key = selected,
                    value = KeyValue(id, phrases)
                )
            }
            .let { themes ->
                themes[true].takeIf { launched }.foldNullable(
                    ifNull = {
                        State.NotLaunched(
                            themes = themes
                                .toList()
                                .flatMap { (selected, themes) ->
                                    themes.map { (id, phrases) ->
                                        Triple(id, phrases, selected)
                                    }
                                }
                                .sortedBy { it.first.id },
                        )
                    },
                    ifNotNull = { launchedThemes ->
                        State.Launched(
                            themes = launchedThemes,
                        )
                    }
                )
            }
    }
        .stick(scope) { scope, state ->
            when (state) {
                is State.Launched -> Stickable.predeterminated(
                    ChooseOrProcessStateModel.Process(
                        model = ProcessThemesModel(
                            scope = scope,
                            dependencies = dependencies.process(),
                            skeleton = skeleton::process
                                .toAccessor()
                                .getOrInit { ProcessThemesModel.Skeleton() },
                            themes = state.themes,
                        ),
                    )
                )

                is State.NotLaunched -> ChooseStickable(
                    scope = scope,
                    initial = state,
                    skeleton = skeleton,
                )
            }
        }

    private class ChooseStickable(
        private val scope: CoroutineScope,
        initial: State.NotLaunched,
        private val skeleton: Skeleton,
    ) : Stickable<State, ChooseOrProcessStateModel.Choose> {

        private val themes: List<Pair<ThemeId, MutableStateFlow<Boolean>>> = initial
            .themes
            .map { (id, _, selected) ->
                id to selected.toMutableStateFlowAsInitial()
            }

        private val allIds: Set<ThemeId> = themes
            .map(Pair<ThemeId, *>::first)
            .toSet()

        private val selectedThemes: StateFlow<Set<ThemeId>> = themes
            .map { idWithSelected ->
                idWithSelected.mapSecond { selected ->
                    selected as StateFlow<Boolean>
                }
            }
            .fold<_, StateFlow<Set<ThemeId>>>(
                initial = emptySet<ThemeId>().toMutableStateFlowAsInitial()
            ) { acc, (id, selected) ->
                combineState(
                    scope = scope,
                    a = acc,
                    b = selected,
                ) { currentAcc, currentSelected ->
                    currentSelected.foldBoolean(
                        ifTrue = { currentAcc + id },
                        ifFalse = { currentAcc },
                    )
                }
            }

        override val result: ChooseOrProcessStateModel.Choose
            get() = ChooseOrProcessStateModel.Choose(
                ChooseThemesModel(
                    themes = themes.map { (id, selected) ->
                        ChooseThemesModel.Theme(
                            id = id,
                            isSelected = selected,
                            switchIsSelected = {
                                skeleton.unselectedThemes.update { unselectedThemes ->
                                    (id !in unselectedThemes).foldBoolean(
                                        ifTrue = { unselectedThemes + id },
                                        ifFalse = { unselectedThemes - id },
                                    )
                                }
                            },
                        )
                    },
                    launch = selectedThemes
                        .mapState(scope) { selectedThemes ->
                            val atLeastOneIsSelected = selectedThemes.isNotEmpty()
                            atLeastOneIsSelected.ifTrue { { skeleton.launched.value = true } }
                        },
                    selectAll = selectedThemes.mapState(scope) { selectedThemes ->
                        when (selectedThemes) {
                            allIds -> null
                            else -> {
                                { skeleton.unselectedThemes.value = emptySet() }
                            }
                        }
                    },
                    selectNone = selectedThemes.mapState(scope) { selectedThemes ->
                        when (selectedThemes.size) {
                            0 -> null
                            else -> {
                                { skeleton.unselectedThemes.value = allIds }
                            }
                        }
                    },
                )
            )

        override fun tryUpdateValue(
            newValue: State,
        ): Boolean = when (newValue) {
            is State.Launched -> false
            is State.NotLaunched -> {
                val selectedThemes = newValue.themes.associate { (id, _, selected) ->
                    id to selected
                }
                themes.forEach { (id, selected) ->
                    selected.value = selectedThemes.getValue(id)
                }
                true
            }
        }
    }

    val goBackHandler: GoBackHandler = state.flatMapWithScope(scope) { scope, state ->
        when (state) {
            is ChooseOrProcessStateModel.Choose -> state.model.goBackHandler
            is ChooseOrProcessStateModel.Process -> state
                .model
                .goBackHandler
                .mapState(scope) { processGoBackHandler ->
                    processGoBackHandler.ifNull { { skeleton.launched.value = false } }
                }
        }
    }
}