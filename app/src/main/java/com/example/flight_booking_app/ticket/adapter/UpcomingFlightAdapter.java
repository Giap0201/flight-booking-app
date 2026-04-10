package com.example.flight_booking_app.ticket.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.activity.FlightDetailActivity;
import com.example.flight_booking_app.ticket.model.BookingSummary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingFlightAdapter extends RecyclerView.Adapter<UpcomingFlightAdapter.ViewHolder> {
    private List<BookingSummary> flights;

    public UpcomingFlightAdapter(List<BookingSummary> flights) {
        this.flights = flights;
    }

    // ĐÃ SỬA LỖI 1: Đổi tên hàm thành setTicketList để khớp với
    // UpcomingFlightsActivity
    public void setTicketList(List<BookingSummary> flights) {
        this.flights = flights;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket_upcoming, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingSummary flight = flights.get(position);

        // ==========================================
        // 1. LOGIC GOM NHÓM (BỘ LỌC) THEO NGÀY THÁNG
        // ==========================================
        String currentDateStr = getFormattedDateHeader(flight.getDepartureTime());
        String previousDateStr = position > 0 ? getFormattedDateHeader(flights.get(position - 1).getDepartureTime())
                : "";

        if (position == 0 || !currentDateStr.equals(previousDateStr)) {
            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(currentDateStr);
        } else {
            holder.tvDateHeader.setVisibility(View.GONE);
        }

        // 2. DỮ LIỆU CƠ BẢN TỪ BE
        holder.tvOriginCode.setText(flight.getOrigin() != null ? flight.getOrigin() : "N/A");
        holder.tvDestCode.setText(flight.getDestination() != null ? flight.getDestination() : "N/A");

        if (flight.getDepartureTime() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(flight.getDepartureTime());
                if (date != null) {
                    holder.tvDate.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date));
                }
            } catch (ParseException e) {
                holder.tvDate.setText(flight.getDepartureTime());
            }
        }

        // 3. GÁN DỮ LIỆU TỪ MÔ HÌNH NẾU CÓ, HOẶC MẶC ĐỊNH CHO DỮ LIỆU THIẾU
        if (flight.getClassType() != null) {
            holder.tvClass.setText(formatClass(flight.getClassType()));
        } else {
            holder.tvClass.setText("Economy");
        }

        int passengerCount = resolvePassengerCount(flight);
        holder.tvPassengerCount.setText(String.valueOf(passengerCount));

        if (flight.getArrivalTime() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date parsedDate = inputFormat.parse(flight.getArrivalTime());
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

        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            holder.tvDuration.setText(calculateDuration(flight.getDepartureTime(), flight.getArrivalTime()));
        } else {
            holder.tvDuration.setText("Chi tiết");
        }

        // 4. CHUYỂN SANG TRANG CHI TIẾT
        holder.itemView.setOnClickListener(v -> {
            if (flight.getId() != null) {
                Intent intent = new Intent(v.getContext(), FlightDetailActivity.class);
                intent.putExtra("BOOKING_ID", String.valueOf(flight.getId()));

                // === THÊM 3 DÒNG NÀY: GÓI DỮ LIỆU DỰ PHÒNG MANG THEO ===
                intent.putExtra("ORIGIN", flight.getOrigin());
                intent.putExtra("DEST", flight.getDestination());
                intent.putExtra("DEP_TIME", flight.getDepartureTime());
                // ========================================================

                v.getContext().startActivity(intent);
            }
        });
    }

    private String getFormattedDateHeader(String rawDate) {
        if (rawDate == null || rawDate.isEmpty())
            return "Ngày không xác định";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDate);
            return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return rawDate.split("T")[0];
        }
    }

    private String calculateDuration(String departure, String arrival) {
        if (departure == null || departure.trim().isEmpty() || arrival == null || arrival.trim().isEmpty())
            return "Chi tiết";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String safeDep = departure.length() > 19 ? departure.substring(0, 19) : departure;
            String safeArr = arrival.length() > 19 ? arrival.substring(0, 19) : arrival;

            Date depDate = format.parse(safeDep);
            Date arrDate = format.parse(safeArr);

            if (depDate != null && arrDate != null) {
                long diffInMillis = arrDate.getTime() - depDate.getTime();
                if (diffInMillis < 0)
                    return "Chi tiết";
                long hours = diffInMillis / (60 * 60 * 1000);
                long minutes = (diffInMillis / (60 * 1000)) % 60;
                if (minutes == 0)
                    return hours + "h";
                return hours + "h " + minutes + "m";
            }
        } catch (Exception e) {
            return "Chi tiết";
        }
        return "Chi tiết";
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

    private int resolvePassengerCount(BookingSummary booking) {
        if (booking == null) {
            return 0;
        }

        int passengersSize = booking.getPassengers() != null ? booking.getPassengers().size() : 0;
        int backendCount = booking.getPassengerCount() != null ? booking.getPassengerCount() : 0;

        return Math.max(passengersSize, backendCount);
    }

    @Override
    public int getItemCount() {
        return flights == null ? 0 : flights.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Khai báo thêm tvDateHeader
        TextView tvDateHeader, tvOriginCode, tvDestCode, tvDate, tvClass, tvPassengerCount, tvDestTime, tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvOriginCode = itemView.findViewById(R.id.tvOriginCode);
            tvDestCode = itemView.findViewById(R.id.tvDestCode);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvPassengerCount = itemView.findViewById(R.id.tvPassengerCount);
            tvDestTime = itemView.findViewById(R.id.tvDestTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}