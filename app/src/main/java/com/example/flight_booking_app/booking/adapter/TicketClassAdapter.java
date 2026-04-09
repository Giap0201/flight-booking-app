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

    // 1. Khai báo cái "Bộ đàm" (Interface)
    public interface OnTicketSelectedListener {
        void onSelect(FlightClass ticket);
    }

    private Context context;
    private List<FlightClass> listTickets;
    private OnTicketSelectedListener listener; // Biến giữ bộ đàm

    // Constructor bây giờ siêu gọn nhẹ
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

        holder.tvClassName.setText(ticket.getClassType());
        holder.tvPrice.setText(String.format("$%.2f", ticket.getBasePrice()));

        holder.tvOldPrice.setText(String.format("$%.2f", ticket.getBasePrice() + 20));
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // 2. Bắt sự kiện Click: Thay vì gọi Intent, chỉ cần "alo" qua bộ đàm
        holder.btnSelectTicket.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSelect(ticket); // Ném thông tin vé ra ngoài cho Activity tự xử lý
            }
        });
    }

    @Override
    public int getItemCount() {
        return listTickets != null ? listTickets.size() : 0;
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvPrice, tvOldPrice;
        Button btnSelectTicket;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
            btnSelectTicket = itemView.findViewById(R.id.btnSelectTicket);
        }
    }
}