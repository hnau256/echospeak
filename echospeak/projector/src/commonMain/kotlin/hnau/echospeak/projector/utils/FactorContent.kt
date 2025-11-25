package hnau.echospeak.projector.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.kotlin.foldNullable
import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.engine.RememberFactor
import hnau.echospeak.projector.resources.Res
import hnau.echospeak.projector.resources.know
import hnau.echospeak.projector.resources.remember
import org.jetbrains.compose.resources.stringResource
import kotlin.math.exp
import kotlin.math.ln

@Composable
fun KnowFactor(
    factorOrUnknown: KnowFactor?,
    modifier: Modifier = Modifier,
) {
    FactorContent(
        modifier = modifier,
        factorOrUnknown = factorOrUnknown
            ?.factor
            ?.let { factor ->
                remember(factor) {
                    val x = factor - 1
                    val k = -ln(2.0) / (500 - 1)
                    1f - exp(k * x).toFloat()
                }
            },
        title = stringResource(Res.string.know),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    )
}

@Composable
fun RememberFactor(
    factorOrUnknown: RememberFactor?,
    modifier: Modifier = Modifier,
) {
    FactorContent(
        modifier = modifier,
        factorOrUnknown = factorOrUnknown?.factor,
        title = stringResource(Res.string.remember),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    )
}

@Composable
private fun FactorContent(
    factorOrUnknown: Float?,
    title: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = contentColorFor(containerColor),
) {
    Row(
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(50),
            )
            .padding(
                start = Dimens.smallSeparation,
                top = Dimens.smallSeparation,
                bottom = Dimens.smallSeparation,
                end = Dimens.separation,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation)
    ) {
        val transparentContentColor = contentColor.copy(alpha = 0.25f)
        factorOrUnknown.foldNullable(
            ifNull = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = transparentContentColor,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "?",
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                }
            },
            ifNotNull = { factor ->
                CircularProgressIndicator(
                    progress = { factor },
                    color = contentColor,
                    trackColor = transparentContentColor,
                )
            }
        )
        Text(
            text = title,
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
        )
    }
}