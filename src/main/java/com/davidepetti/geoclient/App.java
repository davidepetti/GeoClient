package com.davidepetti.geoclient;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.UUID;

public class App extends Application {
    public static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    public static String uniqueID = null;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        createUID();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    PRIMARY_CHANNEL_ID,
                    "Geo Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private synchronized void createUID() {
        if (uniqueID == null) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.davidepetti.geoclient.UUID_PREFERENCE_FILE", Context.MODE_PRIVATE);
            uniqueID = sharedPref.getString("UUID", null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("UUID", uniqueID);
                editor.commit();
            }
        }
    }
}
