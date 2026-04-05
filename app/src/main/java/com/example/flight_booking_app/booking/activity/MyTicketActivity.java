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

    // ĐÃ KHAI BÁO BIẾN CHO KHU VỰC TIÊU ĐỀ
    private View layoutUpcomingHeader;

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

        // 1. ÁNH XẠ HEADER "UPCOMING FLIGHT"
        layoutUpcomingHeader = findViewById(R.id.layoutUpcomingHeader);

        // =======================================================
        // ĐÃ SỬA: Gắn Intent cho nút History để bay sang màn Past Ticket
        // =======================================================
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MyTicketActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // =======================================================
        // 2. GẮN SỰ KIỆN CLICK ĐỂ CHUYỂN TRANG UPCOMING
        // =======================================================
        if (layoutUpcomingHeader != null) {
            layoutUpcomingHeader.setOnClickListener(v -> {
                // Tạo Intent để bay từ MyTicket sang UpcomingFlights
                Intent intent = new Intent(MyTicketActivity.this, UpcomingFlightsActivity.class);
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

        // LƯU Ý: Token cứng (Nhớ thay bằng SharedPreferences sau)
        String myToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NDU2MTY1LCJpYXQiOjE3NzUzNjk3NjUsImp0aSI6IjI3NWRiOTk5LWFiNzMtNGQ0Mi04ZDIwLTEzODBjODY2NmQxOCIsInNjb3BlIjoiUk9MRV9VU0VSIn0.OZJaU3JAZouY6F2JJlsqUm4z5pwyeKVyIVxENb-xfexcP4bXYzVBeUmZctnjVwCNCqwEySaU549LyZoTVmUo0g";

        apiService.getMyBookings(myToken,1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResult<BookingSummary>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        List<BookingSummary> allTickets = apiResponse.getResult().getData();

                        if (allTickets == null || allTickets.isEmpty()) {
                            allTickets = new ArrayList<>();
                            BookingSummary fakePending = new BookingSummary();
                            fakePending.setPnrCode("TBW7FWZ"); fakePending.setStatus("PENDING"); fakePending.setOrigin("CGK"); fakePending.setDestination("DPS"); fakePending.setDepartureTime("2024-05-07T16:55:00"); fakePending.setCreatedAt(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
                            allTickets.add(fakePending);

                            BookingSummary fakeUpcoming = new BookingSummary();
                            fakeUpcoming.setPnrCode("UPC999"); fakeUpcoming.setStatus("CONFIRMED"); fakeUpcoming.setOrigin("HAN"); fakeUpcoming.setDestination("SGN"); fakeUpcoming.setDepartureTime("2024-09-10T07:30:00");
                            allTickets.add(fakeUpcoming);
                        }
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
                if ("PENDING".equalsIgnoreCase(ticket.getStatus()) || "AWAITING_PAYMENT".equalsIgnoreCase(ticket.getStatus())) {
                    pendingList.add(ticket);
                } else if ("CONFIRMED".equalsIgnoreCase(ticket.getStatus()) || "ISSUED".equalsIgnoreCase(ticket.getStatus()) || "PAID".equalsIgnoreCase(ticket.getStatus())) {
                    upcomingList.add(ticket);
                }
            }
        }
        pendingAdapter.setTicketList(pendingList);
        upcomingAdapter.setTicketList(upcomingList);
    }
}