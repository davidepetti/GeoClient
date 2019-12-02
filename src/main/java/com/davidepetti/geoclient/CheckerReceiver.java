package com.davidepetti.geoclient;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class CheckerReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = GeoService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "CHECKER ALARM");
        Log.v(LOG_TAG, "Service Running: " + isMyServiceRunning(GeoService.class, context));
        if (!isMyServiceRunning(GeoService.class, context)) {
            Intent startServiceIntent = new Intent(context, GeoService.class);
            // Take current interval from shared pref
            SharedPreferences sharedPref = context.
                    getSharedPreferences("com.davidepetti.geoclient.INTERVAL_PREFERENCE_FILE", Context.MODE_PRIVATE);
            int currentInterval = sharedPref.getInt("currentInterval", 1);
            Log.v(LOG_TAG, "Current interval: " + currentInterval);
            startServiceIntent.putExtra("updatesInterval", currentInterval);
            ContextCompat.startForegroundService(context, startServiceIntent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
