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
import com.example.flight_booking_app.booking.activity.BookingDetailActivity;
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
        notifyDataSetChanged(); // Reset lại view sau khi sort
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

        // 1. HEADER NGÀY THÁNG (Nhờ Activity đã sort chuẩn nên cái này chạy rất mượt)
        String currentDateStr = getFormattedDateHeader(ticket.getDepartureTime());
        String previousDateStr = position > 0 ? getFormattedDateHeader(tickets.get(position - 1).getDepartureTime()) : "";

        if (position == 0 || !currentDateStr.equals(previousDateStr)) {
            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(currentDateStr);
        } else {
            holder.tvDateHeader.setVisibility(View.GONE);
        }

        // 2. THÔNG TIN CƠ BẢN TỪ BE
        holder.tvOrderId.setText(ticket.getPnrCode());
        holder.tvOrigin.setText(ticket.getOrigin() != null ? ticket.getOrigin() : "N/A");
        holder.tvDest.setText(ticket.getDestination() != null ? ticket.getDestination() : "N/A");
        holder.tvDepTime.setText(extractTime(ticket.getDepartureTime()));

        String flightNum = ticket.getFlightNumber() != null ? ticket.getFlightNumber() : "Flight";
        holder.tvFlightInfo.setText("✈ " + flightNum + " • " + formatClass(ticket.getClassType()));

        // 3. THÔNG TIN ĐÃ ĐƯỢC CẬP NHẬT
        holder.tvArrTime.setText(extractTime(ticket.getArrivalTime()));

        if (ticket.getDepartureTime() != null && ticket.getArrivalTime() != null) {
            holder.tvDuration.setText(calculateDuration(ticket.getDepartureTime(), ticket.getArrivalTime()));
        } else {
            holder.tvDuration.setText("Chi tiết");
        }

        // 4. TRẠNG THÁI (Badge Việt Hóa)
        setupStatusBadge(holder.tvStatusBadge, ticket.getStatus());

        // 5. CLICK SANG MÀN DETAIL
        holder.itemView.setOnClickListener(v -> {
            if (ticket.getId() != null) {
                Intent intent = new Intent(v.getContext(), BookingDetailActivity.class);
                intent.putExtra("BOOKING_ID", ticket.getId());
                v.getContext().startActivity(intent);
            }
        });
    }

    private void setupStatusBadge(TextView tvBadge, String status) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);
        if (status == null) status = "";

        // Tự gán các status thành "Đã bay" nếu là History và ko bị huỷ
        if (status.equals("PAID") || status.equals("CONFIRMED")) {
            status = "COMPLETED";
        }

        switch (status) {
            case "COMPLETED":
                shape.setColor(Color.parseColor("#E8F5E9"));
                tvBadge.setTextColor(Color.parseColor("#2E7D32"));
                tvBadge.setText("Hoàn thành");
                break;
            case "CANCELLED":
            case "REFUNDED":
                shape.setColor(Color.parseColor("#FFEBEE"));
                tvBadge.setTextColor(Color.parseColor("#C62828"));
                tvBadge.setText("Đã huỷ");
                break;
            default:
                shape.setColor(Color.parseColor("#F5F5F5"));
                tvBadge.setTextColor(Color.parseColor("#9E9E9E"));
                tvBadge.setText(status);
                break;
        }
        tvBadge.setBackground(shape);
    }

    private String getFormattedDateHeader(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "Ngày không xác định";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDate);
            return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return rawDate.split("T")[0]; // Fallback
        }
    }

    private String extractTime(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "--:--";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDate);
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return "--:--";
        }
    }

    private String calculateDuration(String departure, String arrival) {
        if (departure == null || departure.trim().isEmpty() || arrival == null || arrival.trim().isEmpty()) return "Chi tiết";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String safeDep = departure.length() > 19 ? departure.substring(0, 19) : departure;
            String safeArr = arrival.length() > 19 ? arrival.substring(0, 19) : arrival;
            
            Date depDate = format.parse(safeDep);
            Date arrDate = format.parse(safeArr);

            if (depDate != null && arrDate != null) {
                long diffInMillis = arrDate.getTime() - depDate.getTime();
                if (diffInMillis < 0) return "Chi tiết";
                long hours = diffInMillis / (60 * 60 * 1000);
                long minutes = (diffInMillis / (60 * 1000)) % 60;
                if (minutes == 0) return hours + "h";
                return hours + "h " + minutes + "m";
            }
        } catch (Exception e) {
            return "Chi tiết";
        }
        return "Chi tiết";
    }

    public String formatClass(String rawClass) {
        if (rawClass == null || rawClass.trim().isEmpty()) return "Economy";
        try {
            String[] words = rawClass.replace("_", " ").toLowerCase().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (word != null && word.length() > 0) {
                    sb.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        sb.append(word.substring(1));
                    }
                    sb.append(" ");
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return rawClass;
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