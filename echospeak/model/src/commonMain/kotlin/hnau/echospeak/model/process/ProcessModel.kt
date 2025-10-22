@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.echospeak.model.process

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.echospeak.engine.VariantId
import hnau.echospeak.engine.chooseVariant
import hnau.echospeak.model.process.dto.DialogsProvider
import hnau.echospeak.model.utils.VariantsKnowFactorsRepository
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class ProcessModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val variantsKnowFactorsRepository: VariantsKnowFactorsRepository

        val dialogsProvider: DialogsProvider
    }

    init {
        scope.launch {
            dependencies
                .dialogsProvider
                .loadDialogs()
        }
    }

    private suspend fun switchVariant() {

    }

    init {
        scope.launch {
            switchVariant()
        }
    }

    @Serializable
    data class Skeleton(
        val variant: MutableStateFlow<Pair<VariantId, VariantModel.Skeleton>?> =
            null.toMutableStateFlowAsInitial(),
    )
}