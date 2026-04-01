package com.example.flight_booking_app.booking.viewmodel; // Đổi package cho khớp

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.repository.FlightRepository;

public class FlightViewModel extends ViewModel {

    private FlightRepository repository;

    public FlightViewModel() {
        repository = new FlightRepository();
    }

    // Activity sẽ gọi hàm này để "quan sát" dữ liệu
    public LiveData<FlightDetail> getFlightDetailLiveData(String flightId) {
        return repository.getFlightDetail(flightId);
    }
}