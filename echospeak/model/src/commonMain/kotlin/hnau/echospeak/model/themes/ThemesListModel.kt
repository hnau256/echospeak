@file:UseSerializers(
    NonEmptySetSerializer::class,
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.themes

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptySetSerializer
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifFalse
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.model.themes.dto.Phrase
import hnau.echospeak.model.themes.dto.ThemeId
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ThemesListModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    themes: Map<ThemeId, NonEmptyList<Phrase>>,
) {

    @Pipe
    interface Dependencies {

        val themesOpener: ThemesOpener
    }

    @Serializable
    data class Skeleton(
        val selectedThemes: MutableStateFlow<Set<ThemeId>> = emptySet<ThemeId>().toMutableStateFlowAsInitial()
    )

    private val themesWithIsSelected: List<KeyValue<ThemeId, Pair<NonEmptyList<Phrase>, StateFlow<Boolean>>>> =
        themes.map { idWithPhrases ->
            val (themeId, phrases) = idWithPhrases
            KeyValue(
                key = themeId,
                value = Pair(
                    phrases,
                    skeleton.selectedThemes.mapState(scope) { selectedThemes ->
                        themeId in selectedThemes
                    }
                )
            )
        }

    private inline fun updateSelection(
        update: (Set<ThemeId>) -> Set<ThemeId>,
    ) {
        skeleton.selectedThemes.update(update)
    }

    data class Theme(
        val id: ThemeId,
        val isSelected: StateFlow<Boolean>,
        val switchIsSelected: () -> Unit,
    )

    val themes: List<Theme> = themesWithIsSelected.map { (id, phrasesWithIsSelected) ->
        val (_, isSelected) = phrasesWithIsSelected
        Theme(
            id = id,
            isSelected = isSelected,
            switchIsSelected = {
                updateSelection { current ->
                    (id in current).foldBoolean(
                        ifTrue = { current - id },
                        ifFalse = { current + id },
                    )
                }
            }
        )
    }

    private val selectedThemes: StateFlow<NonEmptyList<KeyValue<ThemeId, NonEmptyList<Phrase>>>?> =
        themesWithIsSelected
            .map { (id, phrasesWithIsSelected) ->
                val (phrases, isSelected) = phrasesWithIsSelected
                isSelected.mapState(scope) { selected ->
                    selected.ifTrue { KeyValue(id, phrases) }
                }
            }
            .fold<_, StateFlow<List<KeyValue<ThemeId, NonEmptyList<Phrase>>>>>(
                initial = emptyList<KeyValue<ThemeId, NonEmptyList<Phrase>>>().toMutableStateFlowAsInitial(),
            ) { acc, themeOrNull ->
                combineState(
                    scope = scope,
                    a = acc,
                    b = themeOrNull,
                ) { currentAcc, currentThemeOrNull ->
                    currentThemeOrNull.foldNullable(
                        ifNull = { currentAcc },
                        ifNotNull = { currentTheme -> currentAcc + currentTheme },
                    )
                }
            }
            .mapState(scope) { selectedThemesOrEmpty ->
                selectedThemesOrEmpty.toNonEmptyListOrNull()
            }

    val launch: StateFlow<(() -> Unit)?> = selectedThemes.mapState(scope) { selectedThemesOrNull ->
        selectedThemesOrNull?.let { selectedThemes ->
            { dependencies.themesOpener.openThemes(selectedThemes) }
        }
    }

    val selectNone: StateFlow<(() -> Unit)?> =
        selectedThemes.mapState(scope) { selectedThemeOrNull ->
            selectedThemeOrNull?.let {
                { updateSelection { emptySet() } }
            }
        }

    val selectAll: StateFlow<(() -> Unit)?> =
        selectedThemes.mapState(scope) { selectedThemeOrNull ->
            val selectedSize = selectedThemeOrNull.orEmpty().size
            val totalSize = themes.size
            (selectedSize == totalSize).ifFalse { { updateSelection { themes.keys } } }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}