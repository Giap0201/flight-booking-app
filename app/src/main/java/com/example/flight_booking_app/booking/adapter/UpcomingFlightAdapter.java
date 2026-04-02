package com.example.flight_booking_app.booking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flight_booking_app.R;
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
        holder.tvDate.setText(flight.getDepartureTime()); // Tạm thời set string, sau này dùng SimpleDateFormat để format lại
        // Vì BookingSummary chưa có trường class và passenger count cụ thể trong JSON bạn gửi,
        // chúng ta tạm để placeholder hoặc lấy dữ liệu mẫu.
        holder.tvClass.setText("Economy");
        holder.tvPassengerCount.setText("1 Person");
    }

    @Override
    public int getItemCount() {
        return flights.size();
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