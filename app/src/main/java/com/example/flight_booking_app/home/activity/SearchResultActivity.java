package com.example.flight_booking_app.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.activity.BookingFormActivity;
import com.example.flight_booking_app.booking.activity.FlightDetailActivity;
import com.example.flight_booking_app.home.adapter.FlightAdapter;
import com.example.flight_booking_app.home.viewmodel.HomeViewModel;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {

    private HomeViewModel viewModel;
    private FlightAdapter flightAdapter;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // 1. Ánh xạ View phần Header
        TextView tvHeaderRoute = findViewById(R.id.tvHeaderRoute);
        TextView tvHeaderDetails = findViewById(R.id.tvHeaderDetails);
        btnBack = findViewById(R.id.btnBack);

        // =========================================================================
        // 2. NHẬN DỮ LIỆU TỪ HOME ACTIVITY (ĐÃ CẬP NHẬT 3 BIẾN HÀNH KHÁCH)
        // =========================================================================
        String origin = getIntent().getStringExtra("ORIGIN");
        String destination = getIntent().getStringExtra("DESTINATION");
        String date = getIntent().getStringExtra("DATE");
        int passengers = getIntent().getIntExtra("PASSENGERS", 1); // Tổng số người (Để hiển thị Header và gọi API)

        // Nhận chi tiết từng loại hành khách
        int adultCount = getIntent().getIntExtra("ADULT_COUNT", 1);
        int childCount = getIntent().getIntExtra("CHILD_COUNT", 0);
        int infantCount = getIntent().getIntExtra("INFANT_COUNT", 0);

        // Hiển thị lên Header cho đẹp
        tvHeaderRoute.setText(origin + "  →  " + destination);
        tvHeaderDetails.setText(date + "  •  " + passengers + " Hành khách");

        // =========================================================================
        // 3. KHỞI TẠO RECYCLERVIEW & BẮT SỰ KIỆN CLICK CHUYẾN BAY
        // =========================================================================
        RecyclerView rvResultFlights = findViewById(R.id.rvResultFlights);
        rvResultFlights.setLayoutManager(new LinearLayoutManager(this));

        // Gắn sự kiện click cho Adapter
        flightAdapter = new FlightAdapter(flight -> {

            // Chuyển sang màn hình Chi tiết chuyến bay hoặc Điền Form (Tùy logic app của bạn)
            Intent intent = new Intent(SearchResultActivity.this, FlightDetailActivity.class);
// Ném nguyên cục dữ liệu chuyến bay (Giống cách truyền State của React)
// Chỉ gửi Số hiệu chuyến bay (VD: "NS8118") để thay cho ID bị thiếu
            intent.putExtra("FLIGHT_ID", flight.getId());
            intent.putExtra("ADULT_COUNT", adultCount);
            intent.putExtra("CHILD_COUNT", childCount);
            intent.putExtra("INFANT_COUNT", infantCount);

            startActivity(intent);
        });

        rvResultFlights.setAdapter(flightAdapter);

        // =========================================================================
        // 4. KHỞI TẠO VIEWMODEL & GỌI API
        // =========================================================================
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Lắng nghe kết quả từ API
        viewModel.getSearchResults().observe(this, flightPageResponse -> {
            if (flightPageResponse != null && flightPageResponse.getData() != null && !flightPageResponse.getData().isEmpty()) {
                // Đổ dữ liệu thật vào Adapter
                flightAdapter.setFlights(flightPageResponse.getData());
            } else {
                flightAdapter.setFlights(new ArrayList<>());
                Toast.makeText(this, "Không tìm thấy chuyến bay nào!", Toast.LENGTH_SHORT).show();
            }
        });

        // GỌI API NGAY KHI MỞ MÀN HÌNH NÀY LÊN
        if (origin != null && destination != null && date != null) {
            viewModel.performSearch(origin, destination, date, passengers);
        }

        // Nút Back
        btnBack.setOnClickListener(v -> finish());
    }
}