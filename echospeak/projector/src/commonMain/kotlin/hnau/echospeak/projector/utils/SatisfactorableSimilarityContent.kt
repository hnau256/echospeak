package hnau.echospeak.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.foldBoolean
import hnau.echospeak.model.utils.compare.SatisfactorableSimilarity

@Composable
internal fun SatisfactorableSimilarity.Content(
    modifier: Modifier = Modifier,
) {
    val state = remember(this) {
        KeyValue(
            key = similarity.factor.times(100).toInt().toString() + "%",
            value = satisfactory,
        )
    }
    state.StateContent(
        modifier = modifier,
        label = "SatisfactorableSimilarity",
        transitionSpec = TransitionSpec.crossfade(),
        contentKey = { it },
    ) { (text, satisfactory) ->
        Text(
            text = text,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
            color = satisfactory.foldBoolean(
                ifTrue = { MaterialTheme.colorScheme.primary },
                ifFalse = { MaterialTheme.colorScheme.error },
            ),
        )
    }
}