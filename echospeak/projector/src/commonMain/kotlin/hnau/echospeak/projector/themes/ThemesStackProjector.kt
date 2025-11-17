package hnau.echospeak.projector.themes

import androidx.compose.runtime.Composable
import hnau.common.app.projector.stack.Content
import hnau.common.app.projector.stack.StackProjectorTail
import hnau.echospeak.model.themes.ThemesStackElementModel
import hnau.echospeak.model.themes.ThemesStackModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ThemesStackProjector(
    private val scope: CoroutineScope,
    private val model: ThemesStackModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun themes(): ThemesProjector.Dependencies
    }

    private val tail: StateFlow<StackProjectorTail<Int, ThemesStackElementProjector>> =
        StackProjectorTail(
            scope = scope,
            modelsStack = model.stack,
            extractKey = { model -> model.key },
            createProjector = { scope, model ->
                when (model) {
                    is ThemesStackElementModel.Themes -> ThemesStackElementProjector.Themes(
                        ThemesProjector(
                            scope = scope,
                            model = model.model,
                            dependencies = dependencies.themes(),
                        )
                    )
                }
            }
        )

    @Composable
    fun Content() {
        tail.Content { elementProjector ->
            elementProjector.Content()
        }
    }
}