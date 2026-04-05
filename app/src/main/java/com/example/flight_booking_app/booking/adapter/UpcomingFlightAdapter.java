package com.example.flight_booking_app.booking.adapter;

import android.content.Intent; // ĐÃ THÊM IMPORT
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.activity.FlightDetailActivity; // ĐÃ THÊM IMPORT
import com.example.flight_booking_app.booking.model.BookingSummary;
import java.util.List;

public class UpcomingFlightAdapter extends RecyclerView.Adapter<UpcomingFlightAdapter.ViewHolder> {
    private List<BookingSummary> flights;

    public UpcomingFlightAdapter(List<BookingSummary> flights) {
        this.flights = flights;
    }

    public void setFlights(List<BookingSummary> flights) {
        this.flights = flights;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_flight_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingSummary flight = flights.get(position);

        holder.tvOriginCode.setText(flight.getOrigin());
        holder.tvDestCode.setText(flight.getDestination());

        // 1. FORMAT LẠI NGÀY BAY (Từ "2026-09-12T10..." -> "12-09-2026")
        if (flight.getDepartureTime() != null && flight.getDepartureTime().length() >= 10) {
            try {
                // Cắt lấy phần yyyy-MM-dd
                String datePart = flight.getDepartureTime().substring(0, 10);
                String[] parts = datePart.split("-");
                String niceDate = parts[2] + "-" + parts[1] + "-" + parts[0]; // dd-MM-yyyy
                holder.tvDate.setText(niceDate);
            } catch (Exception e) {
                holder.tvDate.setText(flight.getDepartureTime());
            }
        }

        // 2. FORMAT LẠI HẠNG VÉ (Viết hoa chữ cái đầu mỗi từ: PREMIUM_ECONOMY -> Premium Economy)
        if (flight.getFlightClass() != null && !flight.getFlightClass().isEmpty()) {
            String rawClass = flight.getFlightClass().replace("_", " ").toLowerCase();
            StringBuilder niceClass = new StringBuilder();

            String[] words = rawClass.split(" ");
            for (String word : words) {
                if (!word.isEmpty()) {
                    niceClass.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1))
                            .append(" ");
                }
            }
            holder.tvClass.setText(niceClass.toString().trim());
        } else {
            holder.tvClass.setText("Chưa xác định");
        }

        // Tạm thời hardcode số lượng hành khách do API chưa trả về field này trong danh sách ngắn
        holder.tvPassengerCount.setText("1 Person");

        // =======================================================
        // 3. SỰ KIỆN CLICK: CHUYỂN SANG TRANG CHI TIẾT VÉ
        // =======================================================
        holder.itemView.setOnClickListener(v -> {
            if (flight.getId() != null) {
                Intent intent = new Intent(v.getContext(), FlightDetailActivity.class);
                intent.putExtra("BOOKING_ID", flight.getId().toString());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flights == null ? 0 : flights.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOriginCode, tvDestCode, tvDate, tvClass, tvPassengerCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOriginCode = itemView.findViewById(R.id.tvOriginCode);
            tvDestCode = itemView.findViewById(R.id.tvDestCode);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvPassengerCount = itemView.findViewById(R.id.tvPassengerCount);
        }
    }
}