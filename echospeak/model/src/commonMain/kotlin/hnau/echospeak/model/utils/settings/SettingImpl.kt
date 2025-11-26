package hnau.echospeak.model.utils.settings

import arrow.core.Option
import arrow.core.Some
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingImpl<T>(
    initialValue: Option<T>,
    private val update: suspend (newValue: T) -> Unit,
) : Setting<T> {

    private val _state: MutableStateFlow<Option<T>> =
        MutableStateFlow(initialValue)

    override val state: StateFlow<Option<T>>
        get() = _state

    override suspend fun update(newValue: T) {
        _state.value = Some(newValue)
        update.invoke(newValue)
    }
}