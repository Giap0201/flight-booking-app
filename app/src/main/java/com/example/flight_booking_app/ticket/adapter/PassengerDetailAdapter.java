package com.example.flight_booking_app.ticket.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.response.client.PassengerTicketResponse;
import com.example.flight_booking_app.ticket.response.client.TicketDetailResponse;

import java.util.List;

public class PassengerDetailAdapter extends RecyclerView.Adapter<PassengerDetailAdapter.ViewHolder> {
    private List<PassengerTicketResponse> passengers;

    public PassengerDetailAdapter(List<PassengerTicketResponse> passengers) {
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

        // 1. Hien thi ten day du
        String firstName = p.getFirstName() != null ? p.getFirstName() : "";
        String lastName = p.getLastName() != null ? p.getLastName() : "";
        holder.tvPassengerName.setText(firstName + " " + lastName);

        // 2. Hien thi ID rut gon (Lay tu UUID cua Passenger)
        String idStr = p.getPassengerId() != null ? p.getPassengerId().toString() : "N/A";
        String shortId = idStr.length() > 8 ? idStr.substring(0, 8).toUpperCase() : idStr.toUpperCase();
        holder.tvPassengerId.setText("ID: " + shortId);

        // 3. Truy cap vao danh sach ve (Tickets) cua hanh khach nay
        if (p.getTickets() != null && !p.getTickets().isEmpty()) {
            // Lay ve dau tien (thuong 1 hanh khach chi co 1 ve cho 1 chang bay)
            TicketDetailResponse ticket = p.getTickets().get(0);

            // Lay hang ve (Class)
            String className = ticket.getClassType() != null ? ticket.getClassType().replace("_", " ") : "Economy";
            if (className.length() > 1) {
                className = className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
            }

            // Lay so ghe (Seat)
            String seat = (ticket.getSeatNumber() != null && !ticket.getSeatNumber().isEmpty()) ? ticket.getSeatNumber() : "TBD";

            // Hien thi: Hang ve / So ghe
            holder.tvSeatInfo.setText(className + " / Ghế: " + seat);
        } else {
            holder.tvSeatInfo.setText("Chưa xác định chỗ ngồi");
        }

        // 4. Thiet lap nhan loai hanh khach (Badge) - DA VIET HOA
        setupPassengerBadge(holder.tvPassengerBadge, p.getType());
    }

    private void setupPassengerBadge(TextView tvBadge, String type) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);

        String pType = type != null ? type.toUpperCase() : "ADULT";

        if (pType.contains("ADULT")) {
            shape.setColor(Color.parseColor("#E8F0FE")); // Xanh duong nhat
            tvBadge.setTextColor(Color.parseColor("#1A73E8"));
            tvBadge.setText("Người lớn");
        } else if (pType.contains("CHILD")) {
            shape.setColor(Color.parseColor("#FCE8E6")); // Do nhat
            tvBadge.setTextColor(Color.parseColor("#D93025"));
            tvBadge.setText("Trẻ em");
        } else if (pType.contains("INFANT")) {
            shape.setColor(Color.parseColor("#FEF3C7")); // Vang nhat
            tvBadge.setTextColor(Color.parseColor("#D97706"));
            tvBadge.setText("Em bé");
        } else {
            shape.setColor(Color.parseColor("#F3F4F6")); // Xam nhat
            tvBadge.setTextColor(Color.parseColor("#374151"));
            tvBadge.setText("Khác");
        }
        tvBadge.setBackground(shape);
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
