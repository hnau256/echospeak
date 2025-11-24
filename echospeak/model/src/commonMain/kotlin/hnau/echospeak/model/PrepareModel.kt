package hnau.echospeak.model

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.map
import hnau.common.kotlin.toAccessor
import hnau.common.kotlin.valueOrElse
import hnau.echospeak.model.themes.LoadThemesModel
import hnau.echospeak.model.utils.EchoSpeakConfig
import hnau.echospeak.model.utils.ExerciseId
import hnau.echospeak.model.utils.Speaker
import hnau.echospeak.model.utils.SpeechRecognizer
import hnau.echospeak.model.utils.VariantsKnowFactorsProvider
import hnau.echospeak.model.utils.compare.CompareRecognizer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class PrepareModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val config: EchoSpeakConfig

        val speakerFactory: Speaker.Factory

        val recognizerFactory: SpeechRecognizer.Factory

        val variantsKnowFactorsProviderFactory: VariantsKnowFactorsProvider.Factory

        @Pipe
        interface Prepared {

            fun themes(
                variantsKnowFactorsProvider: VariantsKnowFactorsProvider,
            ): LoadThemesModel.Dependencies
        }

        fun prepared(
            recognizer: CompareRecognizer,
            speaker: Speaker,
        ): Prepared
    }

    @Serializable
    data class Skeleton(
        var themes: LoadThemesModel.Skeleton? = null,
    )

    val themes: StateFlow<Loadable<LoadThemesModel?>> = LoadableStateFlow(scope) {
        coroutineScope {

            val speakerDeferred: Deferred<Speaker?> = async {
                dependencies
                    .speakerFactory
                    .createSpeaker(
                        config = dependencies.config.speakerConfig,
                        locale = dependencies.config.locale,
                    )
            }

            val recognizerDeferred: Deferred<CompareRecognizer?> = async {
                val recognizer = dependencies
                    .recognizerFactory
                    .create(dependencies.config.locale)
                    ?: return@async null
                CompareRecognizer(
                    recognizer = recognizer,
                    minSimilarityToEarlyStop = dependencies.config.minAllowedRecognitionSimilarity,
                )
            }

            val speaker = speakerDeferred.await() ?: return@coroutineScope null
            val recognizer = recognizerDeferred.await() ?: return@coroutineScope null

            dependencies.prepared(
                speaker = speaker,
                recognizer = recognizer,
            )
        }
    }.mapWithScope(scope) { scope, preparedOrLoadingOrNull ->
        preparedOrLoadingOrNull.map { preparedOrError ->
            preparedOrError?.let { prepared ->
                LoadThemesModel(
                    scope = scope,
                    dependencies = prepared.themes(
                        variantsKnowFactorsProvider = dependencies
                            .variantsKnowFactorsProviderFactory
                            .create(ExerciseId("themes")),
                    ),
                    skeleton = skeleton::themes
                        .toAccessor()
                        .getOrInit { LoadThemesModel.Skeleton() },
                )
            }
        }
    }

    val goBackHandler: GoBackHandler = themes.flatMapState(scope) { themesOrLoadingOrNull ->
        themesOrLoadingOrNull
            .valueOrElse { null }
            ?.goBackHandler
            ?: NeverGoBackHandler
    }

}