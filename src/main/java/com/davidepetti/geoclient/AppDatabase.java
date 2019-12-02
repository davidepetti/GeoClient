package com.davidepetti.geoclient;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Geodata.class, CurrentActivity.class, ForegroundApp.class, UsageData.class,
                      BluetoothData.class, MainRecord.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract GeodataDao geodataDao();
    public abstract CurrentActivityDao currentActivityDao();
    public abstract ForegroundAppDao foregroundAppDao();
    public abstract UsageDataDao usageDataDao();
    public abstract BluetoothDataDao bluetoothDataDao();
    public abstract MainRecordDao mainRecordDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "app_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
