package com.th_nuernberg.homeekg.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.th_nuernberg.homeekg.R;

public class ResetActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView registerForgot;

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
        switch (v.getId()) {
            case R.id.textViewRegisterReset:
                startActivity(new Intent(this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
                break;
            case R.id.cirLoginButtonReset:
                // TO DO
                break;
            case R.id.imageViewFacebookReset:
                // TO DO
                break;
            case R.id.imageViewGoogleReset:
                // TO DO
                break;
        }
    }
}
