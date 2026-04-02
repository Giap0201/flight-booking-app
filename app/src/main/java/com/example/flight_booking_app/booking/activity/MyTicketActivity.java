package com.example.flight_booking_app.booking.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.PendingTicketAdapter;
import com.example.flight_booking_app.booking.adapter.UpcomingTicketAdapter;
import com.example.flight_booking_app.booking.api.BookingApiService;
import com.example.flight_booking_app.booking.model.BookingSummary;
import com.example.flight_booking_app.booking.model.PageResult;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTicketActivity extends AppCompatActivity {

    private RecyclerView rvPendingTickets;
    private RecyclerView rvUpcomingTickets;
    private ImageView btnHistory;

    private PendingTicketAdapter pendingAdapter;
    private UpcomingTicketAdapter upcomingAdapter;

    private List<BookingSummary> pendingList = new ArrayList<>();
    private List<BookingSummary> upcomingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ticket);

        rvPendingTickets = findViewById(R.id.rvPendingTickets);
        rvUpcomingTickets = findViewById(R.id.rvUpcomingTickets);
        btnHistory = findViewById(R.id.btnHistory);

        btnHistory.setOnClickListener(v -> {
            Toast.makeText(MyTicketActivity.this, "Mở màn hình Lịch sử vé", Toast.LENGTH_SHORT).show();
        });

        setupRecyclerViews();
        fetchMyTickets();
    }

    private void setupRecyclerViews() {
        rvPendingTickets.setLayoutManager(new LinearLayoutManager(this));
        pendingAdapter = new PendingTicketAdapter(pendingList);
        rvPendingTickets.setAdapter(pendingAdapter);

        rvUpcomingTickets.setLayoutManager(new LinearLayoutManager(this));
        upcomingAdapter = new UpcomingTicketAdapter(upcomingList);
        rvUpcomingTickets.setAdapter(upcomingAdapter);
    }

    private void fetchMyTickets() {
        BookingApiService apiService = ApiClient.getClient().create(BookingApiService.class);

        // TODO: BẠN HÃY DÁN TOKEN THẬT TỪ POSTMAN VÀO ĐÂY ĐỂ TEST NHÉ
        // Nhớ giữ nguyên chữ "Bearer " (có 1 dấu cách) ở đằng trước.
        String myToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiMDhhOWVlNjUtN2Q1MC00ODVhLThiYmEtZGNhZDcyODdiYzk0IiwiZXhwIjoxNzc1MTUxMDA0LCJpYXQiOjE3NzUwNjQ2MDQsImp0aSI6ImY3MmQ1NzMxLThlMzktNDlkZS04YTg1LTkwOTIwMjcwYzg4OSIsInNjb3BlIjoiUk9MRV9BRE1JTiJ9.F1AQEx6FrWSQGYWvZraHA_99QmeR71eL3szG6TGofxbJtvLF9x-vgpyT3zyIQttZHecQJ8gtvw6wIA6wDSCpEA";

        // Truyền biến myToken vào hàm getMyBookings
        apiService.getMyBookings(myToken).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResult<BookingSummary>> apiResponse = response.body();

                    // Đã sửa code thành 1000 cho khớp với Backend
                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        List<BookingSummary> allTickets = apiResponse.getResult().getData();

                        // --- ĐOẠN CODE "ĂN GIAN" TẠO DỮ LIỆU GIẢ ĐỂ TEST UI ---
                        if (allTickets.isEmpty()) {
                            // Tạo 1 vé đang chờ thanh toán
                            BookingSummary fakePending = new BookingSummary();
                            fakePending.setPnrCode("TBW7FWZ");
                            fakePending.setStatus("PENDING");
                            fakePending.setOrigin("CGK");
                            fakePending.setDestination("DPS");
                            fakePending.setDepartureTime("2024-05-07T16:55:00");
                            // Set thời gian tạo vé là ngay lúc này để đồng hồ đếm ngược 30 phút bắt đầu chạy
                            fakePending.setCreatedAt(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
                            allTickets.add(fakePending);

                            // Tạo 1 vé sắp bay
                            BookingSummary fakeUpcoming = new BookingSummary();
                            fakeUpcoming.setPnrCode("UPC999");
                            fakeUpcoming.setStatus("CONFIRMED"); // Hoặc UPCOMING tùy Backend
                            fakeUpcoming.setOrigin("HAN");
                            fakeUpcoming.setDestination("SGN");
                            fakeUpcoming.setDepartureTime("2024-09-10T07:30:00");
                            allTickets.add(fakeUpcoming);
                        }

                        // Phân loại vé và đưa lên UI
                        filterTickets(allTickets);
                    } else {
                        Toast.makeText(MyTicketActivity.this, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyTicketActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("MyTicketActivity", "API Call Failed: " + t.getMessage());
                Toast.makeText(MyTicketActivity.this, "Không thể kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTickets(List<BookingSummary> allTickets) {
        pendingList.clear();
        upcomingList.clear();

        for (BookingSummary ticket : allTickets) {
            if (ticket.getStatus() != null) {
                if ("PENDING".equalsIgnoreCase(ticket.getStatus())) {
                    pendingList.add(ticket);
                } else if ("CONFIRMED".equalsIgnoreCase(ticket.getStatus()) || "UPCOMING".equalsIgnoreCase(ticket.getStatus())) {
                    upcomingList.add(ticket);
                }
            }
        }

        pendingAdapter.setTicketList(pendingList);
        upcomingAdapter.setTicketList(upcomingList);
    }
}