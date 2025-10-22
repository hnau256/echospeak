package hnau.echospeak.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.engine.VariantId
import hnau.echospeak.model.utils.ExerciseId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
@Entity(
    tableName = ExercisesVariantsKnowFactors.table,
    primaryKeys = [
        ExercisesVariantsKnowFactors.columnExercise,
        ExercisesVariantsKnowFactors.columnVariant,
    ]
)
data class ExercisesVariantsKnowFactors(

    @ColumnInfo(name = columnExercise)
    val exercise: ExerciseId,

    @ColumnInfo(name = columnVariant)
    val variant: VariantId,

    @ColumnInfo(name = columnLastAnswerTimestamp)
    val lastAnswerTimestamp: Instant,

    @ColumnInfo(name = columnKnowFactor)
    val knowFactor: KnowFactor,
) {

    companion object {

        const val table = "exercises_variants_know_factors"

        const val columnExercise = "exercise"

        const val columnVariant = "variant"

        const val columnLastAnswerTimestamp = "last_answer_timestamp"

        const val columnKnowFactor = "know_factor"
    }
}

