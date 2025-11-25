package hnau.echospeak.model.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

interface SpeechRecognizer {

    data class State(
        val recognizedText: String,
        val stage: Stage,
    ) {

        enum class Stage { InProgress, Finished }
    }

    fun recognize(): Flow<State>

    interface Factory {

        suspend fun create(
            locale: Locale,
        ): SpeechRecognizer?
    }
}