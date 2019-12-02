package com.davidepetti.geoclient;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;

import static com.davidepetti.geoclient.App.PRIMARY_CHANNEL_ID;

public class GeoService extends Service {

    private static final String LOG_TAG = GeoService.class.getSimpleName();
    private AlarmManager alarmManager;
    private PendingIntent notifyPendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(NotificationCreator.getNotificationId(), NotificationCreator.getNotification(getApplicationContext()));

        Intent notifyIntent = new Intent(this, AlarmReceiver.class);
        notifyPendingIntent = PendingIntent.getBroadcast
                (this, 10, notifyIntent, 0);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        SharedPreferences sharedPref = getApplicationContext().
                getSharedPreferences("com.davidepetti.geoclient.INTERVAL_PREFERENCE_FILE", Context.MODE_PRIVATE);
        int interval = sharedPref.getInt("currentInterval", 1);
        if (intent != null && intent.getExtras() != null) {
            interval = intent.getExtras().getInt("updatesInterval");
        }
        long repeatInterval = interval * 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime()
                + repeatInterval;

        if (alarmManager != null) {
            alarmManager.setRepeating
                    (AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime, repeatInterval, notifyPendingIntent);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmManager != null) {
            alarmManager.cancel(notifyPendingIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
