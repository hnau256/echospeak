package hnau.echospeak.model.themes

import hnau.common.app.model.goback.GoBackHandler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ThemesStackElementModel {

    val key: Int

    val goBackHandler: GoBackHandler

    data class Themes(
        val model: ThemesModel,
    ) : ThemesStackElementModel {

        override val key: Int
            get() = 0
        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    @Serializable
    sealed interface Skeleton {

        val key: Int

        @Serializable
        @SerialName("themes")
        data class Themes(
            val skeleton: ThemesModel.Skeleton,
        ) : Skeleton {

            override val key: Int
                get() = 0
        }
    }
}