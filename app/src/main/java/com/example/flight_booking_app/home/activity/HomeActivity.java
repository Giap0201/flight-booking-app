package com.example.flight_booking_app.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.home.model.AirportTranslation;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.example.flight_booking_app.home.adapter.LocationAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    // --- KHAI BÁO BIẾN ---
    private TextView tvDeparture, tvArrival, tvDate, tvPassengers;
    private TextView tvReturnDate, tvDayCount; // Thêm biến cho Khứ hồi
    private ImageButton btnSwap;
    private com.google.android.material.button.MaterialButton btnSearch;
    private MaterialButtonToggleGroup toggleGroupTripType;

    // Biến lưu trữ số lượng hành khách
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 0;
    private int totalPassengers = 1;

    // Biến trạng thái để biết đang chọn One-way hay Round-trip
    private boolean isRoundTrip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Ánh xạ View
        tvDeparture = findViewById(R.id.tvDeparture);
        tvArrival = findViewById(R.id.tvArrival);
        tvDate = findViewById(R.id.tvDate);
        tvReturnDate = findViewById(R.id.tvReturnDate);
        tvDayCount = findViewById(R.id.tvDayCount);
        tvPassengers = findViewById(R.id.tvPassengers);
        btnSwap = findViewById(R.id.btnSwap);
        btnSearch = findViewById(R.id.btnSearch);
        toggleGroupTripType = findViewById(R.id.toggleGroupTripType);

        // 2. Xử lý Toggle (Một chiều / Khứ hồi)
        toggleGroupTripType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnRoundTrip) {
                    isRoundTrip = true;
                    tvReturnDate.setVisibility(View.VISIBLE);
                    tvDayCount.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.btnOneWay) {
                    isRoundTrip = false;
                    tvReturnDate.setVisibility(View.GONE);
                    tvDayCount.setVisibility(View.GONE);
                }
            }
        });

        // 3. Nút Đảo chiều (Swap)
        btnSwap.setOnClickListener(v -> {
            String temp = tvDeparture.getText().toString();
            tvDeparture.setText(tvArrival.getText().toString());
            tvArrival.setText(temp);
            btnSwap.animate().rotationBy(180f).setDuration(300).start();
        });

        // 4. Chọn Ngày Bay Đi
        tvDate.setOnClickListener(v -> showDatePicker(tvDate, "Chọn ngày bay đi"));

        // 5. Chọn Ngày Bay Về (Chỉ bấm được khi hiển thị)
        tvReturnDate.setOnClickListener(v -> showDatePicker(tvReturnDate, "Chọn ngày bay về"));

        // 6. Các nút "Mồi" chờ màn hình phụ
        tvDeparture.setOnClickListener(v -> departureLauncher.launch(new Intent(HomeActivity.this, SearchLocationActivity.class)));
        tvArrival.setOnClickListener(v -> arrivalLauncher.launch(new Intent(HomeActivity.this, SearchLocationActivity.class)));

        // 7. Mở Bottom Sheet Hành Khách
        tvPassengers.setOnClickListener(v -> showPassengerBottomSheet());

        // 8. Nút Tìm Kiếm (Gửi dữ liệu sang SearchResultActivity)
        btnSearch.setOnClickListener(v -> {
            String departure = tvDeparture.getText().toString().trim().toUpperCase();
            String arrival = tvArrival.getText().toString().trim().toUpperCase();
            String date = tvDate.getText().toString().trim();
            // Lấy thêm ngày về nếu là khứ hồi
            String returnDate = isRoundTrip ? tvReturnDate.getText().toString().trim() : "";

            // Validate cơ bản
            if (departure.isEmpty() || arrival.isEmpty() || date.equals("Mon, May 7")) { // Đang dùng text mặc định
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isRoundTrip && returnDate.equals("Tue, 10 May")) {
                Toast.makeText(this, "Vui lòng chọn ngày về", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi dữ liệu
            Intent intent = new Intent(HomeActivity.this, SearchResultActivity.class);
            intent.putExtra("ORIGIN", departure);
            intent.putExtra("DESTINATION", arrival);
            intent.putExtra("DATE", date);

            // ⚡ THAY ĐỔI Ở ĐÂY: Gửi riêng biệt từng loại hành khách ⚡
            intent.putExtra("ADULT_COUNT", adultCount);
            intent.putExtra("CHILD_COUNT", childCount);
            intent.putExtra("INFANT_COUNT", infantCount);

            // (Bạn vẫn có thể gửi thêm biến PASSENGERS nếu API tìm kiếm chuyến bay cũ đang cần nó)
            intent.putExtra("PASSENGERS", totalPassengers);

            startActivity(intent);
        });
    }

    // --- HÀM HỖ TRỢ HIỂN THỊ BOTTOM SHEET HÀNH KHÁCH ---
    private void showPassengerBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_passengers);

        // Ánh xạ
        TextView txtAdult = dialog.findViewById(R.id.tvAdultCount);
        TextView txtChild = dialog.findViewById(R.id.tvChildCount);
        TextView txtInfant = dialog.findViewById(R.id.tvInfantCount);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmPassengers);
        ImageButton btnClose = dialog.findViewById(R.id.btnCloseSheet);

        // Gán giá trị hiện tại (dùng mảng 1 phần tử để sửa được trong lambda)
        final int[] tempA = {adultCount};
        final int[] tempC = {childCount};
        final int[] tempI = {infantCount};

        if(txtAdult != null) txtAdult.setText(String.valueOf(tempA[0]));
        if(txtChild != null) txtChild.setText(String.valueOf(tempC[0]));
        if(txtInfant != null) txtInfant.setText(String.valueOf(tempI[0]));

        // Đóng Sheet
        if(btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        // Logic tăng giảm Adult (Tối đa 9, tối thiểu 1)
        dialog.findViewById(R.id.btnPlusAdult).setOnClickListener(v -> {
            if (tempA[0] < 9) { tempA[0]++; txtAdult.setText(String.valueOf(tempA[0])); }
        });
        dialog.findViewById(R.id.btnMinusAdult).setOnClickListener(v -> {
            if (tempA[0] > 1) { tempA[0]--; txtAdult.setText(String.valueOf(tempA[0])); }
        });

        // Logic tăng giảm Child (Tối đa 9, tối thiểu 0)
        dialog.findViewById(R.id.btnPlusChild).setOnClickListener(v -> {
            if (tempC[0] < 9) { tempC[0]++; txtChild.setText(String.valueOf(tempC[0])); }
        });
        dialog.findViewById(R.id.btnMinusChild).setOnClickListener(v -> {
            if (tempC[0] > 0) { tempC[0]--; txtChild.setText(String.valueOf(tempC[0])); }
        });

        // Logic tăng giảm Infant (Tối đa bằng số Adult, tối thiểu 0) - Quy định an toàn bay
        dialog.findViewById(R.id.btnPlusInfant).setOnClickListener(v -> {
            if (tempI[0] < tempA[0]) { tempI[0]++; txtInfant.setText(String.valueOf(tempI[0])); }
            else { Toast.makeText(this, "Số em bé không được vượt quá người lớn", Toast.LENGTH_SHORT).show(); }
        });
        dialog.findViewById(R.id.btnMinusInfant).setOnClickListener(v -> {
            if (tempI[0] > 0) { tempI[0]--; txtInfant.setText(String.valueOf(tempI[0])); }
        });

        // Xác nhận
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                adultCount = tempA[0];
                childCount = tempC[0];
                infantCount = tempI[0];
                totalPassengers = adultCount + childCount + infantCount;

                // Hiển thị tóm tắt đẹp mắt (VD: "2 Adults, 1 Child")
                StringBuilder summary = new StringBuilder();
                summary.append(adultCount).append(adultCount > 1 ? " Adults" : " Adult");
                if (childCount > 0) summary.append(", ").append(childCount).append(childCount > 1 ? " Children" : " Child");
                if (infantCount > 0) summary.append(", ").append(infantCount).append(infantCount > 1 ? " Infants" : " Infant");

                tvPassengers.setText(summary.toString());
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    // --- HÀM HỖ TRỢ MỞ LỊCH ---
    private void showDatePicker(TextView targetTextView, String title) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            String formattedDate = sdf.format(new Date(selection));
            targetTextView.setText(formattedDate);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    // Bưu tá nhận kết quả cho Điểm Đi
    private final ActivityResultLauncher<Intent> departureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String code = result.getData().getStringExtra("SELECTED_CODE");
                    tvDeparture.setText(code);
                }
            }
    );

    // Bưu tá nhận kết quả cho Điểm Đến
    private final ActivityResultLauncher<Intent> arrivalLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String code = result.getData().getStringExtra("SELECTED_CODE");
                    tvArrival.setText(code);
                }
            }
    );
}