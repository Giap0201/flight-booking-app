package com.example.flight_booking_app.search.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.search.adapter.AirlineFilterAdapter;
import com.example.flight_booking_app.search.model.FilterCriteria;
import com.example.flight_booking_app.search.viewmodel.SearchResultViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    private SearchResultViewModel viewModel;
    private FilterCriteria tempCriteria; // Biến tạm để lưu thay đổi trước khi nhấn "Hiển thị"
    private FrameLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Kết nối với ViewModel của Activity
        viewModel = new ViewModelProvider(requireActivity()).get(SearchResultViewModel.class);

        // Tạo bản sao của filter hiện tại để chỉnh sửa tạm thời
        tempCriteria = viewModel.getCurrentFilterValue();

        // 2. Ánh xạ View
        container = view.findViewById(R.id.filterContentContainer);
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutFilter);

        // 3. Xử lý chuyển Tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Chỉ còn 2 case: 0 (Hãng bay) và 1 (Thời gian)
                switch (tab.getPosition()) {
                    case 0: showAirlinesFilter(); break;
                    case 1: showTimesFilter(); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Mặc định hiển thị tab đầu tiên
        showAirlinesFilter();

        // 4. Nút "Hiển thị chuyến bay" -> Áp dụng bộ lọc
        view.findViewById(R.id.btnShowFlights).setOnClickListener(v -> {
            // 1. Thu thập dữ liệu: tempCriteria đã được cập nhật liên tục
            // thông qua Listener của các tab (Airlines, Times, Cabins).

            // 2. Cập nhật ViewModel: Gọi hàm updateFilter.
            // Hành động này sẽ kích hoạt currentFilter.setValue()
            viewModel.updateFilter(tempCriteria);

            // Đóng BottomSheet
            dismiss();
        });

        // 5. Nút "Thiết lập lại" -> Reset về mặc định
        view.findViewById(R.id.btnReset).setOnClickListener(v -> {
            tempCriteria = new FilterCriteria();
            // Cập nhật lại UI của tab hiện tại
            tabLayout.selectTab(tabLayout.getTabAt(0));
            showAirlinesFilter();
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }

    private void showAirlinesFilter() {
        container.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.layout_filter_airlines, container, false);

        RecyclerView rv = view.findViewById(R.id.rvFilterAirlines);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo Adapter với Set đã chọn từ tempCriteria
        AirlineFilterAdapter adapter = new AirlineFilterAdapter(tempCriteria.getSelectedAirlines());
        rv.setAdapter(adapter);

        // --- LOGIC TÌM KIẾM ---
        TextInputEditText etSearch = view.findViewById(R.id.etSearchAirline);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi hàm filter đã viết trong Adapter
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Quan sát dữ liệu hãng bay (Master Data)
        viewModel.getAirlines().observe(getViewLifecycleOwner(), airlines -> {
            if (airlines != null && !airlines.isEmpty()) {
                adapter.setAirlines(airlines);
                android.util.Log.d("Filter", "Đã nhận " + airlines.size() + " hãng bay");
            }
        });

        viewModel.loadAirlinesForFilter();
        container.addView(view);
    }

    private void showTimesFilter() {
        container.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.layout_filter_times, container, false);

        RangeSlider slider = view.findViewById(R.id.sliderDepartureTime);
        TextView tvValue = view.findViewById(R.id.tvDepartureValue);

        // Lắng nghe sự thay đổi của Slider
        slider.addOnChangeListener((slider1, value, fromUser) -> {
            List<Float> values = slider1.getValues();
            float start = values.get(0);
            float end = values.get(1);

            // Cập nhật text hiển thị
            tvValue.setText(String.format("%s - %s", formatTime(start), formatTime(end)));

            // Cập nhật vào tempCriteria để lọc
            tempCriteria.setDepartureTimeRange(start, end);
        });

        container.addView(view);
    }

    // Hàm format số thực sang giờ:phút (VD: 9.5f -> "09:30")
    private String formatTime(float value) {
        int hour = (int) value;
        int minute = (value % 1 == 0) ? 0 : 30;
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

}