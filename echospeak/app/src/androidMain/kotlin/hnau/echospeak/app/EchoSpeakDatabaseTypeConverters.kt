package hnau.echospeak.app

import androidx.room.TypeConverter
import hnau.echospeak.engine.RememberFactor
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
    fun serializeRememberFactor(
        from: RememberFactor,
    ): Float = from.factor

    @TypeConverter
    fun deserializeRememberFactor(
        from: Float,
    ): RememberFactor = RememberFactor(
        factor = from,
    )

    @TypeConverter
    fun serializeInstant(
        from: Instant,
    ): Long = from.epochSeconds

    @TypeConverter
    fun deserializeInstant(
        from: Long,
    ): Instant = Instant.fromEpochSeconds(
        epochSeconds = from,
    )
}