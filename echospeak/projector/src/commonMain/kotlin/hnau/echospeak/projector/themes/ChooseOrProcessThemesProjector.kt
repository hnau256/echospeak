package hnau.echospeak.projector.themes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.app.projector.utils.getTransitionSpecForSlide
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.echospeak.model.themes.ChooseOrProcessStateModel
import hnau.echospeak.model.themes.ChooseOrProcessThemesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sign

class ChooseOrProcessThemesProjector(
    scope: CoroutineScope,
    private val model: ChooseOrProcessThemesModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun choose(): ChooseThemesProjector.Dependencies

        fun process(): ProcessThemesProjector.Dependencies
    }

    private sealed interface State {

        val key: Int

        @Composable
        fun Content()

        data class Choose(
            private val projector: ChooseThemesProjector,
        ) : State {

            override val key: Int
                get() = 0

            @Composable
            override fun Content() {
                projector.Content()
            }
        }

        data class Process(
            private val projector: ProcessThemesProjector,
        ) : State {

            override val key: Int
                get() = 1

            @Composable
            override fun Content() {
                projector.Content()
            }
        }
    }

    private val state: StateFlow<State> = model
        .state
        .mapWithScope(scope) { scope, state ->
            when (state) {
                is ChooseOrProcessStateModel.Choose -> State.Choose(
                    ChooseThemesProjector(
                        model = state.model,
                        dependencies = dependencies.choose(),
                    )
                )

                is ChooseOrProcessStateModel.Process -> State.Process(
                    ProcessThemesProjector(
                        scope = scope,
                        model = state.model,
                        dependencies = dependencies.process(),
                    )
                )
            }
        }

    @Composable
    fun Content() {
        state
            .collectAsState()
            .value
            .StateContent(
                modifier = Modifier.fillMaxSize(),
                transitionSpec = getTransitionSpecForSlide(
                    orientation = SlideOrientation.Horizontal,
                ) {
                    (targetState.key - initialState.key).sign * 0.5f
                },
                label = "ChooseOrProcessThemes",
                contentKey = { it.key },
            ) { state ->
                state.Content()
            }
    }
}