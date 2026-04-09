package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.TicketClassAdapter;
import com.example.flight_booking_app.booking.model.FlightClass;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.viewmodel.BookingViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lớp FlightDetailActivity đại diện cho màn hình "Chi tiết chuyến bay".
 * Hiển thị thời gian bay, tuyến đường và danh sách các HẠNG VÉ để khách chọn.
 */
public class FlightDetailActivity extends AppCompatActivity {

    // =========================================================================
    // 1. KHAI BÁO GIAO DIỆN & VIEWMODEL
    // =========================================================================
    private TextView tvRoute, tvFlightInfo, tvFlightDate;
    private TextView tvDepartureTimeTimeline, tvArrivalTimeTimeline;
    private TextView tvOriginAirport, tvDestinationAirport;
    private ImageView btnBack;

    private RecyclerView rvTickets;
    private TicketClassAdapter adapter;
    private List<FlightClass> listFlightClasses;

    private BookingViewModel viewModel;

    // =========================================================================
    // 2. BIẾN HỨNG DỮ LIỆU TỪ MÀN HÌNH TÌM KIẾM (SEARCH RESULT)
    // =========================================================================
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 0;
    private String currentFlightId;

    // ⚡ CÁC CỜ VÀ BIẾN LƯU TẠM DÀNH CHO KHỨ HỒI ⚡
    private boolean isRoundTrip = false;
    private boolean isSelectingReturnFlight = false;
    private String outboundFlightId;
    private String outboundFlightClassId;
    private double outboundTicketPrice;

    // =========================================================================
    // VÒNG ĐỜI ONCREATE
    // =========================================================================
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

        // 1. Ánh xạ View
        initViews();

        // 2. Hứng dữ liệu Intent gửi sang
        initIntentData();

        // 3. Khởi tạo danh sách Hạng Vé (Nơi chứa logic rẽ nhánh)
        setupRecyclerView();

        // 4. Gọi API lấy chi tiết chuyến bay
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        observeData();

        btnBack.setOnClickListener(v -> finish());
    }

    // =========================================================================
    // CÁC HÀM HỖ TRỢ XỬ LÝ LOGIC
    // =========================================================================

    private void initViews() {
        tvRoute = findViewById(R.id.tvRoute);
        tvFlightInfo = findViewById(R.id.tvFlightInfo);
        tvFlightDate = findViewById(R.id.tvFlightDate);
        tvDepartureTimeTimeline = findViewById(R.id.tvDepartureTimeTimeline);
        tvArrivalTimeTimeline = findViewById(R.id.tvArrivalTimeTimeline);
        tvOriginAirport = findViewById(R.id.tvOriginAirport);
        tvDestinationAirport = findViewById(R.id.tvDestinationAirport);
        rvTickets = findViewById(R.id.rvTickets);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initIntentData() {
        Intent intent = getIntent();

        if (intent != null) {
            adultCount = intent.getIntExtra("ADULT_COUNT", 1);
            childCount = intent.getIntExtra("CHILD_COUNT", 0);
            infantCount = intent.getIntExtra("INFANT_COUNT", 0);

            if (intent.hasExtra("FLIGHT_ID")) {
                currentFlightId = intent.getStringExtra("FLIGHT_ID");
            }

            // HỨNG DỮ LIỆU KHỨ HỒI
            isRoundTrip = intent.getBooleanExtra("IS_ROUND_TRIP", false);
            isSelectingReturnFlight = intent.getBooleanExtra("IS_SELECTING_RETURN_FLIGHT", false);

            if (isSelectingReturnFlight) {
                outboundFlightId = intent.getStringExtra("OUTBOUND_FLIGHT_ID");
                outboundFlightClassId = intent.getStringExtra("OUTBOUND_FLIGHT_CLASS_ID");
                outboundTicketPrice = intent.getDoubleExtra("OUTBOUND_TICKET_PRICE", 0.0);
            }
        }
    }

    /**
     * ⚡ ĐÃ SỬA TOÀN BỘ: Nơi cài đặt Adapter và xử lý logic điều hướng khi chọn vé ⚡
     */
    private void setupRecyclerView() {
        listFlightClasses = new ArrayList<>();

        // Sử dụng Interface (Bộ đàm) để lắng nghe sự kiện từ TicketClassAdapter
        adapter = new TicketClassAdapter(this, listFlightClasses, ticket -> {

            // ==========================================================
            // TRƯỜNG HỢP 1: LÀ KHỨ HỒI VÀ ĐANG CHỌN CHIỀU ĐI
            // ==========================================================
            if (isRoundTrip && !isSelectingReturnFlight) {

                Toast.makeText(this, "Đã chọn xong Chiều đi. Vui lòng chọn chuyến Về!", Toast.LENGTH_SHORT).show();

                // Quay lại trang SearchResultActivity để chọn chuyến về
                // (Đảm bảo đường dẫn tới SearchResultActivity của bạn là đúng)
                Intent intent = new Intent(FlightDetailActivity.this, com.example.flight_booking_app.home.activity.SearchResultActivity.class);

                // Báo hiệu: "Lần tới mở lên là chọn chuyến về nhé"
                intent.putExtra("IS_ROUND_TRIP", true);
                intent.putExtra("IS_SELECTING_RETURN_FLIGHT", true);

                // Đóng gói thông tin chuyến đi mang theo
                intent.putExtra("OUTBOUND_FLIGHT_ID", currentFlightId);
                intent.putExtra("OUTBOUND_FLIGHT_CLASS_ID", ticket.getId());
                intent.putExtra("OUTBOUND_TICKET_PRICE", ticket.getBasePrice());

                // Gửi kèm số lượng hành khách
                intent.putExtra("ADULT_COUNT", adultCount);
                intent.putExtra("CHILD_COUNT", childCount);
                intent.putExtra("INFANT_COUNT", infantCount);

                startActivity(intent);

            }
            // ==========================================================
            // TRƯỜNG HỢP 2: VÉ 1 CHIỀU --- HOẶC --- ĐÃ XONG CẢ 2 CHIỀU
            // ==========================================================
            else {
                // Sang thẳng màn hình điền thông tin (BookingFormActivity)
                Intent intent = new Intent(FlightDetailActivity.this, BookingFormActivity.class);

                intent.putExtra("adultCount", adultCount);
                intent.putExtra("childCount", childCount);
                intent.putExtra("infantCount", infantCount);
                intent.putExtra("isRoundTrip", isRoundTrip);

                // Nếu đang là Khứ hồi (Chiều về) -> Gửi cả 2 vé sang
                if (isRoundTrip && isSelectingReturnFlight) {
                    // Chuyến hiện tại là chiều về
                    intent.putExtra("returnFlightId", currentFlightId);
                    intent.putExtra("returnFlightClassId", ticket.getId());
                    intent.putExtra("returnTicketPrice", ticket.getBasePrice());

                    // Chuyến lúc nãy mang theo là chiều đi
                    intent.putExtra("flightId", outboundFlightId);
                    intent.putExtra("flightClassId", outboundFlightClassId);
                    intent.putExtra("ticketPrice", outboundTicketPrice);
                }
                // Nếu là vé 1 chiều bình thường
                else {
                    intent.putExtra("flightId", currentFlightId);
                    intent.putExtra("flightClassId", ticket.getId());
                    intent.putExtra("ticketPrice", ticket.getBasePrice());
                }

                startActivity(intent);
            }
        });

        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(adapter);
    }

    private void observeData() {
        if (currentFlightId != null) {
            viewModel.getFlightDetailLiveData(currentFlightId).observe(this, flightDetail -> {
                if (flightDetail != null) {
                    updateUI(flightDetail);
                } else {
                    Toast.makeText(this, "Không tải được dữ liệu chi tiết!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy Mã chuyến bay!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(FlightDetail flightDetail) {
        tvRoute.setText(flightDetail.getOrigin().getCityCode() + " — " + flightDetail.getDestination().getCityCode());
        tvOriginAirport.setText(flightDetail.getOrigin().getCode() + " " + flightDetail.getOrigin().getName());
        tvDestinationAirport.setText(flightDetail.getDestination().getCode() + " " + flightDetail.getDestination().getName());
        tvFlightInfo.setText(flightDetail.getAirline().getName());

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

        listFlightClasses.clear();
        listFlightClasses.addAll(flightDetail.getFlightClasses());
        adapter.notifyDataSetChanged();
    }
}