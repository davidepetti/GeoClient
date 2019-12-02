package com.davidepetti.geoclient;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "mainrecord_table")
public class MainRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long timestamp;

    private String uid;

    private long idGeo;

    private long idUsage;

    private long idBluetooth;

    private long idActivity;

    private long idForeground;

    public MainRecord(long timestamp, String uid, long idGeo, long idUsage, long idBluetooth, long idActivity, long idForeground) {
        this.timestamp = timestamp;
        this.uid = uid;
        this.idGeo = idGeo;
        this.idUsage = idUsage;
        this.idBluetooth = idBluetooth;
        this.idActivity = idActivity;
        this.idForeground = idForeground;
    }

    @Ignore
    public MainRecord(long id, long timestamp, String uid, long idGeo, long idUsage, long idBluetooth, long idActivity, long idForeground) {
        this.id = id;
        this.timestamp = timestamp;
        this.uid = uid;
        this.idGeo = idGeo;
        this.idUsage = idUsage;
        this.idBluetooth = idBluetooth;
        this.idActivity = idActivity;
        this.idForeground = idForeground;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    public long getIdGeo() {
        return idGeo;
    }

    public long getIdUsage() {
        return idUsage;
    }

    public long getIdBluetooth() {
        return idBluetooth;
    }

    public long getIdActivity() {
        return idActivity;
    }

    public long getIdForeground() {
        return idForeground;
    }
}
