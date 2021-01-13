package com.th_nuernberg.homeekg.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.th_nuernberg.homeekg.R;
import com.th_nuernberg.homeekg.bluetooth.BluetoothAdapterService;
import com.th_nuernberg.homeekg.bluetooth.SampleGattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.th_nuernberg.homeekg.Constants.fullscreen;
import static com.th_nuernberg.homeekg.Constants.landscape;
import static com.th_nuernberg.homeekg.Constants.orientation;
import static com.th_nuernberg.homeekg.Constants.portrait;

public class MonitorActivity extends AppCompatActivity {
    private final static String TAG = MonitorActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothAdapterService mBluetoothAdapterService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private CheckBox checkOrientation;
    private CheckBox checkFullscreen;
    private LineChart chart;

    private String receiveBuffer = "";
    private ArrayList<Entry> values = new ArrayList<>();
    private Handler handler = new Handler();
    private final float sampleTime = 1.0f/64.0f;
    private float[] data = new float[64];
    private int index = 0;
    private boolean dataAvailable = false;
    private final int updatePeriod = 61;// 4 * (1/64) = 62.5ms - Period must be a little bit faster

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothAdapterService = ((BluetoothAdapterService.LocalBinder) service).getService();
            if (!mBluetoothAdapterService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothAdapterService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothAdapterService = null;
        }
    };

    private void messageHandler() {
        if(dataAvailable)
            System.out.println("Chart has NOT finished drawing.");
        else
            System.out.println("Chart HAS finished drawing.");
        String[] stringNumbers;
        stringNumbers = receiveBuffer.split(",");
        for(int i=0; i < stringNumbers.length; i++) {
            float value;
            try {
                value = Float.parseFloat(stringNumbers[i]);
            } catch (Exception e) {
                value = 0;
                e.printStackTrace();
                System.out.println("Parsing error.");
            }
            if(value > 4095 || value < 0)
                System.out.println("Weird number[" + i + "]: " + value);
            else {
                if(i<64) {
                    try {
                        data[i] = Float.parseFloat(stringNumbers[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        data[i] = 2048.0f;
                    }
                } else
                    System.out.println("Extra number[" + i + "]: " + Float.parseFloat(stringNumbers[i]));
            }
        }
        dataAvailable = true;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapterService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothAdapterService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothAdapterService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothAdapterService.getSupportedGattServices());
            } else if (BluetoothAdapterService.ACTION_DATA_AVAILABLE.equals(action)) {
                receiveBuffer += intent.getStringExtra(BluetoothAdapterService.EXTRA_DATA);
                if(receiveBuffer.contains("\n")) {
                    receiveBuffer = receiveBuffer.substring(0, receiveBuffer.length() - 1);
                    messageHandler();
                    receiveBuffer = "";
                }
            }else if (receiveBuffer.length()>320)
                receiveBuffer = "";
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothAdapterService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothAdapterService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothAdapterService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {

    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mConnectionState = (TextView) findViewById(R.id.connection_state);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("EKG Monitor");
        Intent gattServiceIntent = new Intent(this, BluetoothAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        checkOrientation = findViewById(R.id.checkBoxOrientation);
        checkOrientation.setChecked(orientation);

        checkFullscreen = findViewById(R.id.checkBoxFullscreen);
        checkFullscreen.setChecked(fullscreen);

        if(orientation == portrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        checkOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkOrientation.isChecked() == portrait)
                {
                    orientation = portrait;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }else
                {
                    orientation = landscape;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });

        checkFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkFullscreen.isChecked())
                {
                    fullscreen = true;
                    //TO DO
                } else {
                    fullscreen = false;
                    //TO DO
                }
            }
        });

        chart = findViewById(R.id.chart1);
        chart.setDrawGridBackground(false);

        // no description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(false);

        // enable scaling and dragging
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setScaleY(1.0f);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(false);

        for(int i=0; i<512; i++)
        {
            values.add(new Entry(i*sampleTime, 2048.0f));
        }


        updateChart();

        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                handler.postDelayed(this, updatePeriod);
                updateRoutine();
            }
        }, updatePeriod);
    }

    private void updateRoutine()
    {
        if(dataAvailable)
        {
            for(int i=0; i<4; i++)
            {
                values.set(index, new Entry(index*sampleTime, data[index%64]));
                index++;
            }
            if(index == 512)
                index = 0;

            if(index%64 == 0)
            {
                dataAvailable = false;
                System.out.println("Waiting for new data.");
            }
            updateChart();
        }
        else
            System.out.println("Runnable is doing nothing...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothAdapterService != null) {
            final boolean result = mBluetoothAdapterService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothAdapterService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dropdown_monitor, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothAdapterService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothAdapterService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                //custom code
                if(uuid.equals("0000ffe1-0000-1000-8000-00805f9b34fb") && mNotifyCharacteristic == null) {
                    mBluetoothAdapterService.setCharacteristicNotification(gattCharacteristic, true);
                    mNotifyCharacteristic = gattCharacteristic;
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapterService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothAdapterService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothAdapterService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothAdapterService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void updateChart()
    {
        chart.resetTracking();

        setData();
        // redraw
        chart.invalidate();
    }

    private void setData() {

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "ECG");

        set1.setColor(Color.RED);
        set1.setLineWidth(1.0f);
        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawFilled(false);

        // create a data object with the data sets
        LineData data = new LineData(set1);

        // set data
        chart.setData(data);
        chart.setVisibleYRange(-128, 127, null);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);
    }
}
