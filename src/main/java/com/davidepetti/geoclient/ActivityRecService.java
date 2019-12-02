package com.davidepetti.geoclient;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivityRecService extends Service {

    private static final String LOG_TAG = ActivityRecService.class.getSimpleName();
    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent activityRecIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NotificationCreator.getNotificationId(),
                NotificationCreator.getNotification(getApplicationContext()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "Request current activity");
        activityRecognitionClient = ActivityRecognition.getClient(this);
        Intent activityIntent = new Intent();
        activityIntent.setAction("com.davidepetti.geoclient.ACTION_ACTIVITY_FOUND");
        activityRecIntent = PendingIntent.getBroadcast(getApplicationContext(), 5, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        activityRecognitionClient.requestActivityUpdates(0, activityRecIntent);
        IntentFilter intentFilter = new IntentFilter("com.davidepetti.geoclient.ACTION_ACTIVITY_FOUND");
        registerReceiver(activityReceiver, intentFilter);
        Log.v(LOG_TAG, "Activity requested");

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(activityReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("com.davidepetti.geoclient.ACTION_ACTIVITY_FOUND")) {
                Log.v(LOG_TAG, "ONRECEIVE");
                if (ActivityRecognitionResult.hasResult(intent)) {
                    ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                    DetectedActivity detectedActivity = result.getMostProbableActivity();
                    int currentActivity = detectedActivity.getType();
                    Log.v(LOG_TAG, "Activity detected: " + detectedActivity.getType());
                    activityRecognitionClient.removeActivityUpdates(activityRecIntent);
                    // Save currentActivity in database
                    CurrentActivity activity = new CurrentActivity(currentActivity);
                    AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                    long id = appDatabase.currentActivityDao().insert(activity);

                    SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.MAINRECORD_ID_PREFERENCE_FILE", Context.MODE_PRIVATE);
                    long currentId = sharedPref.getLong("currentId", 0);
                    appDatabase.mainRecordDao().updateIdActivity(currentId, id);
                }
                activityRecognitionClient.removeActivityUpdates(activityRecIntent);
                stopSelf();
            }
        }
    };
}
