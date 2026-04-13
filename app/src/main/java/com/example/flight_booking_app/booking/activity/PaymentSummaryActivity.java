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
import com.example.flight_booking_app.booking.viewmodel.BookingViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentSummaryActivity extends AppCompatActivity {

    // Khai báo các view trên giao diện
    private TextView tvAdultCount, tvAdultPrice;
    private TextView tvChildCount, tvChildPrice;
    private TextView tvInfantCount, tvInfantPrice;
    private TextView tvTaxPrice, tvAncillaryPriceSummary, tvFinalTotal;
    private Button btnPayNow;
    private View layoutChild, layoutInfant;

    private BookingViewModel viewModel;
    private BookingRequest currentBookingRequest;

    // Dữ liệu truyền từ các màn hình trước
    private double ticketPrice = 0.0;       // Giá gốc của 1 vé chiều đi
    private double tongTienDichVu = 0.0;    // Tổng tiền hành lý, ăn uống
    private double taxRateFromDB = 0.0;     // % Thuế lấy từ Database (Ví dụ: 0.1 là 10%)

    // ⚡ [MỚI THÊM]: Biến hỗ trợ tính giá vé Khứ hồi
    private boolean isRoundTrip = false;
    private double returnTicketPrice = 0.0; // Giá vé chiều về

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_summary);

        initViews();
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        // =========================================================================
        // BƯỚC 1: NHẬN DỮ LIỆU TỪ MÀN HÌNH TRƯỚC
        // =========================================================================
        Intent intent = getIntent();
        if (intent != null) {
            currentBookingRequest = (BookingRequest) intent.getSerializableExtra("bookingRequest");
            ticketPrice = intent.getDoubleExtra("ticketPrice", 0.0);
            tongTienDichVu = intent.getDoubleExtra("tongTienDichVu", 0.0);
            taxRateFromDB = intent.getDoubleExtra("taxPercentage", 0.1);

            // ⚡ [MỚI THÊM]: Nhận thêm dữ liệu khứ hồi (Hãy đảm bảo Activity trước có putExtra 2 biến này nhé)
            isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);
            returnTicketPrice = intent.getDoubleExtra("returnTicketPrice", 0.0);
        }

        // =========================================================================
        // BƯỚC 2: TÍNH TOÁN VÀ HIỂN THỊ HÓA ĐƠN
        // =========================================================================
        calculateAndDisplayPrice();

        // =========================================================================
        // BƯỚC 3: XỬ LÝ KHI BẤM NÚT THANH TOÁN
        // =========================================================================
        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khóa nút lại để tránh user bấm liên tục sinh ra nhiều đơn hàng
                btnPayNow.setEnabled(false);
                btnPayNow.setText("Đang khởi tạo đơn hàng...");

                // HÀNH ĐỘNG 1: Gọi API lên Backend để lưu đơn hàng (Booking) vào Database
                viewModel.createBooking(currentBookingRequest).observe(PaymentSummaryActivity.this, bookingResult -> {

                    // Nếu Backend trả về kết quả thành công và có ID đơn hàng
                    if (bookingResult != null && bookingResult.getId() != null) {

                        String bookingId = bookingResult.getId(); // Lấy ID đơn hàng vừa tạo
                        btnPayNow.setText("Đang kết nối cổng thanh toán...");

                        // HÀNH ĐỘNG 2: Dùng ID đơn hàng đó, gọi API tiếp để lấy Link VNPay/MoMo
                        viewModel.createPaymentUrl(bookingId, "android").observe(PaymentSummaryActivity.this, paymentUrl -> {

                            // Nếu Backend trả về link Web VNPay thành công
                            if (paymentUrl != null && !paymentUrl.isEmpty()) {

                                // Mở khóa nút lại (Phòng trường hợp khách quay lại màn hình này)
                                btnPayNow.setEnabled(true);
                                btnPayNow.setText("Thanh toán & Đặt vé");

                                // HÀNH ĐỘNG 3: Bật trình duyệt web lên để khách quẹt thẻ
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                                startActivity(browserIntent);

                                // Đóng màn hình Hóa đơn này lại
                                finish();

                            } else {
                                // ⚡ [ĐÃ SỬA]: LỖI KẸT NÚT - Mở khóa nút lại nếu không lấy được link
                                btnPayNow.setEnabled(true);
                                btnPayNow.setText("Thanh toán & Đặt vé");
                                Toast.makeText(PaymentSummaryActivity.this, "Không lấy được link thanh toán!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        // Nếu Backend lỗi, không tạo được vé
                        btnPayNow.setEnabled(true);
                        btnPayNow.setText("Thanh toán & Đặt vé");
                        Toast.makeText(PaymentSummaryActivity.this, "❌ Đặt vé thất bại! Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Hàm này tính toán tiền
    private void calculateAndDisplayPrice() {
        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        // 1. Phân loại và đếm số lượng hành khách
        if (currentBookingRequest != null && currentBookingRequest.getPassengers() != null) {
            for (PassengerRequest p : currentBookingRequest.getPassengers()) {
                if ("ADULT".equals(p.getType())) {
                    adultCount++;
                } else if ("CHILD".equals(p.getType())) {
                    childCount++;
                } else if ("INFANT".equals(p.getType())) {
                    infantCount++;
                }
            }
        }

        // ⚡ [MỚI THÊM]: Tính tổng giá vé trên 1 người (Đã bao gồm cả đi lẫn về nếu là vé khứ hồi)
        double baseTicketPricePerPerson = isRoundTrip ? (ticketPrice + returnTicketPrice) : ticketPrice;

        // 2. Tính giá tiền cho từng nhóm tuổi (Người lớn 100%, Trẻ em 75%, Em bé 10%)
        // ⚡ [ĐÃ SỬA]: Thay thế ticketPrice bằng baseTicketPricePerPerson
        double adultTotalBase = adultCount * baseTicketPricePerPerson;
        double childTotalBase = childCount * (baseTicketPricePerPerson * 0.75);
        double infantTotalBase = infantCount * (baseTicketPricePerPerson * 0.10);

        // 3. Cộng tổng tiền vé gốc
        double totalBaseFare = adultTotalBase + childTotalBase + infantTotalBase;

        // 4. Tính tiền thuế (Dựa vào % thuế lấy từ Database)
        double taxAmount = totalBaseFare * taxRateFromDB;

        // 5. Tính tổng tiền cuối cùng phải trả
        double finalTotal = totalBaseFare + taxAmount + tongTienDichVu;

        // 6. Hiển thị lên giao diện (Format số tiền kiểu Việt Nam)
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Vé người lớn (Luôn luôn hiện)
        tvAdultCount.setText("Vé người lớn (x" + adultCount + ")");
        tvAdultPrice.setText(formatter.format(adultTotalBase) + " đ");

        // Vé trẻ em (Chỉ hiện nếu có trẻ em)
        if (childCount > 0) {
            tvChildCount.setText("Vé trẻ em (x" + childCount + ")");
            tvChildPrice.setText(formatter.format(childTotalBase) + " đ");
            layoutChild.setVisibility(View.VISIBLE);
        } else {
            layoutChild.setVisibility(View.GONE);
        }

        // Vé em bé (Chỉ hiện nếu có em bé)
        if (infantCount > 0) {
            tvInfantCount.setText("Vé em bé (x" + infantCount + ")");
            tvInfantPrice.setText(formatter.format(infantTotalBase) + " đ");
            layoutInfant.setVisibility(View.VISIBLE);
        } else {
            layoutInfant.setVisibility(View.GONE);
        }

        // Hiện Thuế, Dịch vụ và Tổng tiền
        tvTaxPrice.setText(formatter.format(taxAmount) + " đ");
        tvAncillaryPriceSummary.setText(formatter.format(tongTienDichVu) + " đ");
        tvFinalTotal.setText(formatter.format(finalTotal) + " đ");
    }

    // Hàm này chỉ dùng để tìm các ID trên file XML
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