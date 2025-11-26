package hnau.echospeak.model.utils.settings

import arrow.core.Option
import kotlinx.coroutines.flow.StateFlow

interface Setting<T> {

    suspend fun update(
        newValue: T,
    )

    val state: StateFlow<Option<T>>
}