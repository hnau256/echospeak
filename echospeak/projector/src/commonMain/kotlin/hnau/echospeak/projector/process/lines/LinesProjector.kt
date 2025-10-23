package hnau.echospeak.projector.process.lines

import androidx.annotation.Dimension
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import arrow.core.Either
import hnau.common.app.projector.uikit.state.StateContent
import hnau.common.app.projector.uikit.state.TransitionSpec
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.horizontalDisplayPadding
import hnau.common.app.projector.utils.plus
import hnau.common.app.projector.utils.verticalDisplayPadding
import hnau.common.kotlin.coroutines.mapListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.map
import hnau.echospeak.model.process.lines.ActiveLineModel
import hnau.echospeak.model.process.lines.CompletedLineModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class LinesProjector(
    scope: CoroutineScope,
    dependencies: Dependencies,
    lines: StateFlow<List<Either<CompletedLineModel, ActiveLineModel>>>,
) {

    @Pipe
    interface Dependencies {

        fun active(): ActiveLineProjector.Dependencies

        fun completed(): CompletedLineProjector.Dependencies
    }

    private val lines: StateFlow<List<IndexedValue<Either<CompletedLineProjector, ActiveLineProjector>>>> =
        lines
            .mapState(scope) { models ->
                models.mapIndexed(::IndexedValue)
            }
            .mapListReusable(
                scope = scope,
                extractKey = { (i, model) ->
                    val active = model.isRight()
                    i to active
                },
            ) { scope, indexedModel ->
                indexedModel.map { model ->
                    model
                        .map { model ->
                            ActiveLineProjector(
                                scope = scope,
                                model = model,
                                dependencies = dependencies.active(),
                            )
                        }
                        .mapLeft { model ->
                            CompletedLineProjector(
                                scope = scope,
                                model = model,
                                dependencies = dependencies.completed(),
                            )
                        }
                }
            }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        val lines by lines.collectAsState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(
                horizontal = Dimens.horizontalDisplayPadding,
                vertical = Dimens.verticalDisplayPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            items(
                items = lines,
                key = { it.index },
            ) { (_, projector) ->
                projector
                    .StateContent(
                        modifier = Modifier.fillMaxWidth(),
                        label = "ActiveOrCompleted",
                        contentKey = { it.isRight() },
                        transitionSpec = TransitionSpec.crossfade(),
                    ) { completedOrActive ->
                        completedOrActive.fold(
                            ifLeft = { completed -> completed.Content() },
                            ifRight = { active -> active.Content() },
                        )
                    }
            }
        }
    }
}