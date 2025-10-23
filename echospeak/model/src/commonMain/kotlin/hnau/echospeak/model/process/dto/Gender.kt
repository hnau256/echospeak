package hnau.echospeak.model.process.dto

import hnau.common.gen.enumvalues.annotations.EnumValues
import kotlinx.serialization.SerialName

@EnumValues
enum class Gender {
    @SerialName("male")
    Male,

    @SerialName("female")
    Female,
}