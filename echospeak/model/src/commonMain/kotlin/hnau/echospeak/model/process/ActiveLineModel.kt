package hnau.echospeak.model.process

import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class ActiveLineModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    onReady: () -> Unit,
    cancel: () -> Unit,
) {

    @Pipe
    interface Dependencies

    @Serializable
    data class Skeleton(
        val text: String,
    )
}