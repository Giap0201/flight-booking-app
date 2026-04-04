package com.example.flight_booking_app.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.home.model.Flight;
import java.util.ArrayList;
import java.util.List;

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {

    private List<Flight> flightList = new ArrayList<>();

    // Hàm này để Activity truyền data mới vào Adapter
    public void setFlights(List<Flight> flights) {
        this.flightList = flights;
        notifyDataSetChanged(); // Yêu cầu vẽ lại danh sách
    }

    @NonNull
    @Override
    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight, parent, false);
        return new FlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
        Flight flight = flightList.get(position);

        // 1. Gắn tên hãng bay + Số hiệu chuyến bay
        String airlineInfo = flight.getAirlineName() + " (" + flight.getFlightNumber() + ")";
        holder.tvAirline.setText(airlineInfo);

        // 2. Format Thời gian: Cắt bỏ ngày tháng, chỉ lấy HH:mm
        // Ví dụ: "2026-04-01T17:17:54.979Z" -> Lấy phần từ chữ T đến dấu : thứ 2
        String depTime = formatTime(flight.getDepartureTime());
        String arrTime = formatTime(flight.getArrivalTime());
        holder.tvTime.setText(depTime + " - " + arrTime);

        // 3. Lấy Giá vé từ mảng Classes
        if (flight.getClasses() != null && !flight.getClasses().isEmpty()) {
            // Lấy giá của hạng vé đầu tiên trong danh sách
            double price = flight.getClasses().get(0).getBasePrice();

            // Format giá tiền có dấu phẩy (VD: 1,500,000)
            String formattedPrice = String.format("%,.0f VND", price);
            holder.tvPrice.setText(formattedPrice);
        } else {
            holder.tvPrice.setText("Hết vé");
        }
    }

    // Hàm hỗ trợ format chuỗi thời gian ISO 8601 sang Giờ:Phút
    // Thêm hàm này vào bên trong class FlightAdapter (bên ngoài các hàm khác)
    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.isEmpty()) return "--:--";
        try {
            // Cắt chuỗi đơn giản: 2026-04-01T17:17:54.979Z -> Lấy "17:17"
            int tIndex = isoTime.indexOf('T');
            if (tIndex != -1 && isoTime.length() > tIndex + 6) {
                return isoTime.substring(tIndex + 1, tIndex + 6);
            }
            return isoTime;
        } catch (Exception e) {
            return "--:--";
        }
    }

    @Override
    public int getItemCount() {
        return flightList != null ? flightList.size() : 0;
    }

    // Lớp "giữ chỗ" cho các view trong item_flight.xml
    static class FlightViewHolder extends RecyclerView.ViewHolder {
        TextView tvAirline, tvTime, tvPrice;

        public FlightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAirline = itemView.findViewById(R.id.tvAirline);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}