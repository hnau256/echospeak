package hnau.echospeak.model.utils

import java.util.Locale

fun interface Speaker {

    suspend fun speak(
        text: String,
    ): Boolean

    data class Config(
        val speechRate: Float,
        val tryUseNetworkVoice: Boolean,
    ) {

        companion object {

            val default: Config = Config(
                speechRate = 0.85f,
                tryUseNetworkVoice = true,
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