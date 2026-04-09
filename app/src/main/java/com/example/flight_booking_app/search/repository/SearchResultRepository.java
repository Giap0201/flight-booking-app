package com.example.flight_booking_app.search.repository;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;
import com.example.flight_booking_app.search.api.SearchApiService;
import com.example.flight_booking_app.search.model.CheapestDate;
import com.example.flight_booking_app.search.model.FlightPageResponse;
import com.example.flight_booking_app.search.model.SearchRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultRepository {
    private final SearchApiService apiService;

    public SearchResultRepository(Application application) {
        // Khởi tạo Retrofit service thông qua ApiClient có sẵn của dự án
        apiService = ApiClient.getClient(application).create(SearchApiService.class);
    }

    /**
     * Thực hiện gọi API tìm kiếm và trả về LiveData
     */
    /**
     * CẬP NHẬT: Nhận thêm sortBy và sortDir để gửi lên Backend
     */
    public LiveData<FlightPageResponse> searchFlights(SearchRequest request, String sortBy, String sortDir) {
        MutableLiveData<FlightPageResponse> flightData = new MutableLiveData<>();

        // Bước 1: Gọi API với size 100 để lấy đủ dữ liệu cho việc lọc tại Client
        apiService.searchFlights(request, 0, 100, sortBy, sortDir).enqueue(new Callback<ApiResponse<FlightPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FlightPageResponse>> call, Response<ApiResponse<FlightPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<FlightPageResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000) {
                        flightData.setValue(apiResponse.getResult());
                    } else {
                        flightData.setValue(null);
                    }
                } else {
                    flightData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FlightPageResponse>> call, Throwable t) {
                flightData.setValue(null);
            }
        });

        return flightData;
    }

    // Lấy dải ngày giá rẻ
    public LiveData<List<CheapestDate>> getCheapestDates(String origin, String dest, int year, int month) {
        MutableLiveData<List<CheapestDate>> data = new MutableLiveData<>();
        apiService.getCheapestDates(origin, dest, year, month).enqueue(new Callback<ApiResponse<List<CheapestDate>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CheapestDate>>> call, Response<ApiResponse<List<CheapestDate>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getResult());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CheapestDate>>> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    // Lấy danh sách hãng bay để hiển thị trong Filter BottomSheet
    public LiveData<List<String>> getAirlineNames() {
        MutableLiveData<List<String>> airlineNames = new MutableLiveData<>();
        apiService.getAirlines(100).enqueue(new Callback<ApiResponse<FlightPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FlightPageResponse>> call, Response<ApiResponse<FlightPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> names = new java.util.ArrayList<>();
                    // Giả định response chứa data là danh sách các object có trường airlineName
                    // Bạn có thể tùy chỉnh logic này khớp với DTO Airline của Backend
                    airlineNames.setValue(names);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<FlightPageResponse>> call, Throwable t) {
                airlineNames.setValue(null);
            }
        });
        return airlineNames;
    }
}