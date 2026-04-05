package com.example.flight_booking_app.booking.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.UpcomingFlightAdapter;
import com.example.flight_booking_app.booking.api.BookingApiService;
import com.example.flight_booking_app.booking.model.BookingSummary;
import com.example.flight_booking_app.booking.model.PageResult;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpcomingFlightsActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerDay, spinnerClass;
    private RecyclerView rvUpcomingFlights;

    private List<String> dynamicMonths = new ArrayList<>();
    private List<String> dynamicDays = new ArrayList<>();
    private List<String> dynamicClasses = new ArrayList<>();

    private ArrayAdapter<String> monthAdapter;
    private ArrayAdapter<String> dayAdapter;
    private ArrayAdapter<String> classAdapter;

    private UpcomingFlightAdapter adapter;
    private List<BookingSummary> fullList = new ArrayList<>();
    private List<BookingSummary> filteredList = new ArrayList<>();

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_flights);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerClass = findViewById(R.id.spinnerClass);
        rvUpcomingFlights = findViewById(R.id.rvUpcomingFlights);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupFilters();
        setupRecyclerView();

        fetchApiData(1);
    }

    private void setupRecyclerView() {
        adapter = new UpcomingFlightAdapter(filteredList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvUpcomingFlights.setLayoutManager(layoutManager);
        rvUpcomingFlights.setAdapter(adapter);

        rvUpcomingFlights.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        isLoading = true;
                        currentPage++;
                        fetchApiData(currentPage);
                    }
                }
            }
        });
    }

    private void fetchApiData(int page) {
        BookingApiService apiService = ApiClient.getClient().create(BookingApiService.class);

        // LƯU Ý: Nhớ thay lại bằng SharedPreferences để lấy động, tránh lỗi 401 nhé!
        String myToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NDU2MTY1LCJpYXQiOjE3NzUzNjk3NjUsImp0aSI6IjI3NWRiOTk5LWFiNzMtNGQ0Mi04ZDIwLTEzODBjODY2NmQxOCIsInNjb3BlIjoiUk9MRV9VU0VSIn0.OZJaU3JAZouY6F2JJlsqUm4z5pwyeKVyIVxENb-xfexcP4bXYzVBeUmZctnjVwCNCqwEySaU549LyZoTVmUo0g";

        apiService.getMyBookings(myToken, page).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResult<BookingSummary>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        List<BookingSummary> newData = apiResponse.getResult().getData();

                        if (newData == null || newData.isEmpty()) {
                            isLastPage = true;
                            if (page == 1) {
                                fullList.clear();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(UpcomingFlightsActivity.this, "Bạn chưa có chuyến bay nào", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        if (newData.size() < PAGE_SIZE) {
                            isLastPage = true;
                        }

                        if (page == 1) {
                            fullList.clear();
                        }

                        for (BookingSummary ticket : newData) {
                            if (ticket.getStatus() != null &&
                                    (ticket.getStatus().equalsIgnoreCase("CONFIRMED") || ticket.getStatus().equalsIgnoreCase("ISSUED"))) {
                                fullList.add(ticket);
                            }
                        }

                        updateSpinnersAndList();
                    }
                } else {
                    Toast.makeText(UpcomingFlightsActivity.this, "Lỗi HTTP: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                isLoading = false;
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(UpcomingFlightsActivity.this, "Không thể kết nối Server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSpinnersAndList() {
        dynamicMonths.clear();
        dynamicMonths.addAll(getAvailableMonths());
        monthAdapter.notifyDataSetChanged();

        updateClassSpinner();
        triggerSearch();
    }

    private void setupFilters() {
        dynamicMonths.add("Tất cả");
        dynamicDays.add("Tất cả");
        dynamicClasses.add("Tất cả");

        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dynamicMonths);
        spinnerMonth.setAdapter(monthAdapter);

        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dynamicDays);
        spinnerDay.setAdapter(dayAdapter);

        classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dynamicClasses);
        spinnerClass.setAdapter(classAdapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (dynamicMonths.size() > position) {
                    updateDaySpinner(dynamicMonths.get(position));
                    triggerSearch();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                triggerSearch();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                triggerSearch();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private List<String> getAvailableMonths() {
        Set<String> monthSet = new LinkedHashSet<>();
        monthSet.add("Tất cả");
        for (BookingSummary flight : fullList) {
            String depTime = flight.getDepartureTime();
            if (depTime != null && depTime.length() >= 7) {
                try {
                    String monthNum = depTime.substring(5, 7);
                    monthSet.add("Tháng " + Integer.parseInt(monthNum));
                } catch (Exception ignored) {}
            }
        }
        return new ArrayList<>(monthSet);
    }

    private void updateDaySpinner(String selectedMonth) {
        Set<String> daySet = new LinkedHashSet<>();
        daySet.add("Tất cả");

        if (!selectedMonth.equals("Tất cả")) {
            spinnerDay.setEnabled(true);
            String monthNum = getMonthNumStr(selectedMonth);
            for (BookingSummary flight : fullList) {
                String depTime = flight.getDepartureTime();
                if (depTime != null && depTime.contains("-" + monthNum + "-")) {
                    daySet.add(depTime.substring(8, 10));
                }
            }
        } else {
            spinnerDay.setEnabled(false);
        }

        dynamicDays.clear();
        dynamicDays.addAll(daySet);
        dayAdapter.notifyDataSetChanged();
        spinnerDay.setSelection(0);
    }

    // ==========================================
    // ĐÃ SỬA: Hàm này dùng chung chung logic format chữ cái đầu
    // ==========================================
    private String formatClassName(String rawClass) {
        if (rawClass == null || rawClass.isEmpty()) return "";
        String lowerCaseClass = rawClass.replace("_", " ").toLowerCase();
        StringBuilder niceClass = new StringBuilder();
        String[] words = lowerCaseClass.split(" ");
        for (String word : words) {
            if (!word.isEmpty()) {
                niceClass.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return niceClass.toString().trim();
    }

    private void updateClassSpinner() {
        Set<String> classSet = new LinkedHashSet<>();
        classSet.add("Tất cả");

        // ĐÃ SỬA: Dùng hàm formatClassName để đẩy vào Spinner
        for (BookingSummary flight : fullList) {
            if (flight.getFlightClass() != null && !flight.getFlightClass().isEmpty()) {
                classSet.add(formatClassName(flight.getFlightClass()));
            }
        }

        dynamicClasses.clear();
        dynamicClasses.addAll(classSet);
        classAdapter.notifyDataSetChanged();
    }

    private void triggerSearch() {
        if (spinnerMonth.getSelectedItem() == null) return;

        String filterMonth = spinnerMonth.getSelectedItem().toString();
        String filterDay = spinnerDay.getSelectedItem() != null ? spinnerDay.getSelectedItem().toString() : "Tất cả";
        String filterClass = spinnerClass.getSelectedItem() != null ? spinnerClass.getSelectedItem().toString() : "Tất cả";

        filteredList.clear();
        for (BookingSummary flight : fullList) {

            // 1. Check Tháng
            String depTime = flight.getDepartureTime();
            if (depTime == null) continue;
            boolean matchMonth = filterMonth.equals("Tất cả") || depTime.contains("-" + getMonthNumStr(filterMonth) + "-");

            // 2. Check Ngày
            String formattedDay = filterDay.length() == 1 ? "0" + filterDay : filterDay;
            boolean matchDay = filterDay.equals("Tất cả") || depTime.contains("-" + formattedDay + "T");

            // 3. Check Hạng vé
            boolean matchClass = filterClass.equals("Tất cả");
            if (!matchClass && flight.getFlightClass() != null) {
                // ĐÃ SỬA: Dùng hàm formatClassName để so khớp chính xác với chữ người dùng vừa chọn
                String formattedFlightClass = formatClassName(flight.getFlightClass());
                matchClass = formattedFlightClass.equals(filterClass);
            }

            // Gộp 3 điều kiện
            if (matchMonth && matchDay && matchClass) {
                filteredList.add(flight);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String getMonthNumStr(String monthText) {
        String num = monthText.replace("Tháng ", "").trim();
        return num.length() == 1 ? "0" + num : num;
    }
}