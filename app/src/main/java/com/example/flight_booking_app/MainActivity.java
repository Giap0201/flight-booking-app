package com.example.flight_booking_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.flight_booking_app.ticket.activity.MyTicketFragment;
import com.example.flight_booking_app.user.activity.fragment_profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.flight_booking_app.home.activity.HomeFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Mặc định hiển thị trang Home khi vừa mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_home); // ID này phải khớp với trong bottom_nav_menu.xml
        }

        // Lắng nghe sự kiện chuyển tab
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_tickets) {
                selectedFragment = new MyTicketFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new fragment_profile();
            }



            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}