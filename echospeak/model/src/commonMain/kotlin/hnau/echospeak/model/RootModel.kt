package hnau.echospeak.model

import hnau.common.app.model.goback.GoBackHandler
import hnau.echospeak.model.process.ProcessModel
import hnau.echospeak.model.themes.ThemesStackModel
import hnau.echospeak.model.utils.ExerciseId
import hnau.echospeak.model.utils.VariantsKnowFactorsRepository
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

        val variantsKnowFactorsRepositoryFactory: VariantsKnowFactorsRepository.Factory

        fun process(
            variantsKnowFactorsRepository: VariantsKnowFactorsRepository,
        ): ProcessModel.Dependencies

        fun themes(): ThemesStackModel.Dependencies

        companion object
    }

    @Serializable
    data class Skeleton(
        val process: ProcessModel.Skeleton = ProcessModel.Skeleton(),
        val themes: ThemesStackModel.Skeleton = ThemesStackModel.Skeleton(),
    )

    val process = ProcessModel(
        scope = scope,
        dependencies = dependencies.process(
            variantsKnowFactorsRepository = dependencies.variantsKnowFactorsRepositoryFactory.create(
                exerciseId = ExerciseId("echo_speak_dialogs"),
            )
        ),
        skeleton = skeleton.process,
    )

    val themes = ThemesStackModel(
        scope = scope,
        dependencies = dependencies.themes(),
        skeleton = skeleton.themes,
    )

    val goBackHandler: GoBackHandler
        get() = this@RootModel.themes.goBackHandler
}