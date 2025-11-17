package hnau.echospeak.projector

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import hnau.common.app.projector.uikit.backbutton.BackButtonProjector
import hnau.echospeak.model.RootModel
import hnau.echospeak.projector.process.ProcessProjector
import hnau.echospeak.projector.themes.ThemesStackProjector
import hnau.echospeak.projector.utils.BackButtonWidth
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class RootProjector(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    model: RootModel,
) {

    @Pipe
    interface Dependencies {

        fun process(
            backButtonWidth: BackButtonWidth,
        ): ProcessProjector.Dependencies

        fun themes(
            backButtonWidth: BackButtonWidth,
        ): ThemesStackProjector.Dependencies

        companion object
    }

    /*private val bubblesHolder = SharedBubblesHolder(
        scope = scope,
    )*/

    private val backButton = BackButtonProjector(
        scope = scope,
        goBackHandler = model.goBackHandler,
    )

    private val process = ProcessProjector(
        scope = scope,
        model = model.process,
        dependencies = dependencies.process(
            backButtonWidth = BackButtonWidth.create(backButton),
        ),
    )

    private val loadThemes = ThemesStackProjector(
        scope = scope,
        model = model.themes,
        dependencies = dependencies.themes(
            backButtonWidth = BackButtonWidth.create(backButton),
        ),
    )

    @Composable
    fun Content() {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            //LocalDensity provides Density(LocalDensity.current.density * 1.1f),
        ) {
            loadThemes.Content()
            backButton.Content()
            //bubblesHolder.Content()
        }
    }
}