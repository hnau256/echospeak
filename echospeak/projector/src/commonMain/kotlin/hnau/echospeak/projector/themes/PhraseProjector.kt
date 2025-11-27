package hnau.echospeak.projector.themes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.common.kotlin.coroutines.mapState
import hnau.echospeak.model.themes.phrase.PhraseModel
import hnau.echospeak.model.themes.phrase.PhraseStateModel
import hnau.echospeak.model.utils.compare.SatisfactorableSimilarity
import hnau.echospeak.projector.resources.Res
import hnau.echospeak.projector.resources.comment
import hnau.echospeak.projector.resources.next
import hnau.echospeak.projector.resources.phrase
import hnau.echospeak.projector.resources.say
import hnau.echospeak.projector.resources.translation
import hnau.echospeak.projector.themes.phrase.RecognizeProjector
import hnau.echospeak.projector.themes.phrase.display.PhraseDisplayProjector
import hnau.echospeak.projector.utils.Content
import hnau.echospeak.projector.utils.KnowFactor
import hnau.echospeak.projector.utils.RememberFactor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class PhraseProjector(
    scope: CoroutineScope,
    private val model: PhraseModel,
) {

    private sealed interface State {

        data class WaitingForRecognizing(
            val recognize: () -> Unit,
        ) : State

        data class Recognizing(
            val projector: RecognizeProjector,
        ) : State

        data class Recognized(
            val similarity: SatisfactorableSimilarity,
            val retry: () -> Unit,
            val complete: StateFlow<(() -> Unit)?>?,
        ) : State
    }

    private val state: StateFlow<State> = model
        .state
        .mapState(scope) { state ->
            when (state) {
                is PhraseStateModel.WaitingForRecognizing -> State.WaitingForRecognizing(
                    recognize = state.recognize,
                )

                is PhraseStateModel.Recognizing -> State.Recognizing(
                    projector = RecognizeProjector(
                        model = state.model,
                    )
                )

                is PhraseStateModel.Recognized -> State.Recognized(
                    similarity = state.similarity,
                    retry = state.retry,
                    complete = state.complete,
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .verticalDisplayPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Row(
                modifier = Modifier.horizontalDisplayPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = Dimens.separation,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                KnowFactor(
                    factorOrUnknown = model.learnInfo?.info?.knowFactor,
                )
                RememberFactor(
                    factorOrUnknown = model.learnInfo?.rememberFactor,
                )
            }

            Spacer(Modifier.height(Dimens.separation))

            PhraseDisplayProjector(
                model = model.display,
            )

            Spacer(Modifier.weight(1f))

            state
                .collectAsState()
                .value
                .StateContent(
                    modifier = Modifier.fillMaxWidth(),
                    label = "PhraseState",
                    transitionSpec = TransitionSpec.crossfade(),
                    contentKey = { state ->
                        when (state) {
                            is State.WaitingForRecognizing -> 0
                            is State.Recognizing -> 1
                            is State.Recognized -> 2
                        }
                    }
                ) { state ->
                    when (state) {
                        is State.WaitingForRecognizing -> Actions {
                            ExtendedFloatingActionButton(
                                onClick = state.recognize,
                                text = { Text(stringResource(Res.string.say)) },
                                icon = { Icon(Icons.Default.Mic) },
                            )
                        }

                        is State.Recognizing -> state.projector.Content()
                        is State.Recognized -> Actions {

                            FloatingActionButton(
                                onClick = state.retry,
                            ) { Icon(Icons.Default.Replay) }

                            state.similarity.Content()

                            state.complete?.let { complete ->
                                val onClickOrNull by complete.collectAsState()
                                ExtendedFloatingActionButton(
                                    onClick = { onClickOrNull?.invoke() },
                                    text = { Text(stringResource(Res.string.next)) },
                                    icon = { Icon(Icons.Default.KeyboardDoubleArrowRight) },
                                )
                            }
                        }
                    }
                }
        }
    }

    @Composable
    private fun Actions(
        content: @Composable RowScope.() -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = object : Arrangement.Horizontal {

                override fun Density.arrange(
                    totalSize: Int,
                    sizes: IntArray,
                    layoutDirection: LayoutDirection,
                    outPositions: IntArray
                ) {
                    val delegate = when (sizes.size) {
                        1 -> Arrangement.Center
                        else -> Arrangement.SpaceBetween
                    }
                    with(delegate) {
                        arrange(
                            totalSize = totalSize,
                            sizes = sizes,
                            layoutDirection = layoutDirection,
                            outPositions = outPositions,
                        )
                    }
                }

            },
        ) {
            content()
        }
    }
}