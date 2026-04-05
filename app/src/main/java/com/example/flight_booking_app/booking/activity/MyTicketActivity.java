package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    private View layoutUpcomingHeader;
    private View layoutWaitingHeader;

    private PendingTicketAdapter pendingAdapter;
    private UpcomingTicketAdapter upcomingAdapter;

    private List<BookingSummary> pendingList = new ArrayList<>();
    private List<BookingSummary> upcomingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ticket);

        // Ánh xạ View
        rvPendingTickets = findViewById(R.id.rvPendingTickets);
        rvUpcomingTickets = findViewById(R.id.rvUpcomingTickets);
        btnHistory = findViewById(R.id.btnHistory);
        layoutUpcomingHeader = findViewById(R.id.layoutUpcomingHeader);
        layoutWaitingHeader = findViewById(R.id.layoutWaitingHeader);

        // Nút Lịch sử
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MyTicketActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Click tiêu đề Upcoming -> Sang trang danh sách Upcoming
        if (layoutUpcomingHeader != null) {
            layoutUpcomingHeader.setOnClickListener(v -> {
                Intent intent = new Intent(MyTicketActivity.this, UpcomingFlightsActivity.class);
                startActivity(intent);
            });
        }

        // Click tiêu đề Waiting -> Sang trang danh sách Waiting
        if (layoutWaitingHeader != null) {
            layoutWaitingHeader.setOnClickListener(v -> {
                Intent intent = new Intent(MyTicketActivity.this, WaitingForPaymentActivity.class);
                startActivity(intent);
            });
        }

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
        String myToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NDU2MTY1LCJpYXQiOjE3NzUzNjk3NjUsImp0aSI6IjI3NWRiOTk5LWFiNzMtNGQ0Mi04ZDIwLTEzODBjODY2NmQxOCIsInNjb3BlIjoiUk9MRV9VU0VSIn0.OZJaU3JAZouY6F2JJlsqUm4z5pwyeKVyIVxENb-xfexcP4bXYzVBeUmZctnjVwCNCqwEySaU549LyZoTVmUo0g";

        // 1. LẤY 2 VÉ CHỜ THANH TOÁN (Sử dụng filter PENDING)
        apiService.getMyBookingsWithFilter(myToken, "PENDING", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    List<BookingSummary> data = response.body().getResult().getData();
                    pendingList.clear();
                    if (data != null) {
                        // Chỉ lấy 2 vé đầu tiên từ kết quả trả về
                        for (int i = 0; i < Math.min(data.size(), 2); i++) {
                            pendingList.add(data.get(i));
                        }
                    }
                    pendingAdapter.setTicketList(pendingList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("MyTicket", "Lỗi Pending: " + t.getMessage());
            }
        });

        // 2. LẤY 2 VÉ SẮP BAY (Sử dụng filter CONFIRMED)
        apiService.getMyBookingsWithFilter(myToken, "CONFIRMED", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    List<BookingSummary> data = response.body().getResult().getData();
                    upcomingList.clear();
                    if (data != null) {
                        // Chỉ lấy 2 vé đầu tiên từ kết quả trả về
                        for (int i = 0; i < Math.min(data.size(), 2); i++) {
                            upcomingList.add(data.get(i));
                        }
                    }
                    upcomingAdapter.setTicketList(upcomingList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("MyTicket", "Lỗi Upcoming: " + t.getMessage());
            }
        });
    }

    // Đã loại bỏ hàm filterTickets vì Backend đã thực hiện việc lọc này thông qua Query Filter
}