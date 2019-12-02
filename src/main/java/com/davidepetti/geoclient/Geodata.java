package com.davidepetti.geoclient;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "geodata_table")
public class Geodata {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private double latitude;

    private double longitude;

    public Geodata(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
