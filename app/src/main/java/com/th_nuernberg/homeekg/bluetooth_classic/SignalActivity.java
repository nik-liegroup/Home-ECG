package com.th_nuernberg.homeekg.bluetooth_classic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
import com.th_nuernberg.homeekg.R;

public class SignalActivity extends Activity implements View.OnClickListener{

    //Toggle Buttons Status Variables
    static boolean Lock;
    static boolean AutoScrollX;
    static boolean Stream;

    //Buttons and ToggleButtons
    Button bConnect, bDisconnect, bXminus, bXplus;
    ToggleButton tbLock;
    ToggleButton tbScroll;
    ToggleButton tbStream;

    //GraphView
    static LinearLayout GraphView;
    static GraphView graphView;
    static GraphViewSeries Series;
    private static double graph2LastXValue = 0;
    private static int Xview = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set Orientation Landscape
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Hide title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Hide Status bar
        this.getWindow().setFlags(WindowManager.LayoutParams.
                FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signal);

        //Set Background Color
        LinearLayout background = (LinearLayout) findViewById(R.id.bg);
        background.setBackgroundColor(Color.BLACK);

        //Set Handler
        BluetoothActivity.gethandler(mHandler);

        //Initialize GraphView
        GraphView = (LinearLayout) findViewById(R.id.Graph);
        Series = new GraphViewSeries("Signal",
                //Color and thickness of the line
                new GraphViewSeriesStyle(Color.YELLOW, 2),
                new GraphViewData[] {new GraphViewData(0, 0)});

        graphView = new LineGraphView(this, "Graph");
        graphView.setViewPort(0, Xview);
        graphView.setScrollable(true);
        graphView.setScalable(true);
        graphView.setShowLegend(true);
        graphView.setLegendAlign(LegendAlign.BOTTOM);
        graphView.setManualYAxis(true);
        graphView.setManualYAxisBounds(5, 0);
        graphView.addSeries(Series); // data
        GraphView.addView(graphView);

        //Initialize Buttons
        bConnect = (Button)findViewById(R.id.bConnect);
        bConnect.setOnClickListener(this);

        bDisconnect = (Button)findViewById(R.id.bDisconnect);
        bDisconnect.setOnClickListener(this);

        bXminus = (Button)findViewById(R.id.bXminus);
        bXminus.setOnClickListener(this);

        bXplus = (Button)findViewById(R.id.bXplus);
        bXplus.setOnClickListener(this);

        tbLock = (ToggleButton)findViewById(R.id.tbLock);
        tbLock.setOnClickListener(this);

        tbScroll = (ToggleButton)findViewById(R.id.tbScroll);
        tbScroll.setOnClickListener(this);

        tbStream = (ToggleButton)findViewById(R.id.tbStream);
        tbStream.setOnClickListener(this);

        Lock = true;
        AutoScrollX = true;
        Stream = true;
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            //Handle different messages
            switch(msg.what){
                case BluetoothActivity.SUCCESS_CONNECT:
                    BluetoothActivity.connectedThread = new BluetoothActivity.ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
                    String s = "successfully connected";
                    BluetoothActivity.connectedThread.start();
                    break;

                case BluetoothActivity.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String(readBuf, 0, 5);

                    Log.d("strIncom", strIncom);
                    if (strIncom.indexOf('.')==2 && strIncom.indexOf('s')==0){
                        strIncom = strIncom.replace("s", "");
                        if (isFloatNumber(strIncom)){
                            Series.appendData(new GraphViewData(graph2LastXValue,Double.parseDouble(strIncom)),AutoScrollX);

                            //X-axis control
                            if (graph2LastXValue >= Xview && Lock == true){
                                Series.resetData(new GraphViewData[] {});
                                graph2LastXValue = 0;
                            }
                            else graph2LastXValue += 0.1;

                            if(Lock == true)
                                graphView.setViewPort(0, Xview);
                            else
                                graphView.setViewPort(graph2LastXValue-Xview, Xview);

                            //Update
                            GraphView.removeView(graphView);
                            GraphView.addView(graphView);
                        }
                    }
                    break;
            }
        }

        public boolean isFloatNumber(String num){
            try{
                Double.parseDouble(num);
            } catch(NumberFormatException nfe) {
                return false;
            }
            return true;
        }
    };

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch(v.getId()){
            case R.id.bConnect:
                startActivity(new Intent("android.intent.action.BT1"));
                break;

            case R.id.bDisconnect:
                BluetoothActivity.disconnect();
                break;

            case R.id.bXminus:
                if (Xview>1) Xview--;
                break;

            case R.id.bXplus:
                if (Xview<30) Xview++;
                break;

            case R.id.tbLock:
                if (tbLock.isChecked()){
                    Lock = true;
                } else {
                    Lock = false;
                }
                break;

            case R.id.tbScroll:
                if (tbScroll.isChecked()){
                    AutoScrollX = true;
                } else {
                    AutoScrollX = false;
                }
                break;

            case R.id.tbStream:
                if (tbStream.isChecked()){
                    if (BluetoothActivity.connectedThread != null)
                        BluetoothActivity.connectedThread.write("E");
                } else {
                    if (BluetoothActivity.connectedThread != null)
                        BluetoothActivity.connectedThread.write("Q");
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (BluetoothActivity.connectedThread != null) {
            //Stop streaming
            BluetoothActivity.connectedThread.write("Q");
        }
        super.onBackPressed();
    }
}
