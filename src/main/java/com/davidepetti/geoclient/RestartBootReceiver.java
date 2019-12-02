package com.davidepetti.geoclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class RestartBootReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = RestartBootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent startServiceIntent = new Intent(context, GeoService.class);
            // Take current interval from shared pref
            SharedPreferences sharedPref = context.
                    getSharedPreferences("com.davidepetti.geoclient.INTERVAL_PREFERENCE_FILE", Context.MODE_PRIVATE);
            int currentInterval = sharedPref.getInt("currentInterval", 1);
            Log.v(LOG_TAG, "Current interval: " + currentInterval);
            startServiceIntent.putExtra("updatesInterval", currentInterval);
            ContextCompat.startForegroundService(context, startServiceIntent);

            Intent startServiceChecker = new Intent(context, ServiceChecker.class);
            ContextCompat.startForegroundService(context, startServiceChecker);
        }
    }
}
