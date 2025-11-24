package hnau.echospeak.engine

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class KnowFactor(
    val factor: Float,
) {

    companion object {

        val initial: KnowFactor = KnowFactor(1f)
    }
}