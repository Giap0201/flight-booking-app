package com.example.flight_booking_app.booking.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.PassengerAdapter;
import com.example.flight_booking_app.booking.api.BookingApiService;
import com.example.flight_booking_app.booking.response.client.BookingDetailResponse;
import com.example.flight_booking_app.booking.response.client.PassengerTicketResponse;
import com.example.flight_booking_app.booking.response.client.TicketDetailResponse;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightDetailActivity extends AppCompatActivity {

    private TextView tvBookingSpace, tvOrderId, tvDepTime, tvArrTime, tvDuration, tvOrigin, tvDest, tvAirline;
    private TextView tvPriceAirline, tvAdultCount, tvAdultSum, tvChildCount, tvChildSum, tvInfantCount, tvInfantSum, tvTotalPrice;
    private View layoutAdultPrice, layoutChildPrice, layoutInfantPrice;

    private RecyclerView rvPassengers;
    private PassengerAdapter passengerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_detail);

        initViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String bookingIdString = getIntent().getStringExtra("BOOKING_ID");
        if (bookingIdString != null) {
            try {
                UUID id = UUID.fromString(bookingIdString);
                fetchBookingDetail(id);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "ID vé không đúng định dạng!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin vé!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvBookingSpace = findViewById(R.id.tvBookingSpace);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvDepTime = findViewById(R.id.tvDepTime);
        tvArrTime = findViewById(R.id.tvArrTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvOrigin = findViewById(R.id.tvOrigin);
        tvDest = findViewById(R.id.tvDest);
        tvAirline = findViewById(R.id.tvAirline);

        rvPassengers = findViewById(R.id.rvPassengers);
        rvPassengers.setLayoutManager(new LinearLayoutManager(this));
        passengerAdapter = new PassengerAdapter(new ArrayList<>());
        rvPassengers.setAdapter(passengerAdapter);

        tvPriceAirline = findViewById(R.id.tvPriceAirline);
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

    private void fetchBookingDetail(UUID bookingId) {
        BookingApiService apiService = ApiClient.getClient().create(BookingApiService.class);
        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ1dGMuY29tIiwic3ViIjoiNmYzNmIzOTItZjI4YS00ODg4LTgzM2MtY2ZjNmMxMDkyMDM0IiwiZXhwIjoxNzc1NDU2MTY1LCJpYXQiOjE3NzUzNjk3NjUsImp0aSI6IjI3NWRiOTk5LWFiNzMtNGQ0Mi04ZDIwLTEzODBjODY2NmQxOCIsInNjb3BlIjoiUk9MRV9VU0VSIn0.OZJaU3JAZouY6F2JJlsqUm4z5pwyeKVyIVxENb-xfexcP4bXYzVBeUmZctnjVwCNCqwEySaU549LyZoTVmUo0g";

        // ĐÃ SỬA: Đã truyền biến token vào hàm getBookingById
        apiService.getBookingById(token, bookingId).enqueue(new Callback<ApiResponse<BookingDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDetailResponse>> call, Response<ApiResponse<BookingDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    bindDataToUi(response.body().getResult());
                } else {
                    Toast.makeText(FlightDetailActivity.this, "Lỗi tải chi tiết vé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDetailResponse>> call, Throwable t) {
                Log.e("DETAIL_ERROR", t.getMessage());
                Toast.makeText(FlightDetailActivity.this, "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDataToUi(BookingDetailResponse data) {
        if (data.getPnrCode() != null) {
            tvOrderId.setText("Order ID  " + data.getPnrCode());
        }

        if (data.getPassengers() != null && !data.getPassengers().isEmpty()) {
            passengerAdapter.setPassengers(data.getPassengers());

            PassengerTicketResponse firstPassenger = data.getPassengers().get(0);
            if (firstPassenger.getTickets() != null && !firstPassenger.getTickets().isEmpty()) {
                TicketDetailResponse firstTicket = firstPassenger.getTickets().get(0);

                tvDepTime.setText(formatTime(firstTicket.getDepartureTime()));
                tvArrTime.setText(formatTime(firstTicket.getArrivalTime()));

                tvDuration.setText("2h 15m");
                tvOrigin.setText(firstTicket.getDepartureAirport() + " Airport");
                tvDest.setText(firstTicket.getArrivalAirport() + " Airport");

                // ĐÃ SỬA: Bỏ .name() vì getClassType() đã trả về String
                String className = firstTicket.getClassType() != null ? firstTicket.getClassType().replace("_", " ") : "Economy";
                if (className.length() > 1) {
                    className = className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
                }

                String flightName = "✈ " + (firstTicket.getFlightNumber() != null ? firstTicket.getFlightNumber() : "") + " • " + className;
                tvAirline.setText(flightName);
                tvPriceAirline.setText(flightName);
            }

            int adultQty = 0, childQty = 0, infantQty = 0;
            BigDecimal adultSum = BigDecimal.ZERO;
            BigDecimal childSum = BigDecimal.ZERO;
            BigDecimal infantSum = BigDecimal.ZERO;

            for (PassengerTicketResponse p : data.getPassengers()) {
                BigDecimal pTotal = BigDecimal.ZERO;
                if (p.getTickets() != null && !p.getTickets().isEmpty() && p.getTickets().get(0).getTotalAmount() != null) {
                    pTotal = p.getTickets().get(0).getTotalAmount();
                }

                // ĐÃ SỬA: Bỏ .name() vì getType() đã trả về String
                String pType = p.getType() != null ? p.getType().toUpperCase() : "ADULT";
                if (pType.contains("ADULT")) {
                    adultQty++; adultSum = adultSum.add(pTotal);
                } else if (pType.contains("CHILD")) {
                    childQty++; childSum = childSum.add(pTotal);
                } else {
                    infantQty++; infantSum = infantSum.add(pTotal);
                }
            }

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            if (adultQty > 0) {
                layoutAdultPrice.setVisibility(View.VISIBLE);
                tvAdultCount.setText(adultQty + " Adult");
                tvAdultSum.setText(format.format(adultSum));
            } else { layoutAdultPrice.setVisibility(View.GONE); }

            if (childQty > 0) {
                layoutChildPrice.setVisibility(View.VISIBLE);
                tvChildCount.setText(childQty + " Children");
                tvChildSum.setText(format.format(childSum));
            } else { layoutChildPrice.setVisibility(View.GONE); }

            if (infantQty > 0) {
                layoutInfantPrice.setVisibility(View.VISIBLE);
                tvInfantCount.setText(infantQty + " Infants");
                tvInfantSum.setText(format.format(infantSum));
            } else { layoutInfantPrice.setVisibility(View.GONE); }
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        BigDecimal total = data.getTotalAmount() != null ? data.getTotalAmount() : BigDecimal.ZERO;
        tvTotalPrice.setText(format.format(total));
    }

    private String formatTime(String localDateTimeStr) {
        if (localDateTimeStr == null || localDateTimeStr.equals("null")) return "--:--";
        try {
            int tIndex = localDateTimeStr.indexOf('T');
            if (tIndex != -1 && localDateTimeStr.length() >= tIndex + 6) {
                return localDateTimeStr.substring(tIndex + 1, tIndex + 6);
            }
        } catch (Exception ignored) {}
        return localDateTimeStr;
    }
}