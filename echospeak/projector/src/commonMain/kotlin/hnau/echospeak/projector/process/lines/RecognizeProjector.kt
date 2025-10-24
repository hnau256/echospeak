package hnau.echospeak.projector.process.lines

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VoiceChat
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.foldBoolean
import hnau.echospeak.model.process.lines.RecognizeModel
import hnau.pipe.annotations.Pipe

class RecognizeProjector(
    private val model: RecognizeModel,
) {

    @Composable
    fun Content() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.VoiceChat)
            IconButton(
                onClick = model.retry
            ) {
                Icon(Icons.Default.Replay)
            }
            Text(
                modifier = Modifier.weight(1f),
                text = model
                    .recognitionResult
                    .collectAsState()
                    .value,
            )
            model
                .correctFactor
                .collectAsState()
                .value
                .NullableStateContent(
                    transitionSpec = TransitionSpec.horizontal(),
                ) { (correctFactor, success) ->
                    Row {
                        Spacer(Modifier.width(Dimens.smallSeparation))
                        Text(
                            text = (correctFactor * 100).toInt().toString() + "%",
                            style = MaterialTheme.typography.titleMedium,
                            color = success.foldBoolean(
                                ifTrue = { MaterialTheme.colorScheme.primary },
                                ifFalse = { MaterialTheme.colorScheme.error },
                            )
                        )
                    }
                }
        }

    }
}