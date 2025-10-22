package hnau.echospeak.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hnau.echospeak.model.utils.ExerciseId

@Dao
interface ExercisesVariantsKnowFactorsDao {

    @Query(
        """
        SELECT * 
        FROM ${ExercisesVariantsKnowFactors.table} 
        WHERE ${ExercisesVariantsKnowFactors.columnExercise} = :exerciseId
        """
    )
    suspend fun getExerciseVariants(
        exerciseId: ExerciseId,
    ): List<ExercisesVariantsKnowFactors>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(
        level: ExercisesVariantsKnowFactors,
    )
}