package hnau.echospeak.app.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = AppSetting.table,
)
data class AppSetting(
    @PrimaryKey
    @ColumnInfo(name = columnKey)
    val key: String,

    @ColumnInfo(name = columnValue)
    val value: String,
) {


    companion object {

        const val table = "settings"

        const val columnKey = "key"
        const val columnValue = "value"
    }
}