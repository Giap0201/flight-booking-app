package com.example.flight_booking_app.ticket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.adapter.PendingTicketAdapter;
import com.example.flight_booking_app.ticket.adapter.UpcomingTicketAdapter;
import com.example.flight_booking_app.ticket.api.TicketApiService;
import com.example.flight_booking_app.ticket.model.BookingSummary;
import com.example.flight_booking_app.ticket.model.PageResult;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.common.AppConfig;
import com.example.flight_booking_app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTicketActivity extends AppCompatActivity {

    private RecyclerView rvPendingTickets, rvUpcomingTickets;
    private ImageView btnHistory;
    private View layoutUpcomingHeader, layoutWaitingHeader;

    private PendingTicketAdapter pendingAdapter;
    private UpcomingTicketAdapter upcomingAdapter;

    private List<BookingSummary> pendingList = new ArrayList<>();
    private List<BookingSummary> upcomingList = new ArrayList<>();

//    private final String TOKEN = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NTU1MTI0LCJpYXQiOjE3NzU0Njg3MjQsImp0aSI6ImI1MjhjYmM3LWRmMTktNGY4OC1hODYzLTg5YmFiNDZlOWM1MyIsInNjb3BlIjoiUk9MRV9VU0VSIn0._h_1wlfj-JFL0LMbVjxhwdkc5Es15Py3WtVM_cayhGcoZOJHb36_YKgOSkEyuiAYwVfdEugKehj3weD0Dbt6KQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ticket);

        initViews();
        setupRecyclerViews();

        // Gọi API lấy dữ liệu
        fetchPendingTickets();
        fetchUpcomingTickets();
    }

    private void initViews() {
        rvPendingTickets = findViewById(R.id.rvPendingTickets);
        rvUpcomingTickets = findViewById(R.id.rvUpcomingTickets);
        btnHistory = findViewById(R.id.btnHistory);
        layoutUpcomingHeader = findViewById(R.id.layoutUpcomingHeader);
        layoutWaitingHeader = findViewById(R.id.layoutWaitingHeader);

        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        if (layoutUpcomingHeader != null) {
            layoutUpcomingHeader.setOnClickListener(v -> startActivity(new Intent(this, UpcomingFlightsActivity.class)));
        }
        if (layoutWaitingHeader != null) {
            layoutWaitingHeader.setOnClickListener(v -> startActivity(new Intent(this, WaitingForPaymentActivity.class)));
        }
    }

    private void setupRecyclerViews() {
        rvPendingTickets.setLayoutManager(new LinearLayoutManager(this));
        pendingAdapter = new PendingTicketAdapter(pendingList);
        rvPendingTickets.setAdapter(pendingAdapter);

        rvUpcomingTickets.setLayoutManager(new LinearLayoutManager(this));
        upcomingAdapter = new UpcomingTicketAdapter(upcomingList);
        rvUpcomingTickets.setAdapter(upcomingAdapter);
    }

    // 1. Logic lấy vé chờ thanh toán (Tự lọc vì BE chưa hỗ trợ filter PENDING)
    private void fetchPendingTickets() {
        TicketApiService apiService = ApiClient.getClient(this).create(TicketApiService.class);

        // Lấy ALL với size lớn một chút (ví dụ 20) để đảm bảo tìm thấy các vé PENDING mới nhất
        apiService.getMyBookingsWithFilter(AppConfig.TOKEN, "ALL", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingSummary> allData = response.body().getResult().getData();
                    pendingList.clear();

                    if (allData != null) {
                        for (BookingSummary item : allData) {
                            String status = item.getStatus();
                            // Lọc đúng các status tương ứng với "Waiting for payment"
                            if ("PENDING".equals(status) || "AWAITING_PAYMENT".equals(status)) {
                                pendingList.add(item);
                            }
                            // Dừng lại khi đủ 2 vé để hiển thị Dashboard
                            if (pendingList.size() == 2) break;
                        }
                    }
                    pendingAdapter.setTicketList(pendingList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("MyTicket", "Error Fetching Pending: " + t.getMessage());
            }
        });
    }

    // 2. Logic lấy vé sắp bay (Dùng filter chuẩn UPCOMING từ BE)
    private void fetchUpcomingTickets() {
        TicketApiService apiService = ApiClient.getClient(this).create(TicketApiService.class);

        // BE đã hỗ trợ lọc vé sắp bay, ta chỉ cần lấy trang 1, size 2
        apiService.getMyBookingsWithFilter(AppConfig.TOKEN, "UPCOMING", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingSummary> data = response.body().getResult().getData();
                    upcomingList.clear();

                    if (data != null) {
                        // Chỉ lấy 2 vé đầu cho Dashboard
                        for (int i = 0; i < Math.min(data.size(), 2); i++) {
                            upcomingList.add(data.get(i));
                        }
                    }
                    upcomingAdapter.setTicketList(upcomingList);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("MyTicket", "Error Fetching Upcoming: " + t.getMessage());
            }
        });
    }
}