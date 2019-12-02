package com.davidepetti.geoclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.UUID;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "ALARM RECEIVED");

        setMainRecord(context);
        getLocation(context);
        getCurrentActivity(context);
        getUsageData(context);
        getBTDevices(context);
    }

    private void getCurrentActivity(Context context) {
        Intent activityRecIntent = new Intent(context, ActivityRecService.class);
        ContextCompat.startForegroundService(context, activityRecIntent);
    }

    private void getLocation(Context context) {
        Intent locationIntent = new Intent(context, LocationService.class);
        ContextCompat.startForegroundService(context, locationIntent);
    }

    private void getBTDevices(Context context) {
        Intent btIntent = new Intent(context, BluetoothService.class);
        ContextCompat.startForegroundService(context, btIntent);
    }

    private void getUsageData(Context context) {
        Intent usageIntent = new Intent(context, UsageDataService.class);
        ContextCompat.startForegroundService(context, usageIntent);
    }

    private void setMainRecord(Context context) {
        long currentTimestamp = System.currentTimeMillis();
        String uid = App.uniqueID;

        MainRecord mainRecord = new MainRecord(currentTimestamp, uid, -1, -1, -1, -1, -1);
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        long id = appDatabase.mainRecordDao().insert(mainRecord);
        Log.d(LOG_TAG, "MainRecord id: " + id);
        saveRecordId(context, id);
    }

    private void saveRecordId(Context context, long id) {
        SharedPreferences sharedPref = context.getSharedPreferences("com.davidepetti.geoclient.MAINRECORD_ID_PREFERENCE_FILE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("currentId", id);
        editor.commit();
    }
}
