package com.davidepetti.geoclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BluetoothService extends Service {

    private static final String LOG_TAG = BluetoothService.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter;
    private boolean btRequested = false;
    private boolean restart = false;
    private long bluetoothId = 1;
    private ArrayList<String> devices;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NotificationCreator.getNotificationId(),
                NotificationCreator.getNotification(getApplicationContext()));
        devices = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(stateReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(deviceFoundReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.BTID_PREFERENCE_FILE", Context.MODE_PRIVATE);
        bluetoothId = sharedPref.getLong("bluetoothId", 1);
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
                btRequested = true;
            } else {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                boolean discoveryStarted = bluetoothAdapter.startDiscovery();
                Log.v(LOG_TAG, "Started: " + discoveryStarted);
                if (!discoveryStarted) {
                    bluetoothAdapter.disable();
                    restart = true;
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.BTID_PREFERENCE_FILE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("bluetoothId", bluetoothId);
        editor.commit();
        unregisterReceiver(stateReceiver);
        unregisterReceiver(deviceFoundReceiver);
        unregisterReceiver(discoveryReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final BroadcastReceiver stateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    Log.v(LOG_TAG, "Bluetooth on");
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    boolean discoveryStarted = bluetoothAdapter.startDiscovery();
                    Log.v(LOG_TAG, "Started: " + discoveryStarted);
                    if (!discoveryStarted && btRequested) {
                        bluetoothAdapter.disable();
                    }
                } else if (state == BluetoothAdapter.STATE_OFF && restart) {
                    bluetoothAdapter.enable();
                }
            }
        }
    };

    private final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add device to database
                String address = device.getAddress();
                if (!devices.contains(address)) {
                    devices.add(address);
                }

                Log.v(LOG_TAG, "Device found: " + device.getAddress());
            }
        }
    };

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.v(LOG_TAG, "Discovery finished");
                if (btRequested && bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                }
                for (String device : devices) {
                    BluetoothData bluetoothData = new BluetoothData(bluetoothId, device);
                    AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                    appDatabase.bluetoothDataDao().insert(bluetoothData);
                }

                SharedPreferences sharedPref = getSharedPreferences("com.davidepetti.geoclient.MAINRECORD_ID_PREFERENCE_FILE", Context.MODE_PRIVATE);
                long currentId = sharedPref.getLong("currentId", 0);
                AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
                appDatabase.mainRecordDao().updateIdBluetooth(currentId, bluetoothId);

                bluetoothId++;
                stopSelf();
            }
        }
    };
}
