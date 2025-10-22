package hnau.echospeak.engine

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class KnowFactor(
    val factor: Float,
)