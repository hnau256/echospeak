package hnau.echospeak.model.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

interface SpeechRecognizer {

    interface Launch {

        val current: StateFlow<String>

        val result: Deferred<String>
    }

    fun recognize(): Launch

    interface Factory {

        suspend fun create(
            locale: Locale,
        ): SpeechRecognizer?
    }
}