package hnau.echospeak.model.utils.compare

import kotlinx.serialization.Serializable

@Serializable
data class SatisfactorableSimilarity(
    val similarity: Similarity,
    val satisfactory: Boolean,
)