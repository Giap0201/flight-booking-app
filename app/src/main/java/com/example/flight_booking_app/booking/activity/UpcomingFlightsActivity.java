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

    private final String[] classes = {"Tất cả", "Economy", "Premium Economy", "Business", "First"};

    private List<String> dynamicMonths = new ArrayList<>();
    private List<String> dynamicDays = new ArrayList<>();
    private ArrayAdapter<String> monthAdapter;
    private ArrayAdapter<String> dayAdapter;

    private UpcomingFlightAdapter adapter;
    private List<BookingSummary> fullList = new ArrayList<>();
    private List<BookingSummary> filteredList = new ArrayList<>();

    // Biến cờ để tránh lỗi filter chạy nhiều lần lúc mới khởi tạo màn hình
    private boolean isInitialSetup = true;

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
        fetchApiData();
    }

    private void setupRecyclerView() {
        adapter = new UpcomingFlightAdapter(filteredList);
        rvUpcomingFlights.setLayoutManager(new LinearLayoutManager(this));
        rvUpcomingFlights.setAdapter(adapter);
    }

    private void fetchApiData() {
        BookingApiService apiService = ApiClient.getClient().create(BookingApiService.class);

        String myToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiMDhhOWVlNjUtN2Q1MC00ODVhLThiYmEtZGNhZDcyODdiYzk0IiwiZXhwIjoxNzc1MTUxMDA0LCJpYXQiOjE3NzUwNjQ2MDQsImp0aSI6ImY3MmQ1NzMxLThlMzktNDlkZS04YTg1LTkwOTIwMjcwYzg4OSIsInNjb3BlIjoiUk9MRV9BRE1JTiJ9.F1AQEx6FrWSQGYWvZraHA_99QmeR71eL3szG6TGofxbJtvLF9x-vgpyT3zyIQttZHecQJ8gtvw6wIA6wDSCpEA";

        apiService.getMyBookings(myToken).enqueue(new Callback<ApiResponse<PageResult<BookingSummary>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResult<BookingSummary>>> call, Response<ApiResponse<PageResult<BookingSummary>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResult<BookingSummary>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        fullList.clear();
//                        fullList.addAll(apiResponse.getResult().getData());
                        for (BookingSummary ticket : apiResponse.getResult().getData()) {
                            if (ticket.getStatus() != null &&
                                    (ticket.getStatus().equalsIgnoreCase("CONFIRMED") || ticket.getStatus().equalsIgnoreCase("ISSUED"))) {
                                fullList.add(ticket);
                            }
                        }
                        // Cập nhật Spinner Tháng
                        dynamicMonths.clear();
                        dynamicMonths.addAll(getAvailableMonths());
                        monthAdapter.notifyDataSetChanged();

                        // Cập nhật Spinner Ngày lần đầu tiên dựa theo Tháng mặc định ("Tất cả")
                        updateDaySpinner("Tất cả");

                        isInitialSetup = false; // Xong bước khởi tạo
                        triggerSearch();
                    } else {
                        Toast.makeText(UpcomingFlightsActivity.this, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UpcomingFlightsActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResult<BookingSummary>>> call, Throwable t) {
                Log.e("UpcomingFlights", "API Call Failed: " + t.getMessage());
                Toast.makeText(UpcomingFlightsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilters() {
        dynamicMonths.add("Tất cả");
        dynamicDays.add("Tất cả");

        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dynamicMonths);
        spinnerMonth.setAdapter(monthAdapter);

        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dynamicDays);
        spinnerDay.setAdapter(dayAdapter);

        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classes);
        spinnerClass.setAdapter(classAdapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isInitialSetup && dynamicMonths.size() > position) {
                    String selectedMonth = dynamicMonths.get(position);
                    updateDaySpinner(selectedMonth); // Khi đổi Tháng, nạp lại danh sách Ngày
                    triggerSearch();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isInitialSetup) triggerSearch();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isInitialSetup) triggerSearch();
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
                    String monthLabel = "Tháng " + Integer.parseInt(monthNum);
                    monthSet.add(monthLabel);
                } catch (Exception e) {
                    Log.e("Filter", "Lỗi parse tháng: " + depTime);
                }
            }
        }
        return new ArrayList<>(monthSet);
    }

    private List<String> getAvailableDays(String selectedMonthLabel) {
        Set<String> daySet = new LinkedHashSet<>();
        daySet.add("Tất cả");

        String monthNum = "";
        // Nếu không phải là "Tất cả", lấy mã tháng (VD: "09", "10")
        if (selectedMonthLabel != null && !selectedMonthLabel.equals("Tất cả")) {
            monthNum = getMonthNumStr(selectedMonthLabel);
        }

        for (BookingSummary flight : fullList) {
            String depTime = flight.getDepartureTime();
            if (depTime != null) {
                // Điều kiện: Nếu chọn "Tất cả" (monthNum rỗng) HOẶC vé thuộc đúng tháng đang chọn
                if (monthNum.isEmpty() || depTime.contains("-" + monthNum + "-")) {
                    try {
                        String day = depTime.substring(8, 10);
                        daySet.add(day);
                    } catch (Exception e) {
                        Log.e("Filter", "Lỗi parse ngày: " + depTime);
                    }
                }
            }
        }
        return new ArrayList<>(daySet);
    }

    private void updateDaySpinner(String selectedMonth) {
        dynamicDays.clear();
        dynamicDays.addAll(getAvailableDays(selectedMonth));

        // LUÔN LUÔN BẬT SPINNER NGÀY (Sáng lên) DÙ CHỌN THÁNG NÀO
        spinnerDay.setEnabled(true);

        dayAdapter.notifyDataSetChanged();

        // Tránh tình trạng list ngày ngắn lại mà ô đang chọn bị kẹt ở index cũ
        spinnerDay.setSelection(0);
    }

    // --- LOGIC TÌM KIẾM ĐÃ SỬA LẠI ---

    private void triggerSearch() {
        if (spinnerMonth.getSelectedItem() == null) return;

        String filterMonth = spinnerMonth.getSelectedItem().toString();
        String filterDay = spinnerDay.getSelectedItem() != null ? spinnerDay.getSelectedItem().toString() : "Tất cả";
        String filterClass = spinnerClass.getSelectedItem().toString();

        filteredList.clear();

        for (BookingSummary flight : fullList) {
            String depTime = flight.getDepartureTime();
            if (depTime == null) depTime = "";

            // 1. Kiểm tra Tháng
            boolean matchMonth = filterMonth.equals("Tất cả") || depTime.contains("-" + getMonthNumStr(filterMonth) + "-");

            // 2. Kiểm tra Ngày
            String formattedDay = filterDay.length() == 1 ? "0" + filterDay : filterDay;
            boolean matchDay = filterDay.equals("Tất cả") || depTime.contains("-" + formattedDay + "T");

            // 3. Kiểm tra Class (Hạng vé)
            // LƯU Ý: Vì API chưa trả về field Hạng vé nên trên Adapter bạn đang set cứng TextView là "Economy".
            // Do đó, logic lọc ở đây mình sẽ quy ước mọi vé nhận về đều tạm thời là "Economy".
            // Logic mới lấy từ dữ liệu thật:
            String currentFlightClass = flight.getFlightClass() != null ? flight.getFlightClass() : "Economy";
            boolean matchClass = filterClass.equals("Tất cả") || currentFlightClass.equalsIgnoreCase(filterClass);
            // Nếu thỏa mãn toàn bộ điều kiện thì mới hiển thị
            if (matchMonth && matchDay && matchClass) {
                filteredList.add(flight);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private String getMonthNumStr(String monthText) {
        String num = monthText.replace("Tháng ", "").trim();
        if (num.length() == 1) {
            return "0" + num;
        }
        return num;
    }
}