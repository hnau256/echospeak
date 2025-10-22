package hnau.echospeak.model.process

import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class VariantModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {


    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton()
}