package hnau.echospeak.model.themes

import arrow.core.NonEmptyList
import hnau.common.kotlin.KeyValue
import hnau.echospeak.model.themes.dto.Phrase
import hnau.echospeak.model.themes.dto.ThemeId

fun interface ThemesOpener {

    fun openThemes(
        themes: NonEmptyList<KeyValue<ThemeId, NonEmptyList<Phrase>>>,
    )
}