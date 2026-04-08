package com.example.flight_booking_app.home.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.home.model.TravelGuide;
import java.util.List;

public class TravelGuideAdapter extends RecyclerView.Adapter<TravelGuideAdapter.ViewHolder> {

    private List<TravelGuide> guideList;

    public TravelGuideAdapter(List<TravelGuide> guideList) {
        this.guideList = guideList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_guide, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelGuide guide = guideList.get(position);

        holder.tvTitle.setText(guide.getTitle());
        holder.tvCategory.setText(guide.getCategory());
        holder.tvDate.setText(guide.getDate());

        // Load ảnh bằng Glide
        Glide.with(holder.itemView.getContext())
                .load(guide.getImageUrl())
                .placeholder(android.R.color.darker_gray)
                .into(holder.imgGuide);

        // Xử lý khi bấm vào bài báo -> Mở trình duyệt Web
        holder.itemView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(guide.getArticleUrl()));
            holder.itemView.getContext().startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return guideList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGuide;
        TextView tvTitle, tvCategory, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            imgGuide = itemView.findViewById(R.id.imgGuide);
            tvTitle = itemView.findViewById(R.id.tvGuideTitle);
            tvCategory = itemView.findViewById(R.id.tvGuideCategory);
            tvDate = itemView.findViewById(R.id.tvGuideDate);
        }
    }
}