package hnau.echospeak.engine

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class RememberFactor(
    val factor: Float,
) {

    companion object {

        val unknown: RememberFactor =
            RememberFactor(0f)
    }
}