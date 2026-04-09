package com.example.flight_booking_app.ticket.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.adapter.HistoryAdapter;
import com.example.flight_booking_app.ticket.api.TicketApiService;
import com.example.flight_booking_app.ticket.model.BookingSummary;
import com.example.flight_booking_app.ticket.model.PageResult;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private ProgressBar progressBar;
    private HistoryAdapter adapter;
    private List<BookingSummary> historyList = new ArrayList<>();

    // Phân trang
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progressBar);
        rvHistory = findViewById(R.id.rvHistory);

        setupRecyclerView();
        fetchHistoryBookings(currentPage);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvHistory.setLayoutManager(layoutManager);
        adapter = new HistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);

        // Bắt sự kiện cuộn xuống đáy để Load More
        rvHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // Đang cuộn xuống
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && currentPage < totalPages) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            currentPage++;
                            fetchHistoryBookings(currentPage);
                        }
                    }
                }
            }
        });
    }

    private void fetchHistoryBookings(int page) {
        isLoading = true;
        if (page == 1) progressBar.setVisibility(View.VISIBLE);

        TicketApiService apiService = ApiClient.getClient(this).create(TicketApiService.class);

        // DÙNG FILTER="ALL", TỰ LỌC VÀ SORT BẰNG JAVA
        apiService.getMyBookingsWithFilter("ALL", page).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    PageResult<BookingSummary> pageResult = response.body().getResult();
                    List<BookingSummary> allData = pageResult.getData();
                    totalPages = pageResult.getTotalPages();

                    if (page == 1) historyList.clear();

                    if (allData != null) {
                        for (BookingSummary ticket : allData) {
                            String status = ticket.getStatus();

                            // 1. Logic lọc vé Đã hủy
                            boolean isCancelled = "CANCELLED".equals(status) || "REFUNDED".equals(status);

                            // 2. Logic lọc vé Đã bay (Hoàn thành)
                            boolean isCompleted = ("PAID".equals(status) || "CONFIRMED".equals(status)) && isPastFlight(ticket.getDepartureTime());

                            // Chỉ nạp vào danh sách nếu là History
                            if (isCancelled || isCompleted) {
                                historyList.add(ticket);
                            }
                        }
                    }

                    // 3. Logic Sort lại toàn bộ danh sách theo Ngày bay giảm dần (Mới nhất lên đầu)
                    sortListByDepartureTimeDesc(historyList);

                    adapter.setTickets(historyList);

                    if (historyList.isEmpty() && page == 1) {
                        Toast.makeText(HistoryActivity.this, "Chưa có lịch sử chuyến bay", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Log.e("HISTORY_PAGE", "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Hàm kiểm tra xem chuyến bay đã cất cánh chưa
    private boolean isPastFlight(String departureTimeStr) {
        if (departureTimeStr == null || departureTimeStr.isEmpty()) return false;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date departureDate = format.parse(departureTimeStr);
            return departureDate != null && departureDate.before(new Date()); // Nhỏ hơn giờ hiện tại
        } catch (ParseException e) {
            return false;
        }
    }

    // Hàm sắp xếp danh sách theo giờ bay giảm dần
    private void sortListByDepartureTimeDesc(List<BookingSummary> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Collections.sort(list, new Comparator<BookingSummary>() {
            @Override
            public int compare(BookingSummary t1, BookingSummary t2) {
                try {
                    Date d1 = format.parse(t1.getDepartureTime() != null ? t1.getDepartureTime() : "");
                    Date d2 = format.parse(t2.getDepartureTime() != null ? t2.getDepartureTime() : "");
                    // Đảo ngược d2 so với d1 để sort DESC
                    if (d1 != null && d2 != null) return d2.compareTo(d1);
                    return 0;
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
    }
}