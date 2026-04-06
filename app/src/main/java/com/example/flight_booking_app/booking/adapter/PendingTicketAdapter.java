package com.example.flight_booking_app.booking.adapter;

import android.content.Intent;
import android.os.CountDownTimer;
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

public class PendingTicketAdapter extends RecyclerView.Adapter<PendingTicketAdapter.PendingViewHolder> {

    private List<BookingSummary> ticketList;

    public PendingTicketAdapter(List<BookingSummary> ticketList) {
        this.ticketList = ticketList;
    }

    public void setTicketList(List<BookingSummary> ticketList) {
        this.ticketList = ticketList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_pending, parent, false);
        return new PendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingViewHolder holder, int position) {
        BookingSummary ticket = ticketList.get(position);

        // 1. Set Order ID (PNR Code)
        // Lưu ý: Đã thêm check null cho PNR
        holder.tvOrderId.setText("Order ID  " + (ticket.getPnrCode() != null ? ticket.getPnrCode() : "N/A"));

        // 2. Set Thông tin ngày bay
        if (ticket.getDepartureTime() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(ticket.getDepartureTime());

                if (date != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String formattedDate = dateFormat.format(date);
                    String className = ticket.getClassType() != null ? formatClass(ticket.getClassType())
                            : "Economy Class";
                    holder.tvFlightShortDesc.setText(formattedDate + " • " + className);
                }
            } catch (ParseException e) {
                holder.tvFlightShortDesc.setText(ticket.getDepartureTime());
            }
        }

        // 3. Xử lý Countdown Timer (Sử dụng createdAt từ BE)
        if (ticket.getCreatedAt() != null) {
            startCountdownTimer(holder.tvTimer, ticket.getCreatedAt());
        } else {
            holder.tvTimer.setText("⚠ payment status unknown");
        }

        // [MỚI] Click sang màn Booking Detail
        holder.itemView.setOnClickListener(v -> {
            if (ticket.getId() != null) {
                Intent intent = new Intent(v.getContext(), BookingDetailActivity.class);
                intent.putExtra("BOOKING_ID", ticket.getId());
                v.getContext().startActivity(intent);
            }
        });
    }

    private void startCountdownTimer(TextView tvTimer, String createdAtIso) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date createdAtDate = format.parse(createdAtIso);

            if (createdAtDate == null)
                return;

            // SỬA Ở ĐÂY: Theo BE logic expire là 10 PHÚT
            long expireTimeMillis = createdAtDate.getTime() + (10 * 60 * 1000);
            long currentTimeMillis = System.currentTimeMillis();
            long timeLeftMillis = expireTimeMillis - currentTimeMillis;

            if (timeLeftMillis > 0) {
                if (tvTimer.getTag() instanceof CountDownTimer) {
                    ((CountDownTimer) tvTimer.getTag()).cancel();
                }

                CountDownTimer timer = new CountDownTimer(timeLeftMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long minutes = (millisUntilFinished / 1000) / 60;
                        long seconds = (millisUntilFinished / 1000) % 60;

                        // Format mm:ss cho gọn giống UI
                        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                        tvTimer.setText("⚠ remaining payment period    " + timeLeftFormatted);
                    }

                    @Override
                    public void onFinish() {
                        tvTimer.setText("⚠ payment period expired");
                    }
                }.start();

                tvTimer.setTag(timer);
            } else {
                tvTimer.setText("⚠ payment period expired");
            }
        } catch (ParseException e) {
            tvTimer.setText("⚠ Timer Error");
        }
    }

    public String formatClass(String rawClass) {
        if (rawClass == null || rawClass.trim().isEmpty())
            return "Economy";
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

    static class PendingViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTimer, tvFlightShortDesc;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTimer = itemView.findViewById(R.id.tvTimer);
            tvFlightShortDesc = itemView.findViewById(R.id.tvFlightShortDesc);
        }
    }
}