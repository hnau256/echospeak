package hnau.echospeak.projector.themes.phrase.display

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.ActionOrCancel
import hnau.common.app.projector.utils.Icon
import hnau.echospeak.model.themes.phrase.display.SpeechDisplayModel
import hnau.echospeak.projector.resources.Res
import hnau.echospeak.projector.resources.listen
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpeechDisplayProjector(
    model: SpeechDisplayModel,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        ActionOrCancel(
            actionOrCancel = model.speakOrCancel.collectAsState().value,
            icon = { Icon(Icons.Default.Headphones) },
        ) { iconOrCancel, onClick ->
            ExtendedFloatingActionButton(
                onClick = onClick,
                text = { Text(stringResource(Res.string.listen)) },
                icon = iconOrCancel,
            )
        }
    }
}