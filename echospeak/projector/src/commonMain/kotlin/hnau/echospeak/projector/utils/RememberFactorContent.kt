package hnau.echospeak.projector.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.echospeak.engine.RememberFactor
import hnau.echospeak.projector.resources.Res
import hnau.echospeak.projector.resources.remember
import org.jetbrains.compose.resources.stringResource

@Composable
fun RememberFactor.Content(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(
            horizontal = Dimens.separation,
            vertical = Dimens.smallSeparation,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation)
    ) {
        CircularProgressIndicator(
            progress = { factor },
        )
        Text(
            stringResource(Res.string.remember)
        )
    }
}