package hnau.echospeak.projector

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import hnau.common.app.projector.uikit.backbutton.BackButtonProjector
import hnau.echospeak.model.RootModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class RootProjector(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    model: RootModel,
) {

    @Pipe
    interface Dependencies {

        companion object
    }

    /*private val bubblesHolder = SharedBubblesHolder(
        scope = scope,
    )*/

    private val backButton = BackButtonProjector(
        scope = scope,
        goBackHandler = model.goBackHandler,
    )

    @Composable
    fun Content() {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            //LocalDensity provides Density(LocalDensity.current.density * 1.1f),
        ) {
            backButton.Content()
            //bubblesHolder.Content()
        }
    }
}