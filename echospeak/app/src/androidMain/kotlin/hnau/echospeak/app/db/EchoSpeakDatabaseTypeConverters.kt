package hnau.echospeak.app.db

import androidx.room.TypeConverter
import hnau.echospeak.engine.KnowFactor
import hnau.echospeak.engine.VariantId
import hnau.echospeak.model.utils.ExerciseId
import kotlin.time.Instant

class EchoSpeakDatabaseTypeConverters {

    @TypeConverter
    fun serializeExerciseId(
        from: ExerciseId,
    ): String = from.id

    @TypeConverter
    fun deserializeExerciseId(
        from: String,
    ): ExerciseId = ExerciseId(
        id = from,
    )

    @TypeConverter
    fun serializeVariantId(
        from: VariantId,
    ): String = from.id

    @TypeConverter
    fun deserializeVariantId(
        from: String,
    ): VariantId = VariantId(
        id = from,
    )

    @TypeConverter
    fun serializeKnowFactor(
        from: KnowFactor,
    ): Float = from.factor

    @TypeConverter
    fun deserializeKnowFactor(
        from: Float,
    ): KnowFactor = KnowFactor(
        factor = from,
    )

    @TypeConverter
    fun serializeInstant(
        from: Instant,
    ): Long = from.epochSeconds

    @TypeConverter
    fun deserializeInstant(
        from: Long,
    ): Instant = Instant.Companion.fromEpochSeconds(
        epochSeconds = from,
    )
}