package com.example.flight_booking_app.booking.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.flight_booking_app.booking.model.AncillaryItem;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.BookingResult;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.repository.BookingRepository;

import java.util.List;
import java.util.Map;

public class BookingViewModel extends AndroidViewModel {

    private BookingRepository repository;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        repository = new BookingRepository(application);
    }

    /**
     * Lấy thông tin chi tiết chuyến bay để hiển thị
     */
    public LiveData<FlightDetail> getFlightDetailLiveData(String flightId) {
        return repository.getFlightDetail(flightId);
    }

    /**
     * Bước 1: Gọi API tạo đơn hàng (Booking)
     */
    public LiveData<BookingResult> createBooking(BookingRequest request) {
        return repository.createBooking(request);
    }

    /**
     * Bước 2: Lấy danh sách Dịch vụ bổ sung (Hành lý, suất ăn...) từ API
     */
    public LiveData<List<AncillaryItem>> getAncillaries() {
        return repository.getAncillaries();
    }

    /**
     * Bước 3: Lấy Link thanh toán VNPay từ đơn hàng đã tạo
     * platform: "android" để Backend biết đường Deep Link quay lại app
     */
    public LiveData<String> createPaymentUrl(String bookingId, String platform) {
        return repository.createPaymentUrl(bookingId, platform);
    }

    // ====================================================================
    // ⚡ MỚI THÊM: Hàm gọi API Kiểm tra trạng thái thanh toán (Verify Status)
    // Dùng cho nút "Kiểm tra lại" khi thanh toán gặp sự cố
    // ====================================================================
    public LiveData<Map<String, Object>> verifyPaymentStatus(String pnrCode) {
        return repository.verifyPaymentStatus(pnrCode);
    }
}