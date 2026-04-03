package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView; // Thêm cho nút Back
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager; // QUAN TRỌNG: Cho RecyclerView
import androidx.recyclerview.widget.RecyclerView;      // QUAN TRỌNG: Cho RecyclerView

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.TicketClassAdapter;
import com.example.flight_booking_app.booking.model.FlightClass;
import com.example.flight_booking_app.booking.viewmodel.FlightViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FlightDetailActivity extends AppCompatActivity {

    // 1. Khai báo View (Đã đổi ListView thành RecyclerView)
    private TextView tvRoute, tvFlightInfo, tvFlightDate;
    private TextView tvDepartureTimeTimeline, tvArrivalTimeTimeline;
    private TextView tvOriginAirport, tvDestinationAirport;
    private RecyclerView rvTickets;
    private ImageView btnBack;

    private TicketClassAdapter adapter;
    private List<FlightClass> listFlightClasses;
    private FlightViewModel viewModel;

    // Dữ liệu nhận từ Intent
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 0;
    private String currentFlightId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flight_detail);

        // Setup Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Hứng dữ liệu từ Intent
        initIntentData();

        // 3. Ánh xạ View
        initViews();

        // 4. Cài đặt RecyclerView (Đây là phần thay đổi lớn nhất)
        setupRecyclerView();

        // 5. Khởi tạo ViewModel và Quan sát dữ liệu
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);
        observeData();

        // Nút back quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());
    }

    private void initIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            adultCount = intent.getIntExtra("adultCount", 1);
            childCount = intent.getIntExtra("childCount", 0);
            infantCount = intent.getIntExtra("infantCount", 0);

            if (intent.hasExtra("flightId")) {
                currentFlightId = intent.getStringExtra("flightId");
            } else {
                // ID test mặc định nếu chạy trực tiếp màn hình này
                currentFlightId = "7efa3b09-5db4-4fd6-83fa-43482c2dd844";
            }
        }
    }

    private void initViews() {
        tvRoute = findViewById(R.id.tvRoute);
        tvFlightInfo = findViewById(R.id.tvFlightInfo);
        tvFlightDate = findViewById(R.id.tvFlightDate);
        tvDepartureTimeTimeline = findViewById(R.id.tvDepartureTimeTimeline);
        tvArrivalTimeTimeline = findViewById(R.id.tvArrivalTimeTimeline);
        tvOriginAirport = findViewById(R.id.tvOriginAirport);
        tvDestinationAirport = findViewById(R.id.tvDestinationAirport);
        rvTickets = findViewById(R.id.rvTickets); // ID mới trong Layout
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        listFlightClasses = new ArrayList<>();
        // Khởi tạo Adapter (Sử dụng constructor mới bạn đã sửa)
        adapter = new TicketClassAdapter(this, listFlightClasses, currentFlightId, adultCount, childCount, infantCount);

        // RecyclerView BẮT BUỘC phải có LayoutManager
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getFlightDetailLiveData(currentFlightId).observe(this, flightDetail -> {
            if (flightDetail != null) {
                updateUI(flightDetail);
            } else {
                Toast.makeText(this, "Lỗi: Không tải được dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(com.example.flight_booking_app.booking.model.FlightDetail flightDetail) {
        // Tuyến đường & Sân bay
        tvRoute.setText(flightDetail.getOrigin().getCityCode() + " — " + flightDetail.getDestination().getCityCode());
        tvOriginAirport.setText(flightDetail.getOrigin().getCode() + " " + flightDetail.getOrigin().getName());
        tvDestinationAirport.setText(flightDetail.getDestination().getCode() + " " + flightDetail.getDestination().getName());
        tvFlightInfo.setText(flightDetail.getAirline().getName());

        // Xử lý Ngày tháng & Thời lượng
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("'Depart' EEE, MMM d", Locale.ENGLISH);

            Date depDate = inFormat.parse(flightDetail.getDepartureTime());
            Date arrDate = inFormat.parse(flightDetail.getArrivalTime());

            if (depDate != null && arrDate != null) {
                tvDepartureTimeTimeline.setText(timeFormat.format(depDate));
                tvArrivalTimeTimeline.setText(timeFormat.format(arrDate));

                long diffMs = arrDate.getTime() - depDate.getTime();
                long diffHours = diffMs / (3600000);
                long diffMinutes = (diffMs / 60000) % 60;
                String duration = diffHours + "h " + diffMinutes + "m";

                tvFlightDate.setText(dateFormat.format(depDate) + " • " + duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cập nhật danh sách vé cho RecyclerView
        listFlightClasses.clear();
        listFlightClasses.addAll(flightDetail.getFlightClasses());
        adapter.notifyDataSetChanged();
    }
}