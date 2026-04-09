package com.example.flight_booking_app.search.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.common.PageResponse;
import com.example.flight_booking_app.network.ApiClient;
import com.example.flight_booking_app.search.api.SearchApiService;
import com.example.flight_booking_app.search.model.Airline;
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
        apiService = ApiClient.getClient(application).create(SearchApiService.class);
    }

    public LiveData<FlightPageResponse> searchFlights(SearchRequest request, String sortBy, String sortDir) {
        MutableLiveData<FlightPageResponse> flightData = new MutableLiveData<>();
        apiService.searchFlights(request, 0, 1000, sortBy, sortDir).enqueue(new Callback<ApiResponse<FlightPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FlightPageResponse>> call, Response<ApiResponse<FlightPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<FlightPageResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000) {
                        flightData.setValue(apiResponse.getResult());
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<FlightPageResponse>> call, Throwable t) {
                flightData.setValue(null);
            }
        });
        return flightData;
    }

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

    /**
     * FIX LỖI: Thêm hàm getAirlines() để SearchResultViewModel gọi được [cite: 34-36]
     * Sử dụng PageResponse<Airline> để hứng dữ liệu từ Backend
     */
    public LiveData<List<Airline>> getAirlines() {
        MutableLiveData<List<Airline>> data = new MutableLiveData<>();

        // Gọi API v1/airlines với size 1000 để lấy đủ danh sách cho bộ lọc
        apiService.getAirlines(1000).enqueue(new Callback<ApiResponse<PageResponse<Airline>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Airline>>> call,
                                   Response<ApiResponse<PageResponse<Airline>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Trích xuất list 'data' từ trong PageResponse [cite: 6, 34]
                    data.setValue(response.body().getResult().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Airline>>> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }
}