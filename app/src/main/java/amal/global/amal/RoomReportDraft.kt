package amal.global.amal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_draft_table")
data class RoomReportDraft(
        @PrimaryKey (autoGenerate = true) val id: Int,
        @ColumnInfo(name="title") val title:String)
)
