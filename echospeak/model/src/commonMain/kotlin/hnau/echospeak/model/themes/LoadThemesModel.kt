package hnau.echospeak.model.themes

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.fold
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.map
import hnau.common.kotlin.toAccessor
import hnau.echospeak.engine.VariantsKnowFactorsStorage
import hnau.echospeak.model.themes.dto.ThemesProvider
import hnau.echospeak.model.utils.VariantsKnowFactorsProvider
import hnau.echospeak.model.utils.toVariantsKnowFactorsStorage
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class LoadThemesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val variantsProvider: VariantsKnowFactorsProvider

        val themesProvider: ThemesProvider

        fun themes(
           storage: VariantsKnowFactorsStorage,
        ): ChooseOrProcessThemesModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var themes: ChooseOrProcessThemesModel.Skeleton? = null,
    )

    val chooseOrProcess: StateFlow<Loadable<ChooseOrProcessThemesModel>> = LoadableStateFlow(
        scope = scope,
    ) {
        coroutineScope {
            val storageDeferred = async {
                dependencies
                    .variantsProvider
                    .toVariantsKnowFactorsStorage()
            }
            val themesDeferred = async {
                dependencies
                    .themesProvider
                    .loadThemes()
            }
            val storage = storageDeferred.await()
            val themes = themesDeferred.await()
            storage to themes
        }
    }
        .mapWithScope(scope) { scope, storageWithThemesOrLoading ->
            storageWithThemesOrLoading.map { (storage, themes) ->
                ChooseOrProcessThemesModel(
                    scope = scope,
                    dependencies = dependencies.themes(
                        storage = storage,
                    ),
                    skeleton = skeleton::themes
                        .toAccessor()
                        .getOrInit { ChooseOrProcessThemesModel.Skeleton() },
                    themes = themes,
                )
            }
        }

    val goBackHandler: GoBackHandler = chooseOrProcess.flatMapState(scope) { themesStackOrLoading ->
        themesStackOrLoading.fold(
            ifLoading = { NeverGoBackHandler },
            ifReady = ChooseOrProcessThemesModel::goBackHandler,
        )
    }
}