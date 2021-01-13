package com.th_nuernberg.homeekg.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.th_nuernberg.homeekg.R;
import com.th_nuernberg.homeekg.login.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //Attributes and Constants
    private Button bluetooth_monitor;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    //Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Home EKG");

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("users");
        userID = user.getUid();

        bluetooth_monitor = (Button) findViewById(R.id.startScanningMonitor);
        bluetooth_monitor.setOnClickListener(this);

        final TextView welcomeTextView = (TextView) findViewById(R.id.welcomeMain);
        final TextView nameTextView = (TextView) findViewById(R.id.nameMain);
        final TextView mailTextView = (TextView) findViewById(R.id.mailMain);
        final TextView ageTextView = (TextView) findViewById(R.id.ageMain);
        final TextView genderTextView = (TextView) findViewById(R.id.genderMain);
        final TextView heightTextView = (TextView) findViewById(R.id.heightMain);
        final TextView weightTextView = (TextView) findViewById(R.id.weightMain);
        final TextView countryTextView = (TextView) findViewById(R.id.countryMain);
        final TextView addressTextView = (TextView) findViewById(R.id.addressMain);
        final TextView insuranceTextView = (TextView) findViewById(R.id.insuranceMain);

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if(userProfile != null) {
                    String name = userProfile.name;
                    String mail = userProfile.mail;
                    String age = userProfile.age;
                    String gender = userProfile.gender;
                    String height = userProfile.height;
                    String weight= userProfile.weight;
                    String country= userProfile.country;
                    String city= userProfile.city;
                    String postcode= userProfile.postcode;
                    String street= userProfile.street;
                    String insurance= userProfile.insurance;

                    welcomeTextView.setText("Willkommen zurück, " + name + "!");
                    nameTextView.setText(name);
                    mailTextView.setText(mail);
                    ageTextView.setText(age);
                    genderTextView.setText(gender);
                    heightTextView.setText(height + "m");
                    weightTextView.setText(weight + "kg");
                    countryTextView.setText(country);
                    addressTextView.setText(street + ", " + postcode + " " + city);
                    insuranceTextView.setText(insurance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startScanningMonitor:
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
                break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}