package com.example.flight_booking_app.booking.repository; // Đổi package cho khớp

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.booking.api.BookingApiService;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.response.client.BookingDetailResponse;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.common.AppConfig;
import com.example.flight_booking_app.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightRepository {
    private static final String TAG = "FlightRepository";
    private BookingApiService apiService;

    public FlightRepository() {
// CÁCH GỌI MỚI: Mượn ApiClient dùng chung để tạo ra Service riêng của Booking
        apiService = ApiClient.getClient().create(BookingApiService.class);
    }

    // Hàm này trả về một LiveData chứa FlightDetail
    public MutableLiveData<FlightDetail> getFlightDetail(String flightId) {
        MutableLiveData<FlightDetail> flightData = new MutableLiveData<>();

        apiService.getFlightDetail(flightId).enqueue(new Callback<ApiResponse<FlightDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<FlightDetail>> call, Response<ApiResponse<FlightDetail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getCode() == 1000) { // Code = 1000 là thành công
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

    // =====================================================================================
    // [MỚI] Lấy chi tiết booking theo ID — dùng cho BookingDetailActivity
    // Pattern hoàn toàn giống getFlightDetail(): enqueue → check code 1000 → đẩy vào LiveData
    // =====================================================================================
    public MutableLiveData<BookingDetailResponse> getBookingDetail(String bookingId) {
        MutableLiveData<BookingDetailResponse> bookingData = new MutableLiveData<>();

        apiService.getBookingById(AppConfig.TOKEN, bookingId)
                .enqueue(new Callback<ApiResponse<BookingDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDetailResponse>> call,
                                   Response<ApiResponse<BookingDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getCode() == 1000) {
                        // Thành công — đẩy BookingDetailResponse vào LiveData
                        bookingData.setValue(response.body().getResult());
                    } else {
                        Log.w(TAG, "API trả code lỗi: " + response.body().getCode()
                                + " — " + response.body().getMessage());
                        bookingData.setValue(null);
                    }
                } else {
                    Log.w(TAG, "Response không thành công: " + response.code());
                    bookingData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDetailResponse>> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi gọi getBookingDetail: " + t.getMessage());
                bookingData.setValue(null);
            }
        });

        return bookingData;
    }
}