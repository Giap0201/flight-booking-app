package com.example.flight_booking_app.ticket.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.adapter.PendingTicketAdapter;
import com.example.flight_booking_app.ticket.api.TicketApiService;
import com.example.flight_booking_app.ticket.model.BookingSummary;
import com.example.flight_booking_app.ticket.model.PageResult;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaitingForPaymentActivity extends AppCompatActivity {

    private RecyclerView rvAllWaiting;
    private PendingTicketAdapter adapter;
    private List<BookingSummary> waitingList = new ArrayList<>();
    private ProgressBar progressBar;


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
        TicketApiService apiService = ApiClient.getClient(this).create(TicketApiService.class);

        // Theo BE: Lấy ALL rồi lọc ở FE
        // Lấy size lớn (ví dụ 50) để đảm bảo không bỏ sót vé pending nào ở các trang sau
        apiService.getMyBookingsWithFilter("ALL", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
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