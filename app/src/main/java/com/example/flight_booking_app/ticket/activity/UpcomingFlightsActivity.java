package com.example.flight_booking_app.ticket.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.adapter.UpcomingTicketAdapter;
import com.example.flight_booking_app.ticket.api.TicketApiService;
import com.example.flight_booking_app.ticket.model.BookingSummary;
import com.example.flight_booking_app.ticket.model.PageResult;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.common.AppConfig;
import com.example.flight_booking_app.network.ApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpcomingFlightsActivity extends AppCompatActivity {

    private RecyclerView rvUpcoming;
    private UpcomingTicketAdapter adapter;
    private ProgressBar progressBar;

    // Giao diện Lọc
    private Spinner spinnerOrigin, spinnerDest;
    private Button btnSearch;

    // Danh sách dữ liệu
    private List<BookingSummary> allFlightsList = new ArrayList<>(); // Chứa data gốc từ API
    private List<BookingSummary> displayList = new ArrayList<>();    // Chứa data sau khi lọc để in ra màn hình

//    private final String TOKEN = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NTU1MTI0LCJpYXQiOjE3NzU0Njg3MjQsImp0aSI6ImI1MjhjYmM3LWRmMTktNGY4OC1hODYzLTg5YmFiNDZlOWM1MyIsInNjb3BlIjoiUk9MRV9VU0VSIn0._h_1wlfj-JFL0LMbVjxhwdkc5Es15Py3WtVM_cayhGcoZOJHb36_YKgOSkEyuiAYwVfdEugKehj3weD0Dbt6KQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_flights);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Ánh xạ UI
        progressBar = findViewById(R.id.progressBar);
        rvUpcoming = findViewById(R.id.rvUpcoming);
        spinnerOrigin = findViewById(R.id.spinnerOrigin);
        spinnerDest = findViewById(R.id.spinnerDest);
        btnSearch = findViewById(R.id.btnSearch);

        // Setup RecyclerView
        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UpcomingTicketAdapter(displayList);
        rvUpcoming.setAdapter(adapter);

        // Bắt sự kiện bấm nút TÌM KIẾM
        btnSearch.setOnClickListener(v -> applyFilter());

        fetchUpcomingBookings();
    }

    private void fetchUpcomingBookings() {
        progressBar.setVisibility(View.VISIBLE);
        TicketApiService apiService = ApiClient.getClient(this).create(TicketApiService.class);

        apiService.getMyBookingsWithFilter(AppConfig.TOKEN, "UPCOMING", 1).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingSummary> data = response.body().getResult().getData();
                    allFlightsList.clear();

                    if (data != null && !data.isEmpty()) {
                        allFlightsList.addAll(data);
                        setupFilterSpinners(); // Nạp tên Sân bay vào 2 cái Spinner
                        applyFilter();         // Chạy lọc lần đầu để in ra danh sách (Mặc định là "Tất cả")
                    } else {
                        Toast.makeText(UpcomingFlightsActivity.this, "Không có chuyến bay nào sắp tới", Toast.LENGTH_SHORT).show();
                        adapter.setTicketList(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("UPCOMING_PAGE", "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Nạp danh sách các sân bay duy nhất vào Spinner (Người dùng tha hồ chọn mà không bị giật list)
// Nạp danh sách các sân bay duy nhất vào Spinner
    private void setupFilterSpinners() {
        Set<String> origins = new HashSet<>();
        Set<String> dests = new HashSet<>();

        // 1. Chỉ lọc lấy các sân bay thực tế từ Backend
        for (BookingSummary flight : allFlightsList) {
            if (flight.getOrigin() != null && !flight.getOrigin().isEmpty()) {
                origins.add(flight.getOrigin());
            }
            if (flight.getDestination() != null && !flight.getDestination().isEmpty()) {
                dests.add(flight.getDestination());
            }
        }

        // 2. Chuyển sang List để sắp xếp
        List<String> originList = new ArrayList<>(origins);
        List<String> destList = new ArrayList<>(dests);

        // 3. (Tùy chọn) Sắp xếp tên sân bay theo A-Z cho chuyên nghiệp
        Collections.sort(originList);
        Collections.sort(destList);

        // 4. [QUAN TRỌNG NHẤT] Ép chữ "Tất cả" vào vị trí số 0 (Đầu tiên)
        originList.add(0, "Tất cả");
        destList.add(0, "Tất cả");

        // 5. Đổ vào Spinner
        ArrayAdapter<String> originAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, originList);
        originAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(originAdapter);

        ArrayAdapter<String> destAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, destList);
        destAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDest.setAdapter(destAdapter);
    }
    // Hàm Lọc và Sắp xếp (Chỉ chạy khi bấm nút hoặc tải lần đầu)
    private void applyFilter() {
        if (spinnerOrigin.getSelectedItem() == null || spinnerDest.getSelectedItem() == null) return;

        String selectedOrigin = spinnerOrigin.getSelectedItem().toString();
        String selectedDest = spinnerDest.getSelectedItem().toString();

        displayList.clear();

        for (BookingSummary flight : allFlightsList) {
            boolean matchOrigin = selectedOrigin.equals("Tất cả") || selectedOrigin.equals(flight.getOrigin());
            boolean matchDest = selectedDest.equals("Tất cả") || selectedDest.equals(flight.getDestination());

            if (matchOrigin && matchDest) {
                displayList.add(flight);
            }
        }

        // Bắt buộc Sort lại theo Ngày (Mới nhất lên đầu) để Header nhóm ngày chạy đúng
        sortListByDepartureTimeAsc(displayList);

        // Cập nhật lên UI
        adapter.setTicketList(displayList);

        if(displayList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy chuyến bay phù hợp", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm Sort (Sắp bay lên đầu) - Giữ nguyên logic cũ của bạn
    private void sortListByDepartureTimeAsc(List<BookingSummary> list) {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
        Collections.sort(list, (t1, t2) -> {
            try {
                java.util.Date d1 = format.parse(t1.getDepartureTime() != null ? t1.getDepartureTime() : "");
                java.util.Date d2 = format.parse(t2.getDepartureTime() != null ? t2.getDepartureTime() : "");
                if (d1 != null && d2 != null) return d1.compareTo(d2);
                return 0;
            } catch (java.text.ParseException e) {
                return 0;
            }
        });
    }
}