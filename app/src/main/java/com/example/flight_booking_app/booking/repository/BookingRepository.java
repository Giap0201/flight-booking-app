package com.example.flight_booking_app.booking.repository; // Đổi package cho khớp

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

public class BookingRepository {
    private BookingApiService apiService;

    public BookingRepository() {
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
                    if (response.body().getCode() == 1000) { // Code = 0 là thành công
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

    // --- THÊM HÀM MỚI NÀY VÀO BÊN DƯỚI HÀM CŨ ---

    /**
     * Hàm này nhận vào một Thùng hàng (BookingRequest) và trả về một cái Hộp chứa Biên lai (LiveData)
     */
    public MutableLiveData<BookingResult> createBooking(BookingRequest request) {

        // 1. Tạo một cái "Hộp rỗng" (LiveData) để chuẩn bị hứng kết quả.
        // Giao diện (Activity) lát nữa sẽ "ngồi canh" cái hộp này. Hễ có dữ liệu rớt vào là nó chộp lấy ngay.
        MutableLiveData<BookingResult> bookingData = new MutableLiveData<>();

        // 2. Giao Thùng hàng cho Shipper (apiService) mang đi gửi lên đường dẫn POST /bookings
        // Lệnh .enqueue() nghĩa là "Cứ chạy ngầm đi nhé, đừng làm đơ màn hình của người dùng"
        apiService.createBooking(request).enqueue(new Callback<ApiResponse<BookingResult>>() {

            // Hàm này tự động chạy khi Server NHẬN ĐƯỢC thư và TRẢ LỜI lại
            @Override
            public void onResponse(Call<ApiResponse<BookingResult>> call, Response<ApiResponse<BookingResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Kiểm tra xem Server có chốt đơn thành công không (Code 1000)
                    if (response.body().getCode() == 1000) {
                        // THÀNH CÔNG! Lấy cái Biên lai (Result) bỏ vào Hộp LiveData
                        bookingData.setValue(response.body().getResult());
                    } else {
                        // Lỗi logic từ Backend (Ví dụ: Chuyến bay đã hết chỗ)
                        bookingData.setValue(null);
                    }
                } else {
                    // Lỗi Server (Ví dụ: Server sập, lỗi 500, lỗi 404...)
                    bookingData.setValue(null);
                }
            }

            // Hàm này tự động chạy khi GỬI THẤT BẠI (Ví dụ: Người dùng bị rớt mạng 4G/Wifi)
            @Override
            public void onFailure(Call<ApiResponse<BookingResult>> call, Throwable t) {
                bookingData.setValue(null);
            }
        });

        // 3. Trả cái Hộp (lúc này có thể vẫn đang rỗng vì mạng chưa chạy xong) về cho ViewModel.
        // Khi nào mạng chạy xong, đoạn code ở trên sẽ tự động nhét dữ liệu vào Hộp sau!
        return bookingData;
    }

    // --- THÊM HÀM LẤY DANH SÁCH DỊCH VỤ VÀO ĐÂY ---

    /**
     * Hàm này gọi API GET để lấy danh sách các dịch vụ bổ sung (Hành lý, Suất ăn, Chỗ ngồi)
     */
    public MutableLiveData<List<AncillaryItem>> getAncillaries() {

        // 1. Tạo "Hộp rỗng" để chuẩn bị hứng danh sách Dịch vụ từ Server
        MutableLiveData<List<AncillaryItem>> ancillaryData = new MutableLiveData<>();

        // 2. Yêu cầu Shipper (apiService) chạy đi lấy danh sách dịch vụ
        // ApiResponse<List<AncillaryItem>> nghĩa là gói hàng trả về chứa một danh sách các món đồ
        apiService.getAncillaries().enqueue(new Callback<ApiResponse<List<AncillaryItem>>>() {

            @Override
            public void onResponse(Call<ApiResponse<List<AncillaryItem>>> call, Response<ApiResponse<List<AncillaryItem>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Nếu code = 1000 (Thành công) theo chuẩn API của bạn
                    if (response.body().getCode() == 1000) {
                        // Nhét danh sách lấy được vào Hộp LiveData
                        ancillaryData.setValue(response.body().getResult());
                    } else {
                        ancillaryData.setValue(null);
                    }
                } else {
                    ancillaryData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AncillaryItem>>> call, Throwable t) {
                // Lỗi mạng hoặc lỗi kết nối
                ancillaryData.setValue(null);
            }
        });

        // 3. Trả hộp về cho ViewModel "ngồi canh"
        return ancillaryData;
    }

    // THÊM HÀM NÀY VÀO TRONG REPOSITORY
    public MutableLiveData<String> createPaymentUrl(String bookingId, String platform) { // Thêm tham số platform
        MutableLiveData<String> paymentUrlLiveData = new MutableLiveData<>();

        // Truyền cả 2 tham số vào hàm của ApiService
        apiService.createPaymentUrl(bookingId, platform).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                // Nếu kết nối thành công và backend trả về code HTTP 200
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();

                    // Kiểm tra xem code có bằng 0 (Thành công) như định dạng JSON của bạn không
                    if (apiResponse.getCode() == 1000) {
                        // Lấy URL từ trường "result" và truyền lên UI
                        paymentUrlLiveData.setValue(apiResponse.getResult());
                    } else {
                        // Nếu có lỗi logic từ backend
                        paymentUrlLiveData.setValue(null);
                    }
                } else {
                    paymentUrlLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // Nếu rớt mạng hoặc backend sập
                paymentUrlLiveData.setValue(null);
            }
        });

        return paymentUrlLiveData;
    }
}