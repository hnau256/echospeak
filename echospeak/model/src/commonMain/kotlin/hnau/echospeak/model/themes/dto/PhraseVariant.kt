package hnau.echospeak.model.themes.dto

import kotlinx.serialization.Serializable

@Serializable
data class PhraseVariant(
    val theme: ThemeId,
    val phrase: Phrase,
)