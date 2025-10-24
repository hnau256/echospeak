@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.process.lines

import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.model.process.dto.Gender
import hnau.echospeak.model.utils.Speaker
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ActiveLineModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    val gender: Gender,
    onReady: () -> Unit,
    val cancel: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val speaker: Speaker

        fun recognize(): RecognizeModel.Dependencies
    }

    private val recognizeModelVersion: MutableStateFlow<Int> = MutableStateFlow(0)

    val recognize: StateFlow<RecognizeModel?> = skeleton
        .alreadySpoken
        .flatMapWithScope(scope) { scope, alreadySpoken ->
            alreadySpoken.foldBoolean(
                ifFalse = { null.toMutableStateFlowAsInitial() },
                ifTrue = {
                    recognizeModelVersion
                        .mapWithScope(scope) { scope, _ ->
                            RecognizeModel(
                                scope = scope,
                                dependencies = dependencies.recognize(),
                                retry = { recognizeModelVersion.update { it + 1 } },
                                onReady = onReady,
                            )
                        }
                }
            )
        }

    init {
        scope.launch {
            skeleton
                .alreadySpoken
                .collectLatest { alreadySpoken ->
                    if (alreadySpoken) {
                        return@collectLatest
                    }
                    dependencies
                        .speaker
                        .speak(
                            gender = gender,
                            text = skeleton.text,
                        )
                    skeleton.alreadySpoken.value = true
                }
        }
    }

    @Serializable
    data class Skeleton(
        val text: String,
        val alreadySpoken: MutableStateFlow<Boolean> = false.toMutableStateFlowAsInitial(),
    )

    val text: String
        get() = skeleton.text
}