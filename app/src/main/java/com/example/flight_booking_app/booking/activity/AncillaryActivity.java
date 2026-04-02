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
import com.example.flight_booking_app.booking.adapter.AncillaryAdapter;
import com.example.flight_booking_app.booking.model.AncillaryItem;
import com.example.flight_booking_app.booking.model.AncillaryRequest;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.viewmodel.FlightViewModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AncillaryActivity extends AppCompatActivity {

    // 1. Khai báo View (ĐÃ SỬA THÀNH RECYCLERVIEW)
    private RecyclerView rvAncillaries;
    private TextView tvTotalAncillaryPrice;
    private Button btnConfirmAncillaries;

    // 2. Khai báo dữ liệu và Adapter
    private List<AncillaryItem> dsAncillary;
    private AncillaryAdapter adapter;
    private FlightViewModel viewModel;

    // 3. Khai báo các biến hứng dữ liệu từ màn hình trước truyền sang
    private String[] dsTenHanhKhach;
    private BookingRequest currentBookingRequest; // Cục Data JSON chuẩn bị gửi API
    private double basePrice = 0; // Giá vé gốc truyền sang
    private double tongTienDichVu = 0; // Tiền dịch vụ mua thêm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ancillary);

        // Ánh xạ View
        rvAncillaries = findViewById(R.id.rvAncillaries);
        tvTotalAncillaryPrice = findViewById(R.id.tvTotalAncillaryPrice);
        btnConfirmAncillaries = findViewById(R.id.btnConfirmAncillaries);

        // Bắt buộc phải set LayoutManager cho RecyclerView
        rvAncillaries.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        // ==============================================================================
        // NHẬN DỮ LIỆU TỪ MÀN HÌNH NHẬP THÔNG TIN TRUYỀN SANG
        // ==============================================================================
        Intent intent = getIntent();
        if (intent != null) {
            dsTenHanhKhach = intent.getStringArrayExtra("passengerNames");
            currentBookingRequest = (BookingRequest) intent.getSerializableExtra("bookingRequest");
            basePrice = intent.getDoubleExtra("basePrice", 0.0);

            // Hiện giá tiền gốc lên luôn
            capNhatTongTien();
        }

        // ==============================================================================
        // GỌI API LẤY DANH SÁCH DỊCH VỤ VỀ HIỂN THỊ LÊN MÀN HÌNH
        // ==============================================================================
        layDuLieuTuApi();

        // ==============================================================================
        // XỬ LÝ NÚT XÁC NHẬN - CHỐT ĐƠN HÀNG VÀ GỌI API ĐẶT VÉ
        // ==============================================================================
        btnConfirmAncillaries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khóa nút lại để tránh user bấm 2 lần liên tục sinh ra 2 mã vé
                btnConfirmAncillaries.setEnabled(false);
                btnConfirmAncillaries.setText("Đang xử lý...");

                // Gọi API Create Booking
                viewModel.createBooking(currentBookingRequest).observe(AncillaryActivity.this, result -> {
                    // Mở khóa lại nút
                    btnConfirmAncillaries.setEnabled(true);
                    btnConfirmAncillaries.setText("Xác nhận");

                    if (result != null) {
                        Toast.makeText(AncillaryActivity.this,
                                "🎉 Đặt vé thành công! PNR: " + result.getPnrCode(),
                                Toast.LENGTH_LONG).show();

                        // TODO: Chuyển sang màn hình Chi tiết vé / Hoàn tất thanh toán
                    } else {
                        Toast.makeText(AncillaryActivity.this,
                                "❌ Đặt vé thất bại! Vui lòng thử lại.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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

                // 1. Cộng tiền
                tongTienDichVu += item.getPrice();
                capNhatTongTien();

                // 2. TẠO REQUEST DỊCH VỤ DỰA THEO CẤU TRÚC JSON CỦA BẠN
                // Truyền vào ID của gói, vị trí hành khách, và chặng bay (mặc định là 0 nếu bay 1 chiều)
                AncillaryRequest ancillaryRequest = new AncillaryRequest(item.getId(), passengerIndex, 0);

                // 3. Nhét vào danh sách `bookingAncillaries` trong Cục Data to
                currentBookingRequest.getBookingAncillaries().add(ancillaryRequest);

                Toast.makeText(AncillaryActivity.this,
                        "Đã thêm " + item.getName() + " cho " + dsTenHanhKhach[passengerIndex],
                        Toast.LENGTH_SHORT).show();
            }
        });

        rvAncillaries.setAdapter(adapter);
    }

    // Hàm cập nhật tổng tiền = Tiền vé màn trước (basePrice) + Tiền dịch vụ mua thêm
    private void capNhatTongTien() {
        double tongCong = basePrice + tongTienDichVu;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalAncillaryPrice.setText(formatter.format(tongCong) + " đ");
    }
}