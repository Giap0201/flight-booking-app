package com.example.flight_booking_app.booking.repository; // Đổi package cho khớp

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.booking.api.BookingApiService;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightRepository {
    private BookingApiService apiService;

    public FlightRepository(Application application) {
// CÁCH GỌI MỚI: Mượn ApiClient dùng chung để tạo ra Service riêng của Booking
        apiService = ApiClient.getClient(application).create(BookingApiService.class);
    }

    // Hàm này trả về một LiveData chứa FlightDetail
    public MutableLiveData<FlightDetail> getFlightDetail(String flightId) {
        MutableLiveData<FlightDetail> flightData = new MutableLiveData<>();

        apiService.getFlightDetail(flightId).enqueue(new Callback<ApiResponse<FlightDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<FlightDetail>> call, Response<ApiResponse<FlightDetail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getCode() == 1000) { // Code = 0 là thành công
                        // Đẩy dữ liệu lấy được vào LiveData
                        flightData.setValue(response.body().getResult());
                    } else {
                        flightData.setValue(null);
                    }
                } else {
                    flightData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FlightDetail>> call, Throwable t) {
                flightData.setValue(null);
            }
        });

        return flightData;
    }
}