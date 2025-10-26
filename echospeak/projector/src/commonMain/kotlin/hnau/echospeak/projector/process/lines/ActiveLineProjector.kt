package hnau.echospeak.projector.process.lines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.coroutines.mapState
import hnau.echospeak.model.process.lines.ActiveLineModel
import hnau.echospeak.projector.resources.Res
import hnau.echospeak.projector.resources.repeat_after
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

class ActiveLineProjector(
    scope: CoroutineScope,
    private val model: ActiveLineModel,
) {

    private val translate = TranslateProjector(
        scope = scope,
        model = model.translate,
    )

    private val recognize: StateFlow<RecognizeProjector?> = model
        .recognizing
        .mapState(scope) { modelOrNull ->
            modelOrNull?.let { model ->
                RecognizeProjector(
                    model = model,
                )
            }
        }

    @Composable
    fun Content() {
        Column {
            LineBubble(
                isActive = true,
                gender = model.gender,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(
                        onClick = model.cancel,
                    ) {
                        Icon(Icons.Default.StopCircle)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        translate.MainContent(Modifier.fillMaxWidth())
                        Row {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = model.text,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            translate.IconContent()
                        }
                        model
                            .recognize
                            .collectAsState()
                            .value
                            .NullableStateContent(
                                transitionSpec = TransitionSpec.vertical(),
                            ) { recognize ->
                                Button(
                                    onClick = recognize,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.repeat_after)
                                    )
                                }
                            }
                    }
                }
            }
            recognize
                .collectAsState()
                .value
                .NullableStateContent(
                    transitionSpec = TransitionSpec.vertical(),
                ) { recognize ->
                    recognize.Content()
                }
        }

    }
}