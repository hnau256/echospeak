package hnau.echospeak.app.themes

import android.content.Context
import android.content.res.AssetManager
import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.removeSuffixOrNull
import hnau.echospeak.R
import hnau.echospeak.model.themes.dto.Phrase
import hnau.echospeak.model.themes.dto.ThemeId
import hnau.echospeak.model.themes.dto.ThemesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

class AndroidThemesProvider(
    private val context: Context,
) : ThemesProvider {

    override suspend fun loadThemes(): Map<ThemeId, NonEmptyList<Phrase>> =
        withContext(Dispatchers.IO) {

            val assetsManager = context.assets

            assetsManager
                .list(dir)
                .orEmpty()
                .associate { fileName ->

                    val filePath = dir + fileName

                    val themeId = fileName
                        .removeSuffixOrNull(fileNameSuffix)
                        .ifNull { error("Asset '$filePath' has no suffix '$fileNameSuffix'") }
                        .let(::ThemeId)

                    val phrases = assetsManager
                        .open(filePath)
                        .use { inputStream -> Json.decodeFromStream(serializer, inputStream) }

                    themeId to phrases
                }
        }

    companion object {

        private const val fileNameSuffix = ".json"

        private const val dir = "themes/"

        private val serializer = NonEmptyListSerializer(
            Phrase.serializer(),
        )
    }
}