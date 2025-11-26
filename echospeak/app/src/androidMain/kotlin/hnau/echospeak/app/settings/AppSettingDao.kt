package hnau.echospeak.app.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppSettingDao {

    @Query("SELECT * FROM ${AppSetting.table}")
    suspend fun getAll(): List<AppSetting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        setting: AppSetting,
    )
}