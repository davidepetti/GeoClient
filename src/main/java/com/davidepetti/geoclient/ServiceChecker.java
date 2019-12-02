package com.davidepetti.geoclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

public class ServiceChecker extends Service {

    private static final String LOG_TAG = GeoService.class.getSimpleName();
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private PendingIntent secondAlarmIntent;


    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NotificationCreator.getNotificationId(),
                NotificationCreator.getNotification(getApplicationContext()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // When the service start wait ~3 min and start a one time alarm
        // to check if the main service was started at boot
        // Then start a repeating alarm that check every 20-30 min if the main
        // service is running
        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent receiverIntent = new Intent(this, CheckerReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 24, receiverIntent, 0);
        secondAlarmIntent = PendingIntent.getBroadcast(this, 25, receiverIntent, 0);

        if (alarmMgr != null) {
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                    2 * 60 * 1000, alarmIntent);

            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                    4 * 60 * 1000, 2 * 60 * 1000, secondAlarmIntent);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
