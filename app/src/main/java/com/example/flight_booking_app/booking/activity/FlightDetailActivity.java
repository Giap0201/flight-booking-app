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
import com.example.flight_booking_app.home.model.Flight;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lớp FlightDetailActivity đại diện cho màn hình "Chi tiết chuyến bay".
 * Kế thừa AppCompatActivity: Nghĩa là nó là một màn hình Android có hỗ trợ các tính năng hiện đại.
 */
public class FlightDetailActivity extends AppCompatActivity {

    // =========================================================================
    // BƯỚC 1: KHAI BÁO CÁC BIẾN (VIEW VÀ DỮ LIỆU)
    // =========================================================================

    // Khai báo các View (Giao diện) để lát nữa liên kết với file XML (layout)
    private TextView tvRoute;               // Hiển thị tuyến đường (VD: HAN - SGN)
    private TextView tvFlightInfo;          // Tên hãng hàng không (VD: Vietnam Airlines)
    private TextView tvFlightDate;          // Ngày bay và thời gian bay
    private TextView tvDepartureTimeTimeline; // Giờ cất cánh (trên trục thời gian)
    private TextView tvArrivalTimeTimeline;   // Giờ hạ cánh (trên trục thời gian)
    private TextView tvOriginAirport;       // Sân bay đi (Mã + Tên)
    private TextView tvDestinationAirport;  // Sân bay đến (Mã + Tên)
    private ImageView btnBack;              // Nút quay lại (Mũi tên back)

    // Khai báo các thành phần cho danh sách (RecyclerView)
    private RecyclerView rvTickets;         // View dùng để hiển thị danh sách cuộn mượt mà
    private TicketClassAdapter adapter;     // "Cầu nối" đưa dữ liệu vào RecyclerView
    private List<FlightClass> listFlightClasses; // Danh sách chứa dữ liệu các hạng vé

    // Biến quản lý dữ liệu (ViewModel - Giúp giữ dữ liệu không bị mất khi xoay màn hình)
    private BookingViewModel viewModel;

    // Biến lưu trữ dữ liệu nhận từ màn hình trước chuyển sang (qua Intent)
    private int adultCount = 1;      // Số người lớn (mặc định 1)
    private int childCount = 0;      // Số trẻ em (mặc định 0)
    private int infantCount = 0;     // Số em bé (mặc định 0)
    private String currentFlightId;  // ID của chuyến bay cần xem chi tiết


    // =========================================================================
    // BƯỚC 2: HÀM ONCREATE - NƠI BẮT ĐẦU CỦA MỌI MÀN HÌNH
    // =========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Bắt buộc phải có

        // Cấu hình màn hình tràn viền (ẩn thanh trạng thái phía trên nếu cần)
        EdgeToEdge.enable(this);

        // Gắn file giao diện XML (activity_flight_detail.xml) vào class Java này
        setContentView(R.layout.activity_flight_detail);

        // Chỉnh sửa lại khoảng cách (padding) để nội dung không bị đè lên tai thỏ (notch) hoặc thanh điều hướng
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Chạy lần lượt các hàm khởi tạo từ trên xuống dưới
        initViews();         // Tìm và ánh xạ các thành phần giao diện

        initIntentData();    // Lấy dữ liệu từ màn hình cũ
        setupRecyclerView(); // Cài đặt danh sách hạng vé

        // Khởi tạo ViewModel (Quản lý dữ liệu tập trung)
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        observeData();       // Lắng nghe dữ liệu tải về từ mạng/database

        // Bắt sự kiện khi người dùng bấm vào nút Back
        btnBack.setOnClickListener(v -> {
            finish(); // finish() là lệnh đóng màn hình hiện tại, tự động quay về màn hình trước đó
        });
    }

    // =========================================================================
    // CÁC HÀM HỖ TRỢ (Được gọi trong onCreate)
    // =========================================================================

    /**
     * Hàm này dùng để lấy dữ liệu do màn hình trước gửi sang.
     * Ở Android, "Intent" giống như một người đưa thư giữa các màn hình.
     */
    /**
     * Hàm này dùng để lấy dữ liệu do màn hình trước gửi sang.
     */
    private void initIntentData() {
        Intent intent = getIntent(); // Nhận "bức thư" (Intent) gửi đến màn hình này

        if (intent != null) {
            // ⚡ ĐÃ SỬA: Viết HOA các Key để khớp chính xác với SearchResultActivity ⚡
            adultCount = intent.getIntExtra("ADULT_COUNT", 1);
            childCount = intent.getIntExtra("CHILD_COUNT", 0);
            infantCount = intent.getIntExtra("INFANT_COUNT", 0);

            // ⚡ ĐÃ SỬA: Tìm đúng Key "FLIGHT_ID" ⚡
            // Hứng cục dữ liệu
            if (intent.hasExtra("FLIGHT_ID")) {
                currentFlightId = intent.getStringExtra("FLIGHT_ID"); // Lúc này currentFlightId sẽ chứa "NS8118"
            }
        }
    }

    /**
     * Hàm này tìm các thẻ XML bên giao diện và gán vào các biến Java đã khai báo ở trên.
     * R.id.xxx chính là ID bạn đặt trong file XML.
     */
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

    /**
     * Để RecyclerView hoạt động, nó cần 3 thứ:
     * 1. Danh sách dữ liệu rỗng (listFlightClasses)
     * 2. LayoutManager: Định dạng cách hiển thị (Dọc, ngang, hay lưới?)
     * 3. Adapter: Thợ xây dựng giao diện cho từng mục trong danh sách.
     */
    private void setupRecyclerView() {
        // 1. Tạo một cái giỏ trống để đựng dữ liệu
        listFlightClasses = new ArrayList<>();

        // 2. Tạo Adapter và truyền cho nó cái giỏ rỗng, cộng thêm các thông tin vé
        adapter = new TicketClassAdapter(this, listFlightClasses, currentFlightId, adultCount, childCount, infantCount);

        // 3. Nói cho RecyclerView biết: "Hãy hiển thị danh sách theo chiều dọc (LinearLayoutManager)"
        rvTickets.setLayoutManager(new LinearLayoutManager(this));

        // 4. Gắn Adapter vào RecyclerView
        rvTickets.setAdapter(adapter);
    }

    /**
     * Hàm này lắng nghe dữ liệu từ ViewModel.
     * Giống như bạn đăng ký nhận thông báo, khi nào ViewModel tải xong dữ liệu chuyến bay,
     * nó sẽ "báo cáo" về đây qua biến 'flightDetail'.
     */
    private void observeData() {
        // Gọi hàm getFlightDetailLiveData từ ViewModel và quan sát (observe) nó
        viewModel.getFlightDetailLiveData(currentFlightId).observe(this, flightDetail -> {
            // Đoạn code bên trong này sẽ tự động chạy MỖI KHI dữ liệu tải xong hoặc có thay đổi
            if (flightDetail != null) {
                // Nếu có dữ liệu, đem đi hiển thị lên giao diện
                updateUI(flightDetail);
            } else {
                // Nếu bị rỗng (lỗi mạng, sai ID...), hiện thông báo lỗi nhỏ (Toast)
                Toast.makeText(this, "Lỗi: Không tải được dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hàm này lấy dữ liệu thực tế ghép vào các View trên màn hình.
     */
    private void updateUI(FlightDetail flightDetail) {

        // 1. Hiển thị text cơ bản (Tuyến đường, Sân bay, Hãng bay)
        tvRoute.setText(flightDetail.getOrigin().getCityCode() + " — " + flightDetail.getDestination().getCityCode());
        tvOriginAirport.setText(flightDetail.getOrigin().getCode() + " " + flightDetail.getOrigin().getName());
        tvDestinationAirport.setText(flightDetail.getDestination().getCode() + " " + flightDetail.getDestination().getName());
        tvFlightInfo.setText(flightDetail.getAirline().getName());

        // 2. Xử lý thời gian (Đây là phần hay xảy ra lỗi nên cần bọc trong try-catch)
        try {
            // Định dạng chuỗi thời gian GỐC nhận từ Server (VD: 2024-12-01T15:30:00)
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            // Định dạng ĐẦU RA muốn hiển thị (VD: 15:30)
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            // Định dạng ĐẦU RA cho ngày (VD: Depart Mon, Dec 1)
            SimpleDateFormat dateFormat = new SimpleDateFormat("'Depart' EEE, MMM d", Locale.ENGLISH);

            // Chuyển chuỗi chữ thành đối tượng Date của Java
            Date depDate = inFormat.parse(flightDetail.getDepartureTime()); // Giờ cất cánh
            Date arrDate = inFormat.parse(flightDetail.getArrivalTime());   // Giờ hạ cánh

            // Nếu chuyển đổi thành công
            if (depDate != null && arrDate != null) {
                // In giờ ra giao diện
                tvDepartureTimeTimeline.setText(timeFormat.format(depDate));
                tvArrivalTimeTimeline.setText(timeFormat.format(arrDate));

                // TÍNH THỜI GIAN BAY (Khoảng cách giữa cất cánh và hạ cánh)
                long diffMs = arrDate.getTime() - depDate.getTime(); // Tính ra số mili-giây
                long diffHours = diffMs / (3600000);                 // Đổi mili-giây ra Giờ (1 giờ = 3.600.000 ms)
                long diffMinutes = (diffMs / 60000) % 60;            // Lấy phần dư đổi ra Phút

                String duration = diffHours + "h " + diffMinutes + "m"; // Ghép thành chuỗi "2h 30m"

                // In ngày và thời gian bay ra giao diện
                tvFlightDate.setText(dateFormat.format(depDate) + " • " + duration);
            }
        } catch (Exception e) {
            // Nếu chuỗi thời gian từ server gửi về bị sai định dạng, app sẽ chạy vào đây thay vì bị văng (crash)
            e.printStackTrace(); // In lỗi ra màn hình Logcat cho lập trình viên sửa
        }

        // 3. Cập nhật dữ liệu cho RecyclerView (Danh sách hạng vé)
        listFlightClasses.clear(); // Xóa sạch dữ liệu cũ trong giỏ (nếu có)
        listFlightClasses.addAll(flightDetail.getFlightClasses()); // Đổ toàn bộ dữ liệu mới tải về vào giỏ

        // BÁO CHO ADAPTER BIẾT: "Ê, giỏ dữ liệu có đồ mới rồi, vẽ lại danh sách trên màn hình đi!"
        adapter.notifyDataSetChanged();
    }
}