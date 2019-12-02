package com.davidepetti.geoclient;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_CODE = 1;
    private int currentInterval;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Controlla nelle Pref se autosend e' checked
        SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.AUTOSEND_PREFERENCE_FILE", Context.MODE_PRIVATE);
        boolean checked = sharedPref.getBoolean("checked", false);
        menu.findItem(R.id.action_autosend).setChecked(checked);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_request:
                composeEmail("Richiesta copia dei dati");
                return true;
            case R.id.action_delete:
                composeEmail("Richiesta cancellazione dei dati");
                return true;
            case R.id.action_export:
                if (isStoragePermissionGranted()) {
                    exportDatabase();
                }
                return true;
            case R.id.action_send:
                sendDatabase();
                return true;
            case R.id.action_autosend:
                if (item.isChecked()) {
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.AUTOSEND_PREFERENCE_FILE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("checked", false);
                    editor.commit();
                } else {
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.AUTOSEND_PREFERENCE_FILE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("checked", true);
                    editor.commit();
                }
                // Set auto send
                setEmailAutoSend(item.isChecked());
                return true;
            case R.id.action_gdpr:
                showGDPR();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PrivacyDialogFragment dialogFragment = new PrivacyDialogFragment();

        SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.DIALOG_PREFERENCE_FILE", Context.MODE_PRIVATE);
        boolean agreed = sharedPref.getBoolean("agreed", false);

        if (!agreed) {
            dialogFragment.showNow(getSupportFragmentManager(), "dialog");
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions already granted!", Toast.LENGTH_SHORT).show();
        } else {
            requestLocationPermission();
        }

        Button buttonCollect = findViewById(R.id.buttonCollect);
        buttonCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceCheckerIntent = new Intent(MainActivity.this, ServiceChecker.class);
                ContextCompat.startForegroundService(MainActivity.this, serviceCheckerIntent);

                Intent startServiceIntent = new Intent(MainActivity.this, GeoService.class);
                if (currentInterval == 0) {
                    currentInterval = 1;
                    Toast.makeText(MainActivity.this, "1 min setted as default", Toast.LENGTH_SHORT).show();
                }
                startServiceIntent.putExtra("updatesInterval", currentInterval);
                SharedPreferences sharedPref = getApplicationContext().
                        getSharedPreferences("com.davidepetti.geoclient.INTERVAL_PREFERENCE_FILE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("currentInterval", currentInterval);
                editor.commit();
                sharedPref = getApplicationContext().getSharedPreferences("com.davidepetti.geoclient.LOCATION_PREFERENCE_FILE", Context.MODE_PRIVATE);
                boolean locationPermissionGranted = sharedPref.getBoolean("locationPermissionGranted", false);
                Log.v(LOG_TAG, "location: " + locationPermissionGranted);
                if (locationPermissionGranted) {
                    ContextCompat.startForegroundService(MainActivity.this, startServiceIntent);
                } else {
                    Toast.makeText(MainActivity.this, "You do not have permissions!", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button buttonStop = findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopServiceIntent = new Intent(MainActivity.this, GeoService.class);
                stopService(stopServiceIntent);

                Intent stopServiceCheckerIntent = new Intent(MainActivity.this, ServiceChecker.class);
                stopService(stopServiceCheckerIntent);
            }
        });

        final TextView seekBarTextView = findViewById(R.id.seekBarTextView);
        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(300);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentInterval = progress;
                seekBarTextView.setText(getString(R.string.minutes, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (!isUsagePermissionsGranted()) {
            Toast.makeText(this, "You must grant usage permissions for this app", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.SENDING_TIME_PREFERENCE_FILE", Context.MODE_PRIVATE);
        String time = sharedPref.getString("lastSending", "-");
        TextView lastSending = findViewById(R.id.sendingTime);
        lastSending.setText(lastSending.getText() + time);

        sharedPref = getSharedPreferences("com.davidepetti.geoclient.MAINRECORD_ID_PREFERENCE_FILE", Context.MODE_PRIVATE);
        long lastId = sharedPref.getLong("currentId", -1);
        String numRecord;
        if (lastId == -1) {
            numRecord = "-";
        } else {
            numRecord = String.valueOf(lastId);
        }
        TextView recordNumber = findViewById(R.id.recordNumber);
        recordNumber.setText(recordNumber.getText() + numRecord);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is critical for the app to functioning")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SharedPreferences sharedPref = getApplicationContext().
                getSharedPreferences("com.davidepetti.geoclient.LOCATION_PREFERENCE_FILE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                editor.putBoolean("locationPermissionGranted", true);
                editor.commit();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                editor.putBoolean("locationPermissionGranted", false);
                editor.commit();
            }
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission
            }
        }
    }

    private boolean isUsagePermissionsGranted() {
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), this.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                granted = (this.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
            }
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    private void composeEmail(String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"petti.davide@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void exportDatabase() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            String packageName = getApplicationContext().getApplicationInfo().packageName;
            String currentDBPath = String.format("//data//%s//databases//%s",
                    packageName, "app_database");
            String backupDBPath = String.format("debug_%s.sqlite", packageName);
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(this, "Exported!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG, "Permission is granted");
                return true;
            } else {
                Log.v(LOG_TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(LOG_TAG, "Permission is granted");
            return true;
        }
    }

    private void sendDatabase() {
        exportDatabase();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"petti.davide@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Invio Database");
        String packageName = getApplicationContext().getApplicationInfo().packageName;
        String backupDBPath = String.format("debug_%s.sqlite", packageName);
        File sd = Environment.getExternalStorageDirectory();
        File backupDB = new File(sd, backupDBPath);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(backupDB));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    private void setEmailAutoSend(boolean checked) {
        if (checked) {
            setName();
            alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, EmailAlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(this, 24, intent, 0);

            Calendar calendar = Calendar.getInstance();
            //.calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 21);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
        } else {
            if (alarmMgr != null) {
                alarmMgr.cancel(alarmIntent);
            }
        }
    }

    private void setName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Username");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isStoragePermissionGranted();
                String name = input.getText().toString();
                Log.v(LOG_TAG, name);
                // Setta nome nelle pref
                SharedPreferences sharedPref = getApplicationContext().
                        getSharedPreferences("com.davidepetti.geoclient.USERNAME_PREFERENCE_FILE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", name);
                editor.commit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showGDPR() {
        PrivacyDialogFragment dialogFragment = new PrivacyDialogFragment();
        dialogFragment.showNow(getSupportFragmentManager(), "dialog");
    }
}
