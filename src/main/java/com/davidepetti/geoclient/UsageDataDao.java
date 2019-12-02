package com.davidepetti.geoclient;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface UsageDataDao {

    @Insert
    void insert(UsageData usageData);
}
