package hnau.echospeak.projector.process.lines

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.state.NullableStateContent
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.common.kotlin.coroutines.mapState
import hnau.echospeak.model.process.lines.TranslateModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class TranslateProjector(
    scope: CoroutineScope,
    private val model: TranslateModel,
) {

    private val onClickOrNull: StateFlow<(() -> Unit)?> = model
        .state
        .mapState(scope) { state ->
            when (state) {
                is TranslateModel.State.Translated -> state.close
                is TranslateModel.State.Translate -> state.translate
                TranslateModel.State.Translating -> null
            }
        }

    @Composable
    fun IconContent() {
        val onClick by onClickOrNull.collectAsState()
        IconButton(
            onClick = { onClick?.invoke() },
            enabled = onClick != null,
        ) {
            model
                .state
                .collectAsState()
                .value
                .StateContent(
                    label = "TranslationState",
                    contentKey = { state ->
                        when (state) {
                            is TranslateModel.State.Translate -> 0
                            TranslateModel.State.Translating -> 1
                            is TranslateModel.State.Translated -> 2
                        }
                    },
                    transitionSpec = TransitionSpec.crossfade(),
                ) { state ->
                    when (state) {
                        is TranslateModel.State.Translate -> Icon(Icons.Default.Translate)
                        is TranslateModel.State.Translated -> Icon(Icons.Default.Close)
                        TranslateModel.State.Translating -> CircularProgressIndicator()
                    }
                }
        }
    }

    private val translation: StateFlow<String?> = model
        .state
        .mapState(scope) { state ->
            when (state) {
                is TranslateModel.State.Translated -> state.translation
                is TranslateModel.State.Translate,
                TranslateModel.State.Translating -> null
            }
        }

    @Composable
    fun MainContent(
        modifier: Modifier,
    ) {
        translation
            .collectAsState()
            .value
            .NullableStateContent(
                modifier = modifier,
                transitionSpec = TransitionSpec.vertical(),
            ) { translation ->
                Text(
                    modifier = Modifier.padding(vertical = Dimens.smallSeparation),
                    text = translation,
                )
            }
    }
}