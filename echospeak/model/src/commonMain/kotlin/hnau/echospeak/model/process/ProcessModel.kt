@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.process

import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.toNonEmptyListOrThrow
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.engine.ChooseVariantConfig
import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.engine.VariantId
import hnau.echospeak.engine.VariantLastAnswerInfo
import hnau.echospeak.engine.VariantsKnowFactorsStorage
import hnau.echospeak.engine.chooseVariant
import hnau.echospeak.model.process.dto.Dialog
import hnau.echospeak.model.process.dto.DialogsProvider
import hnau.echospeak.model.utils.VariantsKnowFactorsRepository
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class ProcessModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val variantsKnowFactorsRepository: VariantsKnowFactorsRepository

        val dialogsProvider: DialogsProvider

        fun variant(): VariantModel.Dependencies
    }

    private val variantsIdsWithDialogs: Deferred<Pair<NonEmptyList<VariantId>, Map<VariantId, Dialog>>> =
        scope.async {
            withContext(Dispatchers.Default) {
                val variantsIdsWithDialogs = dependencies
                    .dialogsProvider
                    .loadDialogs()
                    .map { dialog ->
                        val variantId = dialog.title.let(::VariantId)
                        variantId to dialog
                    }

                val variantsIds = variantsIdsWithDialogs
                    .map(Pair<VariantId, *>::first)

                val dialogs = variantsIdsWithDialogs.associate(::identity)

                variantsIds to dialogs
            }
        }

    private val variantsKnowFactorsStorage: Deferred<VariantsKnowFactorsStorage> = scope.async {

        val variantsLastAnswerInfo: MutableMap<VariantId, VariantLastAnswerInfo> = dependencies
            .variantsKnowFactorsRepository
            .loadAllKnowFactors()
            .toMutableMap()

        object : VariantsKnowFactorsStorage {

            override fun get(
                id: VariantId,
            ): VariantLastAnswerInfo? = variantsLastAnswerInfo[id]

            override suspend fun update(
                variantId: VariantId,
                newKnowFactor: KnowFactor
            ) {
                val info = VariantLastAnswerInfo(
                    knowFactor = newKnowFactor,
                    lastIterationTimestamp = Clock.System.now()
                )
                variantsLastAnswerInfo[variantId] = info
                dependencies
                    .variantsKnowFactorsRepository
                    .updateVariant(
                        id = variantId,
                        info = info,
                    )
            }

        }
    }

    private suspend fun switchVariant() {
        val (variantsIds, dialogsByIds) = variantsIdsWithDialogs.await()
        val variantsIdsWithoutCurrent = withContext(Dispatchers.Default) {
            skeleton.variant.value?.first.foldNullable(
                ifNull = { variantsIds },
                ifNotNull = { current ->
                    (variantsIds - current).toNonEmptyListOrThrow()
                }
            )
        }
        val storage = variantsKnowFactorsStorage.await()
        val newVariant = chooseVariant(
            variantsIds = variantsIdsWithoutCurrent,
            storage = storage,
            config = chooseVariantConfig,
        )
        val variantSkeleton = VariantModel.Skeleton(
            dialog = dialogsByIds.getValue(newVariant.id),
            learnInfo = newVariant.learnInfo,
        )
        skeleton
            .variant
            .value = (newVariant.id to variantSkeleton)
    }

    init {
        scope.launch {
            switchVariant()
        }
    }

    @Serializable
    data class Skeleton(
        val variant: MutableStateFlow<Pair<VariantId, VariantModel.Skeleton>?> =
            null.toMutableStateFlowAsInitial(),
    )

    val variantOrNull: StateFlow<VariantModel?> = skeleton
        .variant
        .mapWithScope(scope) { scope, skeletonOrNull ->
            skeletonOrNull?.let { (_, skeleton) ->
                VariantModel(
                    scope = scope,
                    dependencies = dependencies.variant(),
                    skeleton = skeleton,
                )
            }
        }

    companion object {

        private val chooseVariantConfig = ChooseVariantConfig(
            baseInterval = 1.minutes,
            weightPow = 3f,
        )
    }
}