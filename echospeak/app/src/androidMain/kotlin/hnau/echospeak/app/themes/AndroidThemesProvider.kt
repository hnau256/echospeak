package hnau.echospeak.app.themes

import android.content.Context
import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
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
            context
                .resources
                .openRawResource(R.raw.phrases)
                .let { jsonStream -> Json.decodeFromStream(serializer, jsonStream) }
        }

    companion object {

        private val serializer = MapSerializer(
            keySerializer = ThemeId.serializer(),
            valueSerializer = NonEmptyListSerializer(
                Phrase.serializer(),
            )
        )
    }
}