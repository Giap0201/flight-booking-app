//package com.example.flight_booking_app.home.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.flight_booking_app.R;
//import com.example.flight_booking_app.home.model.Flight;
//import java.util.ArrayList;
//import java.util.List;
//
//public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {
//
//    // =========================================================
//    // 1. KHAI BÁO INTERFACE ĐỂ BẮT SỰ KIỆN CLICK
//    // =========================================================
//    public interface OnItemClickListener {
//        void onClick(Flight flight);
//    }
//
//    private List<Flight> flightList = new ArrayList<>();
//    private OnItemClickListener listener; // Biến giữ cái "bộ đàm"
//
//    // =========================================================
//    // 2. CONSTRUCTOR MỚI (Bắt buộc Activity phải truyền listener vào)
//    // =========================================================
//    public FlightAdapter(OnItemClickListener listener) {
//        this.listener = listener;
//    }
//
//    // Hàm này để Activity truyền data mới vào Adapter
//    public void setFlights(List<Flight> flights) {
//        this.flightList = flights;
//        notifyDataSetChanged(); // Yêu cầu vẽ lại danh sách
//    }
//
//    @NonNull
//    @Override
//    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight, parent, false);
//        return new FlightViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
//        Flight flight = flightList.get(position);
//
//        // Gắn tên hãng bay + Số hiệu chuyến bay
//        String airlineInfo = flight.getAirlineName() + " (" + flight.getFlightNumber() + ")";
//        holder.tvAirline.setText(airlineInfo);
//
//        // Format Thời gian
//        String depTime = formatTime(flight.getDepartureTime());
//        String arrTime = formatTime(flight.getArrivalTime());
//        holder.tvTime.setText(depTime + " - " + arrTime);
//
//        // Lấy Giá vé từ mảng Classes
//        if (flight.getClasses() != null && !flight.getClasses().isEmpty()) {
//            double price = flight.getClasses().get(0).getBasePrice();
//            String formattedPrice = String.format("%,.0f VND", price);
//            holder.tvPrice.setText(formattedPrice);
//        } else {
//            holder.tvPrice.setText("Hết vé");
//        }
//
//        // =========================================================
//        // 3. BẮT SỰ KIỆN KHÁCH HÀNG BẤM VÀO THẺ CHUYẾN BAY
//        // =========================================================
//        holder.itemView.setOnClickListener(v -> {
//            if (listener != null) {
//                // Ném cái object flight ra ngoài cho Activity xử lý
//                listener.onClick(flight);
//            }
//        });
//    }
//
//    // Hàm hỗ trợ format chuỗi thời gian ISO 8601 sang Giờ:Phút
//    private String formatTime(String isoTime) {
//        if (isoTime == null || isoTime.isEmpty()) return "--:--";
//        try {
//            int tIndex = isoTime.indexOf('T');
//            if (tIndex != -1 && isoTime.length() > tIndex + 6) {
//                return isoTime.substring(tIndex + 1, tIndex + 6);
//            }
//            return isoTime;
//        } catch (Exception e) {
//            return "--:--";
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return flightList != null ? flightList.size() : 0;
//    }
//
//    // Lớp "giữ chỗ" cho các view trong item_flight.xml
//    static class FlightViewHolder extends RecyclerView.ViewHolder {
//        TextView tvAirline, tvTime, tvPrice;
//
//        public FlightViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvAirline = itemView.findViewById(R.id.tvAirline);
//            tvTime = itemView.findViewById(R.id.tvTime);
//            tvPrice = itemView.findViewById(R.id.tvPrice);
//        }
//    }
//}