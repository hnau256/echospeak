package hnau.echospeak.projector.process.lines

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.kotlin.coroutines.mapState
import hnau.echospeak.model.process.lines.ActiveLineModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ActiveLineProjector(
    scope: CoroutineScope,
    private val model: ActiveLineModel,
) {

    private val recognize: StateFlow<RecognizeProjector?> = model
        .recognize
        .mapState(scope) { modelOrNull ->
            modelOrNull?.let { model ->
                RecognizeProjector(
                    model = model,
                )
            }
        }

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
}