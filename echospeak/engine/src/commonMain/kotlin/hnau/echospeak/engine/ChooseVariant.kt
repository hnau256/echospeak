package hnau.echospeak.engine

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

data class ChosenVariant(
    val id: VariantId,
    val learnInfo: LearnInfo?,
) {

    data class LearnInfo(
        val info: VariantLastAnswerInfo,
        val rememberFactor: RememberFactor,
    )
}

suspend fun chooseVariant(
    variantsIds: NonEmptyList<VariantId>,
    storage: VariantsKnowFactorsStorage,
    config: ChooseVariantConfig,
): ChosenVariant = withContext(Dispatchers.Default) {

    val (unansweredId, answered) =
        variantsIds.fold<_, Pair<VariantId?, List<Pair<VariantId, VariantLastAnswerInfo>>>>(
            initial = null to emptyList(),
        ) { (unansweredId, answered), id ->
            storage[id].foldNullable(
                ifNull = {
                    val newUnansweredId = unansweredId ?: id
                    newUnansweredId to answered
                },
                ifNotNull = { info ->
                    unansweredId to (answered + (id to info))
                }
            )
        }

    val now = Clock.System.now()
    val variantsToChooseFrom: NonEmptyList<ChosenVariant> =
        buildList {
            addAll(answered)
            unansweredId?.let { id -> add(id to null) }
        }
            .toNonEmptyListOrNull()
            .ifNull { nonEmptyListOf(variantsIds.head to null) }
            .map { (id, infoOrNull) ->
                ChosenVariant(
                    id = id,
                    learnInfo = infoOrNull?.let { info ->
                        ChosenVariant.LearnInfo(
                            info = info,
                            rememberFactor = calcRememberFactor(
                                now = now,
                                baseInterval = config.baseInterval,
                                info = info,
                            ),
                        )
                    }
                )
            }

    variantsToChooseFrom.chooseRandom { variant ->
        variant
            .learnInfo
            ?.rememberFactor
            .ifNull { RememberFactor.unknown }
            .calcWeight(config.weightPow)
    }
}

private fun calcRememberFactor(
    now: Instant,
    baseInterval: Duration,
    info: VariantLastAnswerInfo,
): RememberFactor {
    val duration = (now - info.lastIterationTimestamp)
    val timeFactor = duration / baseInterval
    val result = exp(-timeFactor / info.knowFactor.factor)
    return RememberFactor(
        factor = result.toFloat(),
    )
}

private fun RememberFactor.calcWeight(
    weightPow: Float
): Float = (1f - factor).pow(weightPow)

private inline fun <T> NonEmptyList<T>.chooseRandom(
    calcWeight: (T) -> Float,
): T {
    val (weightSum, items) = tail.fold(
        initial = calcWeight(head).let { headWeight ->
            headWeight to nonEmptyListOf(headWeight to head)
        }
    ) { (weightSum, acc), item ->
        val weight = calcWeight(item)
        val newWeightSum = weightSum + weight
        newWeightSum to (acc + (newWeightSum to item))
    }
    if (weightSum <= 0f) {
        return head
    }
    val target = Random.nextFloat() * weightSum
    return items
        .firstOrNull { (itemSumWeight) -> itemSumWeight >= target }
        ?.second
        ?: last()
}