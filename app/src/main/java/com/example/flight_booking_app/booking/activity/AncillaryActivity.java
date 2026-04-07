package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class AncillaryActivity extends AppCompatActivity {

    private RecyclerView rvAncillaries;
    private TextView tvTotalAncillaryPrice;
    private Button btnConfirmAncillaries;
    private BookingViewModel viewModel;

    // ⚡ MỚI THÊM: Các View để chuyển Tab (Chiều đi / Chiều về)
    private LinearLayout layoutTabsKhuHoi;
    private Button btnTabChieuDi;
    private Button btnTabChieuVe;

    private List<AncillaryItem> dsAncillary;
    private AncillaryAdapter adapter;

    private String[] dsTenHanhKhach;
    private BookingRequest currentBookingRequest;
    private double basePrice = 0;
    private double ticketPrice = 0;
    private double tongTienDichVu = 0;

    // ⚡ MỚI THÊM: Các biến kiểm soát luồng Khứ Hồi
    private boolean isRoundTrip = false; // Nhận từ màn hình trước
    private int currentSegmentNo = 1;    // 1: Chiều đi, 2: Chiều về

    private final androidx.activity.result.ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Toast.makeText(this, "Đăng nhập thành công! Đang chuyển sang thanh toán...", Toast.LENGTH_SHORT).show();
                            chuyenSangTrangThanhToan();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ancillary);

        rvAncillaries = findViewById(R.id.rvAncillaries);
        tvTotalAncillaryPrice = findViewById(R.id.tvTotalAncillaryPrice);
        btnConfirmAncillaries = findViewById(R.id.btnConfirmAncillaries);

        // ⚡ MỚI THÊM: Ánh xạ 2 nút Tab (Bạn cần thêm vào XML ở Bước 2 nhé)
        layoutTabsKhuHoi = findViewById(R.id.layoutTabsKhuHoi);
        btnTabChieuDi = findViewById(R.id.btnTabChieuDi);
        btnTabChieuVe = findViewById(R.id.btnTabChieuVe);

        rvAncillaries.setLayoutManager(new LinearLayoutManager(this));
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        nhanDuLieuTuManHinhTruoc();

        // ⚡ MỚI THÊM: Thiết lập giao diện Tab dựa trên loại vé
        setupTabsKhuHoi();

        layDuLieuTuApi();

        btnConfirmAncillaries.setOnClickListener(v -> xuLyNutXacNhan());
    }

    private void nhanDuLieuTuManHinhTruoc() {
        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            dsTenHanhKhach = incomingIntent.getStringArrayExtra("passengerNames");
            currentBookingRequest = (BookingRequest) incomingIntent.getSerializableExtra("bookingRequest");
            basePrice = incomingIntent.getDoubleExtra("basePrice", 0.0);
            ticketPrice = incomingIntent.getDoubleExtra("ticketPrice", 0.0);

            // ⚡ MỚI THÊM: Nhận cờ kiểm tra Khứ Hồi
            isRoundTrip = incomingIntent.getBooleanExtra("isRoundTrip", false);

            capNhatTongTien();
        }
    }

    // ⚡ MỚI THÊM: Logic chuyển đổi giữa 2 chiều bay
    private void setupTabsKhuHoi() {
        if (layoutTabsKhuHoi == null) return; // Đề phòng layout XML chưa cập nhật

        if (isRoundTrip) {
            // NẾU LÀ KHỨ HỒI: Hiện 2 nút bấm lên
            layoutTabsKhuHoi.setVisibility(View.VISIBLE);

            // Bấm nút Chiều Đi
            btnTabChieuDi.setOnClickListener(v -> {
                currentSegmentNo = 1;
                Toast.makeText(this, "Đang chọn Hành lý Chiều Đi", Toast.LENGTH_SHORT).show();
                // Ở đây bạn có thể đổi màu nút để khách biết đang ở tab nào
            });

            // Bấm nút Chiều Về
            btnTabChieuVe.setOnClickListener(v -> {
                currentSegmentNo = 2;
                Toast.makeText(this, "Đang chọn Hành lý Chiều Về", Toast.LENGTH_SHORT).show();
            });

        } else {
            // NẾU LÀ 1 CHIỀU: Giấu 2 nút bấm đi, mặc định luông là chiều đi
            layoutTabsKhuHoi.setVisibility(View.GONE);
            currentSegmentNo = 1;
        }
    }

    private void layDuLieuTuApi() {
        viewModel.getAncillaries().observe(this, result -> {
            if (result != null && !result.isEmpty()) {
                dsAncillary = result;
                caiDatAdapter();
            } else {
                Toast.makeText(AncillaryActivity.this, "Không tải được danh sách dịch vụ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void caiDatAdapter() {
        adapter = new AncillaryAdapter(this, dsAncillary, dsTenHanhKhach, new AncillaryAdapter.OnAncillaryAddedListener() {
            @Override
            public void onAdded(AncillaryItem item, int passengerIndex) {

                tongTienDichVu += item.getPrice();
                capNhatTongTien();

                // ⚡ ĐÃ SỬA: Thay số 1 cứng nhắc bằng biến currentSegmentNo (Linh hoạt theo Tab đang chọn) ⚡
                AncillaryRequest ancillaryRequest = new AncillaryRequest(item.getId(), passengerIndex, currentSegmentNo);

                currentBookingRequest.getBookingAncillaries().add(ancillaryRequest);

                String chieuBay = (currentSegmentNo == 1) ? "Chiều đi" : "Chiều về";
                Toast.makeText(AncillaryActivity.this,
                        "Đã thêm " + item.getName() + " cho " + dsTenHanhKhach[passengerIndex] + " (" + chieuBay + ")",
                        Toast.LENGTH_SHORT).show();
            }
        });

        rvAncillaries.setAdapter(adapter);
    }

    private void capNhatTongTien() {
        double tongCong = basePrice + tongTienDichVu;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalAncillaryPrice.setText(formatter.format(tongCong) + " đ");
    }

    private void xuLyNutXacNhan() {
        SessionManager sessionManager = new SessionManager(AncillaryActivity.this);
        if (sessionManager.fetchAuthToken() != null && !sessionManager.fetchAuthToken().isEmpty()) {
            chuyenSangTrangThanhToan();
        } else {
            Toast.makeText(AncillaryActivity.this, "Vui lòng đăng nhập để tạo đơn hàng!", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(AncillaryActivity.this, LoginActivity.class);
            loginIntent.putExtra("IS_FROM_BOOKING", true);
            loginLauncher.launch(loginIntent);
        }
    }

    private void chuyenSangTrangThanhToan() {
        Intent nextIntent = new Intent(AncillaryActivity.this, PaymentSummaryActivity.class);
        nextIntent.putExtra("bookingRequest", currentBookingRequest);
        nextIntent.putExtra("tongTienDichVu", tongTienDichVu);
        nextIntent.putExtra("ticketPrice", ticketPrice);
        startActivity(nextIntent);
    }
}