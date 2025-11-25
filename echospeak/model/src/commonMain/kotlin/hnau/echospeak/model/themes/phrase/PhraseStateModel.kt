package hnau.echospeak.model.themes.phrase

import hnau.echospeak.model.utils.compare.SatisfactorableSimilarity
import hnau.echospeak.model.utils.compare.Similarity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface PhraseStateModel {

    val key: Int

    data class WaitingForRecognizing(
        val recognize: () -> Unit,
    ) : PhraseStateModel {

        override val key: Int
            get() = 0
    }

    data class Recognizing(
        val model: RecognizeModel,
    ) : PhraseStateModel {

        override val key: Int
            get() = 1
    }

    data class Recognized(
        val similarity: SatisfactorableSimilarity,
        val retry: () -> Unit,
        val complete: StateFlow<(() -> Unit)?>?,
    ) : PhraseStateModel {

        override val key: Int
            get() = 2
    }

    @Serializable
    sealed interface Skeleton {

        val key: Int

        @Serializable
        @SerialName("waiting")
        data object WaitingForRecognizing : Skeleton {

            override val key: Int
                get() = 0
        }

        @Serializable
        @SerialName("recognizing")
        data object Recognizing : Skeleton {

            override val key: Int
                get() = 1
        }

        @Serializable
        @SerialName("ready")
        data class Recognized(
            val result: RecognitionResult,
        ) : Skeleton {

            override val key: Int
                get() = 2
        }

    }
}