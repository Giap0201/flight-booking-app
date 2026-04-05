package com.example.flight_booking_app.authen.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;


import com.example.flight_booking_app.MainActivity;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.authen.model.DTO.SessionManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Giao diện có logo app

        // Bỏ hết logic check SessionManager ở đây đi
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Luôn luôn vào màn hình chính
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}