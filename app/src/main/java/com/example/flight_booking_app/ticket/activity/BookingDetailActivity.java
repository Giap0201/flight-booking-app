package com.example.flight_booking_app.ticket.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.adapter.BookingDetailAdapter;
import com.example.flight_booking_app.ticket.adapter.PendingTicketAdapter;
import com.example.flight_booking_app.ticket.api.TicketApiService;
import com.example.flight_booking_app.ticket.model.BookingDetailItem;
import com.example.flight_booking_app.ticket.model.BookingSummary;
import com.example.flight_booking_app.ticket.model.PageResult;
import com.example.flight_booking_app.ticket.response.BookingDetailResponse;
import com.example.flight_booking_app.ticket.response.PassengerTicketResponse;
import com.example.flight_booking_app.ticket.response.TicketDetailResponse;
import com.example.flight_booking_app.ticket.viewmodel.FlightViewModel;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.common.AppConfig;
import com.example.flight_booking_app.network.ApiClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
 * =====================================================================================
 */
public class BookingDetailActivity extends AppCompatActivity {

    private RecyclerView rvBookingDetail;
    private ProgressBar progressBar;
    private BookingDetailAdapter adapter;
    private FlightViewModel viewModel;
    
    // Lưu lại raw object để compose Share text
    private BookingDetailResponse rawBookingData;

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
        rvBookingDetail.setAdapter(adapter);

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        // Observe LiveData
        observeViewModel();

        // Lấy BOOKING_ID từ Intent và gọi API
        String bookingId = getIntent().getStringExtra("BOOKING_ID");
        if (bookingId != null && !bookingId.isEmpty()) {
            viewModel.fetchBookingDetail(bookingId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã booking!", Toast.LENGTH_SHORT).show();
            finish();
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

    public static class WaitingForPaymentActivity extends AppCompatActivity {

        private RecyclerView rvAllWaiting;
        private PendingTicketAdapter adapter;
        private List<BookingSummary> waitingList = new ArrayList<>();
        private ProgressBar progressBar;

    //    private final String TOKEN = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NTU1MTI0LCJpYXQiOjE3NzU0Njg3MjQsImp0aSI6ImI1MjhjYmM3LWRmMTktNGY4OC1hODYzLTg5YmFiNDZlOWM1MyIsInNjb3BlIjoiUk9MRV9VU0VSIn0._h_1wlfj-JFL0LMbVjxhwdkc5Es15Py3WtVM_cayhGcoZOJHb36_YKgOSkEyuiAYwVfdEugKehj3weD0Dbt6KQ";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_waiting_for_payment);

            findViewById(R.id.btnBack).setOnClickListener(v -> finish());

            progressBar = findViewById(R.id.progressBar);
            rvAllWaiting = findViewById(R.id.rvAllWaiting);
            rvAllWaiting.setLayoutManager(new LinearLayoutManager(this));

            adapter = new PendingTicketAdapter(waitingList);
            rvAllWaiting.setAdapter(adapter);

            fetchAllWaitingTickets();
        }

        private void fetchAllWaitingTickets() {
            progressBar.setVisibility(View.VISIBLE);
            TicketApiService apiService = ApiClient.getClient().create(TicketApiService.class);

            // Theo BE: Lấy ALL rồi lọc ở FE
            // Lấy size lớn (ví dụ 50) để đảm bảo không bỏ sót vé pending nào ở các trang sau
            apiService.getMyBookingsWithFilter(AppConfig.TOKEN, "ALL", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
                @Override
                public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<BookingSummary> allData = response.body().getResult().getData();
                        waitingList.clear();

                        if (allData != null) {
                            for (BookingSummary item : allData) {
                                String status = item.getStatus();
                                // Logic filter tại Frontend
                                if ("PENDING".equals(status) || "AWAITING_PAYMENT".equals(status)) {
                                    waitingList.add(item);
                                }
                            }
                        }

                        if (waitingList.isEmpty()) {
                            Toast.makeText(WaitingForPaymentActivity.this, "Không có vé nào chờ thanh toán", Toast.LENGTH_SHORT).show();
                        }
                        adapter.setTicketList(waitingList);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("WAITING_PAGE", "Error: " + t.getMessage());
                }
            });
        }
    }
}
