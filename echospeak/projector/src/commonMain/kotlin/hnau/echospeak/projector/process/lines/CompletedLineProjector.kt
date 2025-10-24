package hnau.echospeak.projector.process.lines

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import hnau.echospeak.model.process.lines.CompletedLineModel

class CompletedLineProjector(
    private val model: CompletedLineModel,
) {

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