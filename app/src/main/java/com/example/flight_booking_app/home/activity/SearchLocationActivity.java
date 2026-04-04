package com.example.flight_booking_app.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.home.adapter.LocationAdapter;
import com.example.flight_booking_app.home.model.AirportTranslation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchLocationActivity extends AppCompatActivity {

    private LocationAdapter adapter;
    private List<AirportTranslation> allAirports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        ImageButton btnBack = findViewById(R.id.btnBack);
        EditText edtSearch = findViewById(R.id.edtSearchLocation);
        RecyclerView rvLocations = findViewById(R.id.rvLocations);

        // Nút Đóng: Hủy bỏ tìm kiếm và quay về
        btnBack.setOnClickListener(v -> finish());

        // 1. Đọc dữ liệu JSON
        Map<String, AirportTranslation> airportMap = loadAirportMapping();
        allAirports = new ArrayList<>(airportMap.values());

        // 2. Cài đặt RecyclerView
        rvLocations.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LocationAdapter(allAirports, selectedAirport -> {
            // KHI BẤM VÀO 1 SÂN BAY: Đóng gói mã sân bay và gửi ngược về HomeActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SELECTED_CODE", selectedAirport.getCode());
            setResult(RESULT_OK, resultIntent);
            finish(); // Đóng màn hình này
        });
        rvLocations.setAdapter(adapter);

        // 3. Logic tìm kiếm (TextWatcher)
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

    // Hàm đọc JSON (Đã dọn từ HomeActivity sang đây)
    private Map<String, AirportTranslation> loadAirportMapping() {
        Map<String, AirportTranslation> mapping = new HashMap<>();
        try {
            InputStream is = getAssets().open("airport_mapping.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            java.lang.reflect.Type type = new TypeToken<Map<String, AirportTranslation>>(){}.getType();
            mapping = new Gson().fromJson(json, type);
            for (Map.Entry<String, AirportTranslation> entry : mapping.entrySet()) {
                entry.getValue().setCode(entry.getKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapping;
    }
}