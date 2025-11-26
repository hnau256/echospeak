package hnau.echospeak.app.settings

import arrow.core.toOption
import hnau.common.kotlin.lazy.AsyncLazy
import hnau.echospeak.app.db.AppDatabase
import hnau.echospeak.model.utils.settings.Setting
import hnau.echospeak.model.utils.settings.SettingImpl
import hnau.echospeak.model.utils.settings.Settings

class AndroidDatabaseSettings(
    private val get: (key: String) -> Setting<String>,
) : Settings {

    override fun get(
        key: String,
    ): Setting<String> = get.invoke(key)

    data class Factory(
        private val getAppDatabase: suspend () -> AppDatabase,
    ) : Settings.Factory {

        private val settings = AsyncLazy<Settings> {

            val dao = getAppDatabase().appSettingDao

            val initialValues = dao
                .getAll()
                .associate { setting -> setting.key to setting.value }
                .toMutableMap()

            val cache: MutableMap<String, Setting<String>> = mutableMapOf()

            AndroidDatabaseSettings { key ->
                synchronized(cache) {
                    cache.getOrPut(key) {
                        val initial = synchronized(initialValues) {
                            initialValues.remove(key)
                        }
                        SettingImpl(
                            initialValue = initial.toOption(),
                            update = { newValue ->
                                dao.insert(
                                    AppSetting(
                                        key = key,
                                        value = newValue,
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        override suspend fun createSettings(): Settings =
            settings.get()
    }
}