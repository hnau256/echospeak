package hnau.echospeak.projector.themes.phrase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.echospeak.model.themes.phrase.RecognizeModel
import hnau.echospeak.projector.utils.Content

class RecognizeProjector(
    private val model: RecognizeModel,
) {

    @Composable
    fun Content() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalDisplayPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            OutlinedIconButton(
                onClick = model.cancel,
            ) {
                Icon(Icons.Default.Close)
            }
            Text(
                modifier = Modifier.weight(1f),
                text = model
                    .text
                    .collectAsState()
                    .value,
                maxLines = 3,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            model
                .similarity
                .collectAsState()
                .value
                .Content()
        }
    }
}