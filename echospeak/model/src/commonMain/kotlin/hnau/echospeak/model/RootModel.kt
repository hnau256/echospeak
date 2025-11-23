package hnau.echospeak.model

import hnau.common.app.model.goback.GoBackHandler
import hnau.echospeak.model.process.ProcessModel
import hnau.echospeak.model.themes.LoadThemesModel
import hnau.echospeak.model.utils.VariantsKnowFactorsProvider
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

        val variantsKnowFactorsProviderFactory: VariantsKnowFactorsProvider.Factory

        /*fun process(
            variantsKnowFactorsRepository: VariantsKnowFactorsRepository,
        ): ProcessModel.Dependencies*/

        fun themes(): LoadThemesModel.Dependencies

        companion object
    }

    @Serializable
    data class Skeleton(
        val process: ProcessModel.Skeleton = ProcessModel.Skeleton(),
        val themes: LoadThemesModel.Skeleton = LoadThemesModel.Skeleton(),
    )

    val themes = LoadThemesModel(
        scope = scope,
        dependencies = dependencies.themes(),
        skeleton = skeleton.themes,
    )

    val goBackHandler: GoBackHandler
        get() = this@RootModel.themes.goBackHandler
}