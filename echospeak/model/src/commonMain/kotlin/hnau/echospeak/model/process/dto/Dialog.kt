@file:UseSerializers(
    NonEmptyListSerializer::class,
)

package hnau.echospeak.model.process.dto

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Dialog(
    @SerialName("title")
    val title: String,
    @SerialName("first_line_gender")
    val firstLineGender: Gender,
    @SerialName("lines")
    val lines: NonEmptyList<String>,
)