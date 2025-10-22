package hnau.echospeak.engine

interface VariantsKnowFactorsStorage {

    operator fun get(
        id: VariantId,
    ): VariantLastAnswerInfo?

    suspend fun update(
        variantId: VariantId,
        newKnowFactor: KnowFactor,
    )
}

