package hnau.echospeak.model.process.lines

import hnau.echospeak.model.process.dto.Gender
import hnau.echospeak.model.utils.Speaker
import hnau.echospeak.model.utils.SpeechRecognizer
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class ActiveLineModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val gender: Gender,
    onReady: () -> Unit,
    cancel: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val speaker: Speaker

        val recognizer: SpeechRecognizer
    }

    init {
        scope.launch {
            dependencies
                .speaker
                .speak(
                    gender = gender,
                    text = skeleton.text.replace(';', '?'),
                )
            onReady()
        }
    }

    @Serializable
    data class Skeleton(
        val text: String,
    )

    val text: String
        get() = skeleton.text
}