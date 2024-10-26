package com.example.heartwise

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class BPMResult (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "result") val result: String,
    @ColumnInfo(name = "timestamp") val timestamp: String,
    @ColumnInfo(name = "systolic") val systolic: Int,
    @ColumnInfo(name = "diastolic") val diastolic: Int
)

@Dao
interface BPMResultDao {
    @Query("SELECT * FROM BPMResult")
    fun getAll(): List<BPMResult>

    @Insert
    fun insertData(bpmResult: BPMResult)
}

@Entity
data class ActivityEntity (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "activity_info") var activityInfo:String,
    @ColumnInfo(name = "heart_rate") var heartRate:String,
)

@Dao
interface ActivityDao {
    @Insert
    fun insertActivity(activityEntity: ActivityEntity)

    @Query("SELECT * FROM ActivityEntity ORDER BY date DESC")
    fun getAllActivities(): List<ActivityEntity?>?

    @Query("SELECT * FROM ActivityEntity WHERE date = :date")
    fun getActivitiesByDate(date: String?): List<ActivityEntity?>?
}

@Database(entities = [BPMResult::class,ActivityEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bpmDao(): BPMResultDao
    abstract fun activityDao(): ActivityDao
}