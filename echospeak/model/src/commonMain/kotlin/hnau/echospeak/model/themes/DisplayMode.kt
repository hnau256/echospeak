package hnau.echospeak.model.themes

import hnau.common.gen.enumvalues.annotations.EnumValues
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.nameToEnum

@EnumValues
enum class DisplayMode {

    Text, Speech;

    val next: DisplayMode
        get() = when (this) {
            Text -> Speech
            Speech -> Text
        }

    companion object {

        val default: DisplayMode
            get() = Text

        val nameMapper: Mapper<String, DisplayMode> =
            Mapper.nameToEnum<DisplayMode>()
    }
}