@file:UseSerializers(
    NonEmptySetSerializer::class,
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.themes

import arrow.core.serialization.NonEmptySetSerializer
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.LoadableStateFlow
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.fold
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.map
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.echospeak.model.themes.dto.ThemesProvider
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ThemesModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun list(): ThemesListModel.Dependencies

        val themesProvider: ThemesProvider
    }

    @Serializable
    data class Skeleton(
        var themes: ThemesListModel.Skeleton? = null
    )

    val list: StateFlow<Loadable<ThemesListModel>> = LoadableStateFlow(
        scope = scope,
    ) {
        dependencies
            .themesProvider
            .loadThemes()
    }
        .mapWithScope(scope) { scope, themesOrLoading ->
            themesOrLoading.map { themes ->
                ThemesListModel(
                    scope = scope,
                    themes = themes,
                    dependencies = dependencies.list(),
                    skeleton = skeleton::themes.toAccessor().getOrInit { ThemesListModel.Skeleton() }
                )
            }
        }


    val goBackHandler: GoBackHandler = list.flatMapState(scope) { themesOrLoading ->
        themesOrLoading.fold(
            ifLoading = { NeverGoBackHandler },
            ifReady = ThemesListModel::goBackHandler,
        )
    }
}