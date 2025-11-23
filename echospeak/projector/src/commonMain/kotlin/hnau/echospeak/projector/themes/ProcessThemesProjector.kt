package hnau.echospeak.projector.themes

import androidx.compose.runtime.Composable
import hnau.echospeak.model.themes.ProcessThemesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class ProcessThemesProjector(
    scope: CoroutineScope,
    model: ProcessThemesModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {


    }

    @Composable
    fun Content() {

    }
}