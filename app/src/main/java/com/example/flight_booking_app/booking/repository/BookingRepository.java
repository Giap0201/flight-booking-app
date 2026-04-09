package com.example.flight_booking_app.booking.repository;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.booking.api.BookingApiService;
import com.example.flight_booking_app.booking.model.AncillaryItem;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.BookingResult;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * REPOSITORY (Kho dữ liệu):
 * Lớp này đóng vai trò là "Người vận chuyển".
 * Nhiệm vụ duy nhất: Đi lấy dữ liệu từ Server về và đưa cho App dùng.
 */
public class BookingRepository {

    // apiService là cái "điện thoại" để gọi lên Server.
    private BookingApiService apiService;

    // Hàm khởi tạo (Constructor): Chạy ngay khi lớp này được tạo ra
    public BookingRepository(Application application) {
        // ApiClient.getClient: Mở đường dây kết nối Internet
        // .create(...): Tạo ra bộ công cụ để gọi các hàm trong BookingApiService
        apiService = ApiClient.getClient(application).create(BookingApiService.class);
    }

    /**
     * HÀM 1: LẤY CHI TIẾT CHUYẾN BAY
     * Trả về: Một cái "thùng" MutableLiveData, bên trong chứa thông tin chuyến bay.
     */
    public MutableLiveData<FlightDetail> getFlightDetail(String flightId) {
        // Tạo một cái "thùng" rỗng để chuẩn bị đựng dữ liệu
        MutableLiveData<FlightDetail> flightData = new MutableLiveData<>();

        // Nhấc máy gọi API: "Lấy cho tôi chuyến bay có ID này"
        // .enqueue: Nghĩa là gọi chạy ngầm (không làm đơ màn hình điện thoại)
        apiService.getFlightDetail(flightId).enqueue(new Callback<ApiResponse<FlightDetail>>() {

            // Hàm này chạy khi Server có phản hồi về cho App
            @Override
            public void onResponse(Call<ApiResponse<FlightDetail>> call, Response<ApiResponse<FlightDetail>> response) {
                // Kiểm tra 1: Kết nối thành công (Lỗi 200) và có dữ liệu trả về
                if (response.isSuccessful() && response.body() != null) {

                    // Kiểm tra 2: Mã lỗi nghiệp vụ của Backend (Ví dụ: 1000 là thành công)
                    if (response.body().getCode() == 1000) {
                        // Lấy dữ liệu thật (Result) bỏ vào "thùng" flightData
                        // Khi dùng .setValue, giao diện (UI) sẽ tự động nhận được thông báo để hiển thị lên
                        flightData.setValue(response.body().getResult());
                    } else {
                        // Nếu mã không phải 1000 (Vd: ID sai), cho thùng bằng null
                        flightData.setValue(null);
                    }
                } else {
                    // Lỗi hệ thống hoặc lỗi mạng
                    flightData.setValue(null);
                }
            }

            // Hàm này chạy khi không thể kết nối tới Server (Mất mạng, đứt cáp...)
            @Override
            public void onFailure(Call<ApiResponse<FlightDetail>> call, Throwable t) {
                flightData.setValue(null);
            }
        });

        // Trả cái thùng về cho ViewModel. Lúc này thùng có thể đang rỗng,
        // nhưng khi mạng chạy xong, nó sẽ tự đầy dữ liệu vào sau.
        return flightData;
    }

    /**
     * HÀM 2: GỬI YÊU CẦU ĐẶT VÉ (Booking)
     * request: Là cái túi chứa thông tin khách hàng bạn muốn gửi lên.
     */
    public MutableLiveData<BookingResult> createBooking(BookingRequest request) {
        // Tạo thùng rỗng để chờ lấy "Biên lai đặt vé" (BookingResult)
        MutableLiveData<BookingResult> bookingData = new MutableLiveData<>();

        // Gửi túi thông tin lên Server qua phương thức POST
        apiService.createBooking(request).enqueue(new Callback<ApiResponse<BookingResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingResult>> call, Response<ApiResponse<BookingResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getCode() == 1000) {
                        // Đặt chỗ thành công, bỏ kết quả vào thùng
                        bookingData.setValue(response.body().getResult());
                    } else {
                        bookingData.setValue(null);
                    }
                } else {
                    bookingData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingResult>> call, Throwable t) {
                bookingData.setValue(null);
            }
        });

        return bookingData;
    }

    /**
     * HÀM 3: LẤY DANH SÁCH DỊCH VỤ (Hành lý, suất ăn...)
     */
    public MutableLiveData<List<AncillaryItem>> getAncillaries() {
        // Thùng này chứa một "Danh sách" (List) các món đồ dịch vụ
        MutableLiveData<List<AncillaryItem>> ancillaryData = new MutableLiveData<>();

        apiService.fetchAncillaryCatalog().enqueue(new Callback<ApiResponse<List<AncillaryItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AncillaryItem>>> call, Response<ApiResponse<List<AncillaryItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    // Đổ nguyên danh sách vào thùng
                    ancillaryData.setValue(response.body().getResult());
                } else {
                    ancillaryData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AncillaryItem>>> call, Throwable t) {
                ancillaryData.setValue(null);
            }
        });

        return ancillaryData;
    }

    /**
     * HÀM 4: TẠO ĐƯỜNG DẪN THANH TOÁN
     * Trả về một chuỗi ký tự (String) chính là link để mở web thanh toán.
     */
    public MutableLiveData<String> createPaymentUrl(String bookingId, String platform) {
        MutableLiveData<String> paymentUrlLiveData = new MutableLiveData<>();

        apiService.generatePaymentUrl(bookingId, platform).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    // Lấy cái link URL trả về từ Server
                    paymentUrlLiveData.setValue(response.body().getResult());
                } else {
                    paymentUrlLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                paymentUrlLiveData.setValue(null);
            }
        });

        return paymentUrlLiveData;
    }
}