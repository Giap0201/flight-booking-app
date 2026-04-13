package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.MainActivity;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.viewmodel.BookingViewModel;

import java.util.Map;

public class PaymentResultActivity extends AppCompatActivity {

    private TextView tvPaymentStatus, tvPaymentMessage;
    private ImageView ivStatusIcon;
    private Button btnGoHome, btnRetry, btnCheckStatus;
    private LinearLayout layoutFailedActions;

    private BookingViewModel viewModel;

    private String bookingId;
    private String pnrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        initViews();
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        // BƯỚC 1: Lấy dữ liệu từ Deep Link
        handleIntentData(getIntent());

        // BƯỚC 2: Cài đặt sự kiện

        // 1. Thử lại (Thanh toán lại)
        btnRetry.setOnClickListener(v -> {
            if (bookingId != null) {
                btnRetry.setEnabled(false);
                btnRetry.setText("Đang lấy link...");

                viewModel.createPaymentUrl(bookingId, "android").observe(this, url -> {
                    btnRetry.setEnabled(true);
                    btnRetry.setText("Thử lại");
                    if (url != null && !url.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } else {
                        Toast.makeText(this, "Không thể tạo link thanh toán mới!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 2. Kiểm tra trạng thái (Nơi bạn bị lỗi status)
        btnCheckStatus.setOnClickListener(v -> {
            if (pnrCode != null) {
                btnCheckStatus.setEnabled(false);
                btnCheckStatus.setText("Đang check...");

                viewModel.verifyPaymentStatus(pnrCode).observe(this, result -> {
                    btnCheckStatus.setEnabled(true);
                    btnCheckStatus.setText("Kiểm tra lại");

                    // ⚡ FIX LỖI Ở ĐÂY: Dùng result.get("status") vì result là một Map ⚡
                    if (result != null && "SUCCESS".equals(result.get("status"))) {
                        // Lấy thêm message từ map nếu cần hiển thị
                        String msg = String.valueOf(result.get("message"));
                        updateUI(true, msg != null ? msg : "Thanh toán thành công!");
                    } else {
                        Toast.makeText(this, "Vẫn chưa tìm thấy giao dịch thành công.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnGoHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    private void handleIntentData(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String responseCode = data.getQueryParameter("code");
                bookingId = data.getQueryParameter("bookingId");
                pnrCode = data.getQueryParameter("pnrCode");

                if ("00".equals(responseCode)) {
                    updateUI(true, "Vé điện tử của bạn đã được xác nhận. Vui lòng kiểm tra Email.");
                } else {
                    updateUI(false, "Giao dịch bị hủy hoặc lỗi (Mã: " + responseCode + ")");
                }
            }
        }
    }

    private void updateUI(boolean isSuccess, String message) {
        if (isSuccess) {
            tvPaymentStatus.setText("Thanh Toán Thành Công!");
            tvPaymentStatus.setTextColor(Color.parseColor("#4CAF50"));
            ivStatusIcon.setImageResource(android.R.drawable.ic_dialog_info);
            ivStatusIcon.setColorFilter(Color.parseColor("#4CAF50"));
            layoutFailedActions.setVisibility(View.GONE);
        } else {
            tvPaymentStatus.setText("Thanh Toán Thất Bại");
            tvPaymentStatus.setTextColor(Color.RED);
            ivStatusIcon.setImageResource(android.R.drawable.ic_delete);
            ivStatusIcon.setColorFilter(Color.RED);
            layoutFailedActions.setVisibility(View.VISIBLE);
        }
        tvPaymentMessage.setText(message);
    }

    private void initViews() {
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMessage = findViewById(R.id.tvPaymentMessage);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        btnGoHome = findViewById(R.id.btnGoHome);
        btnRetry = findViewById(R.id.btnRetry);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
        layoutFailedActions = findViewById(R.id.layoutFailedActions);
    }
}