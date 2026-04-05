package com.example.flight_booking_app.home.activity; // Package của bạn

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.flight_booking_app.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// LƯU Ý: extends Fragment thay vì AppCompatActivity
public class HomeFragment extends Fragment {

    // --- KHAI BÁO BIẾN (Giữ nguyên) ---
    private TextView tvDeparture, tvArrival, tvDate, tvPassengers;
    private TextView tvReturnDate, tvDayCount;
    private ImageButton btnSwap;
    private com.google.android.material.button.MaterialButton btnSearch;
    private MaterialButtonToggleGroup toggleGroupTripType;

    private int adultCount = 1, childCount = 0, infantCount = 0, totalPassengers = 1;
    private boolean isRoundTrip = false;

    // --- BƯU TÁ NHẬN KẾT QUẢ TỪ MÀN HÌNH CHỌN SÂN BAY ---
    private final ActivityResultLauncher<Intent> departureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    tvDeparture.setText(result.getData().getStringExtra("SELECTED_CODE"));
                }
            }
    );

    private final ActivityResultLauncher<Intent> arrivalLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    tvArrival.setText(result.getData().getStringExtra("SELECTED_CODE"));
                }
            }
    );

    // BẮT BUỘC ĐỐI VỚI FRAGMENT: Thay thế onCreate bằng onCreateView
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện fragment_home
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Ánh xạ View (Chú ý: phải gọi view.findViewById thay vì findViewById)
        tvDeparture = view.findViewById(R.id.tvDeparture);
        tvArrival = view.findViewById(R.id.tvArrival);
        tvDate = view.findViewById(R.id.tvDate);
        tvReturnDate = view.findViewById(R.id.tvReturnDate);
        tvDayCount = view.findViewById(R.id.tvDayCount);
        tvPassengers = view.findViewById(R.id.tvPassengers);
        btnSwap = view.findViewById(R.id.btnSwap);
        btnSearch = view.findViewById(R.id.btnSearch);
        toggleGroupTripType = view.findViewById(R.id.toggleGroupTripType);

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

        // 4. Chọn Ngày Bay
        tvDate.setOnClickListener(v -> showDatePicker(tvDate, "Chọn ngày bay đi"));
        tvReturnDate.setOnClickListener(v -> showDatePicker(tvReturnDate, "Chọn ngày bay về"));

        // 5. Chọn Sân Bay (Gọi Activity SearchLocationActivity)
        // LƯU Ý: Phải dùng getActivity() thay vì HomeActivity.this
        tvDeparture.setOnClickListener(v -> departureLauncher.launch(new Intent(getActivity(), SearchLocationActivity.class)));
        tvArrival.setOnClickListener(v -> arrivalLauncher.launch(new Intent(getActivity(), SearchLocationActivity.class)));

        // 6. Hành Khách
        tvPassengers.setOnClickListener(v -> showPassengerBottomSheet());

        // 7. Nút Tìm Kiếm
        btnSearch.setOnClickListener(v -> {
            String departure = tvDeparture.getText().toString().trim().toUpperCase();
            String arrival = tvArrival.getText().toString().trim().toUpperCase();
            String date = tvDate.getText().toString().trim();
            String returnDate = isRoundTrip ? tvReturnDate.getText().toString().trim() : "";

            if (departure.isEmpty() || arrival.isEmpty() || date.equals("Mon, May 7")) {
                // LƯU Ý: Dùng requireContext() thay vì this cho Toast
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getActivity(), SearchResultActivity.class);
            intent.putExtra("ORIGIN", departure);
            intent.putExtra("DESTINATION", arrival);
            intent.putExtra("DATE", date);
            intent.putExtra("PASSENGERS", totalPassengers);
            startActivity(intent);
        });

        return view; // Trả về view cho Fragment
    }

    // --- HÀM HỖ TRỢ BOTTOM SHEET HÀNH KHÁCH ---
    private void showPassengerBottomSheet() {
        // LƯU Ý: Dùng requireContext() thay vì this
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(R.layout.bottom_sheet_passengers);

        TextView txtAdult = dialog.findViewById(R.id.tvAdultCount);
        TextView txtChild = dialog.findViewById(R.id.tvChildCount);
        TextView txtInfant = dialog.findViewById(R.id.tvInfantCount);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmPassengers);

        final int[] tempA = {adultCount}; final int[] tempC = {childCount}; final int[] tempI = {infantCount};
        if(txtAdult != null) txtAdult.setText(String.valueOf(tempA[0]));
        if(txtChild != null) txtChild.setText(String.valueOf(tempC[0]));
        if(txtInfant != null) txtInfant.setText(String.valueOf(tempI[0]));

        dialog.findViewById(R.id.btnPlusAdult).setOnClickListener(v -> { if (tempA[0] < 9) { tempA[0]++; txtAdult.setText(String.valueOf(tempA[0])); }});
        dialog.findViewById(R.id.btnMinusAdult).setOnClickListener(v -> { if (tempA[0] > 1) { tempA[0]--; txtAdult.setText(String.valueOf(tempA[0])); }});
        dialog.findViewById(R.id.btnPlusChild).setOnClickListener(v -> { if (tempC[0] < 9) { tempC[0]++; txtChild.setText(String.valueOf(tempC[0])); }});
        dialog.findViewById(R.id.btnMinusChild).setOnClickListener(v -> { if (tempC[0] > 0) { tempC[0]--; txtChild.setText(String.valueOf(tempC[0])); }});
        dialog.findViewById(R.id.btnPlusInfant).setOnClickListener(v -> {
            if (tempI[0] < tempA[0]) { tempI[0]++; txtInfant.setText(String.valueOf(tempI[0])); }
            else { Toast.makeText(requireContext(), "Số em bé không vượt quá người lớn", Toast.LENGTH_SHORT).show(); }
        });
        dialog.findViewById(R.id.btnMinusInfant).setOnClickListener(v -> { if (tempI[0] > 0) { tempI[0]--; txtInfant.setText(String.valueOf(tempI[0])); }});

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                adultCount = tempA[0]; childCount = tempC[0]; infantCount = tempI[0];
                totalPassengers = adultCount + childCount + infantCount;
                StringBuilder summary = new StringBuilder();
                summary.append(adultCount).append(adultCount > 1 ? " Người lớn" : " Người lớn");
                if (childCount > 0) summary.append(", ").append(childCount).append(childCount > 1 ? " Trẻ em" : " Trẻ em");
                if (infantCount > 0) summary.append(", ").append(infantCount).append(infantCount > 1 ? " Em bé" : " Em bé");
                tvPassengers.setText(summary.toString());
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    // --- HÀM HỖ TRỢ LỊCH ---
    private void showDatePicker(TextView targetTextView, String title) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(title).setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            targetTextView.setText(sdf.format(new Date(selection)));
        });
        // LƯU Ý: Fragment dùng getChildFragmentManager() thay vì getSupportFragmentManager()
        datePicker.show(getChildFragmentManager(), "DATE_PICKER");
    }
}