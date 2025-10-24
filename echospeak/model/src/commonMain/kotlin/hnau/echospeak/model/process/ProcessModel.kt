@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.process

import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.toNonEmptyListOrThrow
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toLoadableStateFlow
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.fold
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.map
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.engine.ChooseVariantConfig
import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.engine.VariantId
import hnau.echospeak.engine.VariantLastAnswerInfo
import hnau.echospeak.engine.VariantsKnowFactorsStorage
import hnau.echospeak.engine.chooseVariant
import hnau.echospeak.model.process.dto.Dialog
import hnau.echospeak.model.process.dto.DialogsProvider
import hnau.echospeak.model.utils.Speaker
import hnau.echospeak.model.utils.SpeechRecognizer
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
import java.util.Locale
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

        val speakerFactory: Speaker.Factory

        val recognizerFactory: SpeechRecognizer.Factory

        fun variant(
            speaker: Speaker,
            recognizer: SpeechRecognizer,
        ): VariantModel.Dependencies
    }

    private val speaker: Deferred<Speaker?> = scope.async {
        dependencies
            .speakerFactory
            .createSpeaker(
                config = Speaker.Config.default, //TODO
                locale = locale,
            )
    }

    private val recognizer: Deferred<SpeechRecognizer?> = scope.async {
        dependencies
            .recognizerFactory
            .create(locale)
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

    init {
        scope.launch {
            switchVariant(
                variantToExclude = null,
            )
        }
    }

    @Serializable
    data class Skeleton(
        val variant: MutableStateFlow<Loadable<Pair<VariantId, VariantModel.Skeleton>>> =
            Loading.toMutableStateFlowAsInitial(),
    )


    private suspend fun switchVariant(
        variantToExclude: VariantId?,
    ) {
        val (variantsIds, dialogsByIds) = variantsIdsWithDialogs.await()
        val variantsIdsWithoutCurrent = withContext(Dispatchers.Default) {
            variantToExclude.foldNullable(
                ifNull = { variantsIds },
                ifNotNull = { current -> (variantsIds - current).toNonEmptyListOrThrow() }
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
            .value = Ready(newVariant.id to variantSkeleton)
    }

    val variantOrLoadingOrError: StateFlow<Loadable<VariantModel?>> = recognizer
        .toLoadableStateFlow(scope)
        .flatMapWithScope(scope) { scope, recognizerOrErrorOrLoading ->
            recognizerOrErrorOrLoading.fold(
                ifLoading = { Loading.toMutableStateFlowAsInitial() },
                ifReady = { recognizerOrError ->
                    recognizerOrError.foldNullable(
                        ifNull = { Ready(null).toMutableStateFlowAsInitial() },
                        ifNotNull = { recognizer ->
                            createVariantModel(
                                scope = scope,
                                recognizer = recognizer,
                            )
                        }
                    )
                }
            )
        }


    private fun createVariantModel(
        scope: CoroutineScope,
        recognizer: SpeechRecognizer,
    ): StateFlow<Loadable<VariantModel?>> = speaker
        .toLoadableStateFlow(scope)
        .flatMapWithScope(scope)
        { scope, speakerOrErrorOrLoading ->
            speakerOrErrorOrLoading.fold(
                ifLoading = { Loading.toMutableStateFlowAsInitial() },
                ifReady = { speakerOrError ->
                    speakerOrError.foldNullable(
                        ifNull = { Ready(null).toMutableStateFlowAsInitial() },
                        ifNotNull = { speaker ->
                            createVariantModel(
                                scope = scope,
                                speaker = speaker,
                                recognizer = recognizer,
                            )
                        }
                    )
                }
            )
        }

    private fun createVariantModel(
        scope: CoroutineScope,
        speaker: Speaker,
        recognizer: SpeechRecognizer,
    ): StateFlow<Loadable<VariantModel>> = skeleton
        .variant
        .mapWithScope(scope) { scope, skeletonOrLoading ->
            skeletonOrLoading.map { (id, skeleton) ->
                VariantModel(
                    scope = scope,
                    dependencies = dependencies.variant(
                        recognizer = recognizer,
                        speaker = speaker,
                    ),
                    skeleton = skeleton,
                    complete = { newKnowFactor ->
                        variantsKnowFactorsStorage.await().update(
                            variantId = id,
                            newKnowFactor = newKnowFactor,
                        )
                        switchVariant(
                            variantToExclude = id,
                        )
                    },
                )
            }
        }


    companion object {

        @Suppress("DEPRECATION")
        private val locale = Locale("el", "GR") //TODO

        private val chooseVariantConfig = ChooseVariantConfig(
            baseInterval = 1.minutes,
            weightPow = 3f,
        )
    }
}