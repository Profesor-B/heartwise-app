package com.example.heartwise;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "activity_log")
public class ActivityEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;
    public String activityDescription;
    public int heartRate;

    public ActivityEntity(String date, String activityDescription, int heartRate) {
        this.date = date;
        this.activityDescription = activityDescription;
        this.heartRate = heartRate;
    }
}