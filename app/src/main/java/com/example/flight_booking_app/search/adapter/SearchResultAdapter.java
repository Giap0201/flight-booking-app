package com.example.flight_booking_app.search.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.search.model.Flight;
import com.example.flight_booking_app.search.model.FlightClass;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private List<Flight> flightList = new ArrayList<>();
    private final OnItemClickListener listener;

    // Interface để xử lý sự kiện click khi chọn chuyến bay
    public interface OnItemClickListener {
        void onItemClick(Flight flight);
    }

    public SearchResultAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFlights(List<Flight> flights) {
        this.flightList = flights;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flight flight = flightList.get(position);

        // 1. Hiển thị tên hãng bay và số hiệu
        holder.tvAirlineName.setText(flight.getAirlineName());
        holder.tvFlightNumber.setText(flight.getFlightNumber());

        // 2. Format và hiển thị thời gian đi/đến
        holder.tvDepTime.setText(formatTime(flight.getDepartureTime()));
        holder.tvArrTime.setText(formatTime(flight.getArrivalTime()));

        // 3. Xử lý hiển thị giá vé thấp nhất
        if (flight.getClasses() != null && !flight.getClasses().isEmpty()) {
            double minPrice = flight.getClasses().get(0).getBasePrice();

            // Duyệt danh sách để chắc chắn lấy được giá thấp nhất
            for (FlightClass fc : flight.getClasses()) {
                if (fc.getBasePrice() < minPrice) {
                    minPrice = fc.getBasePrice();
                }
            }
            holder.tvLowestPrice.setText(String.format("%,.0f VND", minPrice));
        } else {
            holder.tvLowestPrice.setText("Hết vé");
        }

        // 4. Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(flight));
    }

    /**
     * Hàm hỗ trợ cắt chuỗi thời gian ISO-8601 (VD: 2026-05-07T16:55:00)
     * để lấy định dạng HH:mm .
     */
    private String formatTime(String isoTime) {
        if (isoTime == null || !isoTime.contains("T")) return "--:--";
        try {
            int tIndex = isoTime.indexOf('T');
            return isoTime.substring(tIndex + 1, tIndex + 6);
        } catch (Exception e) {
            return "--:--";
        }
    }

    @Override
    public int getItemCount() {
        return flightList.size();
    }

    // ViewHolder ánh xạ các View từ item_flight.xml
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAirlineName, tvFlightNumber, tvDepTime, tvArrTime, tvLowestPrice;

        ViewHolder(View view) {
            super(view);
            tvAirlineName = view.findViewById(R.id.tvAirlineName);
            tvFlightNumber = view.findViewById(R.id.tvFlightNumber);
            tvDepTime = view.findViewById(R.id.tvDepTime);
            tvArrTime = view.findViewById(R.id.tvArrTime);
            tvLowestPrice = view.findViewById(R.id.tvLowestPrice);
        }
    }
}