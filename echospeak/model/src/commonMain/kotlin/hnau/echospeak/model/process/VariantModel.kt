@file:UseSerializers(
    MutableStateFlowSerializer::class,
    NonEmptyListSerializer::class,
)

package hnau.echospeak.model.process

import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.kotlin.coroutines.Scoped
import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.runningFoldState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.engine.ChosenVariant
import hnau.echospeak.model.process.dto.Dialog
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class VariantModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun completed(): CompletedLineModel.Dependencies

        fun active(): ActiveLineModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val title: String,
        val learnInfo: ChosenVariant.LearnInfo?,
        val lines: MutableStateFlow<Lines>,
    ) {

        @Serializable
        data class Lines(
            val completed: List<CompletedLineModel.Skeleton>,
            val active: ActiveLineModel.Skeleton,
            val future: List<String>,
        ) {

            constructor(
                lines: NonEmptyList<String>,
            ) : this(
                completed = emptyList(),
                active = ActiveLineModel.Skeleton(lines.head),
                future = lines.tail,
            )
        }

        constructor(
            dialog: Dialog,
            learnInfo: ChosenVariant.LearnInfo?,
        ) : this(
            title = dialog.title,
            learnInfo = learnInfo,
            lines = Lines(
                lines = dialog.lines,
            ).toMutableStateFlowAsInitial(),
        )
    }

    data class Lines(
        val completed: List<CompletedLineModel>,
        val active: ActiveLineModel,
        val future: List<String>,
    )

    private data class ScopedLines(
        val completed: List<Scoped<CompletedLineModel>>,
        val active: Scoped<ActiveLineModel>,
        val future: List<String>,
    ) {

        inline fun <R> use(
            index: Int,
            isCompleted: (Scoped<CompletedLineModel>) -> R,
            isActive: (Scoped<ActiveLineModel>) -> R,
            isFuture: () -> R,
        ): R = when {
            index < completed.size -> isCompleted(completed[index])
            index == completed.size -> isActive(active)
            else -> isFuture()
        }
    }

    private fun moveActive(
        updateIndex: (Int) -> Int,
    ) {
        TODO()
    }

    private fun createCompleted(
        parentScope: CoroutineScope,
        skeleton: CompletedLineModel.Skeleton,
        index: Int,
    ): Scoped<CompletedLineModel> {
        val scope = parentScope.createChild()
        return Scoped(
            scope = scope,
            value = CompletedLineModel(
                scope = scope,
                dependencies = dependencies.completed(),
                skeleton = skeleton,
                retry = { moveActive { index } },
            ),
        )
    }

    private fun createActive(
        parentScope: CoroutineScope,
        skeleton: ActiveLineModel.Skeleton,
    ): Scoped<ActiveLineModel> {
        val scope = parentScope.createChild()
        return Scoped(
            scope = scope,
            value = ActiveLineModel(
                scope = scope,
                dependencies = dependencies.active(),
                skeleton = skeleton,
                onReady = { moveActive { currentIndex -> currentIndex + 1 } },
            ),
        )
    }

    val lines: StateFlow<Lines> = skeleton
        .lines
        .scopedInState(scope)
        .runningFoldState(
            scope = scope,
            createInitial = { (scope, lines) ->
                ScopedLines(
                    completed = lines.completed.mapIndexed { i, skeleton ->
                        createCompleted(
                            index = i,
                            parentScope = scope,
                            skeleton = skeleton,
                        )
                    },
                    active = createActive(
                        parentScope = scope,
                        skeleton = lines.active,
                    ),
                    future = lines.future,
                )
            },
            operation = { previousLines, (scope, lines) ->
                val result = ScopedLines(
                    completed = lines.completed.mapIndexed { i, skeleton ->
                        previousLines.use(
                            index = i,
                            isCompleted = { completed -> completed },
                            isActive = { _ ->
                                createCompleted(
                                    index = i,
                                    parentScope = scope,
                                    skeleton = skeleton,
                                )
                            },
                            isFuture = {
                                createCompleted(
                                    index = i,
                                    parentScope = scope,
                                    skeleton = skeleton,
                                )
                            }
                        )
                    },
                    active = previousLines.use(
                        index = lines.completed.size,
                        isCompleted = { _ ->
                            createActive(
                                parentScope = scope,
                                skeleton = lines.active,
                            )
                        },
                        isActive = ::identity,
                        isFuture = {
                            createActive(
                                parentScope = scope,
                                skeleton = lines.active,
                            )
                        }
                    ),
                    future = lines.future,
                )

                previousLines
                    .completed
                    .drop(lines.completed.size)
                    .forEach { (scope) -> scope.cancel() }

                if (previousLines.completed.size != lines.completed.size) {
                    previousLines.active.scope.cancel()
                }

                result
            }
        )
        .mapState(scope) { scopedLines ->
            Lines(
                completed = scopedLines.completed.map(Scoped<CompletedLineModel>::value),
                active = scopedLines.active.value,
                future = scopedLines.future,
            )
        }
}