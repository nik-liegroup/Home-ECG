package com.th_nuernberg.homeekg.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.th_nuernberg.homeekg.R;
import com.th_nuernberg.homeekg.login.User;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;

    private ImageView facebook_reg, google_reg, back_reg;
    private TextView has_acc_reg;
    private CircularProgressButton cir_button_reg;
    private EditText name_reg, mail_reg, mobile_reg, password_reg;
    private ProgressBar progress_bar_reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        changeStatusBarColor();

        mAuth = FirebaseAuth.getInstance();

        has_acc_reg = (TextView) findViewById(R.id.TextViewHasAccReg);
        has_acc_reg.setOnClickListener(this);

        facebook_reg = (ImageView) findViewById(R.id.ImageViewFacebookReg);
        facebook_reg.setOnClickListener(this);

        google_reg = (ImageView) findViewById(R.id.ImageViewGoogleReg);
        google_reg.setOnClickListener(this);

        back_reg = (ImageView) findViewById(R.id.ImageViewBackReg);
        back_reg.setOnClickListener(this);

        cir_button_reg = (CircularProgressButton) findViewById(R.id.cirRegisterButtonReg);
        cir_button_reg.setOnClickListener(this);

        progress_bar_reg = (ProgressBar) findViewById(R.id.progressBarReg);
        progress_bar_reg.setOnClickListener(this);

        name_reg = (EditText) findViewById(R.id.editTextNameReg);
        mail_reg = (EditText) findViewById(R.id.editTextEmailLogin);
        mobile_reg = (EditText) findViewById(R.id.editTextMobileReg);
        password_reg = (EditText) findViewById(R.id.editTextPasswordLogin);
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class)
                .putExtra("finish", true)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ImageViewGoogleReg:
            case R.id.ImageViewBackReg:
            case R.id.TextViewHasAccReg:
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class)
                        .putExtra("finish", true)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
                finish();
                break;
            case R.id.cirRegisterButtonReg:
                registerUser();
                break;
            case R.id.ImageViewFacebookReg:
                //TO DO
                break;
        }
    }

    private void registerUser() {
        String name = name_reg.getText().toString().trim();
        String mail = mail_reg.getText().toString().trim();
        String mobile = mobile_reg.getText().toString().trim();
        String password = password_reg.getText().toString().trim();

        if(name.isEmpty()) {
            name_reg.setError("Full Name is required");
            name_reg.requestFocus();
            return;
        }

        if(mail.isEmpty()) {
            mail_reg.setError("Mail is required");
            mail_reg.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            mail_reg.setError("Please provide an valid Email");
            mail_reg.requestFocus();
            return;
        }

        if(password.isEmpty()) {
            password_reg.setError("Password is required");
            password_reg.requestFocus();
            return;
        }

        if(password.length() < 6) {
            password_reg.setError("Minimal password length should be 6 characters");
            password_reg.requestFocus();
            return;
        }

        progress_bar_reg.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            User user = new User(name, mail, mobile);
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "User has been registered successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class)
                                                .putExtra("finish", true)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
                                        finish();
                                    } else {
                                        progress_bar_reg.setVisibility(View.INVISIBLE);
                                        Toast.makeText(RegisterActivity.this, "Registered, but database is corrupted!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            progress_bar_reg.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterActivity.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}