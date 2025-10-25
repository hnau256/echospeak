package hnau.echospeak.app.translator

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import hnau.echospeak.model.utils.Translator
import kotlinx.coroutines.tasks.await
import java.util.Locale
import com.google.mlkit.nl.translate.Translator as MLKitTranslator

class AndroidTranslator(
    private val translator: MLKitTranslator,
) : Translator {

    override suspend fun translate(
        text: String,
    ): String? = Result
        .runCatching {
            translator
                .translate(text)
                .await()
        }
        .getOrNull()

    class Factory : Translator.Factory {

        override suspend fun create(
            sourceLanguage: Locale,
        ): Translator? {
            val fromLanguage = sourceLanguage.translateLanguage ?: return null
            val toLanguage = Locale.getDefault().translateLanguage ?: return null

            val translator: MLKitTranslator = Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(fromLanguage)
                    .setTargetLanguage(toLanguage)
                    .build()
            )

            Result
                .runCatching {
                    translator
                        .downloadModelIfNeeded()
                        .await()
                    Unit
                }
                .getOrNull()
                ?: return null

            return AndroidTranslator(
                translator = translator,
            )
        }

        private val Locale.translateLanguage: String?
            get() = language.let(TranslateLanguage::fromLanguageTag)


    }
}