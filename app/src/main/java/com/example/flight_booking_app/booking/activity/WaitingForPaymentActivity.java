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

public class WaitingForPaymentActivity extends AppCompatActivity {

    private RecyclerView rvPendingAll;
    private PendingTicketAdapter adapter;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_payment);

        rvPendingAll = findViewById(R.id.rvPendingAll);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchAllPendingTickets();
    }

    private void setupRecyclerView() {
        rvPendingAll.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingTicketAdapter(new ArrayList<>());
        rvPendingAll.setAdapter(adapter);
    }

    private void fetchAllPendingTickets() {
        BookingApiService apiService = ApiClient.getClient().create(BookingApiService.class);

        // Tạm dùng Token cứng giống file trước của bạn
        String myToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NDU2MTY1LCJpYXQiOjE3NzUzNjk3NjUsImp0aSI6IjI3NWRiOTk5LWFiNzMtNGQ0Mi04ZDIwLTEzODBjODY2NmQxOCIsInNjb3BlIjoiUk9MRV9VU0VSIn0.OZJaU3JAZouY6F2JJlsqUm4z5pwyeKVyIVxENb-xfexcP4bXYzVBeUmZctnjVwCNCqwEySaU549LyZoTVmUo0g";

        // GỌI API VỚI FILTER "PENDING"
        apiService.getMyBookingsWithFilter(myToken, "PENDING", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResult<BookingSummary>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        List<BookingSummary> tickets = apiResponse.getResult().getData();
                        if (tickets != null && !tickets.isEmpty()) {
                            adapter.setTicketList(tickets);
                        } else {
                            Toast.makeText(WaitingForPaymentActivity.this, "Bạn không có vé nào đang chờ thanh toán", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("WAITING_PAYMENT", "Lỗi: " + t.getMessage());
                Toast.makeText(WaitingForPaymentActivity.this, "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}