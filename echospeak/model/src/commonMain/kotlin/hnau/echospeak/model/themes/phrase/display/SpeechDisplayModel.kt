package hnau.echospeak.model.themes.phrase.display

import hnau.common.kotlin.castOrThrow
import hnau.common.kotlin.coroutines.ActionOrCancel
import hnau.common.kotlin.coroutines.actionOrCancelIfExecuting
import hnau.echospeak.model.themes.dto.Phrase
import hnau.echospeak.model.utils.Speaker
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SpeechDisplayModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val phrase: Phrase

        val speaker: Speaker
    }

    val speakOrCancel: StateFlow<ActionOrCancel> = actionOrCancelIfExecuting(scope) {
        dependencies
            .speaker
            .speak(dependencies.phrase.phrase)
    }

    init {
        speakOrCancel
            .value
            .castOrThrow<ActionOrCancel.Action>()
            .action()
    }
}