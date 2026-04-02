package com.example.flight_booking_app.booking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.model.BookingSummary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingTicketAdapter extends RecyclerView.Adapter<UpcomingTicketAdapter.TicketViewHolder> {

    private List<BookingSummary> ticketList;

    public UpcomingTicketAdapter(List<BookingSummary> ticketList) {
        this.ticketList = ticketList;
    }

    public void setTicketList(List<BookingSummary> ticketList) {
        this.ticketList = ticketList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_upcoming, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        BookingSummary ticket = ticketList.get(position);

        // Gán mã sân bay đi và đến
        holder.tvDepartureCode.setText(ticket.getOrigin());
        holder.tvArrivalCode.setText(ticket.getDestination());

        // Format thời gian bay từ chuỗi ISO (VD: 2026-04-01T16:22:27.241Z)
        if (ticket.getDepartureTime() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(ticket.getDepartureTime());

                // Lấy giờ phút (VD: 16:22)
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                holder.tvDepartureTime.setText(timeFormat.format(date));

                // Lấy ngày tháng (VD: Mon, Apr 01)
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
                holder.tvDate.setText(dateFormat.format(date));

            } catch (ParseException e) {
                e.printStackTrace();
                holder.tvDepartureTime.setText(ticket.getDepartureTime());
            }
        }

        // --- LƯU Ý DÀNH CHO BẠN ---
        // UI Figma có yêu cầu hiển thị: Giờ đến (ArrivalTime), Thời gian bay (Duration), Hạng ghế (Class), Số người (Passenger Count).
        // Tuy nhiên, API List 'my-bookings' hiện tại Backend chưa trả về các trường này.
        // Tạm thời tôi set dữ liệu giả (Placeholder) để UI không bị trống.
        // Sau này bạn có thể yêu cầu Backend bổ sung vào BookingSummary, hoặc chúng ta tính toán sau.
        holder.tvArrivalTime.setText("18:00"); // Tạm thời hardcode
        holder.tvDuration.setText("1h 30m");  // Tạm thời hardcode
        holder.tvClass.setText("Economy");    // Tạm thời hardcode
        holder.tvPassengerCount.setText("1 Person"); // Tạm thời hardcode
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvDepartureCode, tvDepartureTime;
        TextView tvArrivalCode, tvArrivalTime;
        TextView tvDuration, tvDate, tvClass, tvPassengerCount;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDepartureCode = itemView.findViewById(R.id.tvDepartureCode);
            tvDepartureTime = itemView.findViewById(R.id.tvDepartureTime);
            tvArrivalCode = itemView.findViewById(R.id.tvArrivalCode);
            tvArrivalTime = itemView.findViewById(R.id.tvArrivalTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvPassengerCount = itemView.findViewById(R.id.tvPassengerCount);
        }
    }
}