package hnau.echospeak.model.themes.dto

import kotlinx.serialization.Serializable

@Serializable
data class Phrase(
    val phrase: String,
    val translation: String,
    val comment: String,
)