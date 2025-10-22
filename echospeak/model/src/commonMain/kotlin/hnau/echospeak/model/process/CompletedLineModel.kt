package hnau.echospeak.model.process

import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class CompletedLineModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    retry: () -> Unit,
) {

    @Pipe
    interface Dependencies

    @Serializable
    data class Skeleton(
        val text: String,
    )
}