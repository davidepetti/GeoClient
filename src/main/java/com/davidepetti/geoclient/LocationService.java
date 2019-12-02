package com.davidepetti.geoclient;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationService extends Service {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NotificationCreator.getNotificationId(),
                NotificationCreator.getNotification(getApplicationContext()));
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You have to grant permissions", Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // If the difference from the current time and the location time
                            // is less than 30 sec ok else start a onetime alarm tu get location
                            // after 10 seconds
                            Log.v(LOG_TAG, "" + location);
                            Log.v(LOG_TAG, "Time: " + location.getTime());
                            Log.v(LOG_TAG, "CurrentTime: " + System.currentTimeMillis());
                            Log.v(LOG_TAG, "Difference: " + (System.currentTimeMillis() - location.getTime()));

                            Geodata geodata = new Geodata(location.getLatitude(), location.getLongitude());
                            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                            long idGeo = appDatabase.geodataDao().insert(geodata);

                            SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.MAINRECORD_ID_PREFERENCE_FILE", Context.MODE_PRIVATE);
                            long currentId = sharedPref.getLong("currentId", 0);
                            appDatabase.mainRecordDao().updateIdGeo(currentId, idGeo);

                            /*if (SystemClock.currentThreadTimeMillis() - location.getTime() < 30000) {
                                // Save location in database
                                Geodata geodata = new Geodata(location.getLatitude(), location.getLongitude());
                                AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                                appDatabase.geodataDao().insert(geodata);
                            } else {
                                Intent notifyIntent = new Intent(LocationService.this, AlarmReceiver.class);
                                PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                                        (LocationService.this, 10, notifyIntent, 0);

                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                alarmManager.cancel(notifyPendingIntent);
                                notifyPendingIntent.cancel();
                                notifyPendingIntent = PendingIntent.getBroadcast
                                        (LocationService.this, 10, notifyIntent, 0);
                                SharedPreferences sharedPref = getApplicationContext().
                                        getSharedPreferences("com.davidepetti.geoclient.INTERVAL_PREFERENCE_FILE", Context.MODE_PRIVATE);
                                int interval = sharedPref.getInt("currentInterval", 1);
                                if (intent != null && intent.getExtras() != null) {
                                    interval = intent.getExtras().getInt("updatesInterval");
                                }
                                long repeatInterval = interval * 60 * 1000;
                                //long triggerTime = SystemClock.elapsedRealtime()
                                //       + repeatInterval;

                                // set(int type, long triggerAtMillis, PendingIntent operation)
                                long triggerTime = SystemClock.elapsedRealtime() + 15000;
                                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, repeatInterval, notifyPendingIntent);
                            }*/
                        }
                    }
                });

        stopSelf();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
