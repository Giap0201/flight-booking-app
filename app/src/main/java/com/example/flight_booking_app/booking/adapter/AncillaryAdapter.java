package com.example.flight_booking_app.booking.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.booking.model.AncillaryItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * ADAPTER QUẢN LÝ DANH SÁCH DỊCH VỤ (Hành lý, suất ăn...)
 * Đã được nâng cấp để hỗ trợ Khứ hồi (Tab chiều đi/về) và tính năng Hủy dịch vụ.
 */
public class AncillaryAdapter extends RecyclerView.Adapter<AncillaryAdapter.AncillaryViewHolder> {

    // --- 1. KHAI BÁO CÁC BIẾN DỮ LIỆU ---
    private Context context;
    private List<AncillaryItem> dsAncillary;
    private String[] danhSachTenHanhKhach;

    // ⚡ MỚI THÊM: Biến lưu trữ xem người dùng đang ở Tab nào (1: Chiều đi, 2: Chiều về)
    private int currentSegment = 1;

    // ⚡ ĐÃ SỬA: Đổi tên Listener để phản ánh việc nó có thể lắng nghe cả Thêm và Hủy
    private OnAncillaryChangeListener listener;

    // Interface tạo kênh giao tiếp giữa Adapter và Activity
    public interface OnAncillaryChangeListener {
        // Hàm báo cáo khi khách bấm "Thêm"
        void onAdded(AncillaryItem item, int passengerIndex);
        // Hàm báo cáo khi khách bấm "Hủy"
        void onRemoved(AncillaryItem item, int passengerIndex);
    }

    // --- 2. HÀM KHỞI TẠO (CONSTRUCTOR) ---
    public AncillaryAdapter(Context context, List<AncillaryItem> dsAncillary, String[] danhSachTenHanhKhach, OnAncillaryChangeListener listener) {
        this.context = context;
        this.dsAncillary = dsAncillary;
        this.danhSachTenHanhKhach = danhSachTenHanhKhach;
        this.listener = listener;
    }

    // ⚡ MỚI THÊM: Hàm này được Activity gọi khi người dùng bấm chuyển Tab (Chiều đi / Chiều về)
    public void setCurrentSegment(int segmentNo) {
        this.currentSegment = segmentNo;
    }

    /**
     * BƯỚC 1: TẠO GIAO DIỆN CHO TỪNG ITEM (Chỉ chạy vài lần để lấp đầy màn hình)
     */
    @NonNull
    @Override
    public AncillaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Bơm" file XML giao diện thành một View
        View view = LayoutInflater.from(context).inflate(R.layout.item_ancillary, parent, false);
        return new AncillaryViewHolder(view);
    }

    /**
     * BƯỚC 2: ĐỔ DỮ LIỆU VÀ BẮT SỰ KIỆN (Chạy liên tục mỗi khi cuộn danh sách)
     */
    @Override
    public void onBindViewHolder(@NonNull AncillaryViewHolder holder, int position) {
        // Lấy ra dịch vụ ở vị trí hiện tại
        AncillaryItem ancillary = dsAncillary.get(position);

        // --- ĐỔ DỮ LIỆU LÊN GIAO DIỆN ---
        holder.tvAncillaryName.setText(ancillary.getName());

        // Phân loại mô tả hiển thị
        if ("BAGGAGE".equals(ancillary.getType())) {
            holder.tvAncillaryDescription.setText("Hành lý ký gửi");
        } else if ("MEAL".equals(ancillary.getType())) {
            holder.tvAncillaryDescription.setText("Suất ăn trên chuyến bay");
        } else {
            holder.tvAncillaryDescription.setText("Dịch vụ khác");
        }

        // Format tiền tệ chuẩn VN
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvAncillaryPrice.setText(formatter.format(ancillary.getPrice()) + " đ");

        // --- BẮT SỰ KIỆN KHI BẤM NÚT CHỌN DỊCH VỤ ---
        holder.btnAddAncillary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TẠO HỘP THOẠI 1: CHỌN HÀNH KHÁCH
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                // Hiển thị tiêu đề động theo Tab đang chọn cho rõ ràng
                String chieuBay = (currentSegment == 1) ? "Chiều đi" : "Chiều về";
                builder.setTitle("Chọn hành khách (" + chieuBay + ")");

                builder.setItems(danhSachTenHanhKhach, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // which: là vị trí hành khách được chọn (0, 1, 2...)

                        // ⚡ MỚI THÊM: TẠO HỘP THOẠI 2: HỎI THÊM HAY HỦY?
                        // Chỗ này giúp người dùng có quyền rút lại quyết định nếu lỡ bấm nhầm
                        AlertDialog.Builder actionBuilder = new AlertDialog.Builder(context);
                        actionBuilder.setTitle("Thao tác");
                        actionBuilder.setMessage("Bạn muốn Thêm hay Hủy [" + ancillary.getName() + "] cho " + danhSachTenHanhKhach[which] + "?");

                        // Nút THÊM
                        actionBuilder.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (listener != null) {
                                    // Báo về Activity là hãy THÊM dịch vụ này
                                    listener.onAdded(ancillary, which);
                                }
                            }
                        });

                        // Nút HỦY
                        actionBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (listener != null) {
                                    // Báo về Activity là hãy XÓA dịch vụ này khỏi danh sách
                                    listener.onRemoved(ancillary, which);
                                }
                            }
                        });

                        // Nút BỎ QUA (Tắt popup)
                        actionBuilder.setNeutralButton("Đóng", null);

                        actionBuilder.show(); // Hiển thị hộp thoại 2
                    }
                });

                builder.show(); // Hiển thị hộp thoại 1
            }
        });
    }

    /**
     * BƯỚC 3: KHAI BÁO SỐ LƯỢNG PHẦN TỬ
     */
    @Override
    public int getItemCount() {
        return dsAncillary != null ? dsAncillary.size() : 0;
    }

    /**
     * LỚP VIEWHOLDER: Tìm và lưu trữ các thành phần giao diện
     */
    public static class AncillaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvAncillaryName;
        TextView tvAncillaryDescription;
        TextView tvAncillaryPrice;
        Button btnAddAncillary;

        public AncillaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAncillaryName = itemView.findViewById(R.id.tvAncillaryName);
            tvAncillaryDescription = itemView.findViewById(R.id.tvAncillaryDescription);
            tvAncillaryPrice = itemView.findViewById(R.id.tvAncillaryPrice);
            btnAddAncillary = itemView.findViewById(R.id.btnAddAncillary);
        }
    }
}