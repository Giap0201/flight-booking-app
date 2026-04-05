package com.example.flight_booking_app.booking.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.activity.FlightDetailActivity;
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

        // 1. Header Ngày tháng
        String currentDateStr = getFormattedDateHeader(ticket.getDepartureTime());
        String previousDateStr = position > 0 ? getFormattedDateHeader(tickets.get(position - 1).getDepartureTime()) : "";

        if (position == 0 || !currentDateStr.equals(previousDateStr)) {
            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(currentDateStr);
        } else {
            holder.tvDateHeader.setVisibility(View.GONE);
        }

        // 2. Dữ liệu cơ bản
        holder.tvOrderId.setText(ticket.getPnrCode());
        holder.tvOrigin.setText(ticket.getOrigin() + " Airport");
        holder.tvDest.setText(ticket.getDestination() + " Airport");

        String rawClass = ticket.getFlightClass() != null ? ticket.getFlightClass().replace("_", " ") : "Economy";
        holder.tvFlightInfo.setText("✈ " + (ticket.getFlightNumber() != null ? ticket.getFlightNumber() : "Chuyến bay") + " • " + rawClass);
        holder.tvDuration.setText(ticket.getDuration() != null ? ticket.getDuration() : "--h --m");
        holder.tvDepTime.setText(extractTime(ticket.getDepartureTime()));
        holder.tvArrTime.setText(extractTime(ticket.getArrivalTime()));

        // 3. HIỂN THỊ TRẠNG THÁI TIẾNG VIỆT
        setupStatusBadge(holder.tvStatusBadge, ticket.getStatus());

        // 4. Sự kiện Click chuyển trang
        holder.itemView.setOnClickListener(v -> {
            if (ticket.getId() != null) {
                Intent intent = new Intent(v.getContext(), FlightDetailActivity.class);
                intent.putExtra("BOOKING_ID", ticket.getId().toString());
                v.getContext().startActivity(intent);
            }
        });
    }

    // ========================================================
    // HÀM VIỆT HÓA TRẠNG THÁI (STATUS)
    // ========================================================
    private void setupStatusBadge(TextView tvBadge, String status) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);

        if (status == null) status = "";

        switch (status) {
            case "PAID":
                shape.setColor(Color.parseColor("#E8F5E9")); // Xanh lá nhạt
                tvBadge.setTextColor(Color.parseColor("#2E7D32"));
                tvBadge.setText("Đã thanh toán");
                break;
            case "CONFIRMED":
                shape.setColor(Color.parseColor("#E8F5E9"));
                tvBadge.setTextColor(Color.parseColor("#2E7D32"));
                tvBadge.setText("Xác nhận");
                break;
            case "AWAITING_PAYMENT":
                shape.setColor(Color.parseColor("#FFF3E0")); // Cam nhạt
                tvBadge.setTextColor(Color.parseColor("#EF6C00"));
                tvBadge.setText("Chờ thanh toán");
                break;
            case "CANCELLED":
                shape.setColor(Color.parseColor("#FFEBEE")); // Đỏ nhạt
                tvBadge.setTextColor(Color.parseColor("#C62828"));
                tvBadge.setText("Đã huỷ");
                break;
            case "REFUNDED":
                shape.setColor(Color.parseColor("#FFEBEE"));
                tvBadge.setTextColor(Color.parseColor("#C62828"));
                tvBadge.setText("Hoàn tiền");
                break;
            case "PENDING":
            default:
                shape.setColor(Color.parseColor("#E3F2FD")); // Xanh dương nhạt
                tvBadge.setTextColor(Color.parseColor("#1565C0"));
                tvBadge.setText("Chờ xử lý");
                break;
        }
        tvBadge.setBackground(shape);
    }

    private String getFormattedDateHeader(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "Ngày không xác định";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM, yyyy", new Locale("vi", "VN"));
            return outputFormat.format(date);
        } catch (ParseException e) {
            return rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
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
        TextView tvDateHeader, tvOrderId, tvStatusBadge;
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