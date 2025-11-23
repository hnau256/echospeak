@file:UseSerializers(
    NonEmptySetSerializer::class,
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.themes

import arrow.core.serialization.NonEmptySetSerializer
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.model.themes.dto.ThemeId
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.UseSerializers

data class ChooseThemesModel(
    val themes: List<Theme>,
    val launch: StateFlow<(() -> Unit)?>,
    val selectAll: StateFlow<(() -> Unit)?>,
    val selectNone: StateFlow<(() -> Unit)?>,
) {

    data class Theme(
        val id: ThemeId,
        val isSelected: StateFlow<Boolean>,
        val switchIsSelected: () -> Unit,
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}