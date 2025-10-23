package hnau.echospeak.projector.process

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.identity
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.Overcompose
import hnau.common.app.projector.utils.copy
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.common.kotlin.foldBoolean
import hnau.echospeak.model.process.VariantModel
import hnau.echospeak.projector.process.lines.LinesProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class VariantProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val model: VariantModel,
) {

    @Pipe
    interface Dependencies {

        fun lines(): LinesProjector.Dependencies
    }

    private val lines = LinesProjector(
        scope = scope,
        dependencies = dependencies.lines(),
        lines = model.lines,
    )

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        Overcompose(
            modifier = Modifier.fillMaxSize(),
            bottom = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding.copy(top = 0.dp)),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    model
                        .completeIfAvailable
                        .collectAsState()
                        .value
                        .NullableStateContent(
                            transitionSpec = TransitionSpec.vertical(),
                        ) { clickOrNull ->
                            Box(
                                modifier = Modifier
                                    .horizontalDisplayPadding()
                                    .verticalDisplayPadding(),
                            ) {
                                val currentOnClickOrNull by clickOrNull.collectAsState()
                                FloatingActionButton(
                                    onClick = { currentOnClickOrNull?.invoke() },
                                ) {
                                    (currentOnClickOrNull != null).StateContent(
                                        label = "CompleteOrLoading",
                                        contentKey = ::identity,
                                        transitionSpec = TransitionSpec.crossfade(),
                                    ) { clickable ->
                                        clickable.foldBoolean(
                                            ifTrue = {
                                                Icon(
                                                    icon = Icons.Default.ChevronRight,
                                                )
                                            },
                                            ifFalse = { CircularProgressIndicator() },
                                        )
                                    }
                                }
                            }
                        }
                }
            }
        ) { padding ->
            lines.Content(
                contentPadding = contentPadding.copy(
                    bottom = padding.calculateBottomPadding(),
                )
            )
        }
    }
}