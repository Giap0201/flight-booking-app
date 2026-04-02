package com.example.flight_booking_app.booking.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
    private LayoutInflater inflater;
    private String currentFlightId;

    // Khai báo thêm biến
    private int adultCount, childCount, infantCount;
    // Sửa lại Constructor để nhận thêm flightId
    public TicketClassAdapter(Context context, List<FlightClass> listTickets, String currentFlightId, int adultCount, int childCount, int infantCount) {        this.context = context;
        this.listTickets = listTickets;
        this.currentFlightId = currentFlightId; // Gán biến
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.infantCount = infantCount;
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
            convertView = inflater.inflate(R.layout.item_ticket_class, parent, false);
        }

        TextView tvClassName = convertView.findViewById(R.id.tvClassName);
        TextView tvPrice = convertView.findViewById(R.id.tvPrice);
        TextView tvOldPrice = convertView.findViewById(R.id.tvOldPrice);
        Button btnSelectTicket = convertView.findViewById(R.id.btnSelectTicket);

        FlightClass ticket = listTickets.get(position);

        tvClassName.setText(ticket.getClassType());

        // Cập nhật giá (Giả lập tiền)
        tvPrice.setText("$" + ticket.getBasePrice());

        // Làm hiệu ứng gạch ngang cho giá cũ
        tvOldPrice.setText("$" + (ticket.getBasePrice() + 20)); // Cộng thêm tí tiền để làm giá cũ
        tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // TÌM ĐẾN SỰ KIỆN NÚT SELECT VÀ SỬA LẠI THẾ NÀY:
        // Ở chỗ nút btnSelectTicket.setOnClickListener:
        btnSelectTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, com.example.flight_booking_app.booking.activity.BookingFormActivity.class);

                intent.putExtra("flightId", currentFlightId);
                intent.putExtra("flightClassId", ticket.getId());
                intent.putExtra("ticketPrice", ticket.getBasePrice());

                // TRUYỀN TIẾP SỐ LƯỢNG SANG MÀN HÌNH FORM CỦA BẠN
                intent.putExtra("adultCount", adultCount);
                intent.putExtra("childCount", childCount);
                intent.putExtra("infantCount", infantCount);

                context.startActivity(intent);
            }
        });

        return convertView;
    }
}