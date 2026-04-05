package com.example.flight_booking_app.home.repository;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.home.api.HomeApiService;
import com.example.flight_booking_app.home.model.FlightPageResponse;
import com.example.flight_booking_app.home.model.SearchRequest;
import com.example.flight_booking_app.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightRepository {
    private HomeApiService apiService;

    public FlightRepository(Application application) {
        // Khởi tạo Retrofit từ file ApiClient dùng chung của bạn
        apiService = ApiClient.getClient(application).create(HomeApiService.class);
    }

    public LiveData<FlightPageResponse> searchFlights(SearchRequest request) {
        MutableLiveData<FlightPageResponse> flightData = new MutableLiveData<>();

        apiService.searchFlights(request).enqueue(new Callback<ApiResponse<FlightPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FlightPageResponse>> call, Response<ApiResponse<FlightPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<FlightPageResponse> apiResponse = response.body();

                    // SỬA SỐ 200 THÀNH 1000 Ở DÒNG NÀY
                    if (apiResponse.getCode() == 1000) {
                        flightData.setValue(apiResponse.getResult());
                    } else {
                        Log.e("Repository", "Lỗi logic từ server: " + apiResponse.getMessage());
                        flightData.setValue(null);
                    }
                } else {
                    Log.e("Repository", "Lỗi HTTP: " + response.code());
                    flightData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FlightPageResponse>> call, Throwable t) {
                Log.e("Repository", "Lỗi mạng: " + t.getMessage());
                flightData.setValue(null);
            }
        });

        return flightData;
    }
}