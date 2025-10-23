package hnau.echospeak.projector.process.lines

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hnau.common.app.model.utils.Hue
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.SwitchHue
import hnau.echospeak.model.process.dto.Gender
import hnau.echospeak.model.process.dto.GenderValues

@Composable
fun LineBubble(
    gender: Gender,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SwitchHue(
        hue = gendersHues[gender]
    ) {
        Card(
            modifier = modifier,
        ) {
            Box(
                modifier = Modifier.padding(
                    horizontal = Dimens.separation,
                    vertical = Dimens.smallSeparation,
                )
            ) {
                content()
            }
        }
    }
}

private val gendersHues: GenderValues<Hue> = GenderValues(
    male = Hue(270),
    female = Hue(330),
)