package hnau.echospeak.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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