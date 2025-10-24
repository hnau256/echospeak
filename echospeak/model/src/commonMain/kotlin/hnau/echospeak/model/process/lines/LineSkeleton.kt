package hnau.echospeak.model.process.lines

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class LineSkeleton(
    val text: String,
    val translation: MutableStateFlow<String?> = null.toMutableStateFlowAsInitial(),
)