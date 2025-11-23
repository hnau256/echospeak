package hnau.echospeak.app.knowfactors

import hnau.echospeak.app.db.knowfactors.ExercisesVariantsKnowFactors
import hnau.echospeak.app.db.knowfactors.ExercisesVariantsKnowFactorsDao
import hnau.echospeak.engine.VariantId
import hnau.echospeak.engine.VariantLastAnswerInfo
import hnau.echospeak.model.utils.ExerciseId
import hnau.echospeak.model.utils.VariantsKnowFactorsProvider
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class VariantsKnowFactorsProviderRoomImpl(
    private val exerciseId: ExerciseId,
    private val getDao: suspend () -> ExercisesVariantsKnowFactorsDao,
) : VariantsKnowFactorsProvider {

    override suspend fun loadAllKnowFactors(): Map<VariantId, VariantLastAnswerInfo> = getDao()
        .getExerciseVariants(exerciseId)
        .associate { entry ->
            entry.variant to VariantLastAnswerInfo(
                knowFactor = entry.knowFactor,
                lastIterationTimestamp = entry.lastAnswerTimestamp,
            )
        }

    override suspend fun updateVariant(
        id: VariantId,
        info: VariantLastAnswerInfo,
    ) {
        getDao().upsert(
            ExercisesVariantsKnowFactors(
                exercise = exerciseId,
                variant = id,
                lastAnswerTimestamp = info.lastIterationTimestamp,
                knowFactor = info.knowFactor,
            )
        )
    }
}