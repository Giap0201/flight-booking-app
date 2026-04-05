package com.example.flight_booking_app.booking.api;

import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.booking.model.BookingSummary;
import com.example.flight_booking_app.booking.model.PageResult;
import com.example.flight_booking_app.booking.response.client.BookingDetailResponse;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookingApiService {

    @GET("flights/{id}")
    Call<ApiResponse<FlightDetail>> getFlightDetail(@Path("id") String flightId);

    // Dùng cho trang My Ticket (Lấy tất cả)
    @GET("bookings/my-bookings")
    Call<ApiResponse<PageResult<BookingSummary>>> getMyBookings(
            @Header("Authorization") String token,
            @Query("page") int page
    );

    // ĐÃ THÊM HÀM NÀY DÀNH RIÊNG CHO CÁC TRANG DANH SÁCH LỌC (Waiting, Upcoming...)
    @GET("bookings/my-bookings")
    Call<ApiResponse<PageResult<BookingSummary>>> getMyBookingsWithFilter(
            @Header("Authorization") String token,
            @Query("filter") String filter,
            @Query("page") int page
    );

    @GET("bookings/{id}")
    Call<ApiResponse<BookingDetailResponse>> getBookingById(
            @Header("Authorization") String token,
            @Path("id") UUID id
    );
}