package com.example.flight_booking_app.booking.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.model.PassengerRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PassengerAdapter extends RecyclerView.Adapter<PassengerAdapter.PassengerViewHolder> {

    private Context context;
    private List<PassengerRequest> listPassengers;

    public PassengerAdapter(Context context, List<PassengerRequest> listPassengers) {
        this.context = context;
        this.listPassengers = listPassengers;
    }

    @NonNull
    @Override
    public PassengerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_passenger_form, parent, false);
        return new PassengerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerViewHolder holder, int position) {
        PassengerRequest passenger = listPassengers.get(position);

        // ==============================================================================
        // 1. GỠ BỎ SỰ KIỆN LẮNG NGHE CŨ TRÁNH LỖI KHI CUỘN
        // ==============================================================================
        if (holder.firstNameWatcher != null) holder.edtFirstName.removeTextChangedListener(holder.firstNameWatcher);
        if (holder.lastNameWatcher != null) holder.edtLastName.removeTextChangedListener(holder.lastNameWatcher);
        holder.rgGender.setOnCheckedChangeListener(null);

        // ==============================================================================
        // 2. ĐỔ DỮ LIỆU TỪ MÔ HÌNH VÀO GIAO DIỆN
        // ==============================================================================
        String typeVN = passenger.getType().equals("ADULT") ? "Người lớn" :
                (passenger.getType().equals("CHILD") ? "Trẻ em" : "Em bé");
        holder.tvPassengerTitle.setText("Hành khách " + (position + 1) + " (" + typeVN + ")");

        holder.edtFirstName.setText(passenger.getFirstName() != null ? passenger.getFirstName() : "");
        holder.edtLastName.setText(passenger.getLastName() != null ? passenger.getLastName() : "");
        holder.edtDateOfBirth.setText(passenger.getDateOfBirth() != null ? passenger.getDateOfBirth() : "");

        if ("FEMALE".equals(passenger.getGender())) {
            holder.rgGender.check(R.id.rbFemale);
        } else {
            holder.rgGender.check(R.id.rbMale);
        }

        // ==============================================================================
        // 3. GẮN LẠI SỰ KIỆN LẮNG NGHE MỚI
        // ==============================================================================

        // Lắng nghe Tên
        holder.firstNameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                passenger.setFirstName(s.toString().trim());
            }
        };
        holder.edtFirstName.addTextChangedListener(holder.firstNameWatcher);

        // Lắng nghe Họ
        holder.lastNameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                passenger.setLastName(s.toString().trim());
            }
        };
        holder.edtLastName.addTextChangedListener(holder.lastNameWatcher);

        // Giới tính
        holder.rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMale) {
                passenger.setGender("MALE");
            } else if (checkedId == R.id.rbFemale) {
                passenger.setGender("FEMALE");
            }
        });

        // ==============================================================================
        // TÍNH NĂNG MỚI: BẬT LỊCH KHI NHẤN VÀO Ô NGÀY SINH
        // ==============================================================================
        holder.edtDateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format ngày tháng năm thành chuẩn DD/MM/YYYY
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);

                        // Hiển thị lên EditText
                        holder.edtDateOfBirth.setText(formattedDate);

                        // Lưu thẳng vào Model luôn
                        passenger.setDateOfBirth(formattedDate);
                    },
                    year, month, day);

            // Chặn không cho chọn ngày sinh ở tương lai
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return listPassengers != null ? listPassengers.size() : 0;
    }

    public static class PassengerViewHolder extends RecyclerView.ViewHolder {
        TextView tvPassengerTitle;
        TextInputEditText edtFirstName;
        TextInputEditText edtLastName;
        TextInputEditText edtDateOfBirth;
        RadioGroup rgGender;

        TextWatcher firstNameWatcher;
        TextWatcher lastNameWatcher;

        public PassengerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPassengerTitle = itemView.findViewById(R.id.tvPassengerTitle);
            edtFirstName = itemView.findViewById(R.id.edtFirstName);
            edtLastName = itemView.findViewById(R.id.edtLastName);
            edtDateOfBirth = itemView.findViewById(R.id.edtDateOfBirth);
            rgGender = itemView.findViewById(R.id.rgGender);
        }
    }
}