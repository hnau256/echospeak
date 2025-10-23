package hnau.echospeak.model.utils

import hnau.echospeak.model.process.dto.Gender
import hnau.echospeak.model.process.dto.GenderValues
import java.util.Locale

fun interface Speaker {

    suspend fun speak(
        gender: Gender,
        text: String,
    ): Boolean

    data class Config(
        val tryUseNetworkVoice: Boolean,
        val pitchFactors: GenderValues<Float>,
    ) {

        companion object {

            val default: Config = Config(
                tryUseNetworkVoice = true,
                pitchFactors = GenderValues(
                    male = 0.85f,
                    female = 1.15f,
                ),
            )
        }
    }

    fun interface Factory {

        suspend fun createSpeaker(
            config: Config,
            locale: Locale,
        ): Speaker?
    }
}