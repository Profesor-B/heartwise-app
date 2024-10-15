package com.example.heartwise;

// ActivityDao.java
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ActivityDao {
    @Insert
    void insertActivity(ActivityEntity activityEntity);

    @Query("SELECT * FROM activity_log ORDER BY date DESC")
    List<ActivityEntity> getAllActivities();

    @Query("SELECT * FROM activity_log WHERE date = :date")
    List<ActivityEntity> getActivitiesByDate(String date);
}
