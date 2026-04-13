package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.PassengerAdapter;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.FlightRequest;
import com.example.flight_booking_app.booking.model.PassengerRequest;
import com.example.flight_booking_app.common.DateUtils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingFormActivity extends AppCompatActivity {

    private EditText edtContactName, edtContactEmail, edtContactPhone;
    private TextView tvTotalPrice;
    private Button btnConfirmBooking;
    private RecyclerView rvPassengers;
    private PassengerAdapter passengerAdapter;
    private List<PassengerRequest> listPassengers;

    private String currentFlightId, currentFlightClassId;
    private double ticketPrice;
    private boolean isRoundTrip = false;
    private String returnFlightId, returnFlightClassId;
    private double returnTicketPrice;
    private double totalPrice;
    private int adultCount, childCount, infantCount;

    // ⚡ MỚI: Cần ngày khởi hành để tính tuổi chính xác
    private String departureDateStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);

        nhanDuLieuTuIntent();
        initViews();
        tinhToanHienThiTien();
        khoiTaoFormHanhKhach();

        btnConfirmBooking.setOnClickListener(v -> submitBooking());
    }

    private void nhanDuLieuTuIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            adultCount = intent.getIntExtra("adultCount", 1);
            childCount = intent.getIntExtra("childCount", 0);
            infantCount = intent.getIntExtra("infantCount", 0);
            currentFlightId = intent.getStringExtra("flightId");
            currentFlightClassId = intent.getStringExtra("flightClassId");
            ticketPrice = intent.getDoubleExtra("ticketPrice", 0.0);
            isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);

            // Nhận ngày khởi hành (Format dd/MM/yyyy) để tính tuổi
            departureDateStr = intent.getStringExtra("departureDate");

            if (isRoundTrip) {
                returnFlightId = intent.getStringExtra("returnFlightId");
                returnFlightClassId = intent.getStringExtra("returnFlightClassId");
                returnTicketPrice = intent.getDoubleExtra("returnTicketPrice", 0.0);
            }
        }
    }

    private void tinhToanHienThiTien() {
        // 1. Tính giá vé cơ bản của 1 người lớn (Bao gồm cả vé đi và vé về nếu là khứ hồi)
        double baseTicketPricePerPerson = isRoundTrip ? (ticketPrice + returnTicketPrice) : ticketPrice;

        // 2. Tính tổng tiền vé theo tỷ lệ của từng độ tuổi (Giống logic bên PaymentSummaryActivity)
        double adultTotalBase = adultCount * baseTicketPricePerPerson;                  // Người lớn: 100% giá vé
        double childTotalBase = childCount * (baseTicketPricePerPerson * 0.75);         // Trẻ em: 75% giá vé
        double infantTotalBase = infantCount * (baseTicketPricePerPerson * 0.10);       // Em bé: 10% giá vé

        // 3. Cộng tổng tiền lại
        totalPrice = adultTotalBase + childTotalBase + infantTotalBase;

        // 4. Format và hiển thị lên UI
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        tvTotalPrice.setText(formatter.format(totalPrice) + " đ");
    }

    private void submitBooking() {
        // 1. Validate Thông tin liên hệ
        if (!validateContactInfo()) return;

        // 2. Validate Thông tin từng hành khách
        if (!validatePassengers()) return;

        // 3. Nếu mọi thứ OK -> Đóng gói dữ liệu
        chuyenSangManHinhDichVu();
    }

    private boolean validateContactInfo() {
        String name = edtContactName.getText().toString().trim();
        String email = edtContactEmail.getText().toString().trim();
        String phone = edtContactPhone.getText().toString().trim();

        if (name.isEmpty()) {
            edtContactName.setError("Vui lòng nhập tên liên hệ");
            return false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtContactEmail.setError("Email không đúng định dạng");
            return false;
        }
        if (phone.isEmpty() || phone.length() < 10) {
            edtContactPhone.setError("Số điện thoại không hợp lệ");
            return false;
        }
        return true;
    }

    private boolean validatePassengers() {
        for (int i = 0; i < listPassengers.size(); i++) {
            PassengerRequest p = listPassengers.get(i);
            String label = "Hành khách thứ " + (i + 1);

            // Kiểm tra tên
            if (p.getFirstName().trim().isEmpty() || p.getLastName().trim().isEmpty()) {
                Toast.makeText(this, label + ": Vui lòng nhập đầy đủ Họ và Tên", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Kiểm tra ngày sinh rỗng
            String dobInput = p.getDateOfBirth();
            if (dobInput == null || dobInput.isEmpty()) {
                Toast.makeText(this, label + ": Vui lòng nhập ngày sinh", Toast.LENGTH_SHORT).show();
                return false;
            }

            // ⚡ KIỂM TRA ĐỘ TUỔI VÀ LOẠI VÉ (Logic cốt lõi) ⚡
            try {
                // Parse ngày sinh và ngày khởi hành
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate birthDate = LocalDate.parse(dobInput, formatter);

                // Nếu không có ngày khởi hành từ intent, lấy tạm ngày hiện tại
                LocalDate depDate = (departureDateStr != null) ? LocalDate.parse(departureDateStr, formatter) : LocalDate.now();

                int age = Period.between(birthDate, depDate).getYears();
                String type = p.getType(); // ADULT, CHILD, INFANT

                if ("INFANT".equals(type) && age >= 2) {
                    Toast.makeText(this, label + " phải dưới 2 tuổi (Hiện tại " + age + " tuổi)", Toast.LENGTH_LONG).show();
                    return false;
                }
                if ("CHILD".equals(type) && (age < 2 || age >= 12)) {
                    Toast.makeText(this, label + " phải từ 2 đến dưới 12 tuổi", Toast.LENGTH_LONG).show();
                    return false;
                }
                if ("ADULT".equals(type) && age < 12) {
                    Toast.makeText(this, label + " phải từ 12 tuổi trở lên", Toast.LENGTH_LONG).show();
                    return false;
                }

                // Chuyển đổi sang định dạng API (yyyy-MM-dd) sau khi đã validate xong
                p.setDateOfBirth(DateUtils.convertToApiFormat(dobInput));

            } catch (Exception e) {
                Toast.makeText(this, label + ": Ngày sinh không đúng định dạng dd/MM/yyyy", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void chuyenSangManHinhDichVu() {
        BookingRequest request = new BookingRequest();
        request.setContactName(edtContactName.getText().toString().trim());
        request.setContactEmail(edtContactEmail.getText().toString().trim());
        request.setContactPhone(edtContactPhone.getText().toString().trim());
        request.setCurrency("VND");

        List<FlightRequest> flights = new ArrayList<>();
        flights.add(new FlightRequest(currentFlightId, currentFlightClassId));
        if (isRoundTrip && returnFlightId != null) {
            flights.add(new FlightRequest(returnFlightId, returnFlightClassId));
        }

        request.setFlights(flights);
        request.setPassengers(listPassengers);
        request.setBookingAncillaries(new ArrayList<>());

        // Chuyển màn hình
        String[] dsTen = new String[listPassengers.size()];
        for (int i = 0; i < listPassengers.size(); i++) {
            dsTen[i] = listPassengers.get(i).getLastName() + " " + listPassengers.get(i).getFirstName();
        }

        Intent nextIntent = new Intent(this, AncillaryActivity.class);
        nextIntent.putExtra("passengerNames", dsTen);
        nextIntent.putExtra("bookingRequest", request);
        nextIntent.putExtra("basePrice", totalPrice);
        nextIntent.putExtra("ticketPrice", isRoundTrip ? (ticketPrice + returnTicketPrice) : ticketPrice);
        nextIntent.putExtra("isRoundTrip", isRoundTrip);
        startActivity(nextIntent);
    }

    private void khoiTaoFormHanhKhach() {
        listPassengers = new ArrayList<>();
        for (int i = 0; i < adultCount; i++) listPassengers.add(new PassengerRequest("", "", "", "MALE", "ADULT"));
        for (int i = 0; i < childCount; i++) listPassengers.add(new PassengerRequest("", "", "", "MALE", "CHILD"));
        for (int i = 0; i < infantCount; i++) listPassengers.add(new PassengerRequest("", "", "", "MALE", "INFANT"));

        rvPassengers.setLayoutManager(new LinearLayoutManager(this));
        passengerAdapter = new PassengerAdapter(this, listPassengers);
        rvPassengers.setAdapter(passengerAdapter);
    }

    private void initViews() {
        edtContactName = findViewById(R.id.edtContactName);
        edtContactEmail = findViewById(R.id.edtContactEmail);
        edtContactPhone = findViewById(R.id.edtContactPhone);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        rvPassengers = findViewById(R.id.rvPassengers);
    }
}