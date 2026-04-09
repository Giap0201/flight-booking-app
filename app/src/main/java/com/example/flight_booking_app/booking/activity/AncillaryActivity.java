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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class AncillaryActivity extends AppCompatActivity {

    // --- KHAI BÁO CÁC BIẾN GIAO DIỆN (UI) ---
    private RecyclerView rvAncillaries;
    private TextView tvTotalAncillaryPrice;
    private Button btnConfirmAncillaries;

    // Các View quản lý Tab Khứ hồi (Chiều đi / Chiều về)
    private LinearLayout layoutTabsKhuHoi;
    private Button btnTabChieuDi;
    private Button btnTabChieuVe;

    // --- KHAI BÁO CÁC BIẾN DỮ LIỆU ---
    private BookingViewModel viewModel;
    private List<AncillaryItem> dsAncillary;
    private AncillaryAdapter adapter;

    private String[] dsTenHanhKhach;
    private BookingRequest currentBookingRequest;

    private double basePrice = 0;
    private double ticketPrice = 0;
    private double tongTienDichVu = 0;

    // --- KHAI BÁO CÁC BIẾN ĐIỀU KHIỂN LUỒNG (LOGIC) ---
    private boolean isRoundTrip = false;

    // ⚡ ĐÃ SỬA: Đánh dấu chặng bay: 0 = Chiều đi, 1 = Chiều về (Khớp chuẩn Backend)
    private int currentSegmentNo = 0;

    // Lắng nghe kết quả trả về từ màn hình Đăng Nhập
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

        anhXaView();

        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        rvAncillaries.setLayoutManager(new LinearLayoutManager(this));

        nhanDuLieuTuManHinhTruoc();
        setupTabsKhuHoi();
        layDuLieuTuApi();

        btnConfirmAncillaries.setOnClickListener(v -> xuLyNutXacNhan());
    }

    private void anhXaView() {
        rvAncillaries = findViewById(R.id.rvAncillaries);
        tvTotalAncillaryPrice = findViewById(R.id.tvTotalAncillaryPrice);
        btnConfirmAncillaries = findViewById(R.id.btnConfirmAncillaries);

        layoutTabsKhuHoi = findViewById(R.id.layoutTabsKhuHoi);
        btnTabChieuDi = findViewById(R.id.btnTabChieuDi);
        btnTabChieuVe = findViewById(R.id.btnTabChieuVe);
    }

    private void nhanDuLieuTuManHinhTruoc() {
        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            dsTenHanhKhach = incomingIntent.getStringArrayExtra("passengerNames");
            currentBookingRequest = (BookingRequest) incomingIntent.getSerializableExtra("bookingRequest");
            basePrice = incomingIntent.getDoubleExtra("basePrice", 0.0);
            ticketPrice = incomingIntent.getDoubleExtra("ticketPrice", 0.0);
            isRoundTrip = incomingIntent.getBooleanExtra("isRoundTrip", false);

            capNhatTongTien();
        }
    }

    private void setupTabsKhuHoi() {
        if (layoutTabsKhuHoi == null) return;

        if (isRoundTrip) {
            layoutTabsKhuHoi.setVisibility(View.VISIBLE);

            // ⚡ ĐÃ SỬA: Cập nhật currentSegmentNo thành 0 cho chiều đi
            btnTabChieuDi.setOnClickListener(v -> {
                currentSegmentNo = 0;
                capNhatGiaoDienAdapterChuyenTab();
                Toast.makeText(this, "Đang chọn dịch vụ Chiều Đi", Toast.LENGTH_SHORT).show();
            });

            // ⚡ ĐÃ SỬA: Cập nhật currentSegmentNo thành 1 cho chiều về
            btnTabChieuVe.setOnClickListener(v -> {
                currentSegmentNo = 1;
                capNhatGiaoDienAdapterChuyenTab();
                Toast.makeText(this, "Đang chọn dịch vụ Chiều Về", Toast.LENGTH_SHORT).show();
            });

        } else {
            // NẾU LÀ VÉ 1 CHIỀU: Mặc định segmentNo = 0
            layoutTabsKhuHoi.setVisibility(View.GONE);
            currentSegmentNo = 0;
        }
    }

    private void capNhatGiaoDienAdapterChuyenTab() {
        if (adapter != null) {
            adapter.setCurrentSegment(currentSegmentNo);
            adapter.notifyDataSetChanged();
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
        adapter = new AncillaryAdapter(this, dsAncillary, dsTenHanhKhach, new AncillaryAdapter.OnAncillaryChangeListener() {

            @Override
            public void onAdded(AncillaryItem item, int passengerIndex) {
                tongTienDichVu += item.getPrice();

                // ⚡ ĐÃ SỬA: Truyền item.getId() (kiểu String) vào constructor
                AncillaryRequest newAncillary = new AncillaryRequest(item.getId(), passengerIndex, currentSegmentNo);
                currentBookingRequest.getBookingAncillaries().add(newAncillary);

                capNhatTongTien();

                // ⚡ ĐÃ SỬA: So sánh currentSegmentNo == 0
                String chieuBay = (currentSegmentNo == 0) ? "Chiều đi" : "Chiều về";
                Toast.makeText(AncillaryActivity.this,
                        "Đã thêm " + item.getName() + " cho " + dsTenHanhKhach[passengerIndex] + " (" + chieuBay + ")",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemoved(AncillaryItem item, int passengerIndex) {
                // ⚡ ĐÃ SỬA: Truyền item.getId() (kiểu String) vào hàm xóa
                xoaDichVuKhoiDanhSach(item.getId(), passengerIndex, currentSegmentNo, item.getPrice());

                // ⚡ ĐÃ SỬA: So sánh currentSegmentNo == 0
                String chieuBay = (currentSegmentNo == 0) ? "Chiều đi" : "Chiều về";
                Toast.makeText(AncillaryActivity.this,
                        "Đã hủy " + item.getName() + " của " + dsTenHanhKhach[passengerIndex] + " (" + chieuBay + ")",
                        Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setCurrentSegment(currentSegmentNo);
        rvAncillaries.setAdapter(adapter);
    }

    // ⚡ ĐÃ SỬA: Thay đổi kiểu dữ liệu tham số đầu tiên thành String catalogId
    private void xoaDichVuKhoiDanhSach(String catalogId, int passengerIndex, int segmentNo, double priceToSubtract) {
        Iterator<AncillaryRequest> iterator = currentBookingRequest.getBookingAncillaries().iterator();
        while (iterator.hasNext()) {
            AncillaryRequest req = iterator.next();

            // ⚡ ĐÃ SỬA: Dùng hàm getCatalogId() và so sánh chuỗi bằng .equals()
            if (req.getCatalogId() != null &&
                    req.getCatalogId().equals(catalogId) &&
                    req.getPassengerIndex() == passengerIndex &&
                    req.getSegmentNo() == segmentNo) {

                iterator.remove();
                tongTienDichVu -= priceToSubtract;
                break;
            }
        }
        capNhatTongTien();
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