package com.example.flight_booking_app.search.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.activity.FlightDetailActivity;
import com.example.flight_booking_app.search.adapter.DateStripAdapter;
import com.example.flight_booking_app.search.adapter.SearchResultAdapter;
import com.example.flight_booking_app.search.bottomsheet.FilterBottomSheet;
import com.example.flight_booking_app.search.bottomsheet.SortBottomSheet;
import com.example.flight_booking_app.search.model.Flight;
import com.example.flight_booking_app.search.viewmodel.SearchResultViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SearchResultActivity extends AppCompatActivity {

    private SearchResultViewModel viewModel;
    private SearchResultAdapter flightAdapter;
    private DateStripAdapter dateStripAdapter;
    private RecyclerView rvResultFlights;

    private String currentSelectedDate;
    private int passengersCount;

    // --- QUẢN LÝ TRẠNG THÁI KHỨ HỒI ---
    private boolean isRoundTrip = false;
    private boolean isSelectingReturn = false;
    private String departureDate;
    private String returnDate;

    // ⚡ QUAN TRỌNG: Lưu trữ context tìm kiếm vì FlightDetail không pass ngược lại các string này
    private static String sOrigin, sDestination, sDepDate, sRetDate;
    private static int sPassengers, sAdult, sChild, sInfant;

    private String originalOrigin;
    private String originalDestination;
    private int adultCount, childCount, infantCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // 1. NHẬN DỮ LIỆU TỪ INTENT
        String intentOrigin = getIntent().getStringExtra("ORIGIN");
        if (intentOrigin != null) {
            // Lần đầu mở từ màn hình Home
            originalOrigin = intentOrigin;
            originalDestination = getIntent().getStringExtra("DESTINATION");
            departureDate = getIntent().getStringExtra("DATE");
            returnDate = getIntent().getStringExtra("RETURN_DATE");
            passengersCount = getIntent().getIntExtra("PASSENGERS", 1);
            adultCount = getIntent().getIntExtra("ADULT_COUNT", 1);
            childCount = getIntent().getIntExtra("CHILD_COUNT", 0);
            infantCount = getIntent().getIntExtra("INFANT_COUNT", 0);

            // Sao lưu vào static để dùng khi quay lại từ FlightDetail
            sOrigin = originalOrigin; sDestination = originalDestination;
            sDepDate = departureDate; sRetDate = returnDate;
            sPassengers = passengersCount; sAdult = adultCount;
            sChild = childCount; sInfant = infantCount;
        } else {
            // Quay lại từ FlightDetailActivity để thực hiện chọn lượt về
            originalOrigin = sOrigin; originalDestination = sDestination;
            departureDate = sDepDate; returnDate = sRetDate;
            passengersCount = sPassengers; adultCount = sAdult;
            childCount = sChild; infantCount = sInfant;
        }

        isRoundTrip = getIntent().getBooleanExtra("IS_ROUND_TRIP", false);
        isSelectingReturn = getIntent().getBooleanExtra("IS_SELECTING_RETURN_FLIGHT", false); // Cờ từ dev sau gửi về

        currentSelectedDate = isSelectingReturn ? returnDate : departureDate;

        // 2. SETUP HEADER (Đảo ngược nếu là lượt về)
        if (isSelectingReturn) {
            initHeader(originalDestination, originalOrigin, currentSelectedDate, passengersCount);
        } else {
            initHeader(originalOrigin, originalDestination, currentSelectedDate, passengersCount);
        }

        // 3. SETUP RECYCLERVIEW DẢI NGÀY
        RecyclerView rvDateStrip = findViewById(R.id.rvDateStrip);
        rvDateStrip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateStripAdapter = new DateStripAdapter(item -> {
            this.currentSelectedDate = item.getDate();
            updateHeaderDate(currentSelectedDate);
            if (isSelectingReturn) {
                viewModel.startSearch(originalDestination, originalOrigin, currentSelectedDate, passengersCount);
            } else {
                viewModel.startSearch(originalOrigin, originalDestination, currentSelectedDate, passengersCount);
            }
        });
        rvDateStrip.setAdapter(dateStripAdapter);

        // 4. SETUP RECYCLERVIEW CHUYẾN BAY
        rvResultFlights = findViewById(R.id.rvResultFlights);
        rvResultFlights.setLayoutManager(new LinearLayoutManager(this));

        // ⚡ THAY ĐỔI: Luôn chuyển sang FlightDetailActivity để xem chi tiết và chọn hạng vé
        flightAdapter = new SearchResultAdapter(this::navigateToDetail);
        rvResultFlights.setAdapter(flightAdapter);

        // 5. KẾT NỐI VIEWMODEL
        viewModel = new ViewModelProvider(this).get(SearchResultViewModel.class);
        viewModel.getFilteredResults().observe(this, flights -> {
            flightAdapter.setFlights(flights != null ? flights : new java.util.ArrayList<>());
        });

        viewModel.getCheapestDates().observe(this, cheapestDates -> {
            if (cheapestDates != null) {
                dateStripAdapter.setDates(cheapestDates, currentSelectedDate);
            }
        });

        // 6. KÍCH HOẠT TÌM KIẾM
        if (isSelectingReturn) {
            viewModel.startSearch(originalDestination, originalOrigin, currentSelectedDate, passengersCount);
            viewModel.fetchCheapestDates(originalDestination, originalOrigin, currentSelectedDate);
        } else {
            viewModel.startSearch(originalOrigin, originalDestination, currentSelectedDate, passengersCount);
            viewModel.fetchCheapestDates(originalOrigin, originalDestination, currentSelectedDate);
        }

        // 7. BỘ LỌC VÀ SẮP XẾP
        findViewById(R.id.chipFilter).setOnClickListener(v -> showFilterBottomSheet());
        findViewById(R.id.btnSort).setOnClickListener(v -> showSortBottomSheet());

        // Xử lý nút Back chuẩn UX
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSelectingReturn) {
                    isSelectingReturn = false;
                    currentSelectedDate = departureDate;
                    initHeader(originalOrigin, originalDestination, currentSelectedDate, passengersCount);
                    viewModel.startSearch(originalOrigin, originalDestination, currentSelectedDate, passengersCount);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void navigateToDetail(Flight selectedFlight) {
        Intent intent = new Intent(this, FlightDetailActivity.class);

        // Truyền ID và thông tin cơ bản cho dev sau
        intent.putExtra("FLIGHT_ID", selectedFlight.getId());
        intent.putExtra("IS_ROUND_TRIP", isRoundTrip);
        intent.putExtra("IS_SELECTING_RETURN_FLIGHT", isSelectingReturn); // Báo cho Detail biết đang chọn chiều nào

        intent.putExtra("ADULT_COUNT", adultCount);
        intent.putExtra("CHILD_COUNT", childCount);
        intent.putExtra("INFANT_COUNT", infantCount);

        // ⚡ Nếu đang ở lượt về, truyền lại thông tin lượt đi mà Detail đã pass sang SearchResult trước đó
        if (isSelectingReturn) {
            intent.putExtra("OUTBOUND_FLIGHT_ID", getIntent().getStringExtra("OUTBOUND_FLIGHT_ID"));
            intent.putExtra("OUTBOUND_FLIGHT_CLASS_ID", getIntent().getStringExtra("OUTBOUND_FLIGHT_CLASS_ID"));
            intent.putExtra("OUTBOUND_TICKET_PRICE", getIntent().getDoubleExtra("OUTBOUND_TICKET_PRICE", 0.0));
        }

        startActivity(intent);
    }

    private void initHeader(String origin, String dest, String date, int passengers) {
        TextView tvHeaderRoute = findViewById(R.id.tvHeaderRoute);
        tvHeaderRoute.setText(String.format("%s  →  %s", origin, dest));
        updateHeaderDate(date);
        findViewById(R.id.btnChangeSearch).setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    private void updateHeaderDate(String dateIso) {
        TextView tvHeaderDetails = findViewById(R.id.tvHeaderDetails);
        String label = isSelectingReturn ? "Lượt về" : (isRoundTrip ? "Lượt đi" : "");
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatter = new SimpleDateFormat("d 'thg' M", Locale.getDefault());
            String formattedDate = formatter.format(parser.parse(dateIso));
            tvHeaderDetails.setText(String.format("%s  •  %d khách %s", formattedDate, passengersCount, label));
        } catch (Exception e) {
            tvHeaderDetails.setText(String.format("%s  •  %d khách %s", dateIso, passengersCount, label));
        }
    }

    private void showFilterBottomSheet() {
        new FilterBottomSheet().show(getSupportFragmentManager(), "FilterBottomSheet");
    }

    private void showSortBottomSheet() {
        new SortBottomSheet().show(getSupportFragmentManager(), "SortBottomSheet");
    }
}