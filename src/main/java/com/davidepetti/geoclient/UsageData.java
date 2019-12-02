package com.davidepetti.geoclient;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usagedata_table")
public class UsageData {

    @PrimaryKey(autoGenerate = true)
    private long rowId;

    private long id;

    private String packageName;

    private long dailyForegroundTime;

    public UsageData(long id, String packageName, long dailyForegroundTime) {
        this.id = id;
        this.packageName = packageName;
        this.dailyForegroundTime = dailyForegroundTime;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public long getId() {
        return id;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getDailyForegroundTime() {
        return dailyForegroundTime;
    }
}
