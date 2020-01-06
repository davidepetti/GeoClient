package com.davidepetti.geoclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FirebaseAlarmReceiver extends BroadcastReceiver {

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final String LOG_TAG = FirebaseAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        StorageReference dbRef = storageRef.child("sqlite/" + App.uniqueID + ".sqlite");
        String packageName = context.getApplicationInfo().packageName;
        String currentDBPath = String.format("//data//%s//databases//%s",
                packageName, "app_database");
        File data = Environment.getDataDirectory();
        File currentDB = new File(data, currentDBPath);
        dbRef.putFile(Uri.fromFile(currentDB));

        //Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String dateString = sdf.format(new Date());
        SharedPreferences sharedPref = context.
                getSharedPreferences("com.davidepetti.geoclient.SENDING_TIME_PREFERENCE_FILE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lastSending", dateString);
        editor.commit();
    }
}
