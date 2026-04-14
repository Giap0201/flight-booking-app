package com.example.flight_booking_app.home.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.home.api.HomeApiService;
import com.example.flight_booking_app.home.model.AirportPageData;
import com.example.flight_booking_app.home.model.AirportTranslation;
import com.example.flight_booking_app.network.ApiClient;
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

public class HomeRepository {

    private HomeApiService apiService;
    private Context context;

    public HomeRepository(Context context) {
        this.context = context;
        // Khởi tạo Retrofit để gọi API
        this.apiService = ApiClient.getClient(context).create(HomeApiService.class);
    }

    public MutableLiveData<List<AirportTranslation>> fetchAndMergeAirports() {
        MutableLiveData<List<AirportTranslation>> liveData = new MutableLiveData<>();

        // Đọc từ điển tiếng Việt từ thư mục assets
        Map<String, AirportTranslation> translationMap = loadAirportMapping();

        // Gọi API lấy tối đa 1000 sân bay
        apiService.getAirports(1000).enqueue(new Callback<ApiResponse<AirportPageData>>() {
            @Override
            public void onResponse(Call<ApiResponse<AirportPageData>> call, Response<ApiResponse<AirportPageData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    List<AirportPageData.ApiAirport> apiList = response.body().getResult().getData();
                    List<AirportTranslation> mergedList = new ArrayList<>();

                    // Vòng lặp đắp tiếng Việt vào data trả về từ API
                    for (AirportPageData.ApiAirport apiItem : apiList) {
                        String code = apiItem.getCode();
                        if (translationMap.containsKey(code)) {
                            AirportTranslation viData = translationMap.get(code);
                            viData.setCode(code);
                            mergedList.add(viData);
                        } else {
                            AirportTranslation enData = new AirportTranslation();
                            enData.setCode(code);
                            enData.setName(apiItem.getName());
                            enData.setCity(apiItem.getCityCode());
                            enData.setCountry(apiItem.getCountryCode());
                            mergedList.add(enData);
                        }
                    }
                    liveData.setValue(mergedList); // Trả dữ liệu đã xào nấu xong về cho ViewModel
                } else {
                    liveData.setValue(null); // Báo lỗi
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AirportPageData>> call, Throwable t) {
                liveData.setValue(null); // Lỗi mạng
            }
        });

        return liveData;
    }

    // Hàm hỗ trợ đọc file JSON
    private Map<String, AirportTranslation> loadAirportMapping() {
        Map<String, AirportTranslation> mapping = new HashMap<>();
        try {
            InputStream is = context.getAssets().open("airport_mapping.json");
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