package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.authen.activity.LoginActivity;
import com.example.flight_booking_app.authen.model.DTO.SessionManager;
import com.example.flight_booking_app.booking.adapter.AncillaryAdapter;
import com.example.flight_booking_app.booking.model.AncillaryItem;
import com.example.flight_booking_app.booking.model.AncillaryRequest;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.viewmodel.BookingViewModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình chọn Dịch vụ bổ sung (Hành lý ký gửi, suất ăn...).
 * Đặc biệt: Áp dụng "Lazy Login" - Cho khách chọn thoải mái, bấm Xác nhận mới đòi Đăng nhập.
 */
public class AncillaryActivity extends AppCompatActivity {

    // ==============================================================================
    // 1. KHAI BÁO CÁC THÀNH PHẦN GIAO DIỆN (VIEWS) VÀ VIEWMODEL
    // ==============================================================================
    private RecyclerView rvAncillaries;        // Danh sách dịch vụ
    private TextView tvTotalAncillaryPrice;    // Chữ hiển thị tổng tiền màu đỏ
    private Button btnConfirmAncillaries;      // Nút "Xác nhận" màu tím
    private BookingViewModel viewModel;        // Cầu nối lấy dữ liệu từ Backend

    // ==============================================================================
    // 2. KHAI BÁO DỮ LIỆU CỦA MÀN HÌNH NÀY
    // ==============================================================================
    private List<AncillaryItem> dsAncillary;   // Danh sách các gói hành lý tải từ API về
    private AncillaryAdapter adapter;          // Thợ xây vẽ từng gói hành lý lên màn hình

    // ==============================================================================
    // 3. KHAI BÁO CÁC "THÙNG HÀNG" ĐỂ HỨNG DỮ LIỆU TỪ MÀN HÌNH TRƯỚC TRUYỀN SANG
    // ==============================================================================
    private String[] dsTenHanhKhach;           // Danh sách tên người bay (VD: "Nguyễn Văn A", "Trần Thị B")
    private BookingRequest currentBookingRequest; // Cục dữ liệu khổng lồ (Gồm mã chuyến bay, người bay) chuẩn bị gửi lên Server
    private double basePrice = 0;              // Tổng tiền gốc (Tiền vé máy bay)
    private double ticketPrice = 0;            // Giá của 1 vé (Để sang màn hình sau hiển thị chi tiết)
    private double tongTienDichVu = 0;         // Tiền dịch vụ mua thêm (Khách bấm thêm 1 gói 250k thì cộng vào đây)

    // ==============================================================================
    // 4. BƯU TÁ HỨNG KẾT QUẢ ĐĂNG NHẬP ("LAZY LOGIN")
    // ==============================================================================
    // Khai báo một "Bưu tá" chờ sẵn. Nó sẽ mở màn hình Login lên, và đứng chờ kết quả.
    private final androidx.activity.result.ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Nếu trang Login báo về là Đăng nhập thành công (RESULT_OK)
                        if (result.getResultCode() == RESULT_OK) {
                            Toast.makeText(this, "Đăng nhập thành công! Đang chuyển sang thanh toán...", Toast.LENGTH_SHORT).show();

                            // Đăng nhập xong rồi thì thực hiện tiếp nhiệm vụ: Chạy sang trang Thanh Toán
                            chuyenSangTrangThanhToan();
                        }
                    }
            );


    // ==============================================================================
    // ONCREATE - HÀM CHẠY ĐẦU TIÊN KHI MỞ MÀN HÌNH
    // ==============================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ancillary);

        // 1. Ánh xạ các nút bấm, chữ viết trên giao diện với biến Java
        rvAncillaries = findViewById(R.id.rvAncillaries);
        tvTotalAncillaryPrice = findViewById(R.id.tvTotalAncillaryPrice);
        btnConfirmAncillaries = findViewById(R.id.btnConfirmAncillaries);

        // Dặn RecyclerView hiển thị theo chiều dọc từ trên xuống dưới
        rvAncillaries.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo ViewModel (Giúp giữ data khi xoay màn hình)
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        // 2. Mở thùng hàng lấy dữ liệu màn hình trước gửi sang
        nhanDuLieuTuManHinhTruoc();

        // 3. Gọi API lên server hỏi xem có những gói hành lý nào
        layDuLieuTuApi();

        // 4. Lắng nghe sự kiện khách bấm nút "Xác nhận"
        btnConfirmAncillaries.setOnClickListener(v -> xuLyNutXacNhan());
    }

    // ==============================================================================
    // CÁC HÀM HỖ TRỢ XỬ LÝ LOGIC (CHIA NHỎ ĐỂ DỄ ĐỌC)
    // ==============================================================================

    /**
     * Hàm mở hộp (Intent) lấy đồ từ màn hình Điền thông tin hành khách truyền sang.
     */
    private void nhanDuLieuTuManHinhTruoc() {
        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            dsTenHanhKhach = incomingIntent.getStringArrayExtra("passengerNames");
            currentBookingRequest = (BookingRequest) incomingIntent.getSerializableExtra("bookingRequest");
            basePrice = incomingIntent.getDoubleExtra("basePrice", 0.0);
            ticketPrice = incomingIntent.getDoubleExtra("ticketPrice", 0.0);

            // Có giá gốc rồi thì in lên màn hình luôn (Dù chưa mua dịch vụ nào)
            capNhatTongTien();
        }
    }

    /**
     * Hàm gọi API lấy danh sách dịch vụ (Hành lý).
     */
    private void layDuLieuTuApi() {
        viewModel.getAncillaries().observe(this, result -> {
            if (result != null && !result.isEmpty()) {
                // Tải thành công -> Lưu vào danh sách và vẽ lên màn hình
                dsAncillary = result;
                caiDatAdapter();
            } else {
                Toast.makeText(AncillaryActivity.this, "Không tải được danh sách dịch vụ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hàm cài đặt thợ xây (Adapter) để vẽ danh sách hành lý.
     * Đồng thời lắng nghe xem khách bấm "Thêm" gói nào.
     */
    private void caiDatAdapter() {
        adapter = new AncillaryAdapter(this, dsAncillary, dsTenHanhKhach, new AncillaryAdapter.OnAncillaryAddedListener() {
            @Override
            public void onAdded(AncillaryItem item, int passengerIndex) {

                // 1. Cộng tiền của gói vừa mua vào Tổng tiền dịch vụ, sau đó vẽ lại giá mới lên màn hình
                tongTienDichVu += item.getPrice();
                capNhatTongTien();

                // 2. Tạo một mảnh giấy ghi chú (AncillaryRequest): "Gói ID này, mua cho khách hàng số X"
                AncillaryRequest ancillaryRequest = new AncillaryRequest(item.getId(), passengerIndex, 1);

                // 3. Nhét mảnh giấy đó vào Cục dữ liệu khổng lồ (currentBookingRequest)
                currentBookingRequest.getBookingAncillaries().add(ancillaryRequest);

                Toast.makeText(AncillaryActivity.this,
                        "Đã thêm " + item.getName() + " cho " + dsTenHanhKhach[passengerIndex],
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Giao việc cho thợ xây
        rvAncillaries.setAdapter(adapter);
    }

    /**
     * Hàm tính toán và hiển thị tổng tiền cuối cùng.
     * Tiền cuối = Tiền vé máy bay (basePrice) + Tiền mua thêm hành lý (tongTienDichVu)
     */
    private void capNhatTongTien() {
        double tongCong = basePrice + tongTienDichVu;

        // Định dạng tiền tệ cho đẹp (VD: 1,500,000 đ)
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalAncillaryPrice.setText(formatter.format(tongCong) + " đ");
    }

    /**
     * Xử lý luồng "Lazy Login": Chặn khách lại nếu chưa đăng nhập.
     */
    private void xuLyNutXacNhan() {
        // 1. Lấy "két sắt" SessionManager ra để kiểm tra
        SessionManager sessionManager = new SessionManager(AncillaryActivity.this);

        // 2. Kiểm tra xem trong két sắt có chữ (Token) nào không?
        if (sessionManager.fetchAuthToken() != null && !sessionManager.fetchAuthToken().isEmpty()) {
            // ĐÃ ĐĂNG NHẬP -> Tốt quá, chạy thẳng sang trang Thanh Toán
            chuyenSangTrangThanhToan();
        } else {
            // CHƯA ĐĂNG NHẬP -> Dừng lại! Đẩy khách sang trang Login
            Toast.makeText(AncillaryActivity.this, "Vui lòng đăng nhập để tạo đơn hàng!", Toast.LENGTH_SHORT).show();

            // Khởi tạo xe chở sang Login
            Intent loginIntent = new Intent(AncillaryActivity.this, LoginActivity.class);

            // Dán cái cờ IS_FROM_BOOKING để dặn trang Login: "Ông này đang mua vé dở, login xong nhớ đẩy ổng về đây nhé!"
            loginIntent.putExtra("IS_FROM_BOOKING", true);

            // Dùng cái "Bưu tá" đã tạo ở mục 4 để phóng xe đi
            loginLauncher.launch(loginIntent);
        }
    }

    /**
     * Hàm gom tất cả đồ đạc và phóng xe sang trang Hóa Đơn cuối cùng.
     */
    private void chuyenSangTrangThanhToan() {
        Intent nextIntent = new Intent(AncillaryActivity.this, PaymentSummaryActivity.class);

        // 1. Gửi cục dữ liệu khổng lồ (Đã nhét thêm phần hành lý)
        nextIntent.putExtra("bookingRequest", currentBookingRequest);

        // 2. Gửi tổng tiền dịch vụ mua thêm
        nextIntent.putExtra("tongTienDichVu", tongTienDichVu);

        // 3. Gửi giá của 1 vé gốc
        nextIntent.putExtra("ticketPrice", ticketPrice);

        startActivity(nextIntent);
    }
}