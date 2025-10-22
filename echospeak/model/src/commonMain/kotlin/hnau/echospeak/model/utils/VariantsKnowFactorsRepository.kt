package hnau.echospeak.model.utils

import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.engine.VariantId
import hnau.echospeak.engine.VariantLastAnswerInfo

interface VariantsKnowFactorsRepository {

    suspend fun loadAllKnowFactors(): Map<VariantId, VariantLastAnswerInfo>

    suspend fun updateVariant(
        id: VariantId,
        info: VariantLastAnswerInfo,
    )

    interface Factory {

        fun create(
            exerciseId: ExerciseId,
        ): VariantsKnowFactorsRepository
    }
}