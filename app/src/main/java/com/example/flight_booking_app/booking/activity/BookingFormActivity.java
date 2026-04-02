package com.example.flight_booking_app.booking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.adapter.PassengerAdapter;
import com.example.flight_booking_app.booking.model.AncillaryRequest;
import com.example.flight_booking_app.booking.model.BookingRequest;
import com.example.flight_booking_app.booking.model.FlightRequest;
import com.example.flight_booking_app.booking.model.PassengerRequest;
import com.example.flight_booking_app.booking.viewmodel.FlightViewModel;

import java.util.ArrayList;
import java.util.List;

public class BookingFormActivity extends AppCompatActivity {

    // Khai báo View thông tin người liên hệ
    private EditText edtContactName, edtContactEmail, edtContactPhone;
    private TextView tvTotalPrice;
    private Button btnConfirmBooking;

    // Khai báo ListView và Adapter cho Hành khách
    private ListView lvPassengers;
    private PassengerAdapter passengerAdapter;
    private List<PassengerRequest> listPassengers;

    // Khai báo ViewModel để gọi API
    private FlightViewModel viewModel;

    // Dữ liệu giả lập (Sau này bạn sẽ lấy từ Intent truyền từ màn hình FlightDetailActivity sang)
    // Đổi mấy biến này thành không có giá trị mặc định nữa
    private String currentFlightId;
    private String currentFlightClassId;
    private double ticketPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form); // Đảm bảo layout khớp với tên file XML bạn vừa tạo

        // --- THÊM ĐOẠN NÀY ĐỂ HỨNG DỮ LIỆU TỪ MÀN HÌNH TRƯỚC ---
        Intent intent = getIntent();
        if (intent != null) {
            currentFlightId = intent.getStringExtra("flightId");
            currentFlightClassId = intent.getStringExtra("flightClassId");
            ticketPrice = intent.getDoubleExtra("ticketPrice", 0.0);
        }
        // --------------------------------------------------------
        // 1. Ánh xạ View
        edtContactName = findViewById(R.id.edtContactName);
        edtContactEmail = findViewById(R.id.edtContactEmail);
        edtContactPhone = findViewById(R.id.edtContactPhone);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        lvPassengers = findViewById(R.id.lvPassengers);

        // Hiển thị giá tiền tạm tính
        tvTotalPrice.setText(ticketPrice + " VND");

        // 2. Cài đặt dữ liệu mặc định cho ListView Hành khách
        listPassengers = new ArrayList<>();
        // Giả sử khách hàng chọn mua 1 vé người lớn, mình tạo sẵn 1 form trống cho họ điền
        listPassengers.add(new PassengerRequest("", "", "", "MALE", "ADULT"));

        passengerAdapter = new PassengerAdapter(this, listPassengers);
        lvPassengers.setAdapter(passengerAdapter);

        // 3. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);

        // 4. Bắt sự kiện bấm nút ĐẶT VÉ
        btnConfirmBooking.setOnClickListener(v -> submitBooking());
    }

    private void submitBooking() {
        // A. Lấy dữ liệu người liên hệ từ các ô nhập
        String contactName = edtContactName.getText().toString().trim();
        String contactEmail = edtContactEmail.getText().toString().trim();
        String contactPhone = edtContactPhone.getText().toString().trim();

        // Kiểm tra nhanh xem khách đã nhập đủ chưa (Validate cơ bản)
        if (contactName.isEmpty() || contactEmail.isEmpty() || contactPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin liên hệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // B. Tạo cục dữ liệu tổng (BookingRequest)
        BookingRequest request = new BookingRequest();
        request.setContactName(contactName);
        request.setContactEmail(contactEmail);
        request.setContactPhone(contactPhone);
        request.setCurrency("VND");
        request.setPromotionCode(""); // Tạm thời để trống

        // C. Tạo mảng Chuyến bay (flights)
        List<FlightRequest> flights = new ArrayList<>();
        flights.add(new FlightRequest(currentFlightId, currentFlightClassId));
        request.setFlights(flights);

        // D. Gắn danh sách Hành khách (Đã được Adapter tự động cập nhật khi người dùng gõ phím)
        request.setPassengers(listPassengers);

        // E. Tạo mảng Dịch vụ mua thêm (bookingAncillaries) - Tạm thời gửi mảng rỗng vì chưa làm form chọn hành lý
        List<AncillaryRequest> ancillaries = new ArrayList<>();
        request.setBookingAncillaries(ancillaries);

        // F. HIỆU LỆNH GỌI API THÔNG QUA VIEWMODEL
        btnConfirmBooking.setEnabled(false); // Khóa nút bấm lại tránh khách bấm 2 lần
        btnConfirmBooking.setText("Processing...");

        viewModel.createBooking(request).observe(this, bookingResult -> {
            btnConfirmBooking.setEnabled(true);
            btnConfirmBooking.setText("Book Now");

            if (bookingResult != null) {
                // API TRẢ VỀ THÀNH CÔNG!
                Toast.makeText(this, "Đặt vé thành công! PNR: " + bookingResult.getPnrCode(), Toast.LENGTH_LONG).show();

                // === LÀM TIẾP Ở ĐÂY SAU ===
                // Chỗ này bạn sẽ dùng Intent để chuyển sang màn hình "Chi tiết vé / Thanh toán"
                // và truyền theo cái bookingResult.getId() để màn sau gọi hàm GET /bookings/{id} nhé!

            } else {
                Toast.makeText(this, "Đặt vé thất bại! Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}