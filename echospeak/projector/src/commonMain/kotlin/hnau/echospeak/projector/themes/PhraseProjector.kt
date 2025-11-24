package hnau.echospeak.projector.themes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import hnau.echospeak.model.themes.PhraseModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class PhraseProjector(
    scope: CoroutineScope,
    model: PhraseModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {

    }
}