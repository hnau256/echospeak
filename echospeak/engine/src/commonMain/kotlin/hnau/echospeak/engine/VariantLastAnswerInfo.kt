package hnau.echospeak.engine

import kotlin.time.Instant

data class VariantLastAnswerInfo(
    val knowFactor: KnowFactor,
    val lastIterationTimestamp: Instant,
)