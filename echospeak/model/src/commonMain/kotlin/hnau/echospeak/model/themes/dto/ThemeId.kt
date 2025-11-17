package hnau.echospeak.model.themes.dto

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ThemeId(
    val id: String,
)