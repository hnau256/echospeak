package hnau.echospeak.model.themes.dto

import arrow.core.NonEmptyList

interface ThemesProvider {

    suspend fun loadThemes(): Map<ThemeId, NonEmptyList<Phrase>>
}