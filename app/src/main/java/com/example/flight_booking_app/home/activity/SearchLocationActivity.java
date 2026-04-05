package com.example.flight_booking_app.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.home.adapter.LocationAdapter;
import com.example.flight_booking_app.home.model.AirportPageData;
import com.example.flight_booking_app.home.model.AirportTranslation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.flight_booking_app.network.ApiClient;

// IMPORT file ApiClient của bạn vào đây
import com.example.flight_booking_app.home.api.HomeApiService;

public class SearchLocationActivity extends AppCompatActivity {

    private LocationAdapter adapter;
    private List<AirportTranslation> allAirports = new ArrayList<>(); // Chứa danh sách đã được "đắp" tiếng Việt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        ImageButton btnBack = findViewById(R.id.btnBack);
        EditText edtSearch = findViewById(R.id.edtSearchLocation);
        RecyclerView rvLocations = findViewById(R.id.rvLocations);

        btnBack.setOnClickListener(v -> finish());

        // 1. Cài đặt RecyclerView rỗng (chờ data API về)
        rvLocations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationAdapter(allAirports, selectedAirport -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SELECTED_CODE", selectedAirport.getCode());
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        rvLocations.setAdapter(adapter);

        // 2. GỌI HÀM KẾT HỢP DỮ LIỆU
        fetchAndMergeAirports();

        // 3. Logic tìm kiếm giữ nguyên
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

    // --- HÀM CỐT LÕI: GỌI API VÀ GHÉP DATA TIẾNG VIỆT ---
    private void fetchAndMergeAirports() {
        // Đọc file JSON từ assets
        Map<String, AirportTranslation> translationMap = loadAirportMapping();

        Log.e("TEST_DATA", "Số lượng sân bay đọc từ JSON: " + translationMap.size());

        HomeApiService apiService = ApiClient.getClient(this).create(HomeApiService.class);

        // Gọi API với kiểu dữ liệu mới
        apiService.getAirports(1000).enqueue(new Callback<ApiResponse<AirportPageData>>() {
            @Override
            public void onResponse(Call<ApiResponse<AirportPageData>> call, Response<ApiResponse<AirportPageData>> response) {
                Log.e("TEST_DATA", "API Response Code: " + response.code());
                // Kiểm tra HTTP status 200 và ApiResponse.code = 1000
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {

                    // Rút trích mảng data nằm sâu bên trong result
                    List<AirportPageData.ApiAirport> apiList = response.body().getResult().getData();

                    Log.e("TEST_DATA", "Số lượng sân bay lấy từ API: " + apiList.size());

                    allAirports.clear();

                    // Vòng lặp đắp data tiếng Việt
                    for (AirportPageData.ApiAirport apiItem : apiList) {
                        String code = apiItem.getCode();

                        if (translationMap.containsKey(code)) {
                            AirportTranslation viData = translationMap.get(code);
                            viData.setCode(code);
                            allAirports.add(viData);
                        } else {
                            AirportTranslation enData = new AirportTranslation();
                            enData.setCode(code);
                            enData.setName(apiItem.getName());
                            enData.setCity(apiItem.getCityCode());
                            enData.setCountry(apiItem.getCountryCode());
                            allAirports.add(enData);
                        }
                    }

                    // Đẩy lên RecyclerView
                    adapter.setFilter(allAirports);

                } else {
                    Log.e("TEST_DATA", "API Lỗi Body: " + new Gson().toJson(response.body()));
                    Toast.makeText(SearchLocationActivity.this, "Lỗi API hoặc không có dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AirportPageData>> call, Throwable t) {
                Log.e("TEST_DATA", "Lỗi Mạng: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(SearchLocationActivity.this, "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm đọc JSON giữ nguyên
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapping;
    }
}