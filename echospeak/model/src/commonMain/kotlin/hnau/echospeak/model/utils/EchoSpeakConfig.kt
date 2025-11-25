package hnau.echospeak.model.utils

import hnau.echospeak.engine.ChooseVariantConfig
import hnau.echospeak.model.utils.compare.Similarity
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class EchoSpeakConfig(
    val locale: Locale,
    val minAllowedRecognitionSimilarity: Similarity = Similarity(0.75f),
    val speakerConfig: Speaker.Config = Speaker.Config.default,
    val chooseVariantConfig: ChooseVariantConfig = ChooseVariantConfig(
        baseInterval = 1.minutes,
        weightPow = 4f,
    ),
    val correctAnswerKnowFactorFactor: Float = 1.5f,
    val pauseAfterMinAllowedRecognitionSimilarityRecognitionResult: Duration = 1.seconds,
    /*val pauseAfterLine: Duration = 1.seconds,*/
)