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

    // --- CÁC BIẾN GIAO DIỆN ---
    private RecyclerView rvAncillaries;
    private TextView tvTotalAncillaryPrice;
    private Button btnConfirmAncillaries;
    private LinearLayout layoutTabsKhuHoi;
    private Button btnTabChieuDi;
    private Button btnTabChieuVe;

    // --- CÁC BIẾN DỮ LIỆU ---
    private BookingViewModel viewModel;
    private List<AncillaryItem> dsAncillary;
    private AncillaryAdapter adapter;
    private String[] dsTenHanhKhach;
    private BookingRequest currentBookingRequest;

    private double basePrice = 0;
    private double ticketPrice = 0;
    private double tongTienDichVu = 0;

    // --- BIẾN ĐIỀU KHIỂN LUỒNG ---
    private boolean isRoundTrip = false;

    /**
     * LOGIC SEGMENT:
     * 1 = Chiều đi (Mặc định cho vé 1 chiều và chặng đầu khứ hồi)
     * 2 = Chiều về (Dành cho chặng thứ 2 của khứ hồi)
     */
    private int currentSegmentNo = 1;

    private final androidx.activity.result.ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
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
        setupTabsKhuHoi(); // Thiết lập Segment mặc định dựa trên loại vé
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
            // Mặc định ban đầu vào là Chiều đi (Segment 1)
            currentSegmentNo = 1;

            btnTabChieuDi.setOnClickListener(v -> {
                currentSegmentNo = 1;
                capNhatGiaoDienAdapterChuyenTab();
                Toast.makeText(this, "Chọn dịch vụ: Chiều Đi", Toast.LENGTH_SHORT).show();
            });

            btnTabChieuVe.setOnClickListener(v -> {
                currentSegmentNo = 2;
                capNhatGiaoDienAdapterChuyenTab();
                Toast.makeText(this, "Chọn dịch vụ: Chiều Về", Toast.LENGTH_SHORT).show();
            });

        } else {
            // Vé 1 chiều: Luôn là Segment 1
            layoutTabsKhuHoi.setVisibility(View.GONE);
            currentSegmentNo = 1;
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

                // Tạo request mới với Segment hiện tại (1 hoặc 2)
                AncillaryRequest newAncillary = new AncillaryRequest(item.getId(), passengerIndex, currentSegmentNo);
                currentBookingRequest.getBookingAncillaries().add(newAncillary);

                capNhatTongTien();

                String loaiChieu = (currentSegmentNo == 1) ? "Chiều đi" : "Chiều về";
                Toast.makeText(AncillaryActivity.this,
                        "Thêm " + item.getName() + " cho " + dsTenHanhKhach[passengerIndex] + " (" + loaiChieu + ")",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemoved(AncillaryItem item, int passengerIndex) {
                xoaDichVuKhoiDanhSach(item.getId(), passengerIndex, currentSegmentNo, item.getPrice());

                String loaiChieu = (currentSegmentNo == 1) ? "Chiều đi" : "Chiều về";
                Toast.makeText(AncillaryActivity.this,
                        "Hủy " + item.getName() + " của " + dsTenHanhKhach[passengerIndex] + " (" + loaiChieu + ")",
                        Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setCurrentSegment(currentSegmentNo);
        rvAncillaries.setAdapter(adapter);
    }

    private void xoaDichVuKhoiDanhSach(String catalogId, int passengerIndex, int segmentNo, double priceToSubtract) {
        Iterator<AncillaryRequest> iterator = currentBookingRequest.getBookingAncillaries().iterator();
        while (iterator.hasNext()) {
            AncillaryRequest req = iterator.next();

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
            Toast.makeText(AncillaryActivity.this, "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
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