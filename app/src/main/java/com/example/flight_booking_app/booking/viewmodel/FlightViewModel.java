package com.example.flight_booking_app.booking.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.booking.model.AncillaryItem; // Nhớ import class này
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.BookingResult;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.repository.FlightRepository;

import java.util.List;

public class FlightViewModel extends ViewModel {

    private FlightRepository repository;

    public FlightViewModel() {
        repository = new FlightRepository();
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
}