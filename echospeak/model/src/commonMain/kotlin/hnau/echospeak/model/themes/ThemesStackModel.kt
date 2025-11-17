@file:UseSerializers(
    MutableStateFlowSerializer::class
)

package hnau.echospeak.model.themes

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.SkeletonWithModel
import hnau.common.app.model.stack.goBackHandler
import hnau.common.app.model.stack.modelsOnly
import hnau.common.app.model.stack.withModels
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ThemesStackModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun themes(
            themesOpener: ThemesOpener,
        ): ThemesModel.Dependencies

    }

    @Serializable
    data class Skeleton(
        val stack: MutableStateFlow<NonEmptyStack<ThemesStackElementModel.Skeleton>> = NonEmptyStack(
            ThemesStackElementModel.Skeleton.Themes(
                skeleton = ThemesModel.Skeleton(),
            )
        ).toMutableStateFlowAsInitial(),
    )

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<ThemesStackElementModel.Skeleton, ThemesStackElementModel>>> = skeleton
        .stack
        .withModels(
            scope = scope,
            getKey = ThemesStackElementModel.Skeleton::key,
            createModel = ::createModel,
        )

    private fun createModel(
        scope: CoroutineScope,
        skeleton: ThemesStackElementModel.Skeleton,
    ): ThemesStackElementModel = when (skeleton) {
        is ThemesStackElementModel.Skeleton.Themes -> ThemesStackElementModel.Themes(
            ThemesModel(
                scope = scope,
                skeleton = skeleton.skeleton,
                dependencies = dependencies.themes(
                    themesOpener = ThemesOpener {
                        TODO()
                    }
                ),
            )
        )
    }

    val stack: StateFlow<NonEmptyStack<ThemesStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = ThemesStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}

