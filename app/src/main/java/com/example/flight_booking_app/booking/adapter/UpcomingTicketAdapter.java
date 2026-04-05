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

        // 1. Sân bay đi và đến
        holder.tvOriginCode.setText(ticket.getOrigin());
        holder.tvDestCode.setText(ticket.getDestination());

        // 2. Format Giờ Đi và Ngày Đi
        if (ticket.getDepartureTime() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(ticket.getDepartureTime());

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                holder.tvOriginTime.setText(timeFormat.format(date));

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
                holder.tvDate.setText(dateFormat.format(date));

            } catch (ParseException e) {
                e.printStackTrace();
                holder.tvOriginTime.setText(ticket.getDepartureTime());
            }
        }

        // 3. Format Giờ Đến (DỮ LIỆU THẬT TỪ BE)
        if (ticket.getArrivalTime() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date dateArrival = inputFormat.parse(ticket.getArrivalTime());

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                holder.tvDestTime.setText(timeFormat.format(dateArrival));
            } catch (ParseException e) {
                e.printStackTrace();
                holder.tvDestTime.setText(ticket.getArrivalTime());
            }
        } else {
            holder.tvDestTime.setText("--:--");
        }

        // 4. Thời lượng bay (DỮ LIỆU THẬT TỪ BE)
        if (ticket.getDuration() != null && !ticket.getDuration().isEmpty()) {
            holder.tvDuration.setText(ticket.getDuration());
        } else {
            holder.tvDuration.setText("--h --m");
        }

        // 5. Số lượng hành khách (DỮ LIỆU THẬT TỪ BE kèm logic số ít/số nhiều)
        int pCount = ticket.getPassengerCount();
        if (pCount > 0) {
            String suffix = (pCount > 1) ? " Persons" : " Person";
            holder.tvPassengerCount.setText(pCount + suffix);
        } else {
            holder.tvPassengerCount.setText("1 Person"); // Fallback an toàn
        }

        // 6. Hạng vé
        if (ticket.getFlightClass() != null && !ticket.getFlightClass().isEmpty()) {
            String niceClass = ticket.getFlightClass().replace("_", " ");
            holder.tvClass.setText(niceClass);
        } else {
            holder.tvClass.setText("Chưa xác định");
        }
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvOriginCode, tvOriginTime;
        TextView tvDestCode, tvDestTime;
        TextView tvDuration, tvDate, tvClass, tvPassengerCount;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOriginCode = itemView.findViewById(R.id.tvOriginCode);
            tvOriginTime = itemView.findViewById(R.id.tvOriginTime);
            tvDestCode = itemView.findViewById(R.id.tvDestCode);
            tvDestTime = itemView.findViewById(R.id.tvDestTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvPassengerCount = itemView.findViewById(R.id.tvPassengerCount);
        }
    }
}