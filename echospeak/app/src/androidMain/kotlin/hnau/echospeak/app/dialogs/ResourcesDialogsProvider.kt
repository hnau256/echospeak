package hnau.echospeak.app.dialogs

import android.content.Context
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrThrow
import hnau.echospeak.R
import hnau.echospeak.model.process.dto.Dialog
import hnau.echospeak.model.process.dto.DialogsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

class ResourcesDialogsProvider(
    private val context: Context,
) : DialogsProvider {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun loadDialogs(): NonEmptyList<Dialog> = withContext(Dispatchers.IO) {
        context
            .resources
            .openRawResource(R.raw.dialogs)
            .let { jsonStream ->
                Json.decodeFromStream(ListSerializer(Dialog.serializer()), jsonStream)
            }
            .toNonEmptyListOrThrow()
    }
}