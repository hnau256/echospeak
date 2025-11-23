package hnau.echospeak.model.utils

import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

interface SpeechRecognizer {

    data class State(
        val recognizedText: String,
        val stage: Stage,
    ) {

        enum class Stage { InProgress, Finished }
    }

    suspend fun recognize(): StateFlow<State>

    interface Factory {

        suspend fun create(
            locale: Locale,
        ): SpeechRecognizer?
    }
}