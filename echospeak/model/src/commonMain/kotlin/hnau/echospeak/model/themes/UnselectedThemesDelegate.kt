package hnau.echospeak.model.themes

import arrow.core.getOrElse
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringSplit
import hnau.common.kotlin.mapper.toListMapper
import hnau.echospeak.model.themes.dto.ThemeId
import hnau.echospeak.model.utils.settings.Setting
import hnau.echospeak.model.utils.settings.Settings
import hnau.echospeak.model.utils.settings.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UnselectedThemesDelegate(
    scope: CoroutineScope,
    settings: Settings,
    @PublishedApi
    internal val overwritten: MutableStateFlow<Set<ThemeId>?>,
) {

    private val setting: Setting<Set<ThemeId>> = settings["unselected_dictionaries"].map(
        Mapper.stringSplit('|') +
                Mapper(::ThemeId, ThemeId::id).toListMapper() +
                Mapper(List<ThemeId>::toSet, Set<ThemeId>::toList),
    )

    val unselectedThemes: StateFlow<Set<ThemeId>> = overwritten
        .flatMapState(
            scope = scope,
        ) { overwritten ->
            overwritten
                ?.let { MutableStateFlow(overwritten) }
                ?: setting.state.mapStateLite { it.getOrElse { emptySet() } }
        }

    inline fun update(
        newValue: (Set<ThemeId>) -> Set<ThemeId>,
    ) {
        overwritten.value = newValue(unselectedThemes.value)
    }

    suspend fun apply() {
        setting.update(unselectedThemes.value)
    }
}