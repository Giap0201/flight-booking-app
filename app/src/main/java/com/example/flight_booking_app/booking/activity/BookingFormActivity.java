package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.flight_booking_app.booking.viewmodel.BookingViewModel;
import com.example.flight_booking_app.common.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class BookingFormActivity extends AppCompatActivity {

    // =========================================================
    // 1. KHAI BÁO GIAO DIỆN & DỮ LIỆU CHUNG
    // =========================================================
    private EditText edtContactName, edtContactEmail, edtContactPhone;
    private TextView tvTotalPrice;
    private Button btnConfirmBooking;

    private RecyclerView rvPassengers;
    private PassengerAdapter passengerAdapter;
    private List<PassengerRequest> listPassengers;

    private BookingViewModel viewModel;

    // =========================================================
    // 2. KHAI BÁO BIẾN CHO CHIỀU ĐI (SEGMENT 1)
    // =========================================================
    private String currentFlightId;
    private String currentFlightClassId;
    private double ticketPrice;

    // =========================================================
    // 3. KHAI BÁO BIẾN CHO CHIỀU VỀ (SEGMENT 2 - NẾU CÓ)
    // =========================================================
    private boolean isRoundTrip = false; // Cờ đánh dấu: true là khứ hồi, false là 1 chiều
    private String returnFlightId;
    private String returnFlightClassId;
    private double returnTicketPrice;

    // =========================================================
    // 4. BIẾN TÍNH TOÁN
    // =========================================================
    private double totalPrice; // Tổng tiền vé gốc (Chưa tính dịch vụ)
    private int adultCount, childCount, infantCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);

        // ==========================================================
        // BƯỚC 1: HỨNG DỮ LIỆU TỪ MÀN HÌNH CHỌN VÉ (INTENT)
        // ==========================================================
        Intent intent = getIntent();
        if (intent != null) {
            // Nhận số lượng khách
            adultCount = intent.getIntExtra("adultCount", 1);
            childCount = intent.getIntExtra("childCount", 0);
            infantCount = intent.getIntExtra("infantCount", 0);

            // Nhận vé CHIỀU ĐI (Bắt buộc phải có)
            currentFlightId = intent.getStringExtra("flightId");
            currentFlightClassId = intent.getStringExtra("flightClassId");
            ticketPrice = intent.getDoubleExtra("ticketPrice", 0.0);

            // ⚡ Nhận vé CHIỀU VỀ (Có thể có hoặc không) ⚡
            // Đọc cờ isRoundTrip để biết khách chọn loại hành trình nào
            isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);

            if (isRoundTrip) {
                returnFlightId = intent.getStringExtra("returnFlightId");
                returnFlightClassId = intent.getStringExtra("returnFlightClassId");
                returnTicketPrice = intent.getDoubleExtra("returnTicketPrice", 0.0);
            }
        }

        // ==========================================================
        // BƯỚC 2: ÁNH XẠ VIEW & TÍNH TOÁN TỔNG TIỀN
        // ==========================================================
        initViews();

        int totalTickets = adultCount + childCount + infantCount;

        // ⚡ LOGIC TÍNH TIỀN KHỨ HỒI ⚡
        // Nếu 1 chiều: Tiền = Giá vé chiều đi * Số người
        // Nếu Khứ hồi: Tiền = (Giá vé chiều đi + Giá vé chiều về) * Số người
        if (isRoundTrip) {
            totalPrice = (ticketPrice + returnTicketPrice) * totalTickets;
        } else {
            totalPrice = ticketPrice * totalTickets;
        }

        // In ra màn hình
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        tvTotalPrice.setText(formatter.format(totalPrice) + " đ");

        // ==========================================================
        // BƯỚC 3: TẠO FORM ĐIỀN TÊN HÀNH KHÁCH
        // ==========================================================
        listPassengers = new ArrayList<>();
        for (int i = 0; i < adultCount; i++) listPassengers.add(new PassengerRequest("", "", "", "MALE", "ADULT"));
        for (int i = 0; i < childCount; i++) listPassengers.add(new PassengerRequest("", "", "", "MALE", "CHILD"));
        for (int i = 0; i < infantCount; i++) listPassengers.add(new PassengerRequest("", "", "", "MALE", "INFANT"));

        rvPassengers.setLayoutManager(new LinearLayoutManager(this));
        passengerAdapter = new PassengerAdapter(this, listPassengers);
        rvPassengers.setAdapter(passengerAdapter);

        // Nút Xác nhận
        btnConfirmBooking.setOnClickListener(v -> submitBooking());
    }

    private void initViews() {
        edtContactName = findViewById(R.id.edtContactName);
        edtContactEmail = findViewById(R.id.edtContactEmail);
        edtContactPhone = findViewById(R.id.edtContactPhone);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        rvPassengers = findViewById(R.id.rvPassengers);
    }

    private void submitBooking() {
        // ===== A. Validate contact =====
        String contactName = edtContactName.getText().toString().trim();
        String contactEmail = edtContactEmail.getText().toString().trim();
        String contactPhone = edtContactPhone.getText().toString().trim();

        if (contactName.isEmpty() || contactEmail.isEmpty() || contactPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin liên hệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== B. Convert & validate passenger =====
        for (PassengerRequest passenger : listPassengers) {

            String dobInput = passenger.getDateOfBirth();

            if (dobInput == null || dobInput.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ngày sinh hành khách!", Toast.LENGTH_SHORT).show();
                return;
            }

            String dobApi = DateUtils.convertToApiFormat(dobInput);

            if (dobApi == null) {
                Toast.makeText(this, "Ngày sinh không hợp lệ! (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ghi đè lại thành format chuẩn API
            passenger.setDateOfBirth(dobApi);
        }

        // ==========================================================
        // ⚡ BƯỚC QUAN TRỌNG: ĐÓNG GÓI CHUYẾN BAY (1 HOẶC NHIỀU CHUYẾN) ⚡
        // ==========================================================
        BookingRequest request = new BookingRequest();
        request.setContactName(contactName);
        request.setContactEmail(edtContactEmail.getText().toString().trim());
        request.setContactPhone(edtContactPhone.getText().toString().trim());
        request.setCurrency("VND");
        request.setPromotionCode("");

        // Tạo giỏ chứa các chuyến bay
        List<FlightRequest> flights = new ArrayList<>();

        // 1. Chắc chắn phải add Chiều Đi (Segment 1)
        flights.add(new FlightRequest(currentFlightId, currentFlightClassId));

        // 2. Nếu là khứ hồi, add thêm Chiều Về (Segment 2)
        if (isRoundTrip && returnFlightId != null) {
            flights.add(new FlightRequest(returnFlightId, returnFlightClassId));
        }

        // Gắn giỏ chuyến bay vào Request tổng
        request.setFlights(flights);
        request.setPassengers(listPassengers);
        request.setBookingAncillaries(new ArrayList<>()); // Dịch vụ để trang sau tính

        // ==========================================================
        // CHUYỂN SANG MÀN HÌNH CHỌN DỊCH VỤ (ANCILLARY)
        // ==========================================================
        String[] dsTenHanhKhach = new String[listPassengers.size()];
        for (int i = 0; i < listPassengers.size(); i++) {
            PassengerRequest p = listPassengers.get(i);
            dsTenHanhKhach[i] = "Khách " + (i + 1) + ": " + p.getLastName() + " " + p.getFirstName();
        }

        Intent nextIntent = new Intent(BookingFormActivity.this, AncillaryActivity.class);
        nextIntent.putExtra("passengerNames", dsTenHanhKhach);
        nextIntent.putExtra("bookingRequest", request);

        // 1. Gửi TỔNG TIỀN (Chỉ để màn Hành lý hiển thị cho đẹp)
        nextIntent.putExtra("basePrice", (double) totalPrice);

        // 2. ⚡ SỬA CHỖ NÀY: Gửi GIÁ VÉ CỦA 1 NGƯỜI (Để màn Hóa đơn tính toán)
        // Nếu là khứ hồi thì giá 1 người = Giá vé đi + Giá vé về
        double unitTicketPrice = isRoundTrip ? (ticketPrice + returnTicketPrice) : ticketPrice;
        nextIntent.putExtra("ticketPrice", (double) unitTicketPrice); // Ép kiểu Double cẩn thận

        // 3. Truyền thêm cờ này sang trang Dịch vụ
        nextIntent.putExtra("isRoundTrip", isRoundTrip);

        startActivity(nextIntent);
    }
}

