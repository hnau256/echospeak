package hnau.echospeak.app.knowfactors

import android.content.Context
import hnau.common.kotlin.lazy.AsyncLazy
import hnau.echospeak.app.db.AppDatabase
import hnau.echospeak.app.db.knowfactors.ExercisesVariantsKnowFactorsDao
import hnau.echospeak.model.utils.ExerciseId
import hnau.echospeak.model.utils.VariantsKnowFactorsProvider

class VariantsKnowFactorsRepositoryFactoryRoomImpl(
    context: Context,
) : VariantsKnowFactorsProvider.Factory {

    private val dao: AsyncLazy<ExercisesVariantsKnowFactorsDao> = AsyncLazy {
        AppDatabase
            .create(context)
            .exercisesVariantsKnowFactorsDao()
    }

    override fun create(
        exerciseId: ExerciseId,
    ): VariantsKnowFactorsProvider = VariantsKnowFactorsProviderRoomImpl(
        exerciseId = exerciseId,
        getDao = { dao.get() },
    )
}