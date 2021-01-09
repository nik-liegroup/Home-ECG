package com.th_nuernberg.homeekg.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.google.firebase.auth.FirebaseUser;
import com.th_nuernberg.homeekg.R;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView register_login, forgot_login;
    private ImageView plus_login, facebook_login, google_login;
    private CircularProgressButton cirLoginButton_login;
    private ProgressBar progress_bar_login;
    private CheckBox remember_login;
    private EditText edit_Text_Mail_login, edit_Text_Password_login;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //for changing status bar icon colors
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_login);
        changeStatusBarColor();

        register_login = (TextView) findViewById(R.id.textViewRegisterReset);
        register_login.setOnClickListener(this);

        forgot_login = (TextView) findViewById(R.id.TextViewForgotLogin);
        forgot_login.setOnClickListener(this);

        plus_login = (ImageView) findViewById(R.id.imageViewPlusLogin);
        plus_login.setOnClickListener(this);

        facebook_login = (ImageView) findViewById(R.id.imageViewFacebookReset);
        facebook_login.setOnClickListener(this);

        google_login  = (ImageView) findViewById(R.id.imageViewGoogleReset);
        google_login.setOnClickListener(this);

        cirLoginButton_login = (CircularProgressButton) findViewById(R.id.cirLoginButtonReset);
        cirLoginButton_login.setOnClickListener(this);

        progress_bar_login = (ProgressBar) findViewById(R.id.progressBarLogin);
        progress_bar_login.setOnClickListener(this);

        edit_Text_Mail_login = (EditText) findViewById(R.id.editTextEmailLogin);
        edit_Text_Mail_login.setOnClickListener(this);

        edit_Text_Password_login = (EditText) findViewById(R.id.editTextPasswordLogin);
        edit_Text_Password_login.setOnClickListener(this);

        //Auto Login
        mAuth = FirebaseAuth.getInstance();
        remember_login = (CheckBox) findViewById(R.id.checkBox_login);

        //Auto Login
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox =  preferences.getString("remember", "");

        if(checkbox.equals("true") && (mAuth.getCurrentUser() != null)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please sign in!", Toast.LENGTH_SHORT).show();
        }
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
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.textViewRegisterReset:
            case R.id.imageViewPlusLogin:
                startActivity(new Intent(this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                break;
            case R.id.cirLoginButtonReset:
                userLogin();
                break;
            case R.id.TextViewForgotLogin:
                startActivity(new Intent(LoginActivity.this, ResetActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                break;
            case R.id.imageViewFacebookReset:
                // TO DO
                break;
            case R.id.imageViewGoogleReset:
                // TO DO
                break;
        }
    }

    private void userLogin() {
        String mail_login = edit_Text_Mail_login.getText().toString().trim();
        String password_login = edit_Text_Password_login.getText().toString().trim();

        if(mail_login.isEmpty()) {
            edit_Text_Mail_login.setError("E-Mail is required!");
            edit_Text_Mail_login.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(mail_login).matches()) {
            edit_Text_Mail_login.setError("Please enter a valid E-Mail address!");
            edit_Text_Mail_login.requestFocus();
            return;
        }

        if(password_login.isEmpty()) {
            edit_Text_Password_login.setError("Password is required!");
            edit_Text_Password_login.requestFocus();
            return;
        }

        if(password_login.length() < 6) {
            edit_Text_Password_login.setError("Minimal password length is 6 characters!");
            edit_Text_Password_login.requestFocus();
            return;
        }

        progress_bar_login.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(mail_login, password_login).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if(task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()) {
                        if(remember_login.isChecked()) {
                            editor.putString("remember", "true");
                            editor.apply();
                            Toast.makeText(LoginActivity.this, "Auto Login checked", Toast.LENGTH_SHORT).show();
                        } else {
                            editor.putString("remember", "false");
                            editor.apply();
                            Toast.makeText(LoginActivity.this, "Auto Login unchecked", Toast.LENGTH_SHORT).show();
                        }
                        //Redirect to user profile
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                        finish();
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(LoginActivity.this, "Check your E-Mail to verify your account!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to login! Please check your credentials!", Toast.LENGTH_LONG).show();
                    progress_bar_login.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}