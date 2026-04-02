package com.example.flight_booking_app.booking.adapter;

import android.os.CountDownTimer;
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
        // Lưu ý: Trong file XML tôi viết tạm là 1 TextView chứa cả chữ "Order ID ...".
        // Lát nữa bạn cần vào file item_ticket_pending.xml tách ra thành 2 TextView (1 cái chữ tĩnh "Order ID", 1 cái ID linh động)
        // hoặc dùng logic bên dưới để nối chuỗi:
        TextView tvOrderId = holder.itemView.findViewById(R.id.tvOrderId); // Cần thêm ID này trong XML
        if (tvOrderId != null) {
            tvOrderId.setText("Order ID  " + ticket.getPnrCode());
        }

        // 2. Set Thông tin cơ bản (Ngày bay, Hạng vé)
        if (ticket.getDepartureTime() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(ticket.getDepartureTime());

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
                String formattedDate = dateFormat.format(date);

                // Tạm thời hardcode Class vì API chưa trả về
                holder.tvFlightShortDesc.setText(formattedDate + " • Economy Class");
            } catch (ParseException e) {
                holder.tvFlightShortDesc.setText(ticket.getDepartureTime());
            }
        }

        // 3. Xử lý logic đếm ngược (Countdown Timer)
        // Giả sử API trả về thời gian tạo đơn (createdAt), ta cho người dùng 30 phút để thanh toán.
        if (ticket.getCreatedAt() != null) {
            startCountdownTimer(holder.tvTimer, ticket.getCreatedAt());
        }
    }

    private void startCountdownTimer(TextView tvTimer, String createdAtIso) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date createdAtDate = format.parse(createdAtIso);

            if (createdAtDate == null) return;

            // Tính thời gian hết hạn (Ví dụ: CreatedAt + 30 phút)
            long expireTimeMillis = createdAtDate.getTime() + (30 * 60 * 1000);
            long currentTimeMillis = System.currentTimeMillis();
            long timeLeftMillis = expireTimeMillis - currentTimeMillis;

            if (timeLeftMillis > 0) {
                // Hủy timer cũ nếu view được tái sử dụng trong RecyclerView
                if (tvTimer.getTag() instanceof CountDownTimer) {
                    ((CountDownTimer) tvTimer.getTag()).cancel();
                }

                CountDownTimer timer = new CountDownTimer(timeLeftMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // Chuyển đổi ms sang HH:mm:ss
                        long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                        long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                        long seconds = (millisUntilFinished / 1000) % 60;

                        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                        tvTimer.setText("⚠ remaining payment period        " + timeLeftFormatted);
                    }

                    @Override
                    public void onFinish() {
                        tvTimer.setText("⚠ payment period expired");
                        // Tùy chọn: Đổi màu chữ/nền sang xám báo hiệu hết hạn
                    }
                }.start();

                // Lưu reference của timer vào tag của view để quản lý
                tvTimer.setTag(timer);

            } else {
                tvTimer.setText("⚠ payment period expired");
            }

        } catch (ParseException e) {
            e.printStackTrace();
            tvTimer.setText("⚠ Timer Error");
        }
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    static class PendingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimer, tvFlightShortDesc;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Tìm các View dựa trên ID trong file item_ticket_pending.xml (Step 2)
            tvTimer = itemView.findViewById(R.id.tvTimer);
            tvFlightShortDesc = itemView.findViewById(R.id.tvFlightShortDesc);

            // Lưu ý: Bạn cần mở file layout item_ticket_pending.xml và gán android:id="@+id/tvOrderId"
            // cho cái TextView đang chứa chữ "Order ID  TBW7FWZ" nhé.
        }
    }
}