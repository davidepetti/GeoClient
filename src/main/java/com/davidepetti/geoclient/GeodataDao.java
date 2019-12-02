package com.davidepetti.geoclient;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface GeodataDao {

    @Insert
    long insert(Geodata geodata);
}
