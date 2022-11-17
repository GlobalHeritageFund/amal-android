package amal.global.amal

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = arrayOf(Report::class), version = 1, exportSchema = false)
public abstract class AmalRoomDatabase : RoomDatabase() {

    abstract fun reportDraftDao(): ReportDraftDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AmalRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AmalRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AmalRoomDatabase::class.java,
                        "amal_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}