package com.davidepetti.geoclient;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "activities_table")
public class CurrentActivity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int activity;

    public CurrentActivity(int activity) {
        this.activity = activity;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public int getActivity() {
        return activity;
    }
}
