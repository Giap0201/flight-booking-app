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
import com.example.flight_booking_app.booking.adapter.FlightClassAdapter;
import com.example.flight_booking_app.booking.model.FlightClass;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.viewmodel.BookingViewModel;
import com.example.flight_booking_app.search.activity.SearchResultActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lớp FlightDetailActivity đại diện cho màn hình "Chi tiết chuyến bay".
 * Nhiệm vụ chính: Hiện thông tin giờ bay và danh sách HẠNG VÉ để khách bấm "Chọn".
 * Đặc biệt: Có xử lý logic rẽ nhánh thông minh cho vé Khứ Hồi và 1 Chiều.
 */
public class FlightClassActivity extends AppCompatActivity {

    // =========================================================================
    // 1. KHAI BÁO CÁC THÀNH PHẦN GIAO DIỆN (VIEWS) VÀ VIEWMODEL
    // =========================================================================
    private TextView tvRoute, tvFlightInfo, tvFlightDate;
    private TextView tvDepartureTimeTimeline, tvArrivalTimeTimeline;
    private TextView tvOriginAirport, tvDestinationAirport;
    private ImageView btnBack;

    private RecyclerView rvTickets;
    private FlightClassAdapter adapter;
    private List<FlightClass> listFlightClasses;

    private BookingViewModel viewModel; // Cầu nối lấy dữ liệu từ Backend (Spring Boot)

    // =========================================================================
    // 2. BIẾN HỨNG DỮ LIỆU TỪ MÀN HÌNH TÌM KIẾM (SEARCH RESULT)
    // =========================================================================
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 0;
    private String currentFlightId; // Mã chuyến bay đang xem (VD: "FL123")

    // =========================================================================
    // 3. CÁC CỜ VÀ BIẾN "LƯU NHÁP" DÀNH RIÊNG CHO KHỨ HỒI
    // =========================================================================
    private boolean isRoundTrip = false;              // Có phải khách đang mua vé Khứ hồi không?
    private boolean isSelectingReturnFlight = false;  // Có phải đang ở bước chọn vé Chiều Về không?

    // Nếu đang chọn Chiều Về, mình phải cầm hộ 3 thông tin của Chiều Đi mà khách vừa chọn lúc nãy
    private String outboundFlightId;       // Mã chuyến đi
    private String outboundFlightClassId;  // Hạng vé chuyến đi (VD: Eco, Thương gia)
    private double outboundTicketPrice;    // Giá vé chuyến đi

    // =========================================================================
    // VÒNG ĐỜI ONCREATE (Hàm chạy đầu tiên khi mở màn hình này)
    // =========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flight_class);

        // Chỉnh padding để giao diện không bị tai thỏ (notch) đè lên
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Ánh xạ View (Tìm các nút, chữ trên giao diện XML gắn vào biến Java)
        initViews();

        // 2. Mở hộp Intent lấy đồ từ màn hình Search gửi sang
        initIntentData();

        // 3. Cài đặt thợ xây (Adapter) để vẽ danh sách Hạng Vé (Và chứa logic bấm chọn vé)
        setupRecyclerView();

        // 4. Nhờ ViewModel gọi API tải thông tin chi tiết chuyến bay này về
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        observeData();

        // 5. Nút mũi tên quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    // =========================================================================
    // CÁC HÀM HỖ TRỢ XỬ LÝ LOGIC (Chia nhỏ ra cho code dễ đọc)
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
            // Lấy số lượng hành khách
            adultCount = intent.getIntExtra("ADULT_COUNT", 1);
            childCount = intent.getIntExtra("CHILD_COUNT", 0);
            infantCount = intent.getIntExtra("INFANT_COUNT", 0);

            // Lấy ID chuyến bay
            if (intent.hasExtra("FLIGHT_ID")) {
                currentFlightId = intent.getStringExtra("FLIGHT_ID");
            }

            // HỨNG DỮ LIỆU KHỨ HỒI
            isRoundTrip = intent.getBooleanExtra("IS_ROUND_TRIP", false);
            isSelectingReturnFlight = intent.getBooleanExtra("IS_SELECTING_RETURN_FLIGHT", false);

            // Nếu đang chọn lượt về, lục ví lấy đồ của lượt đi ra cất vào biến
            if (isSelectingReturnFlight) {
                outboundFlightId = intent.getStringExtra("OUTBOUND_FLIGHT_ID");
                outboundFlightClassId = intent.getStringExtra("OUTBOUND_FLIGHT_CLASS_ID");
                outboundTicketPrice = intent.getDoubleExtra("OUTBOUND_TICKET_PRICE", 0.0);
            }
        }
    }

    /**
     * TẠO DANH SÁCH HẠNG VÉ VÀ XỬ LÝ SỰ KIỆN KHI KHÁCH BẤM "CHỌN VÉ NÀY"
     */
    private void setupRecyclerView() {
        listFlightClasses = new ArrayList<>();

        // Sử dụng Interface (Bộ đàm) để lắng nghe xem Adapter báo khách chọn vé nào
        adapter = new FlightClassAdapter(this, listFlightClasses, ticket -> {

            // ==========================================================
            // TRƯỜNG HỢP 1: LÀ KHỨ HỒI VÀ ĐANG CHỌN CHIỀU ĐI
            // Khách mới chọn được nửa đường, bắt quay lại trang Search chọn tiếp Chiều Về
            // ==========================================================
            if (isRoundTrip && !isSelectingReturnFlight) {

                Toast.makeText(this, "Đã chọn xong Chiều đi. Vui lòng chọn chuyến Về!", Toast.LENGTH_SHORT).show();

                // Quay lại trang SearchResultActivity
                Intent intent = new Intent(FlightClassActivity.this, SearchResultActivity.class);

                // Báo hiệu cờ: "Ông Search ơi, mở lên tìm chuyến về nhé"
                intent.putExtra("IS_ROUND_TRIP", true);
                intent.putExtra("IS_SELECTING_RETURN_FLIGHT", true);

                // ⚡ ÉP KIỂU (double) CỰC KỲ QUAN TRỌNG ĐỂ KHÔNG BỊ LỖI 0 Đ ⚡
                // Đóng gói thông tin chuyến đi mang theo
                intent.putExtra("OUTBOUND_FLIGHT_ID", currentFlightId);
                intent.putExtra("OUTBOUND_FLIGHT_CLASS_ID", ticket.getId());
                intent.putExtra("OUTBOUND_TICKET_PRICE", (double) ticket.getBasePrice());

                // Gửi kèm số lượng hành khách
                intent.putExtra("ADULT_COUNT", adultCount);
                intent.putExtra("CHILD_COUNT", childCount);
                intent.putExtra("INFANT_COUNT", infantCount);

                startActivity(intent);

            }
            // ==========================================================
            // TRƯỜNG HỢP 2: VÉ 1 CHIỀU --- HOẶC --- ĐÃ CHỌN XONG CẢ 2 CHIỀU CỦA KHỨ HỒI
            // Đã đủ điều kiện, phóng xe sang trang Điền Thông Tin (BookingForm)
            // ==========================================================
            else {
                Intent intent = new Intent(FlightClassActivity.this, BookingFormActivity.class);

                intent.putExtra("adultCount", adultCount);
                intent.putExtra("childCount", childCount);
                intent.putExtra("infantCount", infantCount);
                intent.putExtra("isRoundTrip", isRoundTrip);

                // NẾU ĐANG LÀ KHỨ HỒI (Lượt về) -> Phải gửi cả 2 vé sang cho Form
                if (isRoundTrip && isSelectingReturnFlight) {
                    // 1. Chuyến hiện tại khách vừa bấm chính là Chiều Về
                    intent.putExtra("returnFlightId", currentFlightId);
                    intent.putExtra("returnFlightClassId", ticket.getId());
                    intent.putExtra("returnTicketPrice", (double) ticket.getBasePrice()); // ⚡ Ép kiểu (double)

                    // 2. Chuyến lúc nãy mình cầm hộ ở trong ví chính là Chiều Đi
                    intent.putExtra("flightId", outboundFlightId);
                    intent.putExtra("flightClassId", outboundFlightClassId);
                    intent.putExtra("ticketPrice", (double) outboundTicketPrice); // ⚡ Ép kiểu (double)
                }
                // NẾU LÀ VÉ 1 CHIỀU BÌNH THƯỜNG -> Chỉ có 1 vé gửi đi thôi
                else {
                    intent.putExtra("flightId", currentFlightId);
                    intent.putExtra("flightClassId", ticket.getId());
                    intent.putExtra("ticketPrice", (double) ticket.getBasePrice()); // ⚡ Ép kiểu (double)
                }

                startActivity(intent);
            }
        });

        // Báo cho danh sách biết là hãy sắp xếp theo chiều dọc
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(adapter);
    }

    /**
     * LẮNG NGHE DỮ LIỆU TỪ SERVER TRẢ VỀ
     */
    private void observeData() {
        if (currentFlightId != null) {
            viewModel.getFlightDetailLiveData(currentFlightId).observe(this, flightDetail -> {
                if (flightDetail != null) {
                    // Nếu có data thì đem vẽ lên giao diện
                    updateUI(flightDetail);
                } else {
                    Toast.makeText(this, "Không tải được dữ liệu chi tiết!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy Mã chuyến bay!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * VẼ DỮ LIỆU LÊN MÀN HÌNH SAU KHI TẢI XONG
     */
    private void updateUI(FlightDetail flightDetail) {
        // Gắn chữ cơ bản
        tvRoute.setText(flightDetail.getOrigin().getCityCode() + " — " + flightDetail.getDestination().getCityCode());
        tvOriginAirport.setText(flightDetail.getOrigin().getCode() + " " + flightDetail.getOrigin().getName());
        tvDestinationAirport.setText(flightDetail.getDestination().getCode() + " " + flightDetail.getDestination().getName());
        tvFlightInfo.setText(flightDetail.getAirline().getName());

        // Xử lý Ngày Giờ (Rất dễ văng App nên phải bọc bằng try-catch)
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("'Depart' EEE, MMM d", Locale.ENGLISH);

            Date depDate = inFormat.parse(flightDetail.getDepartureTime());
            Date arrDate = inFormat.parse(flightDetail.getArrivalTime());

            if (depDate != null && arrDate != null) {
                // In giờ ra giao diện
                tvDepartureTimeTimeline.setText(timeFormat.format(depDate));
                tvArrivalTimeTimeline.setText(timeFormat.format(arrDate));

                // Tính toán thời gian bay (VD: 2h 30m)
                long diffMs = arrDate.getTime() - depDate.getTime();
                long diffHours = diffMs / (3600000);
                long diffMinutes = (diffMs / 60000) % 60;

                String duration = diffHours + "h " + diffMinutes + "m";
                tvFlightDate.setText(dateFormat.format(depDate) + " • " + duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Báo cho Adapter biết là có dữ liệu Hạng vé mới rồi, vẽ lên đi
        listFlightClasses.clear();
        listFlightClasses.addAll(flightDetail.getFlightClasses());
        adapter.notifyDataSetChanged();
    }
}