package hnau.echospeak.projector.process.lines

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import hnau.common.app.projector.utils.Icon
import hnau.echospeak.model.process.lines.CompletedLineModel

class CompletedLineProjector(
    private val model: CompletedLineModel,
) {

    @Composable
    fun Content() {
        LineBubble(
            gender = model.gender,
            isActive = false,
        ) {
            Row {
                IconButton(
                    onClick = model.retry,
                ) {
                    Icon(Icons.Default.Replay)
                }
                Text(
                    text = model.text,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}