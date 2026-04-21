package com.example.flight_booking_app.ticket.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.adapter.BookingDetailAdapter;
import com.example.flight_booking_app.ticket.model.BookingDetailItem;
import com.example.flight_booking_app.ticket.response.client.BookingDetailResponse;
import com.example.flight_booking_app.ticket.response.client.PassengerTicketResponse;
import com.example.flight_booking_app.ticket.response.client.TicketDetailResponse;
import com.example.flight_booking_app.ticket.viewmodel.FlightViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * =====================================================================================
 * BookingDetailActivity — Màn hình chi tiết booking (MỚI, thay thế
 * FlightDetailActivity).
 * =====================================================================================
 *
 * Sử dụng kiến trúc MVVM:
 * - ViewModel: FlightViewModel (đã được mở rộng)
 * - Adapter: BookingDetailAdapter (multi-ViewType)
 * - Layout: activity_booking_detail.xml
 *
 * Flow hoạt động:
 * 1. Nhận BOOKING_ID từ Intent
 * 2. Gọi ViewModel.fetchBookingDetail(bookingId)
 * 3. Observe LiveData:
 * - bookingDetailItems → cập nhật RecyclerView
 * - isLoading → hiện/ẩn ProgressBar
 * - errorMessage → hiện Toast lỗi
 *
 * ⚡ MỚI: Nút "Thanh toán" trên Header card
 * - Chỉ hiển thị khi status = PENDING / AWAITING_PAYMENT
 * - Click → gọi API tạo payment URL → mở trình duyệt VNPay
 * - Khi quay lại (onResume) → tự động refresh lại booking detail
 * =====================================================================================
 */
public class BookingDetailActivity extends AppCompatActivity
        implements BookingDetailAdapter.OnPayNowClickListener {

    private RecyclerView rvBookingDetail;
    private ProgressBar progressBar;
    private BookingDetailAdapter adapter;
    private FlightViewModel viewModel;

    // Lưu lại raw object để compose Share text
    private BookingDetailResponse rawBookingData;

    // Lưu bookingId để dùng cho payment và auto-refresh
    private String bookingId;

    // Cờ đánh dấu đã điều hướng sang trình duyệt thanh toán
    private boolean navigatedToPayment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        // Nút quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Init Views
        progressBar = findViewById(R.id.progressBar);
        rvBookingDetail = findViewById(R.id.rvBookingDetail);

        // Setup RecyclerView
        rvBookingDetail.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingDetailAdapter(new ArrayList<>());
        adapter.setOnPayNowClickListener(this); // Đăng ký callback thanh toán
        rvBookingDetail.setAdapter(adapter);

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        // Observe LiveData
        observeViewModel();

        // Lấy BOOKING_ID từ Intent và gọi API
        bookingId = getIntent().getStringExtra("BOOKING_ID");
        if (bookingId != null && !bookingId.isEmpty()) {
            viewModel.fetchBookingDetail(bookingId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã booking!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Khi người dùng quay lại từ trình duyệt thanh toán,
     * tự động refresh lại booking detail để cập nhật trạng thái.
     * Nếu thanh toán thành công → status sẽ đổi thành CONFIRMED → nút "Thanh toán" tự ẩn.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (navigatedToPayment && bookingId != null) {
            navigatedToPayment = false;
            // Refresh lại dữ liệu booking sau khi quay về từ VNPay
            viewModel.fetchBookingDetail(bookingId);
        }
    }

    /**
     * Đăng ký observe tất cả LiveData từ ViewModel.
     */
    private void observeViewModel() {
        // 1. Danh sách item phẳng → cập nhật RecyclerView
        viewModel.getBookingDetailItems().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                // Lưu lại rawBookingData từ Header item (luôn là item đầu tiên) để dùng cho chức năng Share
                if (items.get(0).getType() == BookingDetailItem.TYPE_HEADER_INFO) {
                    rawBookingData = items.get(0).getBookingData();
                }

                adapter.setItems(items);
                rvBookingDetail.setVisibility(View.VISIBLE);
            }
        });

        // 2. Trạng thái loading → hiện/ẩn ProgressBar
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    rvBookingDetail.setVisibility(View.GONE);
                }
            }
        });

        // 3. Lỗi → hiện Toast
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(BookingDetailActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ======================== PAYMENT CALLBACK ========================

    /**
     * Xử lý khi người dùng nhấn nút "Thanh toán" trên Header card.
     * Flow:
     * 1. Gọi API tạo payment URL (VNPay) qua ViewModel
     * 2. Nếu thành công → mở URL bằng trình duyệt hệ thống (ACTION_VIEW)
     * 3. Nếu thất bại → hiện Toast lỗi
     *
     * @param bookingId Mã booking (UUID) cần thanh toán
     */
    @Override
    public void onPayNowClick(String bookingId) {
        Toast.makeText(this, "Đang tạo liên kết thanh toán...", Toast.LENGTH_SHORT).show();

        viewModel.createPaymentUrl(bookingId, "android").observe(this, paymentUrl -> {
            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                // Đánh dấu đã chuyển sang trình duyệt để onResume auto-refresh
                navigatedToPayment = true;

                // Mở URL thanh toán VNPay bằng trình duyệt hệ thống
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(BookingDetailActivity.this,
                        "Không thể tạo liên kết thanh toán. Vui lòng thử lại sau.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ======================== ACTION BAR / MENU ========================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_booking_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareBookingDetails();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Logic tạo chuỗi text chia sẻ từ rawBookingData và bắn Intent.
     */
    private void shareBookingDetails() {
        if (rawBookingData == null) {
            Toast.makeText(this, "Dữ liệu đang tải, vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("✈ Thông tin đặt vé máy bay\n\n");
        sb.append("Mã PNR: ").append(rawBookingData.getPnrCode() != null ? rawBookingData.getPnrCode() : "N/A").append("\n");

        String status = rawBookingData.getStatus() != null ? rawBookingData.getStatus() : "N/A";
        sb.append("Trạng thái: ").append(status).append("\n\n");

        if (rawBookingData.getPassengers() != null && !rawBookingData.getPassengers().isEmpty()) {
            for (PassengerTicketResponse pax : rawBookingData.getPassengers()) {
                sb.append("👤 Hành khách: ").append(pax.getFullName()).append("\n");

                if (pax.getTickets() != null && !pax.getTickets().isEmpty()) {
                    TicketDetailResponse tkt = pax.getTickets().get(0);
                    sb.append("   Chuyến bay: ✈ ").append(tkt.getFlightNumber()).append("\n");
                    sb.append("   Hành trình: ").append(tkt.getDepartureAirport()).append(" ➔ ").append(tkt.getArrivalAirport()).append("\n");
                    sb.append("   Giờ bay: ").append(tkt.getDepartureTime()).append("\n");
                    sb.append("   Ghế: ").append(tkt.getSeatNumber() != null ? tkt.getSeatNumber() : "TBD").append("\n");
                    sb.append("   Vé: ").append(tkt.getTicketNumber()).append("\n");
                }
                sb.append("\n");
            }
        }

        NumberFormat formatVND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        sb.append("💰 Tổng tiền: ").append(formatVND.format(rawBookingData.getTotalAmount())).append("\n");

        // Bắn Android Share Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Thông tin vé máy bay - " + rawBookingData.getPnrCode());
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ thông tin vé qua"));
    }
}
