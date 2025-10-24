package hnau.echospeak.projector.process.lines

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.echospeak.model.process.ProcessModel
import hnau.echospeak.model.process.lines.ActiveLineModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class ActiveLineProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val model: ActiveLineModel,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {
        LineBubble(
            gender = model.gender,
        ) {
            Column {
                Text(
                    text = model.text,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                model
                    .recognitionResult
                    .collectAsState()
                    .value
                    .NullableStateContent(
                        transitionSpec = TransitionSpec.vertical(),
                    ) { recognizedText ->
                        Text(
                            text = recognizedText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
            }

        }
    }
}