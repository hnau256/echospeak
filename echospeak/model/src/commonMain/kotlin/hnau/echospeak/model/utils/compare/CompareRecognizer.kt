package hnau.echospeak.model.utils.compare

import hnau.common.kotlin.coroutines.mapStateLite
import hnau.echospeak.model.utils.SpeechRecognizer
import kotlinx.coroutines.flow.StateFlow

class CompareRecognizer(
    private val recognizer: SpeechRecognizer,
    private val minSimilarityToEarlyStop: Similarity,
) {

    data class State(
        val recognizer: SpeechRecognizer.State,
        val similarity: Similarity,
    )

    suspend fun recognize(
        textToCompareTo: String,
    ): StateFlow<State> {

        val comparator = TextComparator(
            base = textToCompareTo,
        )

        return recognizer
            .recognize()
            .mapStateLite { recognizerState ->

                val recognizedText = recognizerState.recognizedText

                val similarity = comparator.compare(recognizedText)

                val optimisticStage = when (recognizerState.stage) {
                    SpeechRecognizer.State.Stage.Finished -> SpeechRecognizer.State.Stage.Finished
                    SpeechRecognizer.State.Stage.InProgress -> when {
                        similarity >= minSimilarityToEarlyStop -> SpeechRecognizer.State.Stage.Finished
                        else -> SpeechRecognizer.State.Stage.InProgress
                    }
                }

                State(
                    recognizer = SpeechRecognizer.State(
                        stage = optimisticStage,
                        recognizedText = recognizedText,
                    ),
                    similarity = similarity,
                )
            }
    }

}