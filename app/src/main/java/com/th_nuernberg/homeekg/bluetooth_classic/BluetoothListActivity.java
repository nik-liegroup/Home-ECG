package com.th_nuernberg.homeekg.bluetooth_classic;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.th_nuernberg.homeekg.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BluetoothListActivity extends Activity {


    private static final int REQUEST_ENABLE_BT = 420;
    private boolean bluetoothEnabled = false;
    private static BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceListAdapter;
    private HashMap<BluetoothDevice, String> bluetoothDevices = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        //TODO Check Manifest Permissions (Location and Admin)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //TODO Return to MainActivity
        }

        if (!bluetoothAdapter.isEnabled()) {
            turnOnBluetooth();
        } else {
            bluetoothEnabled = true;
        }

        //ListView
        ListView deviceListView = (ListView) findViewById(R.id.deviceList);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }

                if(deviceListAdapter.getItem(position).contains("Paired")) {
                    //TODO Start Connection Process
                } else {
                    //TODO Start Pairing Activity
                }
            }
        });

        //ListAdapter
        //TODO Implement better looking adapter layout
        deviceListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        deviceListView.setAdapter(deviceListAdapter);

        //Button
        Button scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Change button text if scanning
                if(bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                    scanButton.setText("Start scanning");
                } else {
                    bluetoothAdapter.startDiscovery();
                    scanButton.setText("Stop scanning");
                }
            }
        });



        //Register Broadcasts
        IntentFilter filterActionFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filterActionFound);

        IntentFilter filterActionStateChanged = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filterActionStateChanged);


        //TODO Enable device discoverability (Not necessary for client)
        //TODO Only add if scanning is bugged

    }

    private void turnOnBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    //BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //Iterate over already paired devices
                    String pairedString = "Not Paired";
                    Set<BluetoothDevice> pairedDevicesSet = bluetoothAdapter.getBondedDevices();
                    if (pairedDevicesSet.size() > 0) {
                        for (BluetoothDevice pairedDevices : pairedDevicesSet) {
                            if(pairedDevices.getAddress().equals(foundDevice.getAddress())) {
                                pairedString = "Paired";
                                break;
                            }
                        }
                        bluetoothDevices.put(foundDevice, pairedString);
                        deviceListAdapter.add(foundDevice.getName() + " " + pairedString + " " + "\n" + foundDevice.getAddress());
                    }
                break;

                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF){
                        turnOnBluetooth();
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Toast.makeText(getApplicationContext(), "Started scanning", Toast.LENGTH_SHORT).show();
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(getApplicationContext(), "Stopped scanning", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Unregister ACTION_FOUND receiver
        unregisterReceiver(receiver);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has been enabled successfully", Toast.LENGTH_SHORT).show();
                    bluetoothEnabled = true;
                } else {
                    Toast.makeText(this, "Bluetooth could not be enabled", Toast.LENGTH_SHORT).show();

                    //TODO Ask again or return to MainActivity
                }
        }
    }
}
