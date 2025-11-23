package hnau.echospeak.model.utils.compare

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Similarity(
    val factor: Float,
) : Comparable<Similarity> {

    override fun compareTo(
        other: Similarity,
    ): Int = factor.compareTo(
        other = other.factor,
    )

    companion object {

        val full = Similarity(
            factor = 1f,
        )
    }
}