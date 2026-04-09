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
    private String departureDate; // Ngày đi gốc
    private String returnDate;    // Ngày về gốc
    private Flight outboundFlight; // Lưu vé lượt đi khi chọn xong

    private String originalOrigin;
    private String originalDestination;
    private int adultCount, childCount, infantCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // 1. NHẬN DỮ LIỆU TỪ INTENT
        originalOrigin = getIntent().getStringExtra("ORIGIN");
        originalDestination = getIntent().getStringExtra("DESTINATION");
        departureDate = getIntent().getStringExtra("DATE");
        returnDate = getIntent().getStringExtra("RETURN_DATE");
        isRoundTrip = getIntent().getBooleanExtra("IS_ROUND_TRIP", false);
        passengersCount = getIntent().getIntExtra("PASSENGERS", 1);
        adultCount = getIntent().getIntExtra("ADULT_COUNT", 1);
        childCount = getIntent().getIntExtra("CHILD_COUNT", 0);
        infantCount = getIntent().getIntExtra("INFANT_COUNT", 0);

        currentSelectedDate = departureDate;

        // 2. SETUP HEADER
        initHeader(originalOrigin, originalDestination, currentSelectedDate, passengersCount);

        // 3. SETUP RECYCLERVIEW DẢI NGÀY
        RecyclerView rvDateStrip = findViewById(R.id.rvDateStrip);
        rvDateStrip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateStripAdapter = new DateStripAdapter(item -> {
            this.currentSelectedDate = item.getDate();
            updateHeaderDate(currentSelectedDate);

            // Tìm kiếm dựa trên trạng thái hiện tại (Lượt đi hay Lượt về)
            if (isSelectingReturn) {
                viewModel.startSearch(originalDestination, originalOrigin, currentSelectedDate, passengersCount);
            } else {
                viewModel.startSearch(originalOrigin, originalDestination, currentSelectedDate, passengersCount);
            }
        });
        rvDateStrip.setAdapter(dateStripAdapter);

        // 4. SETUP RECYCLERVIEW CHUYẾN BAY VỚI LOGIC CHỌN VÉ
        rvResultFlights = findViewById(R.id.rvResultFlights);
        rvResultFlights.setLayoutManager(new LinearLayoutManager(this));
        flightAdapter = new SearchResultAdapter(flight -> {
            if (isRoundTrip && !isSelectingReturn) {
                handleOutboundSelection(flight);
            } else {
                navigateToDetail(flight);
            }
        });
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

        // 6. KÍCH HOẠT TÌM KIẾM BAN ĐẦU
        viewModel.startSearch(originalOrigin, originalDestination, departureDate, passengersCount);
        viewModel.fetchCheapestDates(originalOrigin, originalDestination, departureDate);

        // 7. BỘ LỌC VÀ SẮP XẾP
        findViewById(R.id.chipFilter).setOnClickListener(v -> showFilterBottomSheet());
        findViewById(R.id.btnSort).setOnClickListener(v -> showSortBottomSheet());

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSelectingReturn) {
                    // Quay lại chọn lượt đi
                    isSelectingReturn = false;
                    outboundFlight = null;
                    currentSelectedDate = departureDate;

                    initHeader(originalOrigin, originalDestination, currentSelectedDate, passengersCount);
                    viewModel.startSearch(originalOrigin, originalDestination, currentSelectedDate, passengersCount);
                    viewModel.fetchCheapestDates(originalOrigin, originalDestination, currentSelectedDate);
                } else {
                    // Cho phép thoát màn hình (tương đương super.onBackPressed())
                    setEnabled(false); // Vô hiệu hóa callback này để nút back hoạt động mặc định
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void handleOutboundSelection(Flight flight) {
        this.outboundFlight = flight; // Lưu tạm lượt đi
        this.isSelectingReturn = true;
        this.currentSelectedDate = returnDate;

        // Cập nhật Header: Đảo ngược điểm đi/đến và đổi tiêu đề
        initHeader(originalDestination, originalOrigin, currentSelectedDate, passengersCount);

        // Kích hoạt tìm kiếm lượt về
        viewModel.startSearch(originalDestination, originalOrigin, currentSelectedDate, passengersCount);
        viewModel.fetchCheapestDates(originalDestination, originalOrigin, currentSelectedDate);

        Toast.makeText(this, "Đã chọn lượt đi. Vui lòng chọn lượt về.", Toast.LENGTH_SHORT).show();
        rvResultFlights.scrollToPosition(0);
    }

    private void navigateToDetail(Flight selectedFlight) { // Đổi tên tham số ở đây
        Intent intent = new Intent(this, FlightDetailActivity.class);

        // Truyền thông tin số lượng khách
        intent.putExtra("IS_ROUND_TRIP", isRoundTrip);
        intent.putExtra("ADULT_COUNT", adultCount);
        intent.putExtra("CHILD_COUNT", childCount);
        intent.putExtra("INFANT_COUNT", infantCount);
        intent.putExtra("TOTAL_PASSENGERS", passengersCount);

        // Truyền dữ liệu chuyến bay
        if (isRoundTrip) {
            // Gửi cặp vé: Outbound (lưu từ handleOutboundSelection) và Inbound (vé vừa chọn)
            intent.putExtra("OUTBOUND_FLIGHT", outboundFlight);
            intent.putExtra("INBOUND_FLIGHT", selectedFlight);
        } else {
            // Gửi vé một chiều
            intent.putExtra("SELECTED_FLIGHT", selectedFlight);
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
            tvHeaderDetails.setText(String.format("%s  •  %d Hành khách %s", formattedDate, passengersCount, label));
        } catch (Exception e) {
            tvHeaderDetails.setText(String.format("%s  •  %d Hành khách %s", dateIso, passengersCount, label));
        }
    }

    private void showFilterBottomSheet() {
        new FilterBottomSheet().show(getSupportFragmentManager(), "FilterBottomSheet");
    }

    private void showSortBottomSheet() {
        new SortBottomSheet().show(getSupportFragmentManager(), "SortBottomSheet");
    }
}