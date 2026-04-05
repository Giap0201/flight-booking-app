package com.example.flight_booking_app.booking.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.HistoryAdapter;
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

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistoryTickets;
    private Spinner spinnerStatusFilter;

    private HistoryAdapter adapter;
    private List<BookingSummary> allTickets = new ArrayList<>();
    private List<BookingSummary> filteredTickets = new ArrayList<>();

    // Map các trạng thái UI với BE
    private final String[] statusNames = {"Tất cả", "Chờ xử lý", "Chờ thanh toán", "Đã thanh toán", "Xác nhận", "Đã huỷ", "Hoàn tiền"};
    private final String[] statusKeys = {"ALL", "PENDING", "AWAITING_PAYMENT", "PAID", "CONFIRMED", "CANCELLED", "REFUNDED"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistoryTickets = findViewById(R.id.rvHistoryTickets);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupRecyclerView();
        setupSpinner();

        fetchDataFromApi();
    }

    private void setupRecyclerView() {
        rvHistoryTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(filteredTickets);
        rvHistoryTickets.setAdapter(adapter);
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusNames);
        spinnerStatusFilter.setAdapter(spinnerAdapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyLocalFilter(statusKeys[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchDataFromApi() {
        BookingApiService apiService = ApiClient.getClient().create(BookingApiService.class);
        String myToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NDU2MTY1LCJpYXQiOjE3NzUzNjk3NjUsImp0aSI6IjI3NWRiOTk5LWFiNzMtNGQ0Mi04ZDIwLTEzODBjODY2NmQxOCIsInNjb3BlIjoiUk9MRV9VU0VSIn0.OZJaU3JAZouY6F2JJlsqUm4z5pwyeKVyIVxENb-xfexcP4bXYzVBeUmZctnjVwCNCqwEySaU549LyZoTVmUo0g";

        // VÌ KHÔNG ĐỤNG BE: Ta gọi trang 1 và lọc local. (Có thể tăng size trên BE sau nếu dữ liệu nhiều)
        apiService.getMyBookings(myToken, 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResult<BookingSummary>> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        allTickets.clear();
                        if (apiResponse.getResult().getData() != null) {
                            allTickets.addAll(apiResponse.getResult().getData());
                        }
                        // Lọc lại dựa trên Spinner hiện tại
                        int selectedPosition = spinnerStatusFilter.getSelectedItemPosition();
                        applyLocalFilter(statusKeys[selectedPosition]);
                    }
                } else {
                    Toast.makeText(HistoryActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(HistoryActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyLocalFilter(String statusKey) {
        filteredTickets.clear();
        for (BookingSummary ticket : allTickets) {
            if ("ALL".equals(statusKey)) {
                filteredTickets.add(ticket);
            } else if (statusKey.equalsIgnoreCase(ticket.getStatus())) {
                filteredTickets.add(ticket);
            }
        }
        adapter.notifyDataSetChanged();
    }
}