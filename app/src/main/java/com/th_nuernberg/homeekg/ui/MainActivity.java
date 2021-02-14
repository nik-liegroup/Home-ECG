package com.th_nuernberg.homeekg.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    private Button bluetooth_monitor, update_Main;
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

        //Authentication
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("users");
        userID = user.getUid();

        //TextView
        final TextView welcomeTextView = (TextView) findViewById(R.id.welcomeMain);
        final TextView nameTextView = (TextView) findViewById(R.id.nameMain);

        //EditText
        final EditText mailEditText = (EditText) findViewById(R.id.mailMain);
        final EditText birthdayEditText = (EditText) findViewById(R.id.birthdayMain);
        final EditText genderEditText = (EditText) findViewById(R.id.genderMain);
        final EditText heightEditText = (EditText) findViewById(R.id.heightMain);
        final EditText weightEditText = (EditText) findViewById(R.id.weightMain);
        final EditText countryEditText = (EditText) findViewById(R.id.countryMain);
        final EditText addressEditText = (EditText) findViewById(R.id.addressMain);
        final EditText insuranceEditText = (EditText) findViewById(R.id.insuranceMain);

        bluetooth_monitor = (Button) findViewById(R.id.startScanningMonitor);
        update_Main = (Button) findViewById(R.id.updateButtonMain);

        bluetooth_monitor.setOnClickListener(this);
        update_Main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                user.mail = mailEditText.getText().toString().trim();
                user.birthday = birthdayEditText.getText().toString().trim();
                user.gender = genderEditText.getText().toString().trim();
                user.height = heightEditText.getText().toString().trim().replaceAll("[^0-9]", "");
                user.weight = weightEditText.getText().toString().trim().replaceAll("[^0-9]", "");
                user.country = countryEditText.getText().toString().trim();
                user.address = addressEditText.getText().toString().trim();
                user.insurance = insuranceEditText.getText().toString().trim();
                //Update DataBase
                reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User userLocalProfile = snapshot.getValue(User.class);
                        if(userLocalProfile != null) {
                            if(!userLocalProfile.mail.equals(user.mail)) {
                                reference.child(userID).child("mail").setValue(user.mail).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Email has not been changed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            if(!userLocalProfile.birthday.equals(user.birthday)) {
                                reference.child(userID).child("birthday").setValue(user.birthday).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Birthday has not been changed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(!userLocalProfile.gender.equals(user.gender)) {
                                reference.child(userID).child("gender").setValue(user.gender).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Gender has not been changed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(!userLocalProfile.height.equals(user.height)) {
                                reference.child(userID).child("height").setValue(user.height).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Height has not been changed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(!userLocalProfile.weight.equals(user.weight)) {
                                reference.child(userID).child("weight").setValue(user.weight).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Weight has not been changed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(!userLocalProfile.country.equals(user.country)) {
                                reference.child(userID).child("country").setValue(user.country).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Country has not been changed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(!userLocalProfile.address.equals(user.address)) {
                                reference.child(userID).child("address").setValue(user.address).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Address has not been changed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            if(!userLocalProfile.insurance.equals(user.insurance)) {
                                reference.child(userID).child("insurance").setValue(user.insurance).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Insurance has not been changed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                if(userProfile != null) {
                    String name = userProfile.name;
                    String mail = userProfile.mail;
                    String age = userProfile.birthday;
                    String gender = userProfile.gender;
                    String height = userProfile.height;
                    String weight = userProfile.weight;
                    String country = userProfile.country;
                    String address = userProfile.address;
                    String insurance= userProfile.insurance;

                    welcomeTextView.setText("Welcome back, " + name + "!");
                    nameTextView.setText(name);
                    mailEditText.setText(mail);
                    birthdayEditText.setText(age);
                    genderEditText.setText(gender);
                    if(!height.equals("")) {
                        heightEditText.setText(height + "cm");
                    }
                    if(!weight.equals("")) {
                        weightEditText.setText(weight + "kg");
                    }
                    countryEditText.setText(country);
                    addressEditText.setText(address);
                    insuranceEditText.setText(insurance);
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