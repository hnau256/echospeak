package hnau.echospeak.model.process

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.echospeak.model.utils.VariantsKnowFactorsRepository
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class ProcessModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val variantsKnowFactorsRepository: VariantsKnowFactorsRepository
    }

    @Serializable
    /*data*/ class Skeleton(

    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}