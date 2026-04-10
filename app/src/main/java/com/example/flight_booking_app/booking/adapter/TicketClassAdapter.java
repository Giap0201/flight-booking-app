package com.example.flight_booking_app.booking.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.model.FlightClass;

import java.util.List;

public class TicketClassAdapter extends RecyclerView.Adapter<TicketClassAdapter.TicketViewHolder> {

    public interface OnTicketSelectedListener {
        void onSelect(FlightClass ticket);
    }

    private Context context;
    private List<FlightClass> listTickets;
    private OnTicketSelectedListener listener;

    public TicketClassAdapter(Context context, List<FlightClass> listTickets, OnTicketSelectedListener listener) {
        this.context = context;
        this.listTickets = listTickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_class, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        FlightClass ticket = listTickets.get(position);

        // 1. Gán Tên hạng vé và Giá tiền
        holder.tvClassName.setText(ticket.getClassType());

        // (Nếu BE trả về VNĐ thì bạn sửa lại format cho đúng nhé)
        holder.tvPrice.setText(String.format("$%.2f", ticket.getBasePrice()));

        holder.tvOldPrice.setText(String.format("$%.2f", ticket.getBasePrice() + 20));
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // 2. HIỂN THỊ SỐ GHẾ THỰC TẾ (Load từ API)
        // Chú ý: Đảm bảo model FlightClass của bạn có hàm getAvailableSeats()
        int availableSeats = ticket.getAvailableSeats();
        holder.tvAvailableSeats.setText("Available: " + availableSeats + " seats");

        // Logic chặn người dùng chọn nếu hết ghế
        if (availableSeats > 0) {
            holder.btnSelectTicket.setEnabled(true);
            holder.btnSelectTicket.setText("Chọn");
            holder.btnSelectTicket.setAlpha(1.0f); // Hiện rõ nút
        } else {
            holder.btnSelectTicket.setEnabled(false);
            holder.btnSelectTicket.setText("Hết vé");
            holder.btnSelectTicket.setAlpha(0.5f); // Làm mờ nút đi
        }

        // 3. Bắt sự kiện Click: "Alo" qua bộ đàm
        holder.btnSelectTicket.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSelect(ticket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listTickets != null ? listTickets.size() : 0;
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvPrice, tvOldPrice;

        // ⚡ MỚI THÊM: Biến cho số lượng ghế
        TextView tvAvailableSeats;

        Button btnSelectTicket;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
            btnSelectTicket = itemView.findViewById(R.id.btnSelectTicket);

            // ⚡ MỚI THÊM: Ánh xạ View
            tvAvailableSeats = itemView.findViewById(R.id.tvAvailableSeats);
        }
    }
}