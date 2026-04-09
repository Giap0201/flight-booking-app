package com.example.flight_booking_app.search.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.search.model.CheapestDate;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateStripAdapter extends RecyclerView.Adapter<DateStripAdapter.ViewHolder> {

    private List<CheapestDate> dateList = new ArrayList<>();
    private int selectedPosition = -1; // Vị trí ngày đang được chọn
    private final OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(CheapestDate item);
    }

    public DateStripAdapter(OnDateClickListener listener) {
        this.listener = listener;
    }

    public void setDates(List<CheapestDate> dates, String currentSearchDate) {
        this.dateList = dates;
        // Tự động highlight ngày đang tìm kiếm
        for (int i = 0; i < dates.size(); i++) {
            if (dates.get(i).getDate().equals(currentSearchDate)) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_strip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CheapestDate item = dateList.get(position);

        // Format ngày: Từ "2026-05-07" sang "Mon" và "7 May"
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = parser.parse(item.getDate());

            // Ép buộc dùng Locale Việt Nam để hiển thị "Th 2" và "thg 5"
            Locale vnLocale = new Locale("vi", "VN");
            holder.tvDayOfWeek.setText(new SimpleDateFormat("EEE", vnLocale).format(date));
            holder.tvDateMonth.setText(new SimpleDateFormat("d MMM", vnLocale).format(date));
        } catch (Exception e) {
            holder.tvDayOfWeek.setText("---");
            holder.tvDateMonth.setText(item.getDate());
        }

        // Hiển thị giá
        holder.tvDatePrice.setText(String.format("%,.0f", item.getMinPrice()));

        // Hiệu ứng khi chọn (giống màu tím trong Figma)
        if (selectedPosition == position) {
            holder.cardDate.setStrokeColor(Color.parseColor("#6200EE"));
            holder.cardDate.setCardBackgroundColor(Color.parseColor("#F3E5F5"));
        } else {
            holder.cardDate.setStrokeColor(Color.parseColor("#E0E0E0"));
            holder.cardDate.setCardBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onDateClick(item);
        });
    }

    @Override
    public int getItemCount() { return dateList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayOfWeek, tvDateMonth, tvDatePrice;
        MaterialCardView cardDate;

        ViewHolder(View view) {
            super(view);
            tvDayOfWeek = view.findViewById(R.id.tvDayOfWeek);
            tvDateMonth = view.findViewById(R.id.tvDateMonth);
            tvDatePrice = view.findViewById(R.id.tvDatePrice);
            cardDate = view.findViewById(R.id.cardDate);
        }
    }
}