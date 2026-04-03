package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.PassengerRequest;
import com.example.flight_booking_app.booking.viewmodel.FlightViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentSummaryActivity extends AppCompatActivity {

    private TextView tvAdultCount, tvAdultPrice;
    private TextView tvChildCount, tvChildPrice;
    private TextView tvInfantCount, tvInfantPrice;
    private TextView tvTaxPrice, tvAncillaryPriceSummary, tvFinalTotal;
    private Button btnPayNow;
    private View layoutChild, layoutInfant;

    private FlightViewModel viewModel;
    private BookingRequest currentBookingRequest;

    // Dữ liệu truyền từ các màn hình trước
    private double ticketPrice;
    private double tongTienDichVu;

    // Giả sử thuế là 10% (0.1). Nếu API của bạn có trả về phần trăm thuế, bạn lấy gán vào đây
    private final double TAX_RATE = 0.10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_summary);

        initViews();
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        // 1. Nhận dữ liệu
        Intent intent = getIntent();
        if (intent != null) {
            currentBookingRequest = (BookingRequest) intent.getSerializableExtra("bookingRequest");
            ticketPrice = intent.getDoubleExtra("ticketPrice", 0.0); // Giá gốc của 1 vé
            tongTienDichVu = intent.getDoubleExtra("tongTienDichVu", 0.0); // Tổng tiền hành lý/ăn uống
        }

        // 2. Tính toán tiền y hệt Spring Boot
        calculateAndDisplayPrice();

        // Nhớ import Uri ở đầu file nhé: import android.net.Uri;

        btnPayNow.setOnClickListener(v -> {
            // Khóa nút lại để tránh user bấm spam 2 lần
            btnPayNow.setEnabled(false);
            btnPayNow.setText("Đang khởi tạo đơn hàng...");

            // 1. GỌI API TẠO BOOKING TRƯỚC
            viewModel.createBooking(currentBookingRequest).observe(this, bookingResult -> {

                if (bookingResult != null && bookingResult.getId() != null) {

                    String bookingId = bookingResult.getId();
                    btnPayNow.setText("Đang lấy link thanh toán...");

                    // 2. CÓ BOOKING ID RỒI -> GỌI API LẤY LINK THANH TOÁN
                    viewModel.createPaymentUrl(bookingId).observe(this, paymentUrl -> {
                        // Mở khóa nút
                        btnPayNow.setEnabled(true);
                        btnPayNow.setText("Thanh toán & Đặt vé");

                        if (paymentUrl != null && !paymentUrl.isEmpty()) {

                            // 3. MỞ TRÌNH DUYỆT ĐỂ KHÁCH THANH TOÁN
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                            startActivity(browserIntent);

                            Toast.makeText(this, "Đang chuyển hướng sang cổng thanh toán...", Toast.LENGTH_SHORT).show();

                            // (Optional) Bạn có thể gọi finish() ở đây nếu không muốn khách quay lại màn hình này
                            // finish();

                        } else {
                            Toast.makeText(this, "Lỗi: Không lấy được link thanh toán!", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // Lỗi tạo vé
                    btnPayNow.setEnabled(true);
                    btnPayNow.setText("Thanh toán & Đặt vé");
                    Toast.makeText(this, "❌ Đặt vé thất bại! Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void calculateAndDisplayPrice() {
        int adultCount = 0, childCount = 0, infantCount = 0;

        // Đếm số lượng từng loại hành khách từ Request
        if (currentBookingRequest != null && currentBookingRequest.getPassengers() != null) {
            for (PassengerRequest p : currentBookingRequest.getPassengers()) {
                if ("ADULT".equals(p.getType())) adultCount++;
                else if ("CHILD".equals(p.getType())) childCount++;
                else if ("INFANT".equals(p.getType())) infantCount++;
            }
        }

        // Logic giống y hệt backend Spring Boot
        double adultTotalBase = adultCount * ticketPrice;
        double childTotalBase = childCount * (ticketPrice * 0.75);
        double infantTotalBase = infantCount * (ticketPrice * 0.10);

        double totalBaseFare = adultTotalBase + childTotalBase + infantTotalBase;

        // Tính thuế
        double taxAmount = totalBaseFare * TAX_RATE;

        // Tổng tiền cuối cùng = Giá vé + Thuế + Dịch vụ thêm
        double finalTotal = totalBaseFare + taxAmount + tongTienDichVu;

        // --- HIỂN THỊ LÊN GIAO DIỆN ---
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvAdultCount.setText("Vé người lớn (x" + adultCount + ")");
        tvAdultPrice.setText(formatter.format(adultTotalBase) + " đ");

        if (childCount > 0) {
            tvChildCount.setText("Vé trẻ em (x" + childCount + ")");
            tvChildPrice.setText(formatter.format(childTotalBase) + " đ");
            layoutChild.setVisibility(View.VISIBLE);
        } else {
            layoutChild.setVisibility(View.GONE); // Ẩn đi nếu không có trẻ em
        }

        if (infantCount > 0) {
            tvInfantCount.setText("Vé em bé (x" + infantCount + ")");
            tvInfantPrice.setText(formatter.format(infantTotalBase) + " đ");
            layoutInfant.setVisibility(View.VISIBLE);
        } else {
            layoutInfant.setVisibility(View.GONE);
        }

        tvTaxPrice.setText(formatter.format(taxAmount) + " đ");
        tvAncillaryPriceSummary.setText(formatter.format(tongTienDichVu) + " đ");
        tvFinalTotal.setText(formatter.format(finalTotal) + " đ");
    }

    private void initViews() {
        tvAdultCount = findViewById(R.id.tvAdultCount);
        tvAdultPrice = findViewById(R.id.tvAdultPrice);
        tvChildCount = findViewById(R.id.tvChildCount);
        tvChildPrice = findViewById(R.id.tvChildPrice);
        tvInfantCount = findViewById(R.id.tvInfantCount);
        tvInfantPrice = findViewById(R.id.tvInfantPrice);
        tvTaxPrice = findViewById(R.id.tvTaxPrice);
        tvAncillaryPriceSummary = findViewById(R.id.tvAncillaryPriceSummary);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        btnPayNow = findViewById(R.id.btnPayNow);
        layoutChild = findViewById(R.id.layoutChild);
        layoutInfant = findViewById(R.id.layoutInfant);
    }
}