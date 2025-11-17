package hnau.echospeak.projector.themes

import androidx.compose.runtime.Composable

sealed interface ThemesStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Themes(
        private val projector: ThemesProjector,
    ) : ThemesStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }
}