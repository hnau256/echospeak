package hnau.echospeak.projector.themes.phrase.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.echospeak.model.themes.phrase.display.TextDisplayModel
import hnau.echospeak.projector.resources.Res
import hnau.echospeak.projector.resources.comment
import hnau.echospeak.projector.resources.phrase
import hnau.echospeak.projector.resources.translation
import org.jetbrains.compose.resources.stringResource

@Composable
fun TextDisplayProjector(
    model: TextDisplayModel,
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        TextWithTitle(
            title = stringResource(Res.string.phrase),
            text = model.phrase.phrase,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(Dimens.separation))

        TextWithTitle(
            title = stringResource(Res.string.translation),
            text = model.phrase.translation,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(Dimens.separation))

        TextWithTitle(
            title = stringResource(Res.string.comment),
            text = model.phrase.comment,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

}


@Composable
private fun TextWithTitle(
    title: String,
    text: String,
    color: Color,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = Dimens.separation)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
        )
    }
}