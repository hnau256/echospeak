@file:UseSerializers(
    MutableStateFlowSerializer::class,
    NonEmptyListSerializer::class,
)

package hnau.echospeak.model.process

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.serialization.NonEmptyListSerializer
import arrow.core.toNonEmptyListOrNull
import hnau.common.kotlin.Mutable
import hnau.common.kotlin.coroutines.Scoped
import hnau.common.kotlin.coroutines.createChild
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.runningFoldState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.engine.ChosenVariant
import hnau.echospeak.model.process.dto.Dialog
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.acos

class VariantModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
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
        val lines: MutableStateFlow<Pair<Lines<CompletedLineModel.Skeleton, ActiveLineModel.Skeleton>, List<String>>>,
    ) {

        constructor(
            dialog: Dialog,
            learnInfo: ChosenVariant.LearnInfo?,
        ) : this(
            title = dialog.title,
            learnInfo = learnInfo,
            lines = Pair(
                first = Lines.WithActive(
                    completed = emptyList<CompletedLineModel.Skeleton>(),
                    active = ActiveLineModel.Skeleton(dialog.lines.head),
                ),
                second = dialog.lines.tail,
            ).toMutableStateFlowAsInitial(),
        )
    }

    @Serializable
    sealed interface Lines<out C, out A> {

        @Serializable
        data class WithActive<out C, out A>(
            val completed: List<C>,
            val active: A,
        ) : Lines<C, A>

        @Serializable
        data class CompletedOnly<out C>(
            val completed: NonEmptyList<C>,
        ) : Lines<C, Nothing>
    }

    private fun CompletedLineModel.Skeleton.toActive(): ActiveLineModel.Skeleton =
        ActiveLineModel.Skeleton(
            text = text,
        )

    private fun ActiveLineModel.Skeleton.toCompleted(): CompletedLineModel.Skeleton =
        CompletedLineModel.Skeleton(
            text = text,
        )

    private fun activateCompleted(
        index: Int,
    ) {
        skeleton.lines.update { (lines, future) ->
            
        }
    }

    private fun switchToCompletedOnly() {
        skeleton.lines.update { (lines, future) ->
            val completedOnlyLines = when (lines) {
                is Lines.CompletedOnly -> lines
                is Lines.WithActive -> {
                    val activeAsCompleted = lines.active.toCompleted()
                    Lines.CompletedOnly(
                        completed = lines
                            .completed
                            .toNonEmptyListOrNull()
                            .foldNullable(
                                ifNull = { nonEmptyListOf(activeAsCompleted) },
                                ifNotNull = { nonEmptyCompleted ->
                                    nonEmptyCompleted + activeAsCompleted
                                }
                            )
                    )
                }
            }
            completedOnlyLines to future
        }
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
                retry = { moveActive(index) },
            ),
        )
    }

    private fun createActive(
        parentScope: CoroutineScope,
        skeleton: ActiveLineModel.Skeleton,
        index: Int,
    ): Scoped<ActiveLineModel> {
        val scope = parentScope.createChild()
        return Scoped(
            scope = scope,
            value = ActiveLineModel(
                scope = scope,
                dependencies = dependencies.active(),
                skeleton = skeleton,
                onReady = { moveActive(index + 1) },
                cancel = ::switchToCompletedOnly,
            ),
        )
    }

    val lines: StateFlow<Lines<CompletedLineModel, ActiveLineModel>> = skeleton
        .lines
        .scopedInState(scope)
        .runningFoldState(
            scope = scope,
            createInitial = { (scope, linesWithFuture) ->
                val (lines) = linesWithFuture
                when (lines) {
                    is Lines.CompletedOnly -> Lines.CompletedOnly(
                        completed = lines.completed.mapIndexed { i, skeleton ->
                            createCompleted(
                                index = i,
                                parentScope = scope,
                                skeleton = skeleton,
                            )
                        }
                    )

                    is Lines.WithActive -> Lines.WithActive(
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
                            index = lines.completed.size,
                        )
                    )
                }
            },
            operation = { previousLines, (scope, linesWithFuture) ->

                val cache: List<Mutable<Either<Scoped<CompletedLineModel>, Scoped<ActiveLineModel>>?>> =
                    when (previousLines) {
                        is Lines.CompletedOnly ->
                            previousLines
                                .completed
                                .map { completed -> completed.left() }

                        is Lines.WithActive ->
                            buildList<Either<Scoped<CompletedLineModel>, Scoped<ActiveLineModel>>> {
                                addAll(
                                    previousLines
                                        .completed
                                        .map { completed -> completed.left() }
                                )
                                add(
                                    Either.Right(previousLines.active)
                                )
                            }
                    }
                        .map(::Mutable)

                val extract: (Int) -> Either<Scoped<CompletedLineModel>, Scoped<ActiveLineModel>>? =
                    { i ->
                        cache
                            .getOrNull(i)
                            ?.let { mutable ->
                                val result = mutable.value
                                mutable.value = null
                                result
                            }

                    }

                val extractOrCreateCompleted: (Int, CompletedLineModel.Skeleton) -> Scoped<CompletedLineModel> =
                    { i, skeleton ->
                        extract(i)
                            ?.let { fromCache ->
                                when (fromCache) {
                                    is Either.Left -> fromCache.value
                                    is Either.Right -> {
                                        fromCache.value.scope.cancel()
                                        null
                                    }
                                }
                            } ?: createCompleted(
                            index = i,
                            parentScope = scope,
                            skeleton = skeleton,
                        )
                    }

                val extractOrCreateActive: (Int, ActiveLineModel.Skeleton) -> Scoped<ActiveLineModel> =
                    { i, skeleton ->
                        extract(i)
                            ?.let { fromCache ->
                                when (fromCache) {
                                    is Either.Right -> fromCache.value
                                    is Either.Left -> {
                                        fromCache.value.scope.cancel()
                                        null
                                    }
                                }
                            } ?: createActive(
                            parentScope = scope,
                            skeleton = skeleton,
                            index = i,
                        )
                    }


                val (lines) = linesWithFuture
                val result = when (lines) {
                    is Lines.CompletedOnly -> Lines.CompletedOnly(
                        completed = lines.completed.mapIndexed { i, skeleton ->
                            extractOrCreateCompleted(i, skeleton)
                        }
                    )

                    is Lines.WithActive -> Lines.WithActive(
                        completed = lines.completed.mapIndexed { i, skeleton ->
                            extractOrCreateCompleted(i, skeleton)
                        },
                        active = extractOrCreateActive(lines.completed.size, lines.active)
                    )
                }
                cache.forEach { item ->
                    item.value?.fold(
                        ifLeft = { it.scope.cancel() },
                        ifRight = { it.scope.cancel() },
                    )
                }
                result
            }
        )
        .mapState(scope) { scopedLines ->
            when (scopedLines) {
                is Lines.CompletedOnly -> Lines.CompletedOnly(
                    completed = scopedLines.completed.map(Scoped<CompletedLineModel>::value),
                )

                is Lines.WithActive -> Lines.WithActive(
                    completed = scopedLines.completed.map(Scoped<CompletedLineModel>::value),
                    active = scopedLines.active.value,
                )
            }
        }
}