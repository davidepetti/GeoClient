package com.davidepetti.geoclient;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "foregroundApps_table")
public class ForegroundApp {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String packageName;

    public ForegroundApp(String packageName) {
        this.packageName = packageName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }
}
