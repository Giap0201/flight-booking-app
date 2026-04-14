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

// Nhiệm vụ của nó là lấy dữ liệu từ List và "nhét" vào các khung giao diện (ViewHolder).
public class FlightClassAdapter extends RecyclerView.Adapter<FlightClassAdapter.TicketViewHolder> {

    // Giúp Adapter chỉ làm nhiệm vụ vẽ giao diện. Khi người dùng click, nó báo cáo lại qua hàm onSelect
    // Activity/Fragment sẽ lắng nghe hàm này để tự chuyển màn hình. Tránh viết code Intent vào đây.
    public interface OnTicketSelectedListener {
        void onSelect(FlightClass ticket);
    }

    private Context context; // Dùng để truy cập tài nguyên (như file XML layout)
    private List<FlightClass> listTickets; // Danh sách data các hạng vé
    private OnTicketSelectedListener listener; // Biến giữ "đường dây điện thoại" báo cáo sự kiện

    // Constructor: Nơi nhận dữ liệu truyền vào từ bên ngoài (từ Activity hoặc Fragment)
    public FlightClassAdapter(Context context, List<FlightClass> listTickets, OnTicketSelectedListener listener) {
        this.context = context;
        this.listTickets = listTickets;
        this.listener = listener;
    }

    // Hệ thống KHÔNG tạo ra 1000 cái View nếu list có 1000 vé.
    // Nó chỉ tạo đủ số lượng View lấp đầy màn hình điện thoại (tiết kiệm RAM).
    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater làm nhiệm vụ đọc file XML item_flight_class và "bơm" (inflate) thành View thực tế
        View view = LayoutInflater.from(context).inflate(R.layout.item_flight_class, parent, false);
        return new TicketViewHolder(view);
    }


    // Nhiệm vụ: Lấy cái Khung (holder) đang hiển thị và nhét data ở vị trí tương ứng (position) vào.
    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        // Lấy đối tượng vé máy bay hiện tại
        FlightClass ticket = listTickets.get(position);

        // 1. Gán Tên hạng vé (Ví dụ: Phổ thông, Thương gia)
        holder.tvClassName.setText(ticket.getClassType());

        // 2. Gán Giá tiền (Thủ thuật format string)
        // Dấu %,.0f giúp tự động thêm dấu phẩy ngăn cách hàng nghìn (VD: 1,500,000) và không có số thập phân
        holder.tvPrice.setText(String.format("%,.0f đ", (double) ticket.getBasePrice()));

        // 3. Xử lý Giá cũ (Tạo hiệu ứng chim mồi/giảm giá kích thích người dùng đặt vé)
        holder.tvOldPrice.setText(String.format("%,.0f đ", (double) ticket.getBasePrice() + 50000));
        // Dòng này thêm hiệu ứng Gạch ngang chữ (Strike-through) cho giá cũ
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // 4. HIỂN THỊ SỐ GHẾ THỰC TẾ
        int availableSeats = ticket.getAvailableSeats();
        holder.tvAvailableSeats.setText("Còn trống: " + availableSeats + " ghế");

        // Vì RecyclerView TÁI CHẾ lại View cũ, nên bắt buộc phải viết cả nhánh IF và ELSE
        // Nếu không viết ELSE, khi cuộn qua vé Hết chỗ, cái nút bị xám sẽ bị mang đi dùng cho vé Còn chỗ.
        if (availableSeats > 0) {
            // Còn vé: Trạng thái bình thường
            holder.btnSelectTicket.setEnabled(true); // Cho phép bấm
            holder.btnSelectTicket.setText("Chọn");
            holder.btnSelectTicket.setAlpha(1.0f); // Hiện rõ nét 100%
        } else {
            // Hết vé: Trạng thái vô hiệu hóa
            holder.btnSelectTicket.setEnabled(false); // Khóa nút không cho bấm
            holder.btnSelectTicket.setText("Hết vé");
            holder.btnSelectTicket.setAlpha(0.5f); // Làm mờ nút đi 50%
        }

        // 5. Bắt sự kiện Click vào nút
        holder.btnSelectTicket.setOnClickListener(v -> {
            if (listener != null) {
                // Hét lên: "Khách chọn vé này rồi, Activity tự xử lý tiếp đi!"
                listener.onSelect(ticket);
            }
        });
    }

    // RecyclerView cần biết có bao nhiêu item để nó vẽ thanh cuộn (Scrollbar) cho đúng tỷ lệ
    @Override
    public int getItemCount() {
        // Tránh lỗi Crash (NullPointerException) nếu listTickets bị truyền vào là null
        return listTickets != null ? listTickets.size() : 0;
    }

    // Mục đích duy nhất: Ánh xạ View (findViewById) MỘT LẦN DUY NHẤT và lưu lại.
    // Việc này giúp hàm onBindViewHolder phía trên chạy cực nhanh mà không phải tìm lại View trên file XML.
    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvPrice, tvOldPrice;
        TextView tvAvailableSeats;
        Button btnSelectTicket;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
            btnSelectTicket = itemView.findViewById(R.id.btnSelectTicket);
            tvAvailableSeats = itemView.findViewById(R.id.tvAvailableSeats);
        }
    }
}