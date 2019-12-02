package com.davidepetti.geoclient;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class UsageDataService extends Service {

    private static final String LOG_TAG = UsageDataService.class.getSimpleName();

    private UsageStatsManager usageStatsManager;
    private long usageDataId = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        } else {
            stopSelf();
        }
        startForeground(NotificationCreator.getNotificationId(),
                NotificationCreator.getNotification(getApplicationContext()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "Usage requested");

        SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.USAGEDATAID_PREFERENCE_FILE", Context.MODE_PRIVATE);
        usageDataId = sharedPref.getLong("usageDataId", 1);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long startOfTheDay = c.getTimeInMillis();
        long now = System.currentTimeMillis();
        List<UsageStats> statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                startOfTheDay, now);

        UsageStats fgApp = null;
        long foregroundMillis = 0;
        for (UsageStats us : statsList) {
            if (us.getTotalTimeInForeground() > 0) {
                long millis = us.getLastTimeUsed();
                if (millis > foregroundMillis) {
                    foregroundMillis = millis;
                    fgApp = us;
                }
                UsageData usageData = new UsageData(usageDataId, us.getPackageName(), us.getTotalTimeInForeground());
                AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                appDatabase.usageDataDao().insert(usageData);

                sharedPref = getSharedPreferences("com.davidepetti.geoclient.MAINRECORD_ID_PREFERENCE_FILE", Context.MODE_PRIVATE);
                long currentId = sharedPref.getLong("currentId", 0);
                appDatabase.mainRecordDao().updateIdUsage(currentId, usageDataId);
            }
        }
        usageDataId++;
        if (fgApp != null) {
            ForegroundApp foregroundApp = new ForegroundApp(fgApp.getPackageName());
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            long id = appDatabase.foregroundAppDao().insert(foregroundApp);
            Log.v(LOG_TAG, "Foreground app: " + fgApp.getPackageName());
            sharedPref = getSharedPreferences("com.davidepetti.geoclient.MAINRECORD_ID_PREFERENCE_FILE", Context.MODE_PRIVATE);
            long currentId = sharedPref.getLong("currentId", 0);
            appDatabase.mainRecordDao().updateIdForeground(currentId, id);
        }

        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.USAGEDATAID_PREFERENCE_FILE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("usageDataId", usageDataId);
        editor.commit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
