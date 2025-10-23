package hnau.echospeak.model.process.lines

import hnau.echospeak.model.process.dto.Gender
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class CompletedLineModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    gender: Gender,
    retry: () -> Unit,
) {

    @Pipe
    interface Dependencies

    @Serializable
    data class Skeleton(
        val text: String,
    )
}