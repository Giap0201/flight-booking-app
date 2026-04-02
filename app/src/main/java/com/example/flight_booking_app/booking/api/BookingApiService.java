package com.example.flight_booking_app.booking.api;

import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.booking.model.BookingSummary;
import com.example.flight_booking_app.booking.model.PageResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header; // <-- BẮT BUỘC PHẢI IMPORT CÁI NÀY
import retrofit2.http.Path;

public interface BookingApiService {

    @GET("flights/{id}")
    Call<ApiResponse<FlightDetail>> getFlightDetail(@Path("id") String flightId);

    // Đã thêm @Header("Authorization") để gửi Token lên Backend
    @GET("bookings/my-bookings")
    Call<ApiResponse<PageResult<BookingSummary>>> getMyBookings(@Header("Authorization") String token);
}