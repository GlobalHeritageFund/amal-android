package amal.global.amal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReportDraftDao {
    @Query("SELECT * FROM report_draft_table ORDER BY title ASC")
    fun getReport(): Report

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(report: RoomReportDraft)

    @Query("DELETE FROM report_draft_table")
    suspend fun deleteAll()
}