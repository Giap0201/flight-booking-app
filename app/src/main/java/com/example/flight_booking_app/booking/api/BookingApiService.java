package com.example.flight_booking_app.booking.api;

import com.example.flight_booking_app.booking.model.AncillaryItem;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.BookingResult;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Interface này chứa các đường dẫn API liên quan đến Đặt vé và Chuyến bay.
 * Retrofit sẽ đọc các Annotation (@GET, @POST) ở đây để tự động tạo code gọi mạng.
 */
public interface BookingApiService {

    // --- HÀM CŨ BẠN ĐÃ CÓ (Lấy thông tin chuyến bay) ---
    @GET("flights/{id}")
    Call<ApiResponse<FlightDetail>> getFlightDetail(@Path("id") String flightId);

    // --- HÀM MỚI THÊM VÀO CHO CHỨC NĂNG ĐẶT VÉ ---
    /**
     * @POST("bookings"): Báo cho Retrofit biết đây là phương thức POST,
     * nối vào đuôi BASE_URL (http://10.0.2.2:8080/api/bookings)
     * * @Body: Lệnh này cực kỳ quan trọng! Nó bảo Retrofit lấy cái hộp BookingRequest của mình,
     * tự động dịch (parse) nó thành 1 chuỗi JSON chuẩn chỉ như bạn thiết kế trên Swagger,
     * rồi nhét vào phần "Request Body" để gửi đi.
     * * Call<ApiResponse<BookingResult>>: Cục hứng dữ liệu trả về.
     * ApiResponse bọc bên ngoài (chứa code 1000), BookingResult nằm ở lõi (chứa mã PNR, tiền...).
     */
    @POST("bookings")
    Call<ApiResponse<BookingResult>> createBooking(@Body BookingRequest request);


    // ==========================================================
    // THÊM DÒNG NÀY: Gọi API lấy danh sách dịch vụ
    // Giả sử đường dẫn của bạn là GET /ancillaries (Bạn chỉnh lại cho đúng nhé)
    // ==========================================================
    @GET("ancillary-catalogs")
    Call<ApiResponse<List<AncillaryItem>>> getAncillaries();

}