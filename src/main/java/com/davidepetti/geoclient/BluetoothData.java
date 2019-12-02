package com.davidepetti.geoclient;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bluetoothdata_table")
public class BluetoothData {

    @PrimaryKey(autoGenerate = true)
    private long rowId;

    private long id;

    private String MACAddress;

    public BluetoothData(long id, String MACAddress) {
        this.id = id;
        this.MACAddress = MACAddress;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public long getRowId() {
        return rowId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getMACAddress() {
        return MACAddress;
    }
}
