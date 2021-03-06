package com.toedro.fao.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;

@Entity(tableName = "Calories")
public class Calories {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    Integer id;
    Double value;
    String day;
    String timestamp;

    public Calories(Double value, String day, String timestamp) {
        this.value = value;
        this.day = day;
        this.timestamp = timestamp;
    }

    public Integer getId() {
        return id;
    }
    public Double getValue() {
        return value;
    }
    public String getDay() {
        return day;
    }
    public String getTimestamp() {return timestamp; }
}
