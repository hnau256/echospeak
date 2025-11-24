package hnau.echospeak.model

import hnau.common.app.model.goback.GoBackHandler
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class RootModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun prepare(): PrepareModel.Dependencies

        companion object
    }

    @Serializable
    data class Skeleton(
        val prepare: PrepareModel.Skeleton = PrepareModel.Skeleton(),
    )

    val prepare = PrepareModel(
        scope = scope,
        dependencies = dependencies.prepare(),
        skeleton = skeleton.prepare,
    )

    val goBackHandler: GoBackHandler
        get() = this@RootModel.prepare.goBackHandler
}