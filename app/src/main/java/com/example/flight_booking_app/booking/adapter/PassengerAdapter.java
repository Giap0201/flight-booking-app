package com.example.flight_booking_app.booking.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.response.client.PassengerTicketResponse;
import com.example.flight_booking_app.booking.response.client.TicketDetailResponse;

import java.util.List;

public class PassengerAdapter extends RecyclerView.Adapter<PassengerAdapter.ViewHolder> {
    private List<PassengerTicketResponse> passengers;

    public PassengerAdapter(List<PassengerTicketResponse> passengers) {
        this.passengers = passengers;
    }

    public void setPassengers(List<PassengerTicketResponse> passengers) {
        this.passengers = passengers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_passenger, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PassengerTicketResponse p = passengers.get(position);

        holder.tvPassengerName.setText(p.getFirstName() + " " + p.getLastName());

        // ĐÃ SỬA LỖI SUBSTRING CRASH: Kiểm tra độ dài an toàn
        String idStr = p.getPassengerId() != null ? p.getPassengerId().toString() : "UNKNOWN";
        String shortId = idStr.length() > 10 ? idStr.substring(0, 10).toUpperCase() : idStr.toUpperCase();
        holder.tvPassengerId.setText("ID - " + shortId);

        // ĐÃ SỬA LỖI INDEX OUT OF BOUNDS: Kiểm tra List có rỗng không
        if (p.getTickets() != null && !p.getTickets().isEmpty()) {
            TicketDetailResponse ticket = p.getTickets().get(0);

            // Dùng String.valueOf để tránh lỗi nếu Android không dùng Enum
            String className = ticket.getClassType() != null ? String.valueOf(ticket.getClassType()).replace("_", " ") : "Economy";
            String seat = (ticket.getSeatNumber() != null && !ticket.getSeatNumber().isEmpty()) ? ticket.getSeatNumber() : "-";

            // Viết hoa chữ đầu
            if (className.length() > 1) {
                className = className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
            }
            holder.tvSeatInfo.setText(className + " / " + seat);
        } else {
            holder.tvSeatInfo.setText("Not assigned");
        }

        // Tô màu Badge an toàn
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);

        String type = p.getType() != null ? String.valueOf(p.getType()).toUpperCase() : "ADULT";
        if (type.contains("ADULT")) {
            shape.setColor(Color.parseColor("#E8F0FE")); // Xanh nhạt
            holder.tvPassengerBadge.setTextColor(Color.parseColor("#1A73E8")); // Xanh đậm
            holder.tvPassengerBadge.setText("Adult");
        } else if (type.contains("CHILD")) {
            shape.setColor(Color.parseColor("#FCE8E6")); // Đỏ nhạt
            holder.tvPassengerBadge.setTextColor(Color.parseColor("#D93025")); // Đỏ đậm
            holder.tvPassengerBadge.setText("Children");
        } else {
            shape.setColor(Color.parseColor("#FEF3C7")); // Vàng nhạt
            holder.tvPassengerBadge.setTextColor(Color.parseColor("#D97706")); // Cam đậm
            holder.tvPassengerBadge.setText("Infants");
        }
        holder.tvPassengerBadge.setBackground(shape);
    }

    @Override
    public int getItemCount() {
        return passengers != null ? passengers.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPassengerName, tvPassengerId, tvPassengerBadge, tvSeatInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPassengerName = itemView.findViewById(R.id.tvPassengerName);
            tvPassengerId = itemView.findViewById(R.id.tvPassengerId);
            tvPassengerBadge = itemView.findViewById(R.id.tvPassengerBadge);
            tvSeatInfo = itemView.findViewById(R.id.tvSeatInfo);
        }
    }
}