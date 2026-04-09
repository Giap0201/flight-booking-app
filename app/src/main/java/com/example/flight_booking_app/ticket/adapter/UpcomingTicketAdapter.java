package com.example.flight_booking_app.ticket.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.activity.BookingDetailActivity;
import com.example.flight_booking_app.ticket.model.BookingSummary;

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

        // 1. Sân bay (BE trả về Tên đầy đủ)
        holder.tvOriginCode.setText(ticket.getOrigin() != null ? ticket.getOrigin() : "N/A");
        holder.tvDestCode.setText(ticket.getDestination() != null ? ticket.getDestination() : "N/A");

        // 2. Format Giờ Đi và Ngày Đi
        if (ticket.getDepartureTime() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(ticket.getDepartureTime());

                if (date != null) {
                    holder.tvOriginTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));
                    holder.tvDate.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date));
                }
            } catch (ParseException e) {
                holder.tvOriginTime.setText("--:--");
                holder.tvDate.setText(ticket.getDepartureTime());
            }
        }

        // --- BẮT ĐẦU GÁN DỮ LIỆU ĐỘNG ---
        // 3. Giờ Đến
        if (ticket.getArrivalTime() != null) {
             try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date parsedDate = inputFormat.parse(ticket.getArrivalTime());
                if (parsedDate != null) {
                    holder.tvDestTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(parsedDate));
                } else {
                    holder.tvDestTime.setText("--:--");
                }
             } catch (ParseException e) {
                 holder.tvDestTime.setText("--:--");
             }
        } else {
             holder.tvDestTime.setText("--:--");
        }

        // 4. Thời lượng bay
        if (ticket.getDepartureTime() != null && ticket.getArrivalTime() != null) {
            holder.tvDuration.setText(calculateDuration(ticket.getDepartureTime(), ticket.getArrivalTime()));
        } else {
            holder.tvDuration.setText("Chi tiết");
        }

        // 5. Số lượng hành khách
        if (ticket.getPassengerCount() != null) {
            holder.tvPassengerCount.setText(ticket.getPassengerCount() + " Person" + (ticket.getPassengerCount() > 1 ? "s" : ""));
        } else {
            holder.tvPassengerCount.setText("1 Person");
        }

        // 6. Hạng vé
        if (ticket.getClassType() != null) {
            holder.tvClass.setText(formatClass(ticket.getClassType()));
        } else {
            holder.tvClass.setText("Economy");
        }
        // --- KẾT THÚC GÁN DỮ LIỆU ĐỘNG ---

        // 7. SỰ KIỆN CLICK: Bấm vào vé để xem chi tiết đầy đủ
        holder.itemView.setOnClickListener(v -> {
            if (ticket.getId() != null) {
                Intent intent = new Intent(v.getContext(), BookingDetailActivity.class);
                // Truyền String ID sang màn Detail
                intent.putExtra("BOOKING_ID", ticket.getId());
                v.getContext().startActivity(intent);
            }
        });
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
        return ticketList != null ? ticketList.size() : 0;
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvOriginCode, tvOriginTime;
        TextView tvDestCode, tvDestTime;
        TextView tvDuration, tvDate, tvClass, tvPassengerCount;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vui lòng đảm bảo các ID này TỒN TẠI trong file item_ticket_upcoming.xml
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