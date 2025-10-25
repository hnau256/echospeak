package hnau.echospeak.model.utils

import java.util.Locale

interface Translator {

    suspend fun translate(
        text: String,
    ): String?

    interface Factory {

        suspend fun create(
            sourceLanguage: Locale,
        ): Translator?
    }

}