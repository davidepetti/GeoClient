package com.davidepetti.geoclient;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface ForegroundAppDao {

    @Insert
    long insert(ForegroundApp foregroundApp);
}
