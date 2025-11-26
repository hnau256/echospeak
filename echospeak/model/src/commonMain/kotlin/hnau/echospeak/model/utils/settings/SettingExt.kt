package hnau.echospeak.model.utils.settings

import arrow.core.Option
import hnau.common.kotlin.mapper.Mapper
import kotlin.reflect.KProperty

operator fun <T> Setting<T>.getValue(
    thisRef: Any?,
    property: KProperty<*>,
): Option<T> = state.value

val <T> Setting<T>.value: Option<T>
    get() = state.value

fun <I, O> Setting<I>.map(
    mapper: Mapper<I, O>,
): Setting<O> = SettingImpl(
    initialValue = value.map(mapper.direct),
    update = { newValue ->
        val transformedNewValue = mapper.reverse(newValue)
        update(transformedNewValue)
    }
)