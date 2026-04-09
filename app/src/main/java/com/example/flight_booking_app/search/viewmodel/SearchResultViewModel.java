package com.example.flight_booking_app.search.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.flight_booking_app.search.model.Airline;
import com.example.flight_booking_app.search.model.CheapestDate;
import com.example.flight_booking_app.search.model.FilterCriteria;
import com.example.flight_booking_app.search.model.Flight;
import com.example.flight_booking_app.search.model.FlightClass;
import com.example.flight_booking_app.search.model.FlightPageResponse;
import com.example.flight_booking_app.search.model.SearchRequest;
import com.example.flight_booking_app.search.model.SortOption;
import com.example.flight_booking_app.search.repository.SearchResultRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchResultViewModel extends AndroidViewModel {

    private final SearchResultRepository repository;

    // LiveData dùng để giữ tiêu chí tìm kiếm hiện tại
    private final MutableLiveData<SearchRequest> searchInput = new MutableLiveData<>();

    // LiveData chứa kết quả trả về, tự động "switch" nguồn dữ liệu khi searchInput thay đổi
    private final LiveData<FlightPageResponse> searchResults;

    // LiveData giữ tiêu chí lọc hiện tại
    private final MutableLiveData<FilterCriteria> currentFilter = new MutableLiveData<>(new FilterCriteria());

    // LiveData kết quả cuối cùng sau khi lọc để UI quan sát
    private final MediatorLiveData<List<Flight>> filteredResults = new MediatorLiveData<>();

    // LiveData giữ tiêu chí sắp xếp hiện tại
    private final MutableLiveData<SortOption> currentSort = new MutableLiveData<>(SortOption.DEPARTURE_EARLIEST);

    private final MutableLiveData<List<CheapestDate>> cheapestDates = new MutableLiveData<>();
    private final MutableLiveData<List<Airline>> airlines = new MutableLiveData<>();

    public SearchResultViewModel(@NonNull Application application) {
        super(application);
        repository = new SearchResultRepository(application);

        // Kỹ thuật SwitchMap: Tự động gọi API khi searchInput HOẶC currentSort thay đổi
        // Lưu ý: Chỉ các tiêu chí Server-side mới kích hoạt switchMap này
        searchResults = Transformations.switchMap(searchInput, request -> {
            SortOption sort = currentSort.getValue();
            String sortBy = "departureTime"; // Mặc định
            String sortDir = "asc";

            if (sort == SortOption.PRICE_LOWEST) {
                sortBy = "classes.basePrice";
            } else if (sort == SortOption.DEPARTURE_EARLIEST) {
                sortBy = "departureTime";
            }
            // Lưu ý: DURATION_SHORTEST sẽ dùng mặc định của Server rồi lọc lại ở Client

            return repository.searchFlights(request, sortBy, sortDir);
        });

        // MediatorLiveData để tổng hợp kết quả cuối cùng
        filteredResults.addSource(searchResults, response -> updateFinalResults());
        filteredResults.addSource(currentFilter, filter -> updateFinalResults());
        filteredResults.addSource(currentSort, sort -> updateFinalResults());
    }

    // Hàm gọi lấy dải ngày giá rẻ
    public void fetchCheapestDates(String origin, String destination, String dateStr) {
        try {
            // Tách year và month từ chuỗi "2026-05-07"
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            repository.getCheapestDates(origin, destination, year, month).observeForever(data -> {
                if (data != null) cheapestDates.setValue(data);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lấy danh sách hãng bay để UI hiển thị vào Filter
    public void loadAirlinesForFilter() {
        // Logic gọi repository lấy PageResponse<Airline> từ API v1/airlines
        repository.getAirlines().observeForever(data -> {
            if (data != null) {
                // data ở đây là List<Airline> lấy từ PageResponse.getData()
                airlines.setValue(data);
            }
        });
    }

    public LiveData<List<CheapestDate>> getCheapestDates() { return cheapestDates; }
    public LiveData<List<Airline>> getAirlines() {
        return airlines;
    }

    private void updateFinalResults() {
        if (searchResults.getValue() == null) return;

        // 1. Lấy dữ liệu gốc và chạy hàm lọc
        List<Flight> list = applyFilter(searchResults.getValue().getData(), currentFilter.getValue());

        // 2. ⚡ SỬA TẠI ĐÂY: Sắp xếp cục bộ cho TẤT CẢ các tiêu chí để UI nhảy ngay lập tức
        sortFlights(list, currentSort.getValue());

        // 3. Cập nhật kết quả cuối cùng
        filteredResults.setValue(list);
    }

    private void sortFlightsByDuration(List<Flight> list) {
        Collections.sort(list, (f1, f2) -> {
            long d1 = calculateDuration(f1);
            long d2 = calculateDuration(f2);
            return Long.compare(d1, d2);
        });
    }

    private long calculateDuration(Flight flight) {
        try {
            // Backend dùng LocalDateTime -> ISO String: 2026-05-07T16:55:00
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            long dep = sdf.parse(flight.getDepartureTime()).getTime();
            long arr = sdf.parse(flight.getArrivalTime()).getTime();
            return arr - dep;
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Logic sắp xếp chi tiết trên Client
     */
    private void sortFlights(List<Flight> list, SortOption option) {
        if (list == null || option == null) return;

        Collections.sort(list, (f1, f2) -> {
            switch (option) {
                case PRICE_LOWEST:
                    return Double.compare(getMinPrice(f1), getMinPrice(f2));
                case DEPARTURE_EARLIEST:
                    return f1.getDepartureTime().compareTo(f2.getDepartureTime());
                case DEPARTURE_LATEST:
                    return f2.getDepartureTime().compareTo(f1.getDepartureTime());
                case ARRIVAL_EARLIEST:
                    return f1.getArrivalTime().compareTo(f2.getArrivalTime());
                case ARRIVAL_LATEST:
                    return f2.getArrivalTime().compareTo(f1.getArrivalTime());
                case DURATION_SHORTEST:
                    return Long.compare(getDurationMinutes(f1), getDurationMinutes(f2));
                default:
                    return 0;
            }
        });
    }

    // Lấy giá thấp nhất của một chuyến bay [cite: 93, 103, 157]
    private double getMinPrice(Flight flight) {
        if (flight.getClasses() == null || flight.getClasses().isEmpty()) return Double.MAX_VALUE;
        double min = flight.getClasses().get(0).getBasePrice();
        for (FlightClass fc : flight.getClasses()) {
            if (fc.getBasePrice() < min) min = fc.getBasePrice();
        }
        return min;
    }

    // Tính tổng thời gian bay (phút) từ ISO string [cite: 103, 143, 151]
    private long getDurationMinutes(Flight flight) {
        try {
            // Sử dụng định dạng mặc định của Backend: yyyy-MM-ddTHH:mm:ss [cite: 151]
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            long dep = format.parse(flight.getDepartureTime()).getTime();
            long arr = format.parse(flight.getArrivalTime()).getTime();
            return (arr - dep) / (1000 * 60);
        } catch (Exception e) { return 0; }
    }

    // UI gọi hàm này khi người dùng chọn tiêu chí sắp xếp mới
    public void updateSort(SortOption option) {
        currentSort.setValue(option);

        // Nếu chọn Giá hoặc Giờ bay -> Gọi lại API để Server sắp xếp
        if (option == SortOption.PRICE_LOWEST || option == SortOption.DEPARTURE_EARLIEST) {
            SearchRequest current = searchInput.getValue();
            if (current != null) {
                // Đẩy lại giá trị để trigger switchMap gọi API mới
                searchInput.setValue(current);
            }
        } else {
            // Nếu chọn Thời lượng ngắn nhất -> Chỉ cần chạy lại hàm lọc/sắp xếp tại Client
            updateFinalResults();
        }
    }

    /**
     * Hàm này được gọi từ SearchResultActivity khi nhận được Intent
     * Nó sẽ kích hoạt toàn bộ luồng tìm kiếm từ Repository -> API
     */
    public void startSearch(String origin, String destination, String date, int passengers) {
        // Tạo object request mới dựa trên dữ liệu từ Intent [cite: 522-524]
        SearchRequest request = new SearchRequest(origin, destination, date, passengers);

        // Đẩy giá trị vào MutableLiveData để kích hoạt switchMap
        searchInput.setValue(request);
    }

    /**
     * Activity sẽ "quan sát" (observe) hàm này để cập nhật RecyclerView
     */
    public LiveData<FlightPageResponse> getSearchResults() {
        return searchResults;
    }

    /**
     * HÀM CỐT LÕI: Duyệt danh sách và lọc theo tiêu chí
     */
    public List<Flight> applyFilter(List<Flight> list, FilterCriteria criteria) {
        if (list == null) return new ArrayList<>();
        List<Flight> filteredList = new ArrayList<>();

        for (Flight flight : list) {
            boolean matches = true;

            // 1. Lọc theo hãng bay
            if (!criteria.getSelectedAirlines().isEmpty() &&
                    !criteria.getSelectedAirlines().contains(flight.getAirlineName())) {
                matches = false;
            }

            // 2. Lọc theo khung giờ bay (Cắt chuỗi ISO "T16:55")
            if (matches) {
                float depHour = parseHourFromIso(flight.getDepartureTime());
                if (depHour < criteria.getDepartureTimeStart() || depHour > criteria.getDepartureTimeEnd()) {
                    matches = false;
                }
            }

            // 3. Lọc theo hạng vé (Cabin)
            if (matches && !criteria.getSelectedCabins().isEmpty()) {
                boolean hasCabin = false;
                for (FlightClass fc : flight.getClasses()) {
                    if (criteria.getSelectedCabins().contains(fc.getClassName())) {
                        hasCabin = true;
                        break;
                    }
                }
                if (!hasCabin) matches = false;
            }

            if (matches) filteredList.add(flight);
        }
        return filteredList;
    }

    // Hàm hỗ trợ chuyển "2026-05-07T16:55:00" thành float 16.91 để so sánh giờ
    private float parseHourFromIso(String iso) {
        try {
            int tIdx = iso.indexOf('T');
            int hour = Integer.parseInt(iso.substring(tIdx + 1, tIdx + 3));
            int min = Integer.parseInt(iso.substring(tIdx + 4, tIdx + 6));
            return hour + (min / 60.0f);
        } catch (Exception e) { return 0; }
    }

    // Hàm để UI gọi khi người dùng bấm "Show Flights" trong BottomSheet
    public void updateFilter(FilterCriteria newCriteria) {
        currentFilter.setValue(newCriteria);
    }

    public LiveData<List<Flight>> getFilteredResults() {
        return filteredResults;
    }

    public FilterCriteria getCurrentFilterValue() {
        return currentFilter.getValue() != null ? currentFilter.getValue() : new FilterCriteria();
    }
}