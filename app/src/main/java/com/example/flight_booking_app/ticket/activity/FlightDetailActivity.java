package com.example.flight_booking_app.ticket.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.adapter.PassengerAdapter;
import com.example.flight_booking_app.ticket.api.TicketApiService;
import com.example.flight_booking_app.ticket.response.client.BookingDetailResponse;
import com.example.flight_booking_app.ticket.response.client.PassengerTicketResponse;
import com.example.flight_booking_app.ticket.response.client.TicketDetailResponse;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvDepTime, tvArrTime, tvDuration, tvOrigin, tvDest, tvAirline;
    private TextView tvPriceAirline, tvAdultCount, tvAdultSum, tvChildCount, tvChildSum, tvInfantCount, tvInfantSum, tvTotalPrice;
    private View layoutAdultPrice, layoutChildPrice, layoutInfantPrice;

    private RecyclerView rvPassengers;
    private PassengerAdapter passengerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_flight);

        initViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ĐÃ SỬA: Nhận String trực tiếp, không vòng vèo qua UUID
        String bookingId = getIntent().getStringExtra("BOOKING_ID");
        if (bookingId != null && !bookingId.isEmpty()) {
            fetchBookingDetail(bookingId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã vé!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvDepTime = findViewById(R.id.tvDepTime);
        tvArrTime = findViewById(R.id.tvArrTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvOrigin = findViewById(R.id.tvOrigin);
        tvDest = findViewById(R.id.tvDest);
        tvAirline = findViewById(R.id.tvAirline);
        tvPriceAirline = findViewById(R.id.tvPriceAirline);

        rvPassengers = findViewById(R.id.rvPassengers);
        rvPassengers.setLayoutManager(new LinearLayoutManager(this));
        passengerAdapter = new PassengerAdapter(new ArrayList<>());
        rvPassengers.setAdapter(passengerAdapter);

        layoutAdultPrice = findViewById(R.id.layoutAdultPrice);
        layoutChildPrice = findViewById(R.id.layoutChildPrice);
        layoutInfantPrice = findViewById(R.id.layoutInfantPrice);
        tvAdultCount = findViewById(R.id.tvAdultCount);
        tvAdultSum = findViewById(R.id.tvAdultSum);
        tvChildCount = findViewById(R.id.tvChildCount);
        tvChildSum = findViewById(R.id.tvChildSum);
        tvInfantCount = findViewById(R.id.tvInfantCount);
        tvInfantSum = findViewById(R.id.tvInfantSum);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
    }

    // ĐÃ SỬA: Tham số là String bookingId
    private void fetchBookingDetail(String bookingId) {
        TicketApiService apiService = ApiClient.getClient(this).create(TicketApiService.class);

        apiService.getBookingById(bookingId).enqueue(new Callback<ApiResponse<BookingDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDetailResponse>> call, Response<ApiResponse<BookingDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    bindDataToUi(response.body().getResult());
                } else {
                    Toast.makeText(FlightDetailActivity.this, "Không thể tải chi tiết vé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDetailResponse>> call, Throwable t) {
                Toast.makeText(FlightDetailActivity.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FLIGHT_DETAIL", "Lỗi: " + t.getMessage());
            }
        });
    }

    private void bindDataToUi(BookingDetailResponse data) {
        if (data == null) return;

        // 1. In Order ID
        tvOrderId.setText("Order ID  " + (data.getPnrCode() != null ? data.getPnrCode() : "N/A"));

        boolean hasDetailedFlightInfo = false; // Biến cờ hiệu theo dõi data

        // 2. Thử lấy data xịn từ API (Nếu Backend có trả về)
        List<PassengerTicketResponse> passengers = data.getPassengers();
        if (passengers != null && !passengers.isEmpty()) {
            passengerAdapter.setPassengers(passengers);

            PassengerTicketResponse firstPax = passengers.get(0);
            if (firstPax.getTickets() != null && !firstPax.getTickets().isEmpty()) {
                TicketDetailResponse t = firstPax.getTickets().get(0);

                tvDepTime.setText(formatTime(t.getDepartureTime()));
                tvArrTime.setText(formatTime(t.getArrivalTime()));
                tvOrigin.setText(t.getDepartureAirport() + " Airport");
                tvDest.setText(t.getArrivalAirport() + " Airport");
                tvDuration.setText(calculateDuration(t.getDepartureTime(), t.getArrivalTime()));

                String className = t.getClassType() != null ? formatClass(t.getClassType()) : "Economy";
                String flightInfo = "✈ " + (t.getFlightNumber() != null ? t.getFlightNumber() : "Flight") + " • " + className;
                tvAirline.setText(flightInfo);
                tvPriceAirline.setText(flightInfo);

                hasDetailedFlightInfo = true; // Đánh dấu là đã lấy được data xịn
            }
            calculatePriceBreakdown(passengers);
        }

        // 3. OPTION A: NẾU BE KHÔNG TRẢ DATA -> LÔI DỮ LIỆU DỰ PHÒNG TỪ INTENT RA DÙNG
        if (!hasDetailedFlightInfo) {
            String fallbackOrigin = getIntent().getStringExtra("ORIGIN");
            String fallbackDest = getIntent().getStringExtra("DEST");
            String fallbackDepTime = getIntent().getStringExtra("DEP_TIME");

            tvOrigin.setText(fallbackOrigin != null ? fallbackOrigin : "N/A");
            tvDest.setText(fallbackDest != null ? fallbackDest : "N/A");
            tvDepTime.setText(formatTime(fallbackDepTime));

            // Các trường không có thì gán mặc định cho đẹp UI
            tvArrTime.setText("--:--");
            tvDuration.setText("Chi tiết");
            tvAirline.setText("✈ Flight • Economy");
            tvPriceAirline.setText("✈ Flight • Economy");
        }

        // 4. In Tổng tiền
        NumberFormat formatVND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        BigDecimal total = BigDecimal.valueOf(data.getTotalAmount());
        tvTotalPrice.setText(formatVND.format(total));
    }
    private void calculatePriceBreakdown(List<PassengerTicketResponse> passengers) {
        int adultQty = 0, childQty = 0, infantQty = 0;
        BigDecimal adultSum = BigDecimal.ZERO, childSum = BigDecimal.ZERO, infantSum = BigDecimal.ZERO;

        for (PassengerTicketResponse p : passengers) {
            BigDecimal price = BigDecimal.ZERO;
            if (p.getTickets() != null && !p.getTickets().isEmpty()) {
                // ĐÃ SỬA: Ép kiểu an toàn từ double sang BigDecimal
                price = BigDecimal.valueOf(p.getTickets().get(0).getTotalAmount());
            }

            String type = p.getType() != null ? p.getType().toUpperCase() : "ADULT";

            if (type.contains("ADULT")) {
                adultQty++;
                adultSum = adultSum.add(price);
            } else if (type.contains("CHILD")) {
                childQty++;
                childSum = childSum.add(price);
            } else {
                infantQty++;
                infantSum = infantSum.add(price);
            }
        }

        NumberFormat formatVND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        updatePriceLayout(layoutAdultPrice, tvAdultCount, tvAdultSum, adultQty, "Adult", adultSum, formatVND);
        updatePriceLayout(layoutChildPrice, tvChildCount, tvChildSum, childQty, "Children", childSum, formatVND);
        updatePriceLayout(layoutInfantPrice, tvInfantCount, tvInfantSum, infantQty, "Infants", infantSum, formatVND);
    }

    // Hàm tiện ích cập nhật UI giá tiền
    private void updatePriceLayout(View layout, TextView tvCount, TextView tvSum, int qty, String label, BigDecimal sum, NumberFormat format) {
        if (qty > 0) {
            layout.setVisibility(View.VISIBLE);
            tvCount.setText(qty + " " + label);
            tvSum.setText(format.format(sum));
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    // Hàm định dạng thời gian
    private String formatTime(String localDateTimeStr) {
        if (localDateTimeStr == null || localDateTimeStr.trim().isEmpty() || localDateTimeStr.equals("null")) return "--:--";
        try {
            int tIndex = localDateTimeStr.indexOf('T');
            if (tIndex != -1 && localDateTimeStr.length() >= tIndex + 6) {
                return localDateTimeStr.substring(tIndex + 1, tIndex + 6);
            }
        } catch (Exception ignored) {}
        return localDateTimeStr;
    }

    // Hàm tính khoảng cách thời gian (FE tự xử lý)
    private String calculateDuration(String departure, String arrival) {
        if (departure == null || departure.trim().isEmpty() || arrival == null || arrival.trim().isEmpty()) return "Chi tiết";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String safeDep = departure.length() > 19 ? departure.substring(0, 19) : departure;
            String safeArr = arrival.length() > 19 ? arrival.substring(0, 19) : arrival;
            
            Date depDate = format.parse(safeDep);
            Date arrDate = format.parse(safeArr);

            if (depDate != null && arrDate != null) {
                long diffInMillis = arrDate.getTime() - depDate.getTime();
                if (diffInMillis < 0) return "Chi tiết";
                long hours = diffInMillis / (60 * 60 * 1000);
                long minutes = (diffInMillis / (60 * 1000)) % 60;
                if (minutes == 0) return hours + "h";
                return hours + "h " + minutes + "m";
            }
        } catch (Exception e) {
            return "Chi tiết";
        }
        return "Chi tiết";
    }

    public String formatClass(String rawClass) {
        if (rawClass == null || rawClass.trim().isEmpty()) return "Economy";
        try {
            String[] words = rawClass.replace("_", " ").toLowerCase().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (word != null && word.length() > 0) {
                    sb.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        sb.append(word.substring(1));
                    }
                    sb.append(" ");
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return rawClass;
        }
    }
}