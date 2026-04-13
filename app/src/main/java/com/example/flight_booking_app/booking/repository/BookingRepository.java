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
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * REPOSITORY (Kho dữ liệu):
 * Lớp này đóng vai trò là "Người vận chuyển".
 */
public class BookingRepository {

    private BookingApiService apiService;

    public BookingRepository(Application application) {
        apiService = ApiClient.getClient(application).create(BookingApiService.class);
    }

    /**
     * HÀM 1: LẤY CHI TIẾT CHUYẾN BAY
     */
    public MutableLiveData<FlightDetail> getFlightDetail(String flightId) {
        MutableLiveData<FlightDetail> flightData = new MutableLiveData<>();
        apiService.getFlightDetail(flightId).enqueue(new Callback<ApiResponse<FlightDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<FlightDetail>> call, Response<ApiResponse<FlightDetail>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    flightData.setValue(response.body().getResult());
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

    /**
     * HÀM 2: GỬI YÊU CẦU ĐẶT VÉ (Booking)
     */
    public MutableLiveData<BookingResult> createBooking(BookingRequest request) {
        MutableLiveData<BookingResult> bookingData = new MutableLiveData<>();
        apiService.createBooking(request).enqueue(new Callback<ApiResponse<BookingResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingResult>> call, Response<ApiResponse<BookingResult>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    bookingData.setValue(response.body().getResult());
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
     * HÀM 3: LẤY DANH SÁCH DỊCH VỤ
     */
    public MutableLiveData<List<AncillaryItem>> getAncillaries() {
        MutableLiveData<List<AncillaryItem>> ancillaryData = new MutableLiveData<>();
        apiService.fetchAncillaryCatalog().enqueue(new Callback<ApiResponse<List<AncillaryItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AncillaryItem>>> call, Response<ApiResponse<List<AncillaryItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
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
     */
    public MutableLiveData<String> createPaymentUrl(String bookingId, String platform) {
        MutableLiveData<String> paymentUrlLiveData = new MutableLiveData<>();
        apiService.generatePaymentUrl(bookingId, platform).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
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

    /**
     * ⚡ HÀM 5 (MỚI THÊM): KIỂM TRA TRẠNG THÁI THANH TOÁN QUA PNR
     * Dùng để xác nhận xem tiền đã trừ chưa khi trang kết quả báo lỗi.
     */
    public MutableLiveData<Map<String, Object>> verifyPaymentStatus(String pnrCode) {
        MutableLiveData<Map<String, Object>> verifyData = new MutableLiveData<>();

        // Gọi API lên Backend kiểm tra trạng thái thực tế của đơn hàng
        apiService.verifyPaymentStatus(pnrCode).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                // Backend trả về mã 1000 nghĩa là đã xử lý kiểm tra xong (dù thành công hay thất bại)
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    // Trả về Map chứa {status: "SUCCESS", message: "..."}
                    verifyData.setValue(response.body().getResult());
                } else {
                    verifyData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                // Lỗi kết nối mạng
                verifyData.setValue(null);
            }
        });

        return verifyData;
    }
}