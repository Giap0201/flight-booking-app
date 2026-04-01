package com.example.flight_booking_app.booking.api;

import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

// Đổi tên interface
public interface BookingApiService {

    @GET("flights/{id}")
    Call<ApiResponse<FlightDetail>> getFlightDetail(@Path("id") String flightId);

    // Mấy hôm nữa bạn viết API POST /bookings vào đây là quá chuẩn!
}