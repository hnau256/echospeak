package hnau.echospeak.projector.themes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.map
import hnau.echospeak.model.themes.ThemesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ThemesProjector(
    scope: CoroutineScope,
    model: ThemesModel,
    dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun list(): ThemesListProjector.Dependencies
    }

    private val list: StateFlow<Loadable<ThemesListProjector>> = model
        .list
        .mapState(scope) { themesOrLoading ->
            themesOrLoading.map { themes ->
                ThemesListProjector(
                    model = themes,
                    dependencies = dependencies.list(),
                )
            }
        }

    @Composable
    fun Content() {
        list
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