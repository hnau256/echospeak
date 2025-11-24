package hnau.echospeak.model.themes

import hnau.echospeak.engine.ChosenVariant
import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.model.themes.dto.PhraseVariant
import hnau.echospeak.model.utils.Speaker
import hnau.echospeak.model.utils.SpeechRecognizer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class PhraseModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    private val phrase: ChosenVariant<PhraseVariant>,
    private val complete: suspend (newFactor: KnowFactor) -> Unit,
) {

    @Pipe
    interface Dependencies {

        val speaker: Speaker

        val recognizer: SpeechRecognizer
    }

    @Serializable
    /*data*/ class Skeleton()

}