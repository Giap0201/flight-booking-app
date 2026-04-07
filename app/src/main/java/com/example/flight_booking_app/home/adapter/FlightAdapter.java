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

    // --- BƯỚC 1.1: TẠO INTERFACE LẮNG NGHE SỰ KIỆN CLICK ---
    private OnFlightClickListener listener;

    public interface OnFlightClickListener {
        void onFlightClick(Flight selectedFlight);
    }

    public void setOnFlightClickListener(OnFlightClickListener listener) {
        this.listener = listener;
    }
    // -------------------------------------------------------

    public void setFlights(List<Flight> flights) {
        this.flightList = flights;
        notifyDataSetChanged();
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

        String airlineInfo = flight.getAirlineName() + " (" + flight.getFlightNumber() + ")";
        holder.tvAirline.setText(airlineInfo);

        String depTime = formatTime(flight.getDepartureTime());
        String arrTime = formatTime(flight.getArrivalTime());
        holder.tvTime.setText(depTime + " - " + arrTime);

        if (flight.getClasses() != null && !flight.getClasses().isEmpty()) {
            double price = flight.getClasses().get(0).getBasePrice();
            String formattedPrice = String.format("%,.0f VND", price);
            holder.tvPrice.setText(formattedPrice);
        } else {
            holder.tvPrice.setText("Hết vé");
        }

        // --- BƯỚC 1.2: BẮT SỰ KIỆN BẤM VÀO TỪNG DÒNG ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFlightClick(flight);
            }
        });
    }

    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.isEmpty()) return "--:--";
        try {
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