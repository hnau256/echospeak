@file:UseSerializers(
    NonEmptySetSerializer::class,
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.themes

import arrow.core.serialization.NonEmptySetSerializer
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.model.themes.dto.ThemeId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers

class ChooseThemesModel(
    val scope: CoroutineScope,
    val themes: List<Theme>,
    launch: StateFlow<(suspend () -> Unit)?>,
    val selectAll: StateFlow<(() -> Unit)?>,
    val selectNone: StateFlow<(() -> Unit)?>,
) {

    data class Theme(
        val id: ThemeId,
        val isSelected: StateFlow<Boolean>,
        val switchIsSelected: () -> Unit,
    )

    val launch: StateFlow<StateFlow<(() -> Unit)?>?> = launch.mapWithScope(scope) { scope, launchOrNull ->
        launchOrNull?.let { launch ->
            actionOrNullIfExecuting(scope, launch)
        }
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}