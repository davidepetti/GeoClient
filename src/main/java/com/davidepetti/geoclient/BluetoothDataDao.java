package com.davidepetti.geoclient;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface BluetoothDataDao {

    @Insert
    void insert(BluetoothData bluetoothData);
}
