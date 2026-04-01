package com.example.flight_booking_app.booking.activity; // Đổi tên package cho khớp máy bạn

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.TicketClassAdapter;
import com.example.flight_booking_app.booking.model.FlightClass;
import com.example.flight_booking_app.booking.viewmodel.FlightViewModel;

import java.util.ArrayList;
import java.util.List;

public class FlightDetailActivity extends AppCompatActivity {

    // 1. Khai báo các view
    private TextView tvRoute;
    private TextView tvFlightInfo;
    private ListView lvTickets;

    // 2. Khai báo Adapter
    private TicketClassAdapter adapter;
    private List<FlightClass> listFlightClasses;

    // 3. Khai báo ViewModel
    private FlightViewModel viewModel;

    // Tạm thời fix cứng ID chuyến bay vì Dev kia chưa làm xong màn hình Search
    private String currentFlightId = "005d2fc3-fd73-4fdb-ad31-439e425ee8a9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- GIỮ NGUYÊN CODE MẶC ĐỊNH TRÀN VIỀN CỦA ANDROID STUDIO ---
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flight_detail); // Nhớ đổi tên layout nếu bạn dùng tên khác
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // -------------------------------------------------------------

        // 4. Ánh xạ View
        tvRoute = findViewById(R.id.tvRoute);
        tvFlightInfo = findViewById(R.id.tvFlightInfo);
        lvTickets = findViewById(R.id.lvTickets);

        // 5. Cài đặt Adapter cho ListView
        listFlightClasses = new ArrayList<>();
        adapter = new TicketClassAdapter(this, listFlightClasses, currentFlightId);
        lvTickets.setAdapter(adapter);

        // 6. Khởi tạo ViewModel
        // LƯU Ý: Không dùng toán tử 'new FlightViewModel()' thông thường, mà phải dùng ViewModelProvider
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        // 7. Quan sát dữ liệu (Observe)
        observeData();
    }

    private void observeData() {
        // ViewModel sẽ tự động gọi Repository lấy dữ liệu.
        // Khi nào có kết quả, nó sẽ tự động chạy vào hàm 'onChanged' (hoặc lambda dưới đây)
        viewModel.getFlightDetailLiveData(currentFlightId).observe(this, flightDetail -> {

            if (flightDetail != null) {
                // A. Cập nhật Text
                tvRoute.setText(flightDetail.getOrigin().getCityCode() + " -> " + flightDetail.getDestination().getCityCode());
                tvFlightInfo.setText(flightDetail.getAirline().getName() + " | " + flightDetail.getDepartureTime());

                // B. Cập nhật ListView các hạng vé
                listFlightClasses.clear();
                listFlightClasses.addAll(flightDetail.getFlightClasses());
                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(FlightDetailActivity.this, "Lỗi: Không tải được dữ liệu chuyến bay!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}