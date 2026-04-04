package com.example.flight_booking_app.booking.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.model.AncillaryItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * ĐÃ NÂNG CẤP LÊN RECYCLERVIEW.ADAPTER
 * Giúp danh sách dịch vụ cuộn mượt mà hơn, không bị giật lag khi có nhiều mục.
 */
public class AncillaryAdapter extends RecyclerView.Adapter<AncillaryAdapter.AncillaryViewHolder> {

    // 1. Khai báo các biến dữ liệu cần thiết
    private Context context;
    private List<AncillaryItem> dsAncillary; // Danh sách các dịch vụ lấy từ API
    private String[] danhSachTenHanhKhach;   // Mảng chứa tên hành khách để hiện lên Dialog chọn người

    // Khai báo "người đưa thư" (Listener) để báo tin về cho Activity
    private OnAncillaryAddedListener listener;

    // Interface dùng để tạo kênh giao tiếp giữa Adapter và Activity
    public interface OnAncillaryAddedListener {
        // Hàm này sẽ mang theo Món dịch vụ được chọn + Vị trí của người được chọn bay về Activity
        void onAdded(AncillaryItem item, int passengerIndex);
    }

    // 2. Hàm khởi tạo (Constructor)
    public AncillaryAdapter(Context context, List<AncillaryItem> dsAncillary, String[] danhSachTenHanhKhach, OnAncillaryAddedListener listener) {
        this.context = context;
        this.dsAncillary = dsAncillary;
        this.danhSachTenHanhKhach = danhSachTenHanhKhach;
        this.listener = listener;
    }

    /**
     * BƯỚC 1: TẠO GIAO DIỆN CHO TỪNG ITEM (Chỉ chạy vài lần để lấp đầy màn hình)
     */
    @NonNull
    @Override
    public AncillaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Bơm" file XML giao diện (item_ancillary.xml) thành một View thực sự
        View view = LayoutInflater.from(context).inflate(R.layout.item_ancillary, parent, false);

        // Giao View này cho class AncillaryViewHolder ở dưới cùng quản lý
        return new AncillaryViewHolder(view);
    }

    /**
     * BƯỚC 2: ĐỔ DỮ LIỆU VÀ BẮT SỰ KIỆN (Chạy liên tục mỗi khi người dùng cuộn danh sách)
     */
    @Override
    public void onBindViewHolder(@NonNull AncillaryViewHolder holder, int position) {
        // Lấy ra thông tin dịch vụ ở vị trí hiện tại
        AncillaryItem ancillary = dsAncillary.get(position);

        // --- ĐỔ DỮ LIỆU LÊN GIAO DIỆN ---
        holder.tvAncillaryName.setText(ancillary.getName());

        // Xử lý logic hiển thị mô tả phụ tùy theo loại dịch vụ (Type) trả về từ API
        if ("BAGGAGE".equals(ancillary.getType())) {
            holder.tvAncillaryDescription.setText("Hành lý ký gửi");
        } else if ("MEAL".equals(ancillary.getType())) {
            holder.tvAncillaryDescription.setText("Suất ăn trên chuyến bay");
        } else {
            holder.tvAncillaryDescription.setText("Dịch vụ khác");
        }

        // Format tiền tệ thành chuẩn Việt Nam (VD: 250.000 đ)
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvAncillaryPrice.setText(formatter.format(ancillary.getPrice()) + " đ");

        // --- BẮT SỰ KIỆN KHI BẤM NÚT "THÊM" ---
        holder.btnAddAncillary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi bấm Thêm, tạo một hộp thoại (Dialog) để hỏi xem muốn mua cho ai
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Thêm dịch vụ cho hành khách nào?");

                // Truyền mảng tên hành khách vào Dialog
                builder.setItems(danhSachTenHanhKhach, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Biến 'which' chính là số thứ tự của hành khách (0, 1, 2...) mà người dùng vừa chọn

                        // Nếu "người đưa thư" (listener) đã được khởi tạo ở Activity
                        if (listener != null) {
                            // Thì hét lên cho Activity biết: "Ê, khách hàng vừa chọn gói [ancillary] cho người thứ [which] nè!"
                            listener.onAdded(ancillary, which);
                        }
                    }
                });

                // Hiển thị cái Dialog lên màn hình
                builder.show();
            }
        });
    }

    /**
     * BƯỚC 3: KHAI BÁO SỐ LƯỢNG PHẦN TỬ
     */
    @Override
    public int getItemCount() {
        // Nếu danh sách không rỗng thì trả về số lượng, nếu rỗng thì trả về 0 để tránh lỗi Crash
        return dsAncillary != null ? dsAncillary.size() : 0;
    }

    /**
     * LỚP VIEWHOLDER: Nhiệm vụ duy nhất là tìm (findViewById) các thành phần giao diện 1 LẦN DUY NHẤT.
     * Tránh việc hệ thống phải đi tìm lại giao diện liên tục gây giật lag app.
     */
    public static class AncillaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvAncillaryName;
        TextView tvAncillaryDescription;
        TextView tvAncillaryPrice;
        Button btnAddAncillary; // Dùng Button hoặc MaterialButton đều được

        public AncillaryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View từ file XML (item_ancillary.xml)
            tvAncillaryName = itemView.findViewById(R.id.tvAncillaryName);
            tvAncillaryDescription = itemView.findViewById(R.id.tvAncillaryDescription);
            tvAncillaryPrice = itemView.findViewById(R.id.tvAncillaryPrice);
            btnAddAncillary = itemView.findViewById(R.id.btnAddAncillary);
        }
    }
}