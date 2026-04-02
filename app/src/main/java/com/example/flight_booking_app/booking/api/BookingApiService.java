package com.example.flight_booking_app.booking.api; // Kiểm tra lại tên package

import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Interface này giống như một cái "Bản đồ" chứa các con đường nối từ App của bạn lên Server.
 */
public interface BookingApiService {

    // --- CÁI NÀY LÀ CODE CŨ BẠN ĐÃ LÀM (Lấy thông tin chuyến bay) ---
    @GET("flights/{id}")
    Call<ApiResponse<FlightDetail>> getFlightDetail(@Path("id") String flightId);


    // --- CODE MỚI: THÊM ĐOẠN NÀY VÀO ---

    /**
     * API tạo đơn đặt vé (Gửi dữ liệu lên Server)
     * * 1. @POST("bookings"):
     * Báo cho App biết đây là hành động GỬI dữ liệu đi (POST).
     * Retrofit sẽ tự động nối chữ "bookings" vào sau cái BASE_URL của bạn (thành .../api/bookings).
     * * 2. @Body BookingRequest request:
     * Chữ @Body này cực kỳ quan trọng. Nó bảo Retrofit là: "Ê, lấy cái Thùng Container (BookingRequest) này,
     * dịch nó sang tiếng JSON, rồi nhét nó vào trong Thân (Body) của bức thư gửi lên Server nhé".
     * * 3. Call<ApiResponse<BookingResult>>:
     * Đây là cái "Rổ" để hứng kết quả. Server sẽ trả về một gói ApiResponse (chứa code 1000),
     * và phần lõi bên trong (result) chính là cái BookingResult chứa Mã PNR của chúng ta!
     */
//    @POST("bookings")
//    Call<ApiResponse<BookingResult>> createBooking(@Body BookingRequest request);

}