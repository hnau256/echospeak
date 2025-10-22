package hnau.echospeak.engine

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class ChooseVariantConfig(
    val baseInterval: Duration,
    val weightPow: Float,
) {

    companion object {

        val default = ChooseVariantConfig(
            baseInterval = 1.minutes,
            weightPow = 4f,
        )
    }
}