package com.example.paths

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
import androidx.room.Delete

@Entity(tableName = "stopwatch_records")
data class LocalStopwatchRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val routeFirebaseId: String,
    val userId: String, // Dodano pole użytkownika
    val timeElapsed: Long,
    val timestamp: Long
)

@Dao
interface StopwatchDao {
    @Insert
    suspend fun insertRecord(record: LocalStopwatchRecord)

    @Delete
    suspend fun deleteRecord(record: LocalStopwatchRecord)

    @Query("SELECT * FROM stopwatch_records WHERE routeFirebaseId = :routeId AND userId = :userId ORDER BY timeElapsed ASC LIMIT 5")
    suspend fun getTop5RecordsForUser(routeId: String, userId: String): List<LocalStopwatchRecord>

    @Query("SELECT * FROM stopwatch_records WHERE routeFirebaseId = :routeId AND userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRecordForUser(routeId: String, userId: String): LocalStopwatchRecord?

    @Query("SELECT * FROM stopwatch_records WHERE routeFirebaseId = :routeId ORDER BY timestamp DESC")
    fun getRecordsForRoute(routeId: String): kotlinx.coroutines.flow.Flow<List<LocalStopwatchRecord>>
}

@Database(entities = [LocalStopwatchRecord::class], version = 2) // Podbito wersję bazy
abstract class AppDatabase : RoomDatabase() {
    abstract fun stopwatchDao(): StopwatchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paths_database"
                )
                .fallbackToDestructiveMigration() // Prosta migracja przy zmianie pól
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
