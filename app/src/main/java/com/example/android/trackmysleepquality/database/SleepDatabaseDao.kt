package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.*

/**this is Data Access Object interface where database operations are done*/
@Dao
interface SleepDatabaseDao{
    @Insert
    suspend fun insert(night: SleepNight)

    @Update
    suspend fun update(night: SleepNight)

    @Query("SELECT * FROM daily_sleep_quality_table WHERE nightId = :key")
    suspend fun get(key:Long):SleepNight?

    @Query("DELETE FROM daily_sleep_quality_table")
    suspend fun clear()

    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    suspend fun getTonight():SleepNight?

    /**
     * Room already uses a background thread for that specific @Query which returns LiveData
       so there's no need to add suspend keyword.
     */
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights():LiveData<List<SleepNight>>

    /**
     * Selects and returns the night with given night id
     */
    @Query("Select * from daily_sleep_quality_table WHERE nightId = :key")
    fun getNightWithId(key: Long) :LiveData<SleepNight>
}
