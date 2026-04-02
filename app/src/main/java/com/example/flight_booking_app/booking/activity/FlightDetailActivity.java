package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FlightDetailActivity extends AppCompatActivity {

    // Khai báo các view (Cũ + Mới)
    private TextView tvRoute;
    private TextView tvFlightInfo;
    private ListView lvTickets;

    // Các view mới thêm để hiển thị chi tiết theo UI
    private TextView tvFlightDate;
    private TextView tvDepartureTimeTimeline;
    private TextView tvArrivalTimeTimeline;
    private TextView tvOriginAirport;
    private TextView tvDestinationAirport;

    private TicketClassAdapter adapter;
    private List<FlightClass> listFlightClasses;
    private FlightViewModel viewModel;

    // Fix cứng ID chuyến bay tạm thời
    // Thêm các biến này ở trên cùng
    private int adultCount = 2;
    private int childCount = 0; // Sửa số 0 thành 1
    private int infantCount = 1; // Sửa số 0 thành 1
    private String currentFlightId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flight_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // HỨNG DỮ LIỆU TỪ DEV KIA TRUYỀN SANG
        // HỨNG DỮ LIỆU TỪ DEV KIA (MÀN HÌNH TÌM KIẾM) TRUYỀN SANG
        Intent intent = getIntent();
        if (intent != null) {
            adultCount = intent.getIntExtra("adultCount", 1);
            childCount = intent.getIntExtra("childCount", 0);
            infantCount = intent.getIntExtra("infantCount", 0);

            // THÊM ĐOẠN NÀY VÀO: Hứng ID chuyến bay từ màn hình trước
            if (intent.hasExtra("flightId")) {
                currentFlightId = intent.getStringExtra("flightId");
            } else {
                // NẾU BẠN CHƯA LÀM MÀN HÌNH TÌM KIẾM MÀ CHỈ MUỐN TEST:
                // Lấy đúng cái flightId thật trong Database bạn vừa gửi để Test
                currentFlightId = "7efa3b09-5db4-4fd6-83fa-43482c2dd844";
            }
        }

//        // Truyền cả 3 biến này vào Adapter để lát nữa bấm Select thì Adapter biết đường chuyển sang Form
//        adapter = new TicketClassAdapter(this, listFlightClasses, currentFlightId, adultCount, childCount, infantCount);
//        lvTickets.setAdapter(adapter);


        // 1. Ánh xạ toàn bộ View
        tvRoute = findViewById(R.id.tvRoute);
        tvFlightInfo = findViewById(R.id.tvFlightInfo);
        lvTickets = findViewById(R.id.lvTickets);

        // Nhớ đảm bảo bạn đã thêm các ID này vào file activity_flight_detail.xml nhé
        tvFlightDate = findViewById(R.id.tvFlightDate);
        tvDepartureTimeTimeline = findViewById(R.id.tvDepartureTimeTimeline);
        tvArrivalTimeTimeline = findViewById(R.id.tvArrivalTimeTimeline);
        tvOriginAirport = findViewById(R.id.tvOriginAirport);
        tvDestinationAirport = findViewById(R.id.tvDestinationAirport);

        // 2. Cài đặt ListView và Adapter
        listFlightClasses = new ArrayList<>();
        adapter = new TicketClassAdapter(this, listFlightClasses, currentFlightId, adultCount, childCount, infantCount);
        lvTickets.setAdapter(adapter);

        // 3. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        // 4. Lắng nghe dữ liệu trả về từ API
        observeData();
    }

    private void observeData() {
        viewModel.getFlightDetailLiveData(currentFlightId).observe(this, flightDetail -> {
            if (flightDetail != null) {

                // --- ĐỔ DỮ LIỆU TEXT ĐƠN GIẢN ---
                // Tuyến đường (BOM -> CCU)
                tvRoute.setText(flightDetail.getOrigin().getCityCode() + " — " + flightDetail.getDestination().getCityCode());

                // Tên sân bay chi tiết
                tvOriginAirport.setText(flightDetail.getOrigin().getCode() + " " + flightDetail.getOrigin().getName());
                tvDestinationAirport.setText(flightDetail.getDestination().getCode() + " " + flightDetail.getDestination().getName());

                // Hãng bay
                tvFlightInfo.setText(flightDetail.getAirline().getName());

                // --- XỬ LÝ NGÀY GIỜ PHỨC TẠP TỪ API ---
                try {
                    // Khuôn đọc dữ liệu gốc: "2026-03-31T02:00:00"
                    SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                    // Khuôn hiển thị giờ (Ví dụ: "02:00")
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    // Khuôn hiển thị ngày (Ví dụ: "Depart Tue, Mar 31")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("'Depart' EEE, MMM d", Locale.ENGLISH);

                    // Chuyển chuỗi thành đối tượng Date của Java
                    Date depDate = inFormat.parse(flightDetail.getDepartureTime());
                    Date arrDate = inFormat.parse(flightDetail.getArrivalTime());

                    if (depDate != null && arrDate != null) {
                        // Gán giờ vào 2 mốc trên Timeline
                        tvDepartureTimeTimeline.setText(timeFormat.format(depDate));
                        tvArrivalTimeTimeline.setText(timeFormat.format(arrDate));

                        // Tính khoảng thời gian bay (Thời lượng)
                        long diffMs = arrDate.getTime() - depDate.getTime();
                        long diffHours = diffMs / (60 * 60 * 1000);
                        long diffMinutes = (diffMs / (60 * 1000)) % 60;
                        String duration = diffHours + "h " + diffMinutes + "m";

                        // Ghép chuỗi ngày + thời lượng lên text trên cùng
                        tvFlightDate.setText(dateFormat.format(depDate) + " • " + duration);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // --- CẬP NHẬT DANH SÁCH VÉ ---
                listFlightClasses.clear();
                listFlightClasses.addAll(flightDetail.getFlightClasses());
                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(FlightDetailActivity.this, "Lỗi: Không tải được dữ liệu chuyến bay!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}