package hnau.echospeak.projector.process.lines

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import hnau.echospeak.model.process.lines.RecognizeModel

class RecognizeProjector(
    private val model: RecognizeModel,
) {

    @Composable
    fun Content() {
        val text by model
            .recognitionResult
            .collectAsState()
        Text(
            text = text,
        )
    }
}