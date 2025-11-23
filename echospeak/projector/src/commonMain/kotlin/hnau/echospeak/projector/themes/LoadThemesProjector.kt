package hnau.echospeak.projector.themes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.map
import hnau.echospeak.model.themes.LoadThemesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class LoadThemesProjector(
    scope: CoroutineScope,
    model: LoadThemesModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun chooseOrProcess(): ChooseOrProcessThemesProjector.Dependencies
    }

    private val chooseOrProcess: StateFlow<Loadable<ChooseOrProcessThemesProjector>> = model
        .chooseOrProcess
        .mapWithScope(scope) { scope, themesOrLoading ->
            themesOrLoading.map { themes ->
                ChooseOrProcessThemesProjector(
                    scope = scope,
                    model = themes,
                    dependencies = dependencies.chooseOrProcess(),
                )
            }
        }

    @Composable
    fun Content() {
        chooseOrProcess
            .collectAsState()
            .value
            .LoadableContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = TransitionSpec.crossfade(),
            ) { themes ->
                themes.Content()
            }
    }
}