package com.example.flight_booking_app.booking.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.activity.BookingFormActivity;
import com.example.flight_booking_app.booking.model.FlightClass;

import java.util.List;

public class TicketClassAdapter extends RecyclerView.Adapter<TicketClassAdapter.TicketViewHolder> {

    private Context context;
    private List<FlightClass> listTickets;
    private String currentFlightId;
    private int adultCount, childCount, infantCount;

    public TicketClassAdapter(Context context, List<FlightClass> listTickets, String currentFlightId,
                              int adultCount, int childCount, int infantCount) {
        this.context = context;
        this.listTickets = listTickets;
        this.currentFlightId = currentFlightId;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.infantCount = infantCount;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp layout item_ticket_class vào
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_class, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        FlightClass ticket = listTickets.get(position);

        // Đổ dữ liệu vào view thông qua Holder
        holder.tvClassName.setText(ticket.getClassType());
        holder.tvPrice.setText(String.format("$%.2f", ticket.getBasePrice()));

        // Giá cũ (giả lập khuyến mãi)
        holder.tvOldPrice.setText(String.format("$%.2f", ticket.getBasePrice() + 20));
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Sự kiện click nút Select
        holder.btnSelectTicket.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingFormActivity.class);

            // Truyền các ID quan trọng
            intent.putExtra("flightId", currentFlightId);
            intent.putExtra("flightClassId", ticket.getId());
            intent.putExtra("ticketPrice", ticket.getBasePrice());

            // Truyền số lượng khách từ màn hình tìm kiếm sang
            intent.putExtra("adultCount", adultCount);
            intent.putExtra("childCount", childCount);
            intent.putExtra("infantCount", infantCount);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listTickets != null ? listTickets.size() : 0;
    }

    // Lớp ViewHolder để giữ các tham chiếu View, giúp tối ưu hiệu năng
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