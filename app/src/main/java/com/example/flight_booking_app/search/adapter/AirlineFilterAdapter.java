package com.example.flight_booking_app.search.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.search.model.Airline;
import com.google.android.material.checkbox.MaterialCheckBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AirlineFilterAdapter extends RecyclerView.Adapter<AirlineFilterAdapter.ViewHolder> {

    private List<Airline> airlines = new ArrayList<>();
    private final Set<String> selectedCodes;

    private List<Airline> airlinesFull = new ArrayList<>(); // Danh sách gốc từ API

    public AirlineFilterAdapter(Set<String> selectedCodes) {
        this.selectedCodes = selectedCodes;
    }

    public void setAirlines(List<Airline> airlines) {
        this.airlines = airlines;
        this.airlinesFull = new ArrayList<>(airlines); // Sao lưu lại
        notifyDataSetChanged();
    }

    public void filter(String query) {
        List<Airline> filteredList = new ArrayList<>();
        for (Airline item : airlinesFull) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        this.airlines = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_airline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Airline item = airlines.get(position);
        holder.tvName.setText(item.getName());

        holder.cb.setChecked(selectedCodes.contains(item.getName()));

        holder.itemView.setOnClickListener(v -> {
            if (selectedCodes.contains(item.getName())) {
                selectedCodes.remove(item.getName());
            } else {
                selectedCodes.add(item.getName());
            }
            notifyItemChanged(position);
        });

        holder.cb.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return airlines != null ? airlines.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        MaterialCheckBox cb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAirlineName);
            cb = itemView.findViewById(R.id.cbAirline);
        }
    }
}