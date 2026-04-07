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

        // --- LẤY THÊM 2 BIẾN KHỨ HỒI ---
        boolean isRoundTrip = getIntent().getBooleanExtra("IS_ROUND_TRIP", false);
        String returnDate = getIntent().getStringExtra("RETURN_DATE");

        // 3. Cập nhật UI Header tùy theo loại vé (1 chiều hay khứ hồi)
//        if (isRoundTrip) {
//            tvHeaderRoute.setText(origin + "  ⇌  " + destination); // Mũi tên 2 chiều
//            tvHeaderDetails.setText(date + " đến " + returnDate + "  •  " + passengers + " Hành khách");
//        } else {
//            tvHeaderRoute.setText(origin + "  →  " + destination); // Mũi tên 1 chiều
//            tvHeaderDetails.setText(date + "  •  " + passengers + " Hành khách");
//        }
        if (isRoundTrip) {
            // Khứ hồi: Nhấn mạnh đây là Chiều đi
            tvHeaderRoute.setText("Chiều đi: " + origin + "  →  " + destination);
            tvHeaderDetails.setText(date + "  •  " + passengers + " Hành khách");
        } else {
            // Một chiều: Hiển thị bình thường
            tvHeaderRoute.setText(origin + "  →  " + destination);
            tvHeaderDetails.setText(date + "  •  " + passengers + " Hành khách");
        }

        // 4. Khởi tạo RecyclerView và Adapter
        RecyclerView rvResultFlights = findViewById(R.id.rvResultFlights);
        rvResultFlights.setLayoutManager(new LinearLayoutManager(this));
        flightAdapter = new FlightAdapter();
        rvResultFlights.setAdapter(flightAdapter);

        // --- KIỂM TRA XEM ĐÂY CÓ PHẢI LÀ MÀN HÌNH "CHIỀU VỀ" KHÔNG ---
        boolean isReturnLeg = getIntent().getBooleanExtra("IS_RETURN_LEG", false);

        // --- CẬP NHẬT HEADER DỰA TRÊN LOẠI CHUYẾN ---
        if (isRoundTrip) {
            tvHeaderRoute.setText("Chiều đi: " + origin + "  →  " + destination);
        } else if (isReturnLeg) {
            tvHeaderRoute.setText("Chiều về: " + origin + "  →  " + destination);
        } else {
            tvHeaderRoute.setText(origin + "  →  " + destination);
        }

        // --- XỬ LÝ SỰ KIỆN BẤM CHỌN CHUYẾN BAY ---
        flightAdapter.setOnFlightClickListener(selectedFlight -> {
            if (isRoundTrip) {
                // TÌNH HUỐNG 1: ĐANG CHỌN CHIỀU ĐI (CỦA VÉ KHỨ HỒI)
                // -> Mở lại chính màn hình này nhưng đảo ngược Điểm đi - Điểm đến cho Chiều Về

                Intent returnIntent = new Intent(SearchResultActivity.this, SearchResultActivity.class);
                returnIntent.putExtra("ORIGIN", destination); // Đảo ngược: Điểm đi = SGN
                returnIntent.putExtra("DESTINATION", origin); // Đảo ngược: Điểm đến = HAN
                returnIntent.putExtra("DATE", returnDate);    // Lấy ngày về để tìm kiếm
                returnIntent.putExtra("PASSENGERS", passengers);

                // Mẹo cực hay: Đặt Khứ hồi = false để nó không bị lặp vô tận
                returnIntent.putExtra("IS_ROUND_TRIP", false);
                returnIntent.putExtra("IS_RETURN_LEG", true); // Đánh dấu đây là chuyến về

                // (Tùy chọn) Truyền ID chuyến bay đi để mang sang trang Thanh toán
                // returnIntent.putExtra("OUTBOUND_FLIGHT_ID", selectedFlight.getId());

                startActivity(returnIntent);

            } else {
                // TÌNH HUỐNG 2: ĐÃ CHỌN XONG (Là vé 1 chiều, HOẶC đang chọn chuyến về)
                // -> Chuyển thẳng sang trang Điền thông tin Hành Khách / Thanh Toán

                Toast.makeText(this, "Chuyển sang điền thông tin hành khách!", Toast.LENGTH_SHORT).show();
                // Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
                // startActivity(checkoutIntent);
            }
        });

        // 5. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 6. Lắng nghe kết quả từ API
        viewModel.getSearchResults().observe(this, flightPageResponse -> {
            if (flightPageResponse != null && flightPageResponse.getData() != null && !flightPageResponse.getData().isEmpty()) {
                flightAdapter.setFlights(flightPageResponse.getData());
            } else {
                flightAdapter.setFlights(new ArrayList<>());
                Toast.makeText(this, "Không tìm thấy chuyến bay nào!", Toast.LENGTH_SHORT).show();
            }
        });

        // 7. GỌI API NGAY KHI MỞ MÀN HÌNH NÀY LÊN
        if (origin != null && destination != null && date != null) {
            // --- TRUYỀN THÊM isRoundTrip VÀ returnDate VÀO VIEWMODEL ---
            viewModel.performSearch(origin, destination, date, passengers, isRoundTrip, returnDate);
        }

        btnBack.setOnClickListener(v -> finish());
    }
}