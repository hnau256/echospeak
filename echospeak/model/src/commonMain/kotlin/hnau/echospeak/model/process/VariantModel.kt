@file:UseSerializers(
    MutableStateFlowSerializer::class,
    NonEmptyListSerializer::class,
    EitherSerializer::class,
)

package hnau.echospeak.model.process

import arrow.core.serialization.EitherSerializer
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifTrue
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.engine.ChosenVariant
import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.model.process.dto.Dialog
import hnau.echospeak.model.process.dto.Gender
import hnau.echospeak.model.process.lines.ActiveLineModel
import hnau.echospeak.model.process.lines.CompletedLineModel
import hnau.echospeak.model.process.lines.LinesModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class VariantModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val complete: suspend (newKnowFactor: KnowFactor) -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun lines(): LinesModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val title: String,
        val learnInfo: ChosenVariant.LearnInfo?,
        val lines: LinesModel.Skeleton,
    ) {

        constructor(
            dialog: Dialog,
            learnInfo: ChosenVariant.LearnInfo?,
        ) : this(
            title = dialog.title,
            learnInfo = learnInfo,
            lines = LinesModel.Skeleton(
                lines = dialog.lines,
                firstLineGender = dialog.firstLineGender,
            ),
        )
    }

    private val linesModel = LinesModel(
        scope = scope,
        dependencies = dependencies.lines(),
        skeleton = skeleton.lines,
    )

    val lines: StateFlow<LinesModel.Lines<CompletedLineModel, ActiveLineModel>>
        get() = linesModel.lines

    val completeIfAvailable: StateFlow<StateFlow<(() -> Unit)?>?> = linesModel
        .completed
        .mapWithScope(scope) { scope, completed ->
            completed.ifTrue {
                actionOrNullIfExecuting(scope) {
                    complete(
                        skeleton.learnInfo?.info?.knowFactor.foldNullable(
                            ifNull = { KnowFactor.initial },
                            ifNotNull = { current -> KnowFactor(current.factor * 1.7f) },
                        )
                    )
                }
            }
        }
}