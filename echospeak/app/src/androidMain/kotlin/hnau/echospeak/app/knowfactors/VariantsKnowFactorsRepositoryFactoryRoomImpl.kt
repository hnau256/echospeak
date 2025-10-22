package hnau.echospeak.app.knowfactors

import android.content.Context
import hnau.common.kotlin.lazy.AsyncLazy
import hnau.echospeak.app.db.AppDatabase
import hnau.echospeak.app.db.knowfactors.ExercisesVariantsKnowFactorsDao
import hnau.echospeak.model.utils.ExerciseId
import hnau.echospeak.model.utils.VariantsKnowFactorsRepository

class VariantsKnowFactorsRepositoryFactoryRoomImpl(
    context: Context,
) : VariantsKnowFactorsRepository.Factory {

    private val dao: AsyncLazy<ExercisesVariantsKnowFactorsDao> = AsyncLazy {
        AppDatabase
            .create(context)
            .exercisesVariantsKnowFactorsDao()
    }

    override fun create(
        exerciseId: ExerciseId,
    ): VariantsKnowFactorsRepository = VariantsKnowFactorsRepositoryRoomImpl(
        exerciseId = exerciseId,
        getDao = { dao.get() },
    )
}