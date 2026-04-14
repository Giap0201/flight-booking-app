package com.example.flight_booking_app.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.home.adapter.LocationAdapter;
import com.example.flight_booking_app.home.model.AirportTranslation;
import com.example.flight_booking_app.home.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchLocationActivity extends AppCompatActivity {

    private LocationAdapter adapter;
    private List<AirportTranslation> allAirports = new ArrayList<>();
    private HomeViewModel homeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        ImageButton btnBack = findViewById(R.id.btnBack);
        EditText edtSearch = findViewById(R.id.edtSearchLocation);
        RecyclerView rvLocations = findViewById(R.id.rvLocations);

        btnBack.setOnClickListener(v -> finish());

        // 1. Cài đặt RecyclerView rỗng
        rvLocations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationAdapter(allAirports, selectedAirport -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SELECTED_CODE", selectedAirport.getCode());
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        rvLocations.setAdapter(adapter);

        // ====================================================
        // 2. KHỞI TẠO VIEW_MODEL VÀ LẮNG NGHE DỮ LIỆU
        // ====================================================
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getAirports().observe(this, airportTranslations -> {
            if (airportTranslations != null) {
                allAirports.clear();
                allAirports.addAll(airportTranslations);
                adapter.setFilter(allAirports); // Cập nhật danh sách
            } else {
                Toast.makeText(this, "Không lấy được dữ liệu sân bay", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Logic tìm kiếm (Tìm ngay trên RAM, không gọi lại API)
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<AirportTranslation> filteredList = new ArrayList<>();
                for (AirportTranslation airport : allAirports) {
                    if ((airport.getCode() != null && airport.getCode().toLowerCase().contains(query)) ||
                            (airport.getName() != null && airport.getName().toLowerCase().contains(query)) ||
                            (airport.getCity() != null && airport.getCity().toLowerCase().contains(query))) {
                        filteredList.add(airport);
                    }
                }
                adapter.setFilter(filteredList);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}