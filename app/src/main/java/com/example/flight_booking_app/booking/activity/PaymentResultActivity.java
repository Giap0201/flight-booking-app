package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flight_booking_app.MainActivity;
import com.example.flight_booking_app.R;

public class PaymentResultActivity extends AppCompatActivity {

    // Khai báo View
    private TextView tvPaymentStatus;
    private TextView tvPaymentMessage;
    private Button btnGoHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        // Ánh xạ View
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMessage = findViewById(R.id.tvPaymentMessage);
        btnGoHome = findViewById(R.id.btnGoHome);

        // =========================================================
        // BƯỚC 1: BẮT ĐƯỜNG LINK TỪ TRÌNH DUYỆT VĂNG VÀO APP
        // =========================================================
        Intent intent = getIntent();

        // Kiểm tra xem màn hình này có được mở bằng một đường link web hay không
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {

            Uri data = intent.getData(); // Lấy cục data của đường link

            if (data != null) {
                // Ví dụ link là: flightbooking://payment-result?code=00
                // Hàm này sẽ móc lấy chữ "00" ra
                String responseCode = data.getQueryParameter("code");

                // =========================================================
                // BƯỚC 2: KIỂM TRA MÃ KẾT QUẢ VÀ HIỆN LÊN MÀN HÌNH
                // =========================================================
                if ("00".equals(responseCode)) {
                    // VNPay quy định "00" là khách đã trừ tiền thành công
                    tvPaymentStatus.setText("Thanh Toán Thành Công!");
                    tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    tvPaymentMessage.setText("Tuyệt vời! Vé điện tử của bạn đã được xác nhận. Vui lòng kiểm tra Email nhé.");

                } else {
                    // Các mã khác (như 24 là khách bấm nút Hủy giao dịch, vv...)
                    tvPaymentStatus.setText("Thanh Toán Thất Bại");
                    tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    tvPaymentMessage.setText("Giao dịch chưa hoàn tất hoặc đã bị hủy (Mã lỗi: " + responseCode + ").");
                }
            }
        }

        // =========================================================
        // BƯỚC 3: XỬ LÝ NÚT VỀ TRANG CHỦ
        // =========================================================
        btnGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để quay về màn hình đầu tiên của App (Ví dụ MainActivity)
                Intent homeIntent = new Intent(PaymentResultActivity.this, MainActivity.class); // Tùy tên màn hình chính của bạn

                // 2 Cờ (Flags) này rất quan trọng:
                // Nó giúp dọn sạch toàn bộ các màn hình "Điền form", "Chọn dịch vụ" ở phía dưới
                // Tránh việc khách bấm nút Back (Trở về) trên điện thoại lại lùi về màn hình thanh toán
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(homeIntent);
                finish(); // Đóng màn hình kết quả này lại
            }
        });
    }
}