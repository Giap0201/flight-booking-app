package com.example.flight_booking_app.home.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.flight_booking_app.home.model.FlightPageResponse;
import com.example.flight_booking_app.home.model.SearchRequest;
import com.example.flight_booking_app.home.repository.FlightRepository;

public class HomeViewModel extends AndroidViewModel {

    private FlightRepository repository;

    private final MutableLiveData<SearchRequest> searchInput = new MutableLiveData<>();
    private final LiveData<FlightPageResponse> searchResults;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new FlightRepository(application);

        searchResults = Transformations.switchMap(searchInput, request ->
                repository.searchFlights(request)
        );
    }

    // --- ĐÃ CẬP NHẬT: Thêm boolean isRoundTrip, String returnDate ---
    public void performSearch(String from, String to, String date, int passengers, boolean isRoundTrip, String returnDate) {

        // Cập nhật: Khởi tạo SearchRequest với đủ 6 tham số
        SearchRequest request = new SearchRequest(from, to, date, passengers, isRoundTrip, returnDate);

        searchInput.setValue(request);
    }

    public LiveData<FlightPageResponse> getSearchResults() {
        return searchResults;
    }
}