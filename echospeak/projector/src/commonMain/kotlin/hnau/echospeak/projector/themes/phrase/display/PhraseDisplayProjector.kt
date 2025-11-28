package hnau.echospeak.projector.themes.phrase.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.Tabs
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.app.projector.utils.getTransitionSpecForSlide
import hnau.common.kotlin.KeyValue
import hnau.echospeak.model.themes.DisplayMode
import hnau.echospeak.model.themes.phrase.display.PhraseDisplayModel
import hnau.echospeak.projector.resources.Res
import hnau.echospeak.projector.resources.listen
import hnau.echospeak.projector.resources.read
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import kotlin.math.sign

@Composable
fun PhraseDisplayProjector(
    model: PhraseDisplayModel,
    modifier: Modifier = Modifier,
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {

        val state: KeyValue<DisplayMode, PhraseDisplayModel.State> by model
            .state
            .collectAsState()

        Tabs(
            items = DisplayMode.entries.toImmutableList(),
            selected = state.key,
            onSelectedChanged = model::setDisplayMode,
        ) { displayMode ->
            Text(
                stringResource(
                    when (displayMode) {
                        DisplayMode.Text -> Res.string.read
                        DisplayMode.Speech -> Res.string.listen
                    }
                )
            )
        }

        state.StateContent(
            modifier = Modifier.fillMaxWidth(),
            label = "DisplayMode",
            contentKey = { it.key },
            transitionSpec = getTransitionSpecForSlide(
                orientation = SlideOrientation.Horizontal,
            ) {
                (targetState.key.ordinal - initialState.key.ordinal).sign * 0.5f
            },
            contentAlignment = Alignment.TopCenter,
        ) {
            when (val state = it.value) {
                is PhraseDisplayModel.State.Speech -> SpeechDisplayProjector(
                    model = state.model,
                )

                is PhraseDisplayModel.State.Text -> TextDisplayProjector(
                    model = state.model,
                )
            }
        }
    }
}