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
import com.example.flight_booking_app.booking.viewmodel.FlightViewModel;
import com.example.flight_booking_app.common.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class BookingFormActivity extends AppCompatActivity {

    private EditText edtContactName, edtContactEmail, edtContactPhone;
    private TextView tvTotalPrice;
    private Button btnConfirmBooking;

    // ĐÃ THAY ĐỔI: Chuyển từ ListView sang RecyclerView
    private RecyclerView rvPassengers;
    private PassengerAdapter passengerAdapter;
    private List<PassengerRequest> listPassengers;

    private FlightViewModel viewModel;

    private String currentFlightId;
    private String currentFlightClassId;
    private double ticketPrice;
    private double totalPrice; // Lưu lại tổng tiền gốc để truyền sang màn sau

    // Thêm 3 biến để hứng số lượng hành khách
    private int adultCount;
    private int childCount;
    private int infantCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);

        // 1. Nhận toàn bộ dữ liệu từ màn trước
        Intent intent = getIntent();
        if (intent != null) {
            currentFlightId = intent.getStringExtra("flightId");
            currentFlightClassId = intent.getStringExtra("flightClassId");
            ticketPrice = intent.getDoubleExtra("ticketPrice", 0.0);

            // Lấy số lượng người (Nếu ko có thì mặc định là 1 người lớn)
            adultCount = intent.getIntExtra("adultCount", 1);
            childCount = intent.getIntExtra("childCount", 0);
            infantCount = intent.getIntExtra("infantCount", 0);

            // TODO: KHI NÀO GHÉP CODE THẬT VỚI MÀN HÌNH TÌM KIẾM THÌ BỎ COMMENT 3 DÒNG NÀY ĐỂ KHÔNG BỊ FIX CỨNG
//            adultCount = 1;
//            childCount = 1;
//            infantCount = 1;
        }

        // 2. Ánh xạ view
        edtContactName = findViewById(R.id.edtContactName);
        edtContactEmail = findViewById(R.id.edtContactEmail);
        edtContactPhone = findViewById(R.id.edtContactPhone);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Ánh xạ RecyclerView
        rvPassengers = findViewById(R.id.rvPassengers);

        // 3. Tính toán và hiển thị tổng tiền
        int totalTickets = adultCount + childCount + infantCount;
        totalPrice = ticketPrice * totalTickets;

        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        tvTotalPrice.setText(formatter.format(totalPrice) + " đ");

        // 4. Sinh danh sách form hành khách DỰA TRÊN SỐ LƯỢNG
        listPassengers = new ArrayList<>();

        for (int i = 0; i < adultCount; i++) {
            listPassengers.add(new PassengerRequest("", "", "", "MALE", "ADULT"));
        }
        for (int i = 0; i < childCount; i++) {
            listPassengers.add(new PassengerRequest("", "", "", "MALE", "CHILD"));
        }
        for (int i = 0; i < infantCount; i++) {
            listPassengers.add(new PassengerRequest("", "", "", "MALE", "INFANT"));
        }

        // 5. Cài đặt RecyclerView và Adapter
        // Bắt buộc phải có LayoutManager để RecyclerView biết cách sắp xếp các item (từ trên xuống dưới)
        rvPassengers.setLayoutManager(new LinearLayoutManager(this));
        passengerAdapter = new PassengerAdapter(this, listPassengers);
        rvPassengers.setAdapter(passengerAdapter);

        // 6. Khởi tạo ViewModel & Bắt sự kiện
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);
        btnConfirmBooking.setOnClickListener(v -> submitBooking());
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

        // ===== C. Build request (Tạo object request để mang sang màn hình sau) =====
        BookingRequest request = new BookingRequest();
        request.setContactName(contactName);
        request.setContactEmail(contactEmail);
        request.setContactPhone(contactPhone);
        request.setCurrency("VND");
        request.setPromotionCode("");

        List<FlightRequest> flights = new ArrayList<>();
        flights.add(new FlightRequest(currentFlightId, currentFlightClassId));
        request.setFlights(flights);
        request.setPassengers(listPassengers);

        // Khởi tạo danh sách dịch vụ rỗng (vì bước này chưa chọn)
        request.setBookingAncillaries(new ArrayList<>());

        // ==========================================================
        // D. CHUYỂN SANG MÀN HÌNH CHỌN DỊCH VỤ (ANCILLARY)
        // ==========================================================

        // 1. Tạo mảng tên hành khách để hiện trong Dialog ở màn sau
        String[] dsTenHanhKhach = new String[listPassengers.size()];
        for (int i = 0; i < listPassengers.size(); i++) {
            PassengerRequest p = listPassengers.get(i);
            dsTenHanhKhach[i] = "Khách " + (i + 1) + ": " + p.getLastName() + " " + p.getFirstName();
        }

        // 2. Tạo Intent để chuyển trang
        Intent nextIntent = new Intent(BookingFormActivity.this, AncillaryActivity.class);

        // 3. Gói dữ liệu gửi đi
        nextIntent.putExtra("passengerNames", dsTenHanhKhach);
        nextIntent.putExtra("bookingRequest", request);
        nextIntent.putExtra("basePrice", totalPrice);   // Tiền tổng

        // 🔥 THÊM DÒNG NÀY: Phải gửi giá của 1 vé đi thì màn cuối mới chia tiền được!
        nextIntent.putExtra("ticketPrice", ticketPrice);

        // 4. Thực hiện chuyển trang
        startActivity(nextIntent);
    }
}