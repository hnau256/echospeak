package hnau.echospeak.model.themes

sealed interface ChooseOrProcessStateModel {

    val key: Int

    data class Choose(
        val model: ChooseThemesModel,
    ) : ChooseOrProcessStateModel {

        override val key: Int
            get() = 0
    }

    data class Process(
        val model: ProcessThemesModel,
    ) : ChooseOrProcessStateModel {

        override val key: Int
            get() = 1
    }
}