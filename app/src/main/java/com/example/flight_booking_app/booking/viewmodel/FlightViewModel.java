package com.example.flight_booking_app.booking.viewmodel; // Đổi package cho khớp

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.repository.FlightRepository;

public class FlightViewModel extends AndroidViewModel {

    private FlightRepository repository;

    public FlightViewModel(@NonNull Application application) {
        super(application);
        repository = new FlightRepository(application);
    }

    // Activity sẽ gọi hàm này để "quan sát" dữ liệu
    public LiveData<FlightDetail> getFlightDetailLiveData(String flightId) {
        return repository.getFlightDetail(flightId);
    }
}