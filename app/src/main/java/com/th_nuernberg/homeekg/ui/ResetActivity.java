package com.th_nuernberg.homeekg.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.th_nuernberg.homeekg.R;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class ResetActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView registerForgot;
    private CircularProgressButton cirLoginButton_forgot;
    private EditText edit_Text_Mail_reset;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //for changing status bar icon colors
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_reset);
        changeStatusBarColor();

        registerForgot = (TextView) findViewById(R.id.textViewRegisterReset);
        registerForgot.setOnClickListener(this);

        cirLoginButton_forgot = (CircularProgressButton) findViewById(R.id.cirLoginButtonReset);
        cirLoginButton_forgot.setOnClickListener(this);

        edit_Text_Mail_reset = (EditText) findViewById(R.id.editTextEmailReset);
        edit_Text_Mail_reset.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();

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
        Intent intent = new Intent(ResetActivity.this, LoginActivity.class)
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
            case R.id.textViewRegisterReset:
                startActivity(new Intent(this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
                break;
            case R.id.cirLoginButtonReset:
                resetPassword();
                break;
            case R.id.imageViewFacebookReset:
                // TO DO
                break;
            case R.id.imageViewGoogleReset:
                // TO DO
                break;
        }
    }

    private void resetPassword() {
        String mail = edit_Text_Mail_reset.getText().toString().trim();
        if(mail.isEmpty()) {
            edit_Text_Mail_reset.setError("Email is required!");
            edit_Text_Mail_reset.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            edit_Text_Mail_reset.setError("Please provide valid email!");
            return;
        }
    auth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(task.isSuccessful()) {
                Toast.makeText(ResetActivity.this, "Check your email to reset your password!", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(ResetActivity.this, "Try again! Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    });
    }
}
