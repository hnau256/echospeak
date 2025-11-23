package hnau.echospeak.engine

interface VariantsKnowFactorsStorage {

    operator fun get(
        id: VariantId,
    ): VariantLastAnswerInfo?

    suspend fun update(
        id: VariantId,
        newKnowFactor: KnowFactor,
    )
}

