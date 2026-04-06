package com.example.flight_booking_app.booking.api;

import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.booking.model.BookingSummary;
import com.example.flight_booking_app.booking.model.PageResult;

// IMPORT CHUẨN TỪ PACKAGE BẠN VỪA BÁO
import com.example.flight_booking_app.booking.response.client.BookingDetailResponse;

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
            @Query("filter") String filter,
            @Query("page") int page,
            @Query("size") int size
    );

    // Dành riêng cho các trang danh sách lọc (Waiting, Upcoming...)
    @GET("bookings/my-bookings")
    Call<ApiResponse<PageResult<BookingSummary>>> getMyBookingsWithFilter(
            @Header("Authorization") String token,
            @Query("filter") String filter,
            @Query("page") int page
    );

    // [QUAN TRỌNG] Đã sửa tham số UUID thành String để nhận dữ liệu từ Intent mượt mà nhất
    @GET("bookings/{id}")
    Call<ApiResponse<BookingDetailResponse>> getBookingById(
            @Header("Authorization") String token,
            @Path("id") String id
    );
}