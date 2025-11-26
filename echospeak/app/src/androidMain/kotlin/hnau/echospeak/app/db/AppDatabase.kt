package hnau.echospeak.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hnau.echospeak.app.db.knowfactors.ExercisesVariantsKnowFactors
import hnau.echospeak.app.db.knowfactors.ExercisesVariantsKnowFactorsDao
import hnau.echospeak.app.settings.AppSetting
import hnau.echospeak.app.settings.AppSettingDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        ExercisesVariantsKnowFactors::class,
        AppSetting::class,
    ],
    version = 1,
)
@TypeConverters(EchoSpeakDatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val exercisesVariantsKnowFactorsDao: ExercisesVariantsKnowFactorsDao

    abstract val appSettingDao: AppSettingDao

    companion object {

        suspend fun create(
            context: Context,
        ): AppDatabase = withContext(Dispatchers.IO) {
            Room
                .databaseBuilder(
                    context = context,
                    klass = AppDatabase::class.java,
                    name = "echospeak"
                )
                .build()
        }
    }
}