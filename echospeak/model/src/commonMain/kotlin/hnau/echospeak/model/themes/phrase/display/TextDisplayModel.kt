package hnau.echospeak.model.themes.phrase.display

import hnau.echospeak.model.themes.dto.Phrase
import hnau.pipe.annotations.Pipe

class TextDisplayModel(
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val phrase: Phrase
    }

    val phrase: Phrase
        get() = dependencies.phrase
}