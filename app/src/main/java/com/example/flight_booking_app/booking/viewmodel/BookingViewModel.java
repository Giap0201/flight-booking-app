package com.example.flight_booking_app.booking.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.booking.model.AncillaryItem; // Nhớ import class này
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.BookingResult;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.repository.BookingRepository;

import java.util.List;

public class BookingViewModel extends AndroidViewModel {

    private BookingRepository repository;

    public BookingViewModel(Application application) {
        super(application);
        repository = new BookingRepository(application);
    }

    // Activity sẽ gọi hàm này để "quan sát" dữ liệu chi tiết chuyến bay
    public LiveData<FlightDetail> getFlightDetailLiveData(String flightId) {
        return repository.getFlightDetail(flightId);
    }

    // Hàm gọi API đặt vé
    public LiveData<BookingResult> createBooking(BookingRequest request) {
        return repository.createBooking(request);
    }

    // ====================================================================
    // THÊM MỚI: Hàm gọi API lấy danh sách Dịch vụ bổ sung (Hành lý, suất ăn)
    // ====================================================================
    public LiveData<List<AncillaryItem>> getAncillaries() {
        return repository.getAncillaries();
    }

    // Gọi API lấy Link thanh toán
    // Bạn nhớ thay ResponseDTO bằng Class model trả về thực tế của bạn nhé
    public LiveData<String> createPaymentUrl(String bookingId, String platform) {
        return repository.createPaymentUrl(bookingId, platform);
    }
}