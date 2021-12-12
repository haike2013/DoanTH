package com.subi.vpbike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plash);
        //Ẩn actionbar
        getSupportActionBar().hide();

        //Delay 2.5s vào màn hình chính
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    if (email.split("@")[1].equals("vpbike.com")||email.split("@")[1].equals("vpbike.vn")||email.split("@")[1].equals("vpbike.com.vn")) {
                        i = new Intent(SplashActivity.this, MainActivity.class);
                    } else {
                        i = new Intent(SplashActivity.this, MapsActivity.class);
                    }
                } else {
                    i = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(i);
                finish();
            }
        }, 2500);
    }
}