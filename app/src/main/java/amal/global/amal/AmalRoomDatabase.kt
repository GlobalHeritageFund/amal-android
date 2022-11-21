package amal.global.amal

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.CoroutineScope

@Database(entities = [ReportDraft::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
public abstract class AmalRoomDatabase : RoomDatabase() {

    abstract fun amalRoomDatabaseDao(): AmalRoomDatabaseDao
//  or  abstract var amalRoomDatabaseDao: AmalRoomDatabaseDao  depending on version

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AmalRoomDatabase? = null

        fun getDatabase(
            context: Context
//            scope: CoroutineScope
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