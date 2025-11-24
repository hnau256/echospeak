package hnau.echospeak.engine

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class VariantId(
    val id: String,
)