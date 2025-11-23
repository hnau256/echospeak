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
        weightPow = 3f,
    ),
    val pauseAfterLine: Duration = 1.seconds,
)