package com.example.flight_booking_app.home.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
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

        // 2. Lấy dữ liệu từ màn hình Home gửi sang
        String origin = getIntent().getStringExtra("ORIGIN");
        String destination = getIntent().getStringExtra("DESTINATION");
        String date = getIntent().getStringExtra("DATE");
        int passengers = getIntent().getIntExtra("PASSENGERS", 1);

        // Hiển thị lên Header cho đẹp
        tvHeaderRoute.setText(origin + "  →  " + destination);
        tvHeaderDetails.setText(date + "  •  " + passengers + " Hành khách");

        // 3. Khởi tạo RecyclerView và Adapter (Y hệt như trang Home cũ)
        RecyclerView rvResultFlights = findViewById(R.id.rvResultFlights);
        rvResultFlights.setLayoutManager(new LinearLayoutManager(this));
        flightAdapter = new FlightAdapter();
        rvResultFlights.setAdapter(flightAdapter);

        // 4. Khởi tạo ViewModel
        // Chú ý: Ở đây ta dùng lại HomeViewModel vì nó đã có sẵn hàm performSearch rất chuẩn rồi
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 5. Lắng nghe kết quả từ API
        viewModel.getSearchResults().observe(this, flightPageResponse -> {
            if (flightPageResponse != null && flightPageResponse.getData() != null && !flightPageResponse.getData().isEmpty()) {
                // Đổ dữ liệu thật vào Adapter
                flightAdapter.setFlights(flightPageResponse.getData());
            } else {
                flightAdapter.setFlights(new ArrayList<>());
                Toast.makeText(this, "Không tìm thấy chuyến bay nào!", Toast.LENGTH_SHORT).show();
            }
        });

        // 6. GỌI API NGAY KHI MỞ MÀN HÌNH NÀY LÊN
        if (origin != null && destination != null && date != null) {
            viewModel.performSearch(origin, destination, date, passengers);
        }

        btnBack.setOnClickListener(v -> finish());
    }
}