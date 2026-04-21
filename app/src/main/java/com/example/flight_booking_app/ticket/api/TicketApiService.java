package com.example.flight_booking_app.ticket.api;

import com.example.flight_booking_app.ticket.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.ticket.model.BookingSummary;
import com.example.flight_booking_app.ticket.model.PageResult;
import com.example.flight_booking_app.network.ApiClient;

// IMPORT CHUẨN TỪ PACKAGE BẠN VỪA BÁO
import com.example.flight_booking_app.ticket.response.client.BookingDetailResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TicketApiService {

    @GET("flights/{id}")
    Call<ApiResponse<FlightDetail>> getFlightDetail(@Path("id") String flightId);

    // Dùng cho trang My Ticket (Lấy tất cả)
    @GET("bookings/my-bookings")
    Call<ApiResponse<PageResult<BookingSummary>>> getMyBookings(
            @Query("filter") String filter,
            @Query("page") int page,
            @Query("size") int size
    );

    // Dành riêng cho các trang danh sách lọc (Waiting, Upcoming...)
    @GET("bookings/my-bookings")
    Call<ApiResponse<PageResult<BookingSummary>>> getMyBookingsWithFilter(
            @Query("filter") String filter,
            @Query("page") int page
    );

    // [QUAN TRỌNG] Đã sửa tham số UUID thành String để nhận dữ liệu từ Intent mượt mà nhất
    @GET("bookings/{id}")
    Call<ApiResponse<BookingDetailResponse>> getBookingById(
            @Path("id") String id
    );

    /**
     * Tạo đường dẫn thanh toán VNPay cho booking chưa thanh toán.
     * Dùng trong BookingDetailActivity khi người dùng nhấn "Thanh toán".
     * @param bookingId Mã booking (UUID)
     * @param platform  "android" để Backend cấu hình callback URL
     * @return URL thanh toán VNPay
     */
    @GET("payments/create-url")
    Call<ApiResponse<String>> generatePaymentUrl(
            @Query("bookingId") String bookingId,
            @Query("platform") String platform
    );
}