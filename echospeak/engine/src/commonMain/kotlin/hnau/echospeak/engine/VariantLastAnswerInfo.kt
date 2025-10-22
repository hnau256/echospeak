package hnau.echospeak.engine

import hnau.common.kotlin.serialization.InstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class VariantLastAnswerInfo(
    val knowFactor: KnowFactor,
    @Serializable(InstantSerializer::class)
    val lastIterationTimestamp: Instant,
)