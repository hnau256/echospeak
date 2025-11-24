package hnau.echospeak.projector.themes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.FullScreen
import hnau.common.app.projector.uikit.TopBar
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.app.projector.utils.getTransitionSpecForSlide
import hnau.common.kotlin.KeyValue
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.map
import hnau.echospeak.model.themes.ProcessThemesModel
import hnau.echospeak.model.themes.dto.PhraseVariant
import hnau.echospeak.projector.utils.BackButtonWidth
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ProcessThemesProjector(
    scope: CoroutineScope,
    model: ProcessThemesModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth

        fun phrase(): PhraseProjector.Dependencies
    }

    private val phrase: StateFlow<Loadable<KeyValue<PhraseVariant, PhraseProjector>>> = model
        .phraseOrLoading
        .mapWithScope(scope) { scope, phraseOrLoading ->
            phraseOrLoading.map { variantWithPhrase ->
                variantWithPhrase.map { phrase ->
                    PhraseProjector(
                        scope = scope,
                        model = phrase,
                        dependencies = dependencies.phrase(),
                    )
                }

            }
        }

    @Composable
    fun Content() {
        FullScreen(
            backButtonWidth = dependencies.backButtonWidth.width,
            top = { contentPadding ->
                TopBar(
                    modifier = Modifier.padding(contentPadding),
                ) {
                }
            },
        ) { contentPadding ->
            phrase
                .collectAsState()
                .value
                .LoadableContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = TransitionSpec.crossfade(),
                ) { phrase ->
                    phrase
                        .StateContent(
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = getTransitionSpecForSlide(
                                orientation = SlideOrientation.Horizontal
                            ) { 1f },
                            label = "Phrase",
                            contentKey = KeyValue<PhraseVariant, *>::key
                        ) { (_, projector) ->
                            projector.Content(
                                contentPadding = contentPadding,
                            )
                        }
                }
        }
    }
}