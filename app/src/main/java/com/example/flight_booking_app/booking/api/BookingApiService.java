package com.example.flight_booking_app.booking.api;

import com.example.flight_booking_app.booking.model.AncillaryItem;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.BookingResult;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface định nghĩa các cổng giao tiếp (Endpoints) với Backend.
 * Mỗi hàm ở đây tương ứng với một tính năng mà người dùng thực hiện trên App.
 */
public interface BookingApiService {

    /**
     * Lấy thông tin chi tiết của một chuyến bay cụ thể.
     * @param flightId: Mã định danh chuyến bay (VD: "VN123")
     * URL thực tế: GET /api/flights/{flightId}
     */
    @GET("flights/{id}")
    Call<ApiResponse<FlightDetail>> getFlightDetail(@Path("id") String flightId);

    /**
     * Gửi yêu cầu đặt vé lên hệ thống.
     * @param request: Đối tượng chứa thông tin hành khách, chuyến bay đã chọn.
     * @return Trả về kết quả đặt vé bao gồm mã đặt chỗ (PNR).
     */
    @POST("bookings")
    Call<ApiResponse<BookingResult>> createBooking(@Body BookingRequest request);

    /**
     * Lấy danh sách các dịch vụ bổ trợ (Suất ăn, hành lý, bảo hiểm...).
     * Trả về một List (danh sách) các đối tượng AncillaryItem.
     */
    @GET("ancillary-catalogs")
    Call<ApiResponse<List<AncillaryItem>>> fetchAncillaryCatalog();

    /**
     * Tạo đường dẫn thanh toán trực tuyến (Ví dụ: VNPay, Momo).
     * @param bookingId: Mã đặt chỗ vừa tạo ở bước trên.
     * @param platform: Định danh nền tảng (thường là "android") để Backend xử lý callback.
     * @return Một chuỗi String (URL) để App mở Webview hoặc trình duyệt cho người dùng thanh toán.
     */
    @GET("payments/create-url")
    Call<ApiResponse<String>> generatePaymentUrl(
            @Query("bookingId") String bookingId,
            @Query("platform") String platform
    );
    @GET("payments/verify-status/{pnrCode}")
    Call<ApiResponse<Map<String, Object>>> verifyPaymentStatus(@Path("pnrCode") String pnrCode);
}