package hnau.echospeak.projector

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.map
import hnau.echospeak.model.PrepareModel
import hnau.echospeak.projector.themes.LoadThemesProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class PrepareProjector(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    model: PrepareModel,
) {

    @Pipe
    interface Dependencies {

        fun themes(): LoadThemesProjector.Dependencies

        companion object
    }

    private val themesOrErrorOrLoading: StateFlow<Loadable<LoadThemesProjector?>> = model
        .themes
        .mapWithScope(scope) { scope, themesOrErrorOrLoading ->
            themesOrErrorOrLoading.map { themesOrError ->
                themesOrError?.let { themes ->
                    LoadThemesProjector(
                        scope = scope,
                        dependencies = dependencies.themes(),
                        model = themes,
                    )
                }
            }
        }

    @Composable
    fun Content() {
        themesOrErrorOrLoading
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.crossfade(),
            ) { themesOrError ->
                themesOrError?.Content()
            }
    }
}