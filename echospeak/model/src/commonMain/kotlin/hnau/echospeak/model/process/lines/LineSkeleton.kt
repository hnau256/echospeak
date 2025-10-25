@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.process.lines

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class LineSkeleton(
    val text: String,
    val translation: MutableStateFlow<String?> = null.toMutableStateFlowAsInitial(),
)