@file:UseSerializers(
    MutableStateFlowSerializer::class,
    NonEmptyListSerializer::class,
    EitherSerializer::class,
)

package hnau.echospeak.model.process.lines

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import arrow.core.serialization.EitherSerializer
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
import hnau.echospeak.model.process.dto.Gender
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class LinesModel(
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
        val lines: MutableStateFlow<Pair<Lines<CompletedLineModel.Skeleton, ActiveLineModel.Skeleton>, List<String>>>,
        val firstLineGender: Gender,
    ) {

        constructor(
            lines: NonEmptyList<String>,
            firstLineGender: Gender,
        ) : this(
            lines = Pair(
                first = Lines(
                    completed = emptyList<CompletedLineModel.Skeleton>(),
                    last = createActiveSkeleton(lines.head).right(),
                ),
                second = lines.tail,
            ).toMutableStateFlowAsInitial(),
            firstLineGender = firstLineGender,
        )
    }

    @Serializable
    data class Lines<out C, out A>(
        val completed: List<C>,
        val last: Either<C, A>,
    )

    private inline fun <CI, CO, A> Lines<CI, A>.mapCompleted(
        transform: (Int, CI) -> CO,
    ): Lines<CO, A> = Lines(
        completed = completed.mapIndexed(transform),
        last = last.mapLeft { it ->
            transform(completed.size, it)
        },
    )

    private inline fun <C, AI, AO> Lines<C, AI>.mapActive(
        transform: (Int, AI) -> AO,
    ): Lines<C, AO> = Lines(
        completed = completed,
        last = last.map { last -> transform(completed.size, last) },
    )

    private fun CompletedLineModel.Skeleton.toActive(): ActiveLineModel.Skeleton =
        ActiveLineModel.Skeleton(
            line = line,
        )

    private fun ActiveLineModel.Skeleton.toCompleted(): CompletedLineModel.Skeleton =
        CompletedLineModel.Skeleton(
            line = line,
        )

    private fun setActiveIndex(index: Int) {
        skeleton.lines.update { (lines, future) ->

            val lastAsCompleted = lines.last.fold(
                ifLeft = ::identity,
                ifRight = { active -> active.toCompleted() },
            )

            val completedLines = lines
                .completed
                .toNonEmptyListOrNull()
                .foldNullable(
                    ifNull = { nonEmptyListOf(lastAsCompleted) },
                    ifNotNull = { it + lastAsCompleted }
                )

            when {
                index < completedLines.size -> {

                    val newLines = Lines(
                        completed = completedLines.take(index),
                        last = completedLines[index].toActive().right(),
                    )

                    val newFuture = completedLines
                        .drop(index + 1)
                        .map { skeleton -> skeleton.line.text }
                        .plus(future)

                    newLines to newFuture
                }

                index < completedLines.size + future.size -> {
                    val futuresToCompleteCount = index - completedLines.size
                    val newLines = Lines(
                        completed = completedLines.plus(
                            future
                                .take(futuresToCompleteCount)
                                .map(::createCompletedSkeleton)
                        ),
                        last = createActiveSkeleton(future[futuresToCompleteCount]).right(),
                    )

                    val newFuture = future.drop(futuresToCompleteCount + 1)

                    newLines to newFuture
                }

                else -> {
                    val newCompleted = completedLines + future.map(::createCompletedSkeleton)
                    val newLast = newCompleted.last().left()
                    Lines(
                        completed = newCompleted.dropLast(1),
                        last = newLast,
                    ) to emptyList()
                }
            }
        }
    }

    private fun switchToCompletedOnly() {
        skeleton.lines.update { (lines, future) ->
            val completedOnlyLines = lines
                .copy(
                    last = lines.last
                        .fold(
                            ifLeft = ::identity,
                            ifRight = { it.toCompleted() },
                        )
                        .left()
                )
            completedOnlyLines to future
        }
    }

    private fun getGenderByIndex(
        index: Int,
    ): Gender = Gender.entries.let { entries ->
        entries[(index + skeleton.firstLineGender.ordinal) % entries.size]
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
                retry = { setActiveIndex(index) },
                gender = getGenderByIndex(index),
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
                onReady = { setActiveIndex(index + 1) },
                cancel = ::switchToCompletedOnly,
                gender = getGenderByIndex(index),
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
                lines
                    .mapCompleted { i, skeleton ->
                        createCompleted(
                            index = i,
                            parentScope = scope,
                            skeleton = skeleton,
                        )
                    }
                    .mapActive { i, skeleton ->
                        createActive(
                            parentScope = scope,
                            skeleton = skeleton,
                            index = i,
                        )
                    }
            },
            operation = { previousLines, (scope, linesWithFuture) ->

                val cache: List<Mutable<Either<Scoped<CompletedLineModel>, Scoped<ActiveLineModel>>?>> =
                    buildList {
                        addAll(previousLines.completed.map { completed -> completed.left() })
                        add(previousLines.last)
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


                val (lines) = linesWithFuture
                val result = lines
                    .mapCompleted { i, skeleton ->
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
                    .mapActive { i, skeleton ->
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
            scopedLines
                .mapActive { _, active -> active.value }
                .mapCompleted { _, completed -> completed.value }
        }

    val completed: StateFlow<Boolean> = skeleton
        .lines
        .mapState(scope) { (_, future) -> future.isEmpty() }

    companion object {

        private fun createLineSkeleton(
            text: String,
        ): LineSkeleton = LineSkeleton(
            text = text,
        )

        private fun createActiveSkeleton(
            text: String,
        ): ActiveLineModel.Skeleton = ActiveLineModel.Skeleton(
            line = createLineSkeleton(
                text = text,
            )
        )

        private fun createCompletedSkeleton(
            text: String,
        ): CompletedLineModel.Skeleton = CompletedLineModel.Skeleton(
            line = createLineSkeleton(
                text = text,
            )
        )
    }
}