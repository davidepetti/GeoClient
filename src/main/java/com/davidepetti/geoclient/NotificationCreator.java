package com.davidepetti.geoclient;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import static com.davidepetti.geoclient.App.PRIMARY_CHANNEL_ID;

public class NotificationCreator {

    private static final int NOTIFICATION_ID = 1094;

    private static Notification notification;

    public static Notification getNotification(Context context) {

        if (notification == null) {

            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    0, notificationIntent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                        .setContentTitle("Geo Service")
                        .setContentText("Busy collecting data...")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentIntent(pendingIntent)
                        .setOnlyAlertOnce(true)
                        .build();
            } else {
                notification = new NotificationCompat.Builder(context, null)
                        .setContentTitle("Geo Service")
                        .setContentText("Busy collecting data...")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentIntent(pendingIntent)
                        .setOnlyAlertOnce(true)
                        .build();
            }
        }

        return notification;
    }

    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }
}
