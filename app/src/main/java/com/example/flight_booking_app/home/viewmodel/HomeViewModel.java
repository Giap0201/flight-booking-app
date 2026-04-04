package com.example.flight_booking_app.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.home.model.FlightPageResponse;
import com.example.flight_booking_app.home.model.SearchRequest;
import com.example.flight_booking_app.home.repository.FlightRepository;

public class HomeViewModel extends ViewModel {

    private FlightRepository repository;

    // Lưu trữ request người dùng nhập vào
    private final MutableLiveData<SearchRequest> searchInput = new MutableLiveData<>();

    // LiveData chứa kết quả trả về, tự động gọi Repository khi searchInput thay đổi
    private final LiveData<FlightPageResponse> searchResults;

    public HomeViewModel() {
        repository = new FlightRepository();

        // Cứ mỗi khi searchInput được set giá trị mới (khi user bấm nút tìm),
        // nó sẽ tự động kích hoạt hàm searchFlights trong Repository
        searchResults = Transformations.switchMap(searchInput, request ->
                repository.searchFlights(request)
        );
    }

    // Hàm UI sẽ gọi khi người dùng bấm nút "Tìm kiếm"
    public void performSearch(String from, String to, String date, int passengers) {
        SearchRequest request = new SearchRequest(from, to, date, passengers);
        searchInput.setValue(request);
    }

    // Hàm để UI (Activity/Fragment) quan sát lấy dữ liệu vẽ lên màn hình
    public LiveData<FlightPageResponse> getSearchResults() {
        return searchResults;
    }
}