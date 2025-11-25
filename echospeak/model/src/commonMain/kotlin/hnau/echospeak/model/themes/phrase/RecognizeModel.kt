package hnau.echospeak.model.themes.phrase

import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapStateLite
import hnau.common.kotlin.foldBoolean
import hnau.echospeak.model.utils.EchoSpeakConfig
import hnau.echospeak.model.utils.SpeechRecognizer
import hnau.echospeak.model.utils.compare.SatisfactorableSimilarity
import hnau.echospeak.model.utils.compare.Similarity
import hnau.echospeak.model.utils.compare.TextComparator
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.text.compareTo
import kotlin.time.Duration.Companion.seconds

class RecognizeModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    textToRecognize: String,
    val cancel: () -> Unit,
    onReady: (RecognitionResult) -> Unit,
) {

    @Pipe
    interface Dependencies {

        val config: EchoSpeakConfig

        val recognizer: SpeechRecognizer
    }

    private data class State(
        val stage: SpeechRecognizer.State.Stage,
        val result: RecognitionResult,
    )

    private val comparator = TextComparator(
        base = textToRecognize,
    )

    private val state: StateFlow<State> = dependencies
        .recognizer
        .recognize()
        .map { recognizerState ->
            State(
                stage = recognizerState.stage,
                result = createResult(recognizerState.recognizedText),
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = State(
                stage = SpeechRecognizer.State.Stage.InProgress,
                result = createResult(""),
            ),
        )

    val text: StateFlow<String> = state
        .mapStateLite { state -> state.result.text }

    val similarity: StateFlow<SatisfactorableSimilarity> = state
        .mapStateLite { state -> state.result.similarity }

    private fun createResult(
        text: String,
    ): RecognitionResult = RecognitionResult(
        text = text,
        similarity = comparator
            .compare(text)
            .let { similarity ->
                SatisfactorableSimilarity(
                    similarity = similarity,
                    satisfactory = similarity >= dependencies.config.minAllowedRecognitionSimilarity,
                )
            },
    )

    init {
        scope.launch {
            state
                .mapNotNull { state ->
                    when (state.stage) {
                        SpeechRecognizer.State.Stage.InProgress -> state
                            .result
                            .similarity
                            .satisfactory
                            .foldBoolean(
                                ifTrue = {
                                    val pause = dependencies
                                        .config
                                        .pauseAfterMinAllowedRecognitionSimilarityRecognitionResult
                                    pause to state.result
                                },
                                ifFalse = { null },
                            )

                        SpeechRecognizer.State.Stage.Finished -> 0.seconds to state.result
                    }
                }
                .collectLatest { (pause, result) ->
                    delay(pause)
                    onReady(result)
                }
        }
    }
}