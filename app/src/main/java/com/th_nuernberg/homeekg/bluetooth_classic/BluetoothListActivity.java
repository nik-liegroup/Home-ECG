package com.th_nuernberg.homeekg.bluetooth_classic;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.th_nuernberg.homeekg.Constants;
import com.th_nuernberg.homeekg.R;
import com.th_nuernberg.homeekg.ui.LoginActivity;
import com.th_nuernberg.homeekg.ui.MainActivity;
import com.th_nuernberg.homeekg.ui.RegisterActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static com.th_nuernberg.homeekg.Constants.*;

public class BluetoothListActivity extends Activity {

    //***Variables and Constants***//
    //Handler and Thread
    static private Handler mHandler = new Handler(Looper.myLooper());
    public static void setHandler(Handler handler) {
        mHandler = handler;
    }
    static ConnectedThread connectedThread;

    //Bluetooth
    private static BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private final UUID EKG_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //TODO Specify UUID

    //Views//
    private Button scanButton;
    private ListView deviceListView;


    //***Methods***//
    //onCreate
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        //Check Location Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            }
        }

        //Initialize Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(BluetoothListActivity.this,
                    "Bluetooth is not supported by this device", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BluetoothListActivity.this,
                    MainActivity.class)
                    .putExtra("finish", true)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {
            turnOnBluetooth();
        }

        //ListView Setup
        deviceListView = (ListView) findViewById(R.id.deviceList);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Minimize Bandwidth Usage
                if(bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                BluetoothDevice selectedDevice = bluetoothDevices.get(position);
                if(deviceListAdapter.getItem(position).contains("Paired")) {
                    //Start Connection Process
                    ConnectThread connect = new ConnectThread(selectedDevice);
                    connect.start();
                } else {
                    Toast.makeText(BluetoothListActivity.this,
                            "Please pair with device first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //ListAdapter Initialization
        deviceListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, 0);
        deviceListView.setAdapter(deviceListAdapter);

        //Button Initialization
        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setText("Start scanning");
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                } else {
                    bluetoothAdapter.startDiscovery();
                }
            }
        });

        //Register Broadcasts
        IntentFilter filterActionFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filterActionFound);

        IntentFilter filterActionStateChanged = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filterActionStateChanged);

        IntentFilter filterActionDiscoveryStarted = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filterActionDiscoveryStarted);

        IntentFilter filterActionDiscoveryStopped = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filterActionDiscoveryStopped);
    }

    //onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has been enabled successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bluetooth could not be enabled", Toast.LENGTH_SHORT).show();
                    //Return to MainActivity
                    Intent intent = new Intent(BluetoothListActivity.this, MainActivity.class)
                            .putExtra("finish", true)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                    finish();
                }
        }
    }

    //requestLocationPermission
    private void requestLocationPermission() {
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.i(Constants.TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    //onRequestPermissionResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            Log.i(Constants.TAG, "Received response for location permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted
                Log.i(Constants.TAG, "Location permission has now been granted.");
            } else {
                Log.i(Constants.TAG, "Location permission was NOT granted.");
                Intent intent = new Intent(BluetoothListActivity.this, MainActivity.class)
                        .putExtra("finish", true)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //disconnect
    public static void disconnect(Context context){
        if (connectedThread != null) {
            connectedThread.cancel();
            Toast.makeText(context, "EKG device disconnected", Toast.LENGTH_SHORT).show();
            connectedThread = null;
        }
    }

    //turnOnBluetooth
    private void turnOnBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    //BroadcastReceiver
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //Iterate over paired devices
                    String pairedString = "";
                    Set<BluetoothDevice> pairedDevicesSet = bluetoothAdapter.getBondedDevices();
                    if (pairedDevicesSet.size() > 0) {
                        for (BluetoothDevice pairedDevices : pairedDevicesSet) {
                            if(pairedDevices.getAddress().equals(foundDevice.getAddress())) {
                                pairedString = "Paired";
                                break;
                            }
                        }
                        bluetoothDevices.add(foundDevice);
                        deviceListAdapter.add(foundDevice.getName() + "\n" + foundDevice.getAddress() + " " + pairedString);
                    }
                break;

                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF){
                        turnOnBluetooth();
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    scanButton.setText("Stop scanning");
                    deviceListAdapter.clear();
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    scanButton.setText("Start scanning");
                    break;
            }
        }
    };

    //***Private Classes***//
    //Connect Thread
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        private ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // EKG_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(EKG_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.e(TAG, "Unable to connect. Close the socket and return", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        Toast.makeText(BluetoothListActivity.this,
                                "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            //Connection Succeeded
            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    //***Static Classes***//
    //Connect Thread
    static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            //TODO Adjust Buffer Size
            mmBuffer = new byte[2];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    try {
                        sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            Constants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        Constants.MESSAGE_WRITE, -1, -1, bytes);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
