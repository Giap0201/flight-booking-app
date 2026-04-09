package com.example.flight_booking_app.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.home.model.Destination;
import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {
    private List<Destination> destinationList;

    public DestinationAdapter(List<Destination> destinationList) {
        this.destinationList = destinationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_destination_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination dest = destinationList.get(position);
        holder.tvDestName.setText(dest.getName());
//        holder.tvDestPrice.setText(dest.getPrice());

        // Dùng Glide để load ảnh từ link URL
        Glide.with(holder.itemView.getContext())
                .load(dest.getImageUrl())
                .placeholder(android.R.color.darker_gray) // Màu xám trong lúc chờ tải
                .error(android.R.drawable.ic_dialog_alert) // Hiện icon cảnh báo nếu link hỏng/mất mạng
                .into(holder.imgDestination);
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDestination;
        TextView tvDestName;

        ViewHolder(View itemView) {
            super(itemView);
            imgDestination = itemView.findViewById(R.id.imgDestination);
            tvDestName = itemView.findViewById(R.id.tvDestName);
//            tvDestPrice = itemView.findViewById(R.id.tvDestPrice);
        }
    }
}