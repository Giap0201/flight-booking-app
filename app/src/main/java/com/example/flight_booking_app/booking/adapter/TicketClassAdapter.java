package com.example.flight_booking_app.booking.adapter; // Đổi tên package cho khớp

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.model.FlightClass;

import java.util.List;

public class TicketClassAdapter extends BaseAdapter {

    private Context context;
    private List<FlightClass> listTickets;
    private String currentFlightId;

    public TicketClassAdapter(Context context, List<FlightClass> listTickets, String currentFlightId) {
        this.context = context;
        this.listTickets = listTickets;
        this.currentFlightId = currentFlightId;
    }

    @Override
    public int getCount() {
        return listTickets.size();
    }

    @Override
    public Object getItem(int position) {
        return listTickets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ticket_class, parent, false);
        }

        TextView tvClassName = convertView.findViewById(R.id.tvClassName);
        TextView tvAvailableSeats = convertView.findViewById(R.id.tvAvailableSeats);
        TextView tvPrice = convertView.findViewById(R.id.tvPrice);
        Button btnSelectTicket = convertView.findViewById(R.id.btnSelectTicket);

        FlightClass ticket = listTickets.get(position);

        tvClassName.setText(ticket.getClassType());
        tvAvailableSeats.setText("Available: " + ticket.getAvailableSeats() + " seats");
        tvPrice.setText("$" + ticket.getBasePrice());

        btnSelectTicket.setOnClickListener(v -> {
            Toast.makeText(context, "Đã chọn vé: " + ticket.getClassType(), Toast.LENGTH_SHORT).show();
            // Nơi này sau này sẽ là Intent gọi sang màn hình Nhập thông tin hành khách
        });

        return convertView;
    }
}