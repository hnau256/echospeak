package hnau.echospeak.projector.process

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.FullScreen
import hnau.common.app.projector.uikit.TopBar
import hnau.common.app.projector.uikit.state.LoadableContent
import hnau.common.app.projector.utils.SlideOrientation
import hnau.common.app.projector.utils.getTransitionSpecForSlide
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.map
import hnau.echospeak.model.process.ProcessModel
import hnau.echospeak.projector.utils.BackButtonWidth
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class ProcessProjector(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    model: ProcessModel,
) {

    @Pipe
    interface Dependencies {

        val backButtonWidth: BackButtonWidth
    }

    private val variant: StateFlow<Loadable<VariantProjector?>> = model
        .variantOrLoadingOrError
        .mapWithScope(scope) { scope, variantOrLoadingOrError ->
            variantOrLoadingOrError.map { variantOrError ->
                variantOrError?.let { variant ->
                    VariantProjector(
                        scope = scope,
                        model = variant,
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
                    //TODO text visibility
                }
            },
        ) { contentPadding ->
            variant
                .collectAsState()
                .value
                .LoadableContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = getTransitionSpecForSlide(
                        orientation = SlideOrientation.Horizontal,
                        slideCoefficientProvider = { 1f },
                    ),
                ) { variantOrError ->
                    variantOrError.foldNullable(
                        ifNull = {
                            Error(
                                contentPadding = contentPadding,
                            )
                        },
                        ifNotNull = { variant ->
                            variant.Content(
                                contentPadding = contentPadding,
                            )
                        }
                    )
                }
        }
    }

    @Composable
    private fun Error(
        contentPadding: PaddingValues,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .horizontalDisplayPadding()
                .verticalDisplayPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Error", //TODO
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}