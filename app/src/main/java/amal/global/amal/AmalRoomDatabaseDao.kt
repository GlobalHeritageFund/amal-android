package amal.global.amal

import androidx.room.*

@Dao
interface AmalRoomDatabaseDao {
    //for now not making these suspend functions, but if do work manager will
    @Query("SELECT * FROM report_draft_table ORDER BY creationDate ASC")
    fun getReports(): ReportDraft

    @Insert(onConflict = OnConflictStrategy.REPLACE) //could alternatively choose to IGNORE
    fun insert(draftReport: ReportDraft)

    @Delete
    fun delete(reportDraft: ReportDraft)
}

