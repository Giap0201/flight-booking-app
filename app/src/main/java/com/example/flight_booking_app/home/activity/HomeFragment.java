package com.example.flight_booking_app.home.activity;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.home.adapter.DestinationAdapter;
import com.example.flight_booking_app.home.adapter.TravelGuideAdapter;
import com.example.flight_booking_app.home.model.Destination;
import com.example.flight_booking_app.home.model.TravelGuide;
import com.example.flight_booking_app.search.activity.SearchResultActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvDeparture, tvArrival, tvDate, tvPassengers;
    private TextView tvReturnDate, tvDayCount;
    private ImageButton btnSwap;
    private com.google.android.material.button.MaterialButton btnSearch;
    private RecyclerView rvTravelGuides;
    private MaterialButtonToggleGroup toggleGroupTripType;

    private int adultCount = 1, childCount = 0, infantCount = 0, totalPassengers = 1;
    private boolean isRoundTrip = false;

    private androidx.viewpager2.widget.ViewPager2 vpDestinations;
    private com.google.android.material.tabs.TabLayout tabLayoutDots;
    private DestinationAdapter destinationAdapter;

    private android.os.Handler sliderHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable sliderRunnable;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvDeparture = view.findViewById(R.id.tvDeparture);
        tvArrival = view.findViewById(R.id.tvArrival);
        tvDate = view.findViewById(R.id.tvDate);
        tvReturnDate = view.findViewById(R.id.tvReturnDate);
        tvDayCount = view.findViewById(R.id.tvDayCount);
        tvPassengers = view.findViewById(R.id.tvPassengers);
        btnSwap = view.findViewById(R.id.btnSwap);
        btnSearch = view.findViewById(R.id.btnSearch);
        toggleGroupTripType = view.findViewById(R.id.toggleGroupTripType);
        rvTravelGuides = view.findViewById(R.id.rvTravelGuides);

        rvTravelGuides.setNestedScrollingEnabled(false);

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

        btnSwap.setOnClickListener(v -> {
            String temp = tvDeparture.getText().toString();
            tvDeparture.setText(tvArrival.getText().toString());
            tvArrival.setText(temp);
            btnSwap.animate().rotationBy(180f).setDuration(300).start();
        });

        tvDate.setOnClickListener(v -> showDatePicker(tvDate, "Chọn ngày bay đi"));
        tvReturnDate.setOnClickListener(v -> showDatePicker(tvReturnDate, "Chọn ngày bay về"));

        tvDeparture.setOnClickListener(v -> departureLauncher.launch(new Intent(getActivity(), SearchLocationActivity.class)));
        tvArrival.setOnClickListener(v -> arrivalLauncher.launch(new Intent(getActivity(), SearchLocationActivity.class)));

        tvPassengers.setOnClickListener(v -> showPassengerBottomSheet());

        // =========================================================
        // NÂNG CẤP: XỬ LÝ VALIDATE NGÀY THÁNG KHI BẤM NÚT TÌM KIẾM
        // =========================================================
        btnSearch.setOnClickListener(v -> {
            String departure = tvDeparture.getText().toString().trim().toUpperCase();
            String arrival = tvArrival.getText().toString().trim().toUpperCase();
            String date = tvDate.getText().toString().trim();
            String returnDate = isRoundTrip ? tvReturnDate.getText().toString().trim() : "";

            // 1. Check rỗng hoặc chưa chọn (đang hiện chữ mặc định)
            if (departure.isEmpty() || departure.equals("ĐIỂM ĐI") || arrival.isEmpty() || arrival.equals("ĐIỂM ĐẾN") || date.isEmpty() || date.equals("Ngày đi")) {
                Toast.makeText(requireContext(), "Vui lòng chọn đầy đủ Điểm đi, Điểm đến và Ngày đi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRoundTrip && (returnDate.isEmpty() || returnDate.equals("Ngày về"))) {
                Toast.makeText(requireContext(), "Vui lòng chọn ngày bay về", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Logic so sánh ngày tháng
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date parsedDateDi = sdf.parse(date);

                // Lấy ngày hiện tại, ép format rồi parse lại để loại bỏ giờ/phút/giây (tránh lỗi so sánh trong cùng 1 ngày)
                Date today = sdf.parse(sdf.format(new Date()));

                // Validate 1: Ngày đi < Ngày hiện tại
                if (parsedDateDi != null && parsedDateDi.before(today)) {
                    Toast.makeText(requireContext(), "Ngày khởi hành không được chọn trong quá khứ!", Toast.LENGTH_SHORT).show();
                    return; // Chặn không cho chạy tiếp
                }

                // Validate 2: Ngày về < Ngày đi
                if (isRoundTrip) {
                    Date parsedDateVe = sdf.parse(returnDate);
                    if (parsedDateVe != null && parsedDateVe.before(parsedDateDi)) {
                        Toast.makeText(requireContext(), "Ngày về không được trước ngày khởi hành!", Toast.LENGTH_SHORT).show();
                        return; // Chặn không cho chạy tiếp
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Định dạng ngày không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Vượt qua mọi rào cản -> Chuyển trang
            Intent intent = new Intent(getActivity(), SearchResultActivity.class);
            intent.putExtra("ORIGIN", departure);
            intent.putExtra("DESTINATION", arrival);
            intent.putExtra("DATE", date);
            intent.putExtra("PASSENGERS", totalPassengers);
            intent.putExtra("ADULT_COUNT", adultCount);
            intent.putExtra("CHILD_COUNT", childCount);
            intent.putExtra("INFANT_COUNT", infantCount);
            intent.putExtra("IS_ROUND_TRIP", isRoundTrip);
            intent.putExtra("RETURN_DATE", returnDate);
            startActivity(intent);
        });

        setupDestinationSlider(view);
        setupTravelGuides(view);

        return view;
    }

    private void showPassengerBottomSheet() {
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

    private void showDatePicker(TextView targetTextView, String title) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(title).setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            targetTextView.setText(sdf.format(new Date(selection)));
        });
        datePicker.show(getChildFragmentManager(), "DATE_PICKER");
    }

    private void setupDestinationSlider(View view) {
        vpDestinations = view.findViewById(R.id.vpDestinations);
        tabLayoutDots = view.findViewById(R.id.tabLayoutDots);

        List<Destination> list = new java.util.ArrayList<>();
        list.add(new Destination("1", "Đà Nẵng, Việt Nam", "Vé chỉ từ 599.000 đ", "https://tourism.danang.vn/wp-content/uploads/2023/02/tour-du-lich-da-nang-1.jpg"));
        list.add(new Destination("2", "Phú Quốc, Việt Nam", "Vé chỉ từ 799.000 đ", "https://thoibaotaichinhvietnam.vn/stores/news_dataimages/2024/102024/18/14/phu-quoc20241018144932.1152350.jpg"));
        list.add(new Destination("3", "Vịnh Hạ Long, Việt Nam", "Vé chỉ từ 899.000 đ", "https://nhandan.vn/special/30-nam-mot-chang-duong-di-san-Vinh-Ha-Long/assets/HLCklusX0n/things-to-do-in-ha-long-bay-banner-1-1920x1080.jpg"));
        list.add(new Destination("4", "Hà Nội, Việt Nam", "Vé chỉ từ 499.000 đ", "https://suckhoedoisong.qltns.mediacdn.vn/Images/thanhloan/2020/11/28/Nam-2030-du-lich-ha-noi-phan-dau-tro-thanh-nganh-kinh-te-mui-nhon-cua-thu-do-19.jpg"));
        list.add(new Destination("5", "Hồ Chí Minh, Việt Nam", "Vé chỉ từ 699.000 đ", "https://travel-bus-files.s3.ap-southeast-1.amazonaws.com/images/3601bd2d-4e5c-4a33-bce8-748e684046f3.jpeg"));
        list.add(new Destination("6", "Bali, Indonesia", "Vé chỉ từ 3.500.000 đ", "https://dulichdaiviet.vn/uploaded/anh-cam-nang-dl/cam-nang-dl-bali/7ngoidenlinhthiengnoitiengnhatbali1.jpg"));
        list.add(new Destination("7", "Bangkok, Thái Lan", "Vé chỉ từ 1.800.000 đ", "https://vietlandtravel.vn/upload/images/bangkok-ve-dem.jpg"));
        list.add(new Destination("8", "Singapore", "Vé chỉ từ 2.100.000 đ", "https://images.trvl-media.com/place/6047873/15d3ae30-ef33-406e-971f-9520c03f1089.jpg"));
        list.add(new Destination("9", "Kuala Lumpur, Malaysia", "Vé chỉ từ 1.500.000 đ", "https://res.klook.com/image/upload/fl_lossy.progressive,q_60/Mobile/City/o52yyykrizo0b4th1uuk.jpg"));

        destinationAdapter = new DestinationAdapter(list);
        vpDestinations.setAdapter(destinationAdapter);

        new com.google.android.material.tabs.TabLayoutMediator(tabLayoutDots, vpDestinations,
                (tab, position) -> {}
        ).attach();

        sliderRunnable = () -> {
            int currentItem = vpDestinations.getCurrentItem();
            int totalItems = destinationAdapter.getItemCount();
            if (currentItem < totalItems - 1) {
                vpDestinations.setCurrentItem(currentItem + 1);
            } else {
                vpDestinations.setCurrentItem(0);
            }
        };

        vpDestinations.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private void setupTravelGuides(View view) {
        rvTravelGuides = view.findViewById(R.id.rvTravelGuides);

        androidx.recyclerview.widget.LinearLayoutManager layoutManager =
                new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false);
        rvTravelGuides.setLayoutManager(layoutManager);

        List<TravelGuide> list = new java.util.ArrayList<>();
        list.add(new TravelGuide("1", "Cẩm nang du lịch Phú Quốc: Chơi gì, ăn gì, ở đâu?", "Kinh Nghiệm", "Cập nhật mới nhất", "https://cdn.media.dulich24.com.vn/diemden/ao-phu-quoc-3506/phu-quoc.jpg", "https://dulichkhampha24.com/kinh-nghiem-du-lich-phu-quoc.html"));
        list.add(new TravelGuide("2", "Bản đồ ẩm thực và cẩm nang khám phá thủ đô Hà Nội", "Điểm Đến", "Cập nhật mới nhất", "https://banhtombaloc.vn/medias/2024/05/5.jpg", "https://vietnammedia.com.vn/ban-do-food-tour-ha-noi-cam-nang-du-lich-am-thuc-ha-noi"));
        list.add(new TravelGuide("3", "Kinh nghiệm du lịch tự túc Bangkok, Thái Lan", "Quốc Tế", "Cập nhật mới nhất", "https://file.hstatic.net/200000561069/article/bangkok-tl_3619350a59554298be1b892c3592c023.jpg", "https://phuotvivu.com/blog/kinh-nghiem-du-lich/thai-lan/bangkok/"));

        TravelGuideAdapter guideAdapter = new TravelGuideAdapter(list);
        rvTravelGuides.setAdapter(guideAdapter);
    }
}