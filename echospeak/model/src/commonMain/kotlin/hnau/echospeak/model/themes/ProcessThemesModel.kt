package hnau.echospeak.model.themes

import arrow.core.NonEmptyList
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.KeyValue
import hnau.echospeak.engine.VariantsKnowFactorsStorage
import hnau.echospeak.model.themes.dto.Phrase
import hnau.echospeak.model.themes.dto.ThemeId
import hnau.echospeak.model.utils.EchoSpeakConfig
import hnau.echospeak.model.utils.Speaker
import hnau.echospeak.model.utils.SpeechRecognizer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable

class ProcessThemesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    themes: NonEmptyList<KeyValue<ThemeId, NonEmptyList<Phrase>>>,
) {

    @Pipe
    interface Dependencies {

        val config: EchoSpeakConfig

        //val storage: VariantsKnowFactorsStorage

        val speakerFactory: Speaker.Factory

        val recognizerFactory: SpeechRecognizer.Factory
    }

    @Serializable
    /*data*/ class Skeleton(

    )

    private val speaker: Deferred<Speaker?> = scope.async {
        dependencies
            .speakerFactory
            .createSpeaker(
                config = dependencies.config.speakerConfig,
                locale = dependencies.config.locale,
            )
    }

    private val recognizer: Deferred<SpeechRecognizer?> = scope.async {
        dependencies
            .recognizerFactory
            .create(dependencies.config.locale)
    }


    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}