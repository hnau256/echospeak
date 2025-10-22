package hnau.echospeak.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hnau.echospeak.app.db.knowfactors.ExercisesVariantsKnowFactors
import hnau.echospeak.app.db.knowfactors.ExercisesVariantsKnowFactorsDao

@Database(
    entities = [
        ExercisesVariantsKnowFactors::class,
    ],
    version = 1,
)
@TypeConverters(EchoSpeakDatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exercisesVariantsKnowFactorsDao(): ExercisesVariantsKnowFactorsDao

    companion object {

        fun create(
            context: Context,
        ): AppDatabase = Room
            .databaseBuilder(
                context = context,
                klass = AppDatabase::class.java,
                name = "echospeak"
            )
            .build()
    }
}