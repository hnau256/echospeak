package hnau.echospeak.projector.process.lines

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import hnau.echospeak.model.process.ProcessModel
import hnau.echospeak.model.process.lines.CompletedLineModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope

class CompletedLineProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val model: CompletedLineModel,
) {

    @Pipe
    interface Dependencies

    @Composable
    fun Content() {
        LineBubble(
            gender = model.gender,
        ) {
            Text(
                text = model.text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}