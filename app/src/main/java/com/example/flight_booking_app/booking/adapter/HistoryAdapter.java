package com.example.flight_booking_app.booking.adapter;

import android.content.Intent; // ĐÃ THÊM IMPORT
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.activity.FlightDetailActivity; // ĐÃ THÊM IMPORT
import com.example.flight_booking_app.booking.model.BookingSummary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<BookingSummary> tickets;

    public HistoryAdapter(List<BookingSummary> tickets) {
        this.tickets = tickets;
    }

    public void setTickets(List<BookingSummary> tickets) {
        this.tickets = tickets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_past_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingSummary ticket = tickets.get(position);

        // 1. Xử lý gom nhóm theo Ngày (Hiển thị Header)
        String currentDateStr = getFormattedDateHeader(ticket.getDepartureTime());
        String previousDateStr = position > 0 ? getFormattedDateHeader(tickets.get(position - 1).getDepartureTime()) : "";

        if (position == 0 || !currentDateStr.equals(previousDateStr)) {
            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(currentDateStr);
        } else {
            holder.tvDateHeader.setVisibility(View.GONE);
        }

        // 2. Map dữ liệu cơ bản
        holder.tvOrderId.setText(ticket.getPnrCode());
        holder.tvOrigin.setText(ticket.getOrigin() + " Airport");
        holder.tvDest.setText(ticket.getDestination() + " Airport");

        String niceClass = ticket.getFlightClass() != null ? ticket.getFlightClass().replace("_", " ") : "Economy";
        holder.tvFlightInfo.setText("✈ " + (ticket.getFlightNumber() != null ? ticket.getFlightNumber() : "Flight") + " • " + niceClass);

        holder.tvDuration.setText(ticket.getDuration() != null ? ticket.getDuration() : "--h --m");

        // Format Giờ đi/đến
        holder.tvDepTime.setText(extractTime(ticket.getDepartureTime()));
        holder.tvArrTime.setText(extractTime(ticket.getArrivalTime()));

        // 3. Xử lý Badge Status (Màu sắc theo trạng thái)
        setupStatusBadge(holder.tvStatusBadge, ticket.getStatus());

        // =======================================================
        // 4. SỰ KIỆN CLICK: CHUYỂN SANG TRANG CHI TIẾT VÉ
        // =======================================================
        holder.itemView.setOnClickListener(v -> {
            if (ticket.getId() != null) {
                Intent intent = new Intent(v.getContext(), FlightDetailActivity.class);
                intent.putExtra("BOOKING_ID", ticket.getId().toString());
                v.getContext().startActivity(intent);
            }
        });
    }

    private void setupStatusBadge(TextView tvBadge, String status) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);

        if (status == null) status = "";

        switch (status) {
            case "PAID":
            case "CONFIRMED":
                shape.setColor(Color.parseColor("#E8F5E9")); // Xanh nhạt
                tvBadge.setTextColor(Color.parseColor("#2E7D32")); // Xanh đậm
                tvBadge.setText("Paid off");
                break;
            case "CANCELLED":
            case "REFUNDED":
                shape.setColor(Color.parseColor("#FFEBEE")); // Đỏ nhạt
                tvBadge.setTextColor(Color.parseColor("#C62828")); // Đỏ đậm
                tvBadge.setText(status.equals("REFUNDED") ? "Refunded" : "Cancel");
                break;
            case "AWAITING_PAYMENT":
                shape.setColor(Color.parseColor("#FFF3E0")); // Cam nhạt
                tvBadge.setTextColor(Color.parseColor("#EF6C00")); // Cam đậm
                tvBadge.setText("Awaiting Pay");
                break;
            default: // PENDING
                shape.setColor(Color.parseColor("#E3F2FD")); // Xanh dương nhạt
                tvBadge.setTextColor(Color.parseColor("#1565C0"));
                tvBadge.setText("Pending");
                break;
        }
        tvBadge.setBackground(shape);
    }

    private String getFormattedDateHeader(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "Unknown Date";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return rawDate.substring(0, 10);
        }
    }

    private String extractTime(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "--:--";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            return "--:--";
        }
    }

    @Override
    public int getItemCount() {
        return tickets != null ? tickets.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader, tvOrderLabel, tvOrderId, tvStatusBadge;
        TextView tvDepTime, tvArrTime, tvDuration, tvOrigin, tvDest, tvFlightInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvDepTime = itemView.findViewById(R.id.tvDepTime);
            tvArrTime = itemView.findViewById(R.id.tvArrTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvOrigin = itemView.findViewById(R.id.tvOrigin);
            tvDest = itemView.findViewById(R.id.tvDest);
            tvFlightInfo = itemView.findViewById(R.id.tvFlightInfo);
        }
    }
}