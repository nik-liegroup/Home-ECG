package com.th_nuernberg.homeekg.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.th_nuernberg.homeekg.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //Attributes and Constants
    private ImageView bluetooth_monitor;

    //Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Home EKG");


        bluetooth_monitor = (ImageView) findViewById(R.id.bluetoothMonitor);
        bluetooth_monitor.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bluetoothMonitor:
                startActivity(new Intent(this, ScannerActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dropdown_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_sign_out:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setPositiveButton(R.string.logout_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class)
                                .putExtra("finish", true)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                        FirebaseAuth.getInstance().signOut();
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                builder.setMessage(R.string.logout_dialog_message)
                        .setTitle(R.string.logout_dialog_title);
                AlertDialog dialog = builder.create();
                dialog.show();

            case R.id.action_settings:
                //TO DO
                break;

            case R.id.action_about:
                //TO DO
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}