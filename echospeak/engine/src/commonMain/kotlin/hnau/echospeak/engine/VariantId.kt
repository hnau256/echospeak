package hnau.echospeak.engine

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class VariantId(
    val id: String,
)