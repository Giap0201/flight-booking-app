package com.example.flight_booking_app.search.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.search.adapter.DateStripAdapter;
import com.example.flight_booking_app.search.adapter.SearchResultAdapter;
import com.example.flight_booking_app.search.viewmodel.SearchResultViewModel;

public class SearchResultActivity extends AppCompatActivity {

    private SearchResultViewModel viewModel;
    private SearchResultAdapter flightAdapter;
    private DateStripAdapter dateStripAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // 1. NHẬN DỮ LIỆU TỪ INTENT
        String origin = getIntent().getStringExtra("ORIGIN");
        String destination = getIntent().getStringExtra("DESTINATION");
        String date = getIntent().getStringExtra("DATE");
        int totalPassengers = getIntent().getIntExtra("PASSENGERS", 1);

        // 2. ÁNH XẠ VIEW VÀ SETUP HEADER
        initHeader(origin, destination, date, totalPassengers);

        // 3. SETUP RECYCLERVIEW DẢI NGÀY (DATE STRIP)
        RecyclerView rvDateStrip = findViewById(R.id.rvDateStrip);
        rvDateStrip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateStripAdapter = new DateStripAdapter(item -> {
            // Khi chọn ngày mới trên dải ngày, thực hiện tìm kiếm lại
            viewModel.startSearch(origin, destination, item.getDate(), totalPassengers);
        });
        rvDateStrip.setAdapter(dateStripAdapter);

        // 4. SETUP RECYCLERVIEW CHUYẾN BAY
        RecyclerView rvResultFlights = findViewById(R.id.rvResultFlights);
        rvResultFlights.setLayoutManager(new LinearLayoutManager(this));
        flightAdapter = new SearchResultAdapter(flight -> {
            // Logic chuyển sang chi tiết chuyến bay
            Toast.makeText(this, "Flight: " + flight.getFlightNumber(), Toast.LENGTH_SHORT).show();
        });
        rvResultFlights.setAdapter(flightAdapter);

        // 5. KẾT NỐI VIEWMODEL VÀ QUAN SÁT DỮ LIỆU
        viewModel = new ViewModelProvider(this).get(SearchResultViewModel.class);

        // Quan sát kết quả ĐÃ LỌC (filteredResults) thay vì kết quả thô
        viewModel.getFilteredResults().observe(this, flights -> {
            if (flights != null && !flights.isEmpty()) {
                flightAdapter.setFlights(flights);
            } else {
                flightAdapter.setFlights(new java.util.ArrayList<>());
                Toast.makeText(this, "Không có chuyến bay phù hợp bộ lọc!", Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát dải ngày giá rẻ từ API
        viewModel.getCheapestDates().observe(this, cheapestDates -> {
            if (cheapestDates != null) {
                dateStripAdapter.setDates(cheapestDates, date);
            }
        });

        // 6. KÍCH HOẠT TÌM KIẾM BAN ĐẦU
        if (origin != null && destination != null && date != null) {
            viewModel.startSearch(origin, destination, date, totalPassengers);
            // Lấy thêm dữ liệu dải ngày giá rẻ
            viewModel.fetchCheapestDates(origin, destination, date);
        }

        // 7. XỬ LÝ NÚT BỘ LỌC VÀ SẮP XẾP (Theo Figma)
        // Sửa từ btnFilter thành chipFilter
        findViewById(R.id.chipFilter).setOnClickListener(v -> showFilterBottomSheet());
        findViewById(R.id.btnSort).setOnClickListener(v -> showSortBottomSheet());
    }

    private void initHeader(String origin, String dest, String date, int passengers) {
        TextView tvHeaderRoute = findViewById(R.id.tvHeaderRoute);
        TextView tvHeaderDetails = findViewById(R.id.tvHeaderDetails);
        ImageButton btnBack = findViewById(R.id.btnBack);

        tvHeaderRoute.setText(String.format("%s  →  %s", origin, dest));
        tvHeaderDetails.setText(String.format("%s  •  %d Hành khách", date, passengers));
        btnBack.setOnClickListener(v -> finish());
    }

    private void showFilterBottomSheet() {
        // Gọi Fragment hoặc Dialog hiển thị bộ lọc
        // Sau khi người dùng chọn, gọi viewModel.updateFilter(criteria)
    }

    private void showSortBottomSheet() {
        // Hiển thị các tùy chọn sắp xếp: Lowest Price, Earliest, Shortest
    }
}