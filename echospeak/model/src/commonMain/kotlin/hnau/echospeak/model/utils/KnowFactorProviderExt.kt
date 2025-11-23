package hnau.echospeak.model.utils

import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.engine.VariantId
import hnau.echospeak.engine.VariantLastAnswerInfo
import hnau.echospeak.engine.VariantsKnowFactorsStorage
import kotlin.time.Clock

suspend fun VariantsKnowFactorsProvider.toVariantsKnowFactorsStorage(): VariantsKnowFactorsStorage {

    val repository: VariantsKnowFactorsProvider = this

    val cache: MutableMap<VariantId, VariantLastAnswerInfo> = repository
        .loadAllKnowFactors()
        .toMutableMap()

    return object : VariantsKnowFactorsStorage {

        override fun get(
            id: VariantId,
        ): VariantLastAnswerInfo? = cache[id]

        override suspend fun update(
            id: VariantId,
            newKnowFactor: KnowFactor
        ) {
            val info = VariantLastAnswerInfo(
                knowFactor = newKnowFactor,
                lastIterationTimestamp = Clock.System.now(),
            )
            cache[id] = info
            repository.updateVariant(
                id = id,
                info = info,
            )
        }


    }
}