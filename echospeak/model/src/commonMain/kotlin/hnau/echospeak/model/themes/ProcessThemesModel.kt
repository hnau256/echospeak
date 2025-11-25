@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.themes

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.map
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.engine.ChosenVariant
import hnau.echospeak.engine.VariantId
import hnau.echospeak.engine.VariantsKnowFactorsStorage
import hnau.echospeak.engine.chooseVariant
import hnau.echospeak.model.themes.dto.Phrase
import hnau.echospeak.model.themes.dto.PhraseVariant
import hnau.echospeak.model.themes.dto.ThemeId
import hnau.echospeak.model.themes.phrase.PhraseModel
import hnau.echospeak.model.utils.EchoSpeakConfig
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

class ProcessThemesModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    themes: NonEmptyList<KeyValue<ThemeId, NonEmptyList<Phrase>>>,
) {

    @Pipe
    interface Dependencies {

        val config: EchoSpeakConfig

        val storage: VariantsKnowFactorsStorage

        fun variant(): PhraseModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val variant: MutableStateFlow<Loadable<KeyValue<ChosenVariant<PhraseVariant>, PhraseModel.Skeleton>>> =
            Loading.toMutableStateFlowAsInitial(),
    )

    private val variants: Deferred<NonEmptyList<PhraseVariant>> = scope.async {
        withContext(Dispatchers.Default) {
            themes
                .flatMap { (theme, phrases) ->
                    phrases
                        .map { phrase ->
                            PhraseVariant(
                                theme = theme,
                                phrase = phrase,
                            )
                        }
                }
                .sortedBy { variant ->
                    variant.phrase.phrase.length
                }
                .toNonEmptyListOrThrow()
        }
    }

    init {
        scope.launch {
            switchVariant(
                variantToExclude = null,
            )
        }
    }

    private suspend fun switchVariant(
        variantToExclude: PhraseVariant?,
    ) {
        val variants = withContext(Dispatchers.Default) {
            val all = variants.await()
            variantToExclude.foldNullable(
                ifNull = { all },
                ifNotNull = { toExclude -> (all - toExclude).toNonEmptyListOrThrow() }
            )
        }

        val newVariant = chooseVariant(
            variants = variants,
            extractId = PhraseVariant::id,
            storage = dependencies.storage,
            config = dependencies.config.chooseVariantConfig,
        )

        skeleton
            .variant
            .value = Ready(
            KeyValue(
                key = newVariant,
                value = PhraseModel.Skeleton(),
            )
        )
    }

    val phraseOrLoading: StateFlow<Loadable<KeyValue<PhraseVariant, PhraseModel>>> = skeleton
        .variant
        .mapWithScope(scope) { scope, variantOrLoading ->
            variantOrLoading.map { (variant, variantSkeleton) ->
                val model = PhraseModel(
                    scope = scope,
                    dependencies = dependencies.variant(),
                    skeleton = variantSkeleton,
                    phrase = variant,
                    complete = { newKnowFactor ->
                        dependencies.storage.update(
                            id = variant.variant.id,
                            newKnowFactor = newKnowFactor,
                        )
                        switchVariant(
                            variantToExclude = variant.variant,
                        )
                    },
                )
                KeyValue(
                    key = variant.variant,
                    value = model,
                )
            }
        }


    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}

private val PhraseVariant.id: VariantId
    get() = VariantId(theme.id + "|" + phrase.phrase)