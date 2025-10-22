package hnau.echospeak.app

import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.engine.VariantId
import hnau.echospeak.engine.VariantLastAnswerInfo
import hnau.echospeak.model.utils.ExerciseId
import hnau.echospeak.model.utils.VariantsKnowFactorsRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class VariantsKnowFactorsRepositoryRoomImpl(
    private val exerciseId: ExerciseId,
    private val getDao: suspend () -> ExercisesVariantsKnowFactorsDao,
) : VariantsKnowFactorsRepository {

    private val accessMutex = Mutex()

    private var cache: MutableMap<VariantId, VariantLastAnswerInfo>? = null

    override suspend fun loadAllKnowFactors(): Map<VariantId, VariantLastAnswerInfo> =
        accessMutex.withLock {
            var result = cache
            if (result == null) {
                result = getDao()
                    .getExerciseVariants(exerciseId)
                    .associate { entry ->
                        entry.variant to VariantLastAnswerInfo(
                            knowFactor = entry.knowFactor,
                            lastIterationTimestamp = entry.lastAnswerTimestamp,
                        )
                    }
                    .toMutableMap()
                cache = result
            }
            result
        }

    override suspend fun updateVariant(
        id: VariantId,
        knowFactor: KnowFactor
    ) {
        accessMutex.withLock {
            val info = VariantLastAnswerInfo(
                lastIterationTimestamp = Clock.System.now(),
                knowFactor = knowFactor,
            )
            cache?.set(id, info)
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
}