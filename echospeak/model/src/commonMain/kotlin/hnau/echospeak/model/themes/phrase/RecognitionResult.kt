package hnau.echospeak.model.themes.phrase

import hnau.echospeak.model.utils.compare.SatisfactorableSimilarity
import kotlinx.serialization.Serializable

@Serializable
data class RecognitionResult(
    val text: String,
    val similarity: SatisfactorableSimilarity,
)