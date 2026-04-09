package com.example.flight_booking_app.search.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.search.model.SortOption;
import com.example.flight_booking_app.search.viewmodel.SearchResultViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortBottomSheet extends BottomSheetDialogFragment {

    private SearchResultViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_sort_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Kết nối với ViewModel chung của Activity
        viewModel = new ViewModelProvider(requireActivity()).get(SearchResultViewModel.class);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupSort);

        // Xử lý sự kiện chọn
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPriceLowest) {
                // Sắp xếp theo giá (Server-side)
                viewModel.updateSort(SortOption.PRICE_LOWEST);
            } else if (checkedId == R.id.rbDepartureEarliest) {
                // Sắp xếp theo giờ khởi hành (Server-side)
                viewModel.updateSort(SortOption.DEPARTURE_EARLIEST);
            } else if (checkedId == R.id.rbDurationShortest) {
                // Sắp xếp theo thời lượng (Client-side)
                viewModel.updateSort(SortOption.DURATION_SHORTEST);
            }

            // Tự động đóng sau khi chọn
            dismiss();
        });
    }
}