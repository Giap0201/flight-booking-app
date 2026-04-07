package com.example.flight_booking_app.home.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

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

        TextView tvHeaderRoute = findViewById(R.id.tvHeaderRoute);
        TextView tvHeaderDetails = findViewById(R.id.tvHeaderDetails);
        btnBack = findViewById(R.id.btnBack);

        // 1. NHẬN DỮ LIỆU TỪ TRANG HOME
        String origin = getIntent().getStringExtra("ORIGIN");
        String destination = getIntent().getStringExtra("DESTINATION");
        String date = getIntent().getStringExtra("DATE");
        int passengers = getIntent().getIntExtra("PASSENGERS", 1);

        boolean isRoundTrip = getIntent().getBooleanExtra("IS_ROUND_TRIP", false);
        String returnDate = getIntent().getStringExtra("RETURN_DATE");
        boolean isReturnLeg = getIntent().getBooleanExtra("IS_RETURN_LEG", false);

        // 2. CẬP NHẬT HEADER CHUẨN UX (Giống web)
        if (isRoundTrip) {
            tvHeaderRoute.setText("Bước 1/2: Chiều đi (" + origin + " ➔ " + destination + ")");
            tvHeaderDetails.setText(date + "  •  " + passengers + " Hành khách");
        } else if (isReturnLeg) {
            tvHeaderRoute.setText("Bước 2/2: Chiều về (" + origin + " ➔ " + destination + ")");
            tvHeaderDetails.setText(date + "  •  " + passengers + " Hành khách");
        } else {
            tvHeaderRoute.setText(origin + " ➔ " + destination);
            tvHeaderDetails.setText(date + "  •  " + passengers + " Hành khách");
        }

        // 3. KHỞI TẠO ADAPTER ĐỂ HIỂN THỊ DỮ LIỆU THẬT
        RecyclerView rvResultFlights = findViewById(R.id.rvResultFlights);
        rvResultFlights.setLayoutManager(new LinearLayoutManager(this));
        flightAdapter = new FlightAdapter();
        rvResultFlights.setAdapter(flightAdapter);

        // --- XỬ LÝ KHI NGƯỜI DÙNG BẤM CHỌN 1 VÉ TRONG DANH SÁCH ---
        flightAdapter.setOnFlightClickListener(selectedFlight -> {

            // Nếu là vé Khứ hồi và ĐANG ở màn hình Chiều đi (Bước 1)
            if (isRoundTrip && !isReturnLeg) {
                Toast.makeText(this, "Đã chọn vé đi! Chuyển sang chọn vé về...", Toast.LENGTH_SHORT).show();

                // Tạo Intent để mở lại chính màn hình này nhưng cho Chiều Về
                Intent returnIntent = new Intent(SearchResultActivity.this, SearchResultActivity.class);

                // 1. Đảo ngược Điểm đi và Điểm đến
                returnIntent.putExtra("ORIGIN", destination);
                returnIntent.putExtra("DESTINATION", origin);

                // 2. Lấy ngày về làm ngày tìm kiếm
                returnIntent.putExtra("DATE", returnDate);
                returnIntent.putExtra("PASSENGERS", passengers);

                // 3. Cập nhật trạng thái luồng bay
                returnIntent.putExtra("IS_ROUND_TRIP", false); // Tắt cờ này để không bị lặp lại Bước 1
                returnIntent.putExtra("IS_RETURN_LEG", true);  // Bật cờ này để UI hiểu là đang ở Bước 2

                // 4. BẮT BUỘC: Nhét cái vé chiều đi vừa chọn vào Intent để Dev sau dùng
                returnIntent.putExtra("OUTBOUND_FLIGHT", selectedFlight);

                startActivity(returnIntent);
            }
            else {
                // TÌNH HUỐNG 2: Đã chọn xong vé Một chiều, HOẶC đã chọn xong chiều về (Bước 2)
                Toast.makeText(this, "Hoàn tất! Chuyển sang trang Dịch Vụ / Thanh Toán", Toast.LENGTH_LONG).show();

                // (Chỗ này sau dev khác sẽ viết Intent mở ServiceSelectionActivity / CheckoutActivity)
            }
        });

        // 4. KHỞI TẠO VIEWMODEL & LẮNG NGHE API TRẢ VỀ
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getSearchResults().observe(this, flightPageResponse -> {
            if (flightPageResponse != null && flightPageResponse.getData() != null && !flightPageResponse.getData().isEmpty()) {
                // Có dữ liệu thật -> Đổ vào danh sách
                flightAdapter.setFlights(flightPageResponse.getData());
            } else {
                // Không có chuyến bay nào
                flightAdapter.setFlights(new ArrayList<>());
                Toast.makeText(this, "Không tìm thấy chuyến bay nào cho ngày này!", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. BẮN API TÌM KIẾM THEO NGÀY
        if (origin != null && destination != null && date != null) {
            // Lệnh này sẽ gọi xuống Repository -> Spring Boot Backend
            viewModel.performSearch(origin, destination, date, passengers, isRoundTrip, returnDate);
        }

        // 6. NÚT BACK
        btnBack.setOnClickListener(v -> finish());
    }
}