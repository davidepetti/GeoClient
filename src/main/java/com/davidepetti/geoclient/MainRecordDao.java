package com.davidepetti.geoclient;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MainRecordDao {

    @Insert
    long insert(MainRecord mainRecord);

    @Query("UPDATE mainrecord_table SET idGeo = :idGeo WHERE id = :id")
    void updateIdGeo(long id, long idGeo);

    @Query("UPDATE mainrecord_table SET idUsage = :idUsage WHERE id = :id")
    void updateIdUsage(long id, long idUsage);

    @Query("UPDATE mainrecord_table SET idBluetooth = :idBluetooth WHERE id = :id")
    void updateIdBluetooth(long id, long idBluetooth);

    @Query("UPDATE mainrecord_table SET idActivity = :idActivity WHERE id = :id")
    void updateIdActivity(long id, long idActivity);

    @Query("UPDATE mainrecord_table SET idForeground = :idForeground WHERE id = :id")
    void updateIdForeground(long id, long idForeground);
}
