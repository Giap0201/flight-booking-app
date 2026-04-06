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

        // 1. Hiển thị tên đầy đủ
        String firstName = p.getFirstName() != null ? p.getFirstName() : "";
        String lastName = p.getLastName() != null ? p.getLastName() : "";
        holder.tvPassengerName.setText(firstName + " " + lastName);

        // 2. Hiển thị ID rút gọn (Lấy từ UUID của Passenger)
        String idStr = p.getPassengerId() != null ? p.getPassengerId().toString() : "N/A";
        String shortId = idStr.length() > 8 ? idStr.substring(0, 8).toUpperCase() : idStr.toUpperCase();
        holder.tvPassengerId.setText("ID: " + shortId);

        // 3. Truy cập vào danh sách vé (Tickets) của hành khách này
        if (p.getTickets() != null && !p.getTickets().isEmpty()) {
            // Lấy vé đầu tiên (thường 1 hành khách chỉ có 1 vé cho 1 chặng bay)
            TicketDetailResponse ticket = p.getTickets().get(0);

            // Lấy hạng vé (Class)
            String className = ticket.getClassType() != null ? ticket.getClassType().replace("_", " ") : "Economy";
            if (className.length() > 1) {
                className = className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
            }

            // Lấy số ghế (Seat)
            String seat = (ticket.getSeatNumber() != null && !ticket.getSeatNumber().isEmpty()) ? ticket.getSeatNumber() : "TBD";

            // Hiển thị: Hạng vé / Số ghế
            holder.tvSeatInfo.setText(className + " / Ghế: " + seat);
        } else {
            holder.tvSeatInfo.setText("Chưa xác định chỗ ngồi");
        }

        // 4. Thiết lập nhãn loại hành khách (Badge) - ĐÃ VIỆT HÓA
        setupPassengerBadge(holder.tvPassengerBadge, p.getType());
    }

    private void setupPassengerBadge(TextView tvBadge, String type) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);

        String pType = type != null ? type.toUpperCase() : "ADULT";

        if (pType.contains("ADULT")) {
            shape.setColor(Color.parseColor("#E8F0FE")); // Xanh dương nhạt
            tvBadge.setTextColor(Color.parseColor("#1A73E8"));
            tvBadge.setText("Người lớn");
        } else if (pType.contains("CHILD")) {
            shape.setColor(Color.parseColor("#FCE8E6")); // Đỏ nhạt
            tvBadge.setTextColor(Color.parseColor("#D93025"));
            tvBadge.setText("Trẻ em");
        } else if (pType.contains("INFANT")) {
            shape.setColor(Color.parseColor("#FEF3C7")); // Vàng nhạt
            tvBadge.setTextColor(Color.parseColor("#D97706"));
            tvBadge.setText("Em bé");
        } else {
            shape.setColor(Color.parseColor("#F3F4F6")); // Xám nhạt
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