package com.example.flight_booking_app.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.home.model.AirportTranslation;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<AirportTranslation> airportList;
    private OnItemClickListener listener;

    // Interface để bắt sự kiện click
    public interface OnItemClickListener {
        void onItemClick(AirportTranslation selectedAirport);
    }

    // Constructor
    public LocationAdapter(List<AirportTranslation> airportList, OnItemClickListener listener) {
        this.airportList = airportList;
        this.listener = listener;
    }

    // Hàm cập nhật danh sách khi tìm kiếm
    public void setFilter(List<AirportTranslation> filteredList) {
        this.airportList = filteredList;
        notifyDataSetChanged(); // Cập nhật lại giao diện
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        AirportTranslation airport = airportList.get(position);

        holder.tvCode.setText(airport.getCode() != null ? airport.getCode() : "N/A");
        holder.tvName.setText(airport.getName());
        holder.tvCityCountry.setText(airport.getCity() + ", " + airport.getCountry());

        // Gắn sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(airport));
    }

    @Override
    public int getItemCount() {
        return airportList != null ? airportList.size() : 0;
    }

    // ViewHolder class
    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvCityCountry;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvItemAirportCode);
            tvName = itemView.findViewById(R.id.tvItemAirportName);
            tvCityCountry = itemView.findViewById(R.id.tvItemCityCountry);
        }
    }
}
