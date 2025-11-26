package hnau.echospeak.model.utils.settings

interface Settings {

    operator fun get(
        key: String,
    ): Setting<String>

    interface Factory {

        suspend fun createSettings(): Settings
    }
}