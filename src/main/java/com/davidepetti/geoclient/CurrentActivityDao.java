package com.davidepetti.geoclient;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface CurrentActivityDao {

    @Insert
    long insert(CurrentActivity activity);
}
