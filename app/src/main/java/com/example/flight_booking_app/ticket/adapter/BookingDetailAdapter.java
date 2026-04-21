package com.example.flight_booking_app.ticket.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.ticket.model.BookingDetailItem;
import com.example.flight_booking_app.ticket.response.client.BookingDetailResponse;
import com.example.flight_booking_app.ticket.response.client.PassengerTicketResponse;
import com.example.flight_booking_app.ticket.response.client.TicketDetailResponse;
import com.example.flight_booking_app.ticket.response.client.TransactionResponse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * =====================================================================================
 * BookingDetailAdapter — Adapter đa kiểu (multi-ViewType) cho RecyclerView phẳng hóa.
 * =====================================================================================
 *
 * Adapter này xử lý danh sách List<BookingDetailItem> đã được phẳng hóa bởi ViewModel.
 * Mỗi item trong danh sách có 1 trường `type` (int) để quyết định:
 *   1. Layout nào được inflate (onCreateViewHolder)
 *   2. Dữ liệu nào được bind (onBindViewHolder)
 *
 * CÁCH HOẠT ĐỘNG:
 * ┌─────────────────────────────────────────────────────────┐
 * │ getItemViewType(position)                               │
 * │   → Trả về item.getType() (VD: TYPE_HEADER_INFO = 0)   │
 * │                                                         │
 * │ onCreateViewHolder(parent, viewType)                    │
 * │   → switch(viewType) để inflate đúng layout XML         │
 * │                                                         │
 * │ onBindViewHolder(holder, position)                      │
 * │   → Kiểm tra holder instanceof để gọi đúng bind method │
 * └─────────────────────────────────────────────────────────┘
 *
 * 7 LOẠI VIEWTYPE:
 *   0 = TYPE_HEADER_INFO    → item_bd_header.xml
 *   1 = TYPE_FLIGHT_INFO    → item_bd_flight.xml
 *   2 = TYPE_SECTION_TITLE  → item_bd_section_title.xml
 *   3 = TYPE_PASSENGER      → item_bd_passenger.xml
 *   4 = TYPE_TICKET         → item_bd_ticket.xml
 *   5 = TYPE_PRICE_SUMMARY  → item_bd_price.xml
 *   6 = TYPE_TRANSACTION    → item_bd_transaction.xml
 * =====================================================================================
 */
public class BookingDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ======================== CALLBACK INTERFACE ========================
    /**
     * Interface callback để Activity xử lý khi người dùng nhấn nút "Thanh toán".
     * Activity sẽ implement interface này và gọi ViewModel để lấy payment URL.
     */
    public interface OnPayNowClickListener {
        /**
         * @param bookingId Mã booking (UUID) cần thanh toán
         */
        void onPayNowClick(String bookingId);
    }

    private List<BookingDetailItem> items;
    private OnPayNowClickListener payNowClickListener;

    // Format tiền VND dùng chung
    private final NumberFormat formatVND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public BookingDetailAdapter(List<BookingDetailItem> items) {
        this.items = items;
    }

    /**
     * Đăng ký listener cho nút "Thanh toán". Gọi từ Activity.
     */
    public void setOnPayNowClickListener(OnPayNowClickListener listener) {
        this.payNowClickListener = listener;
    }

    /**
     * Cập nhật toàn bộ danh sách và làm mới RecyclerView.
     */
    public void setItems(List<BookingDetailItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // ======================== getItemViewType ========================
    // Đây là hàm THEN CHỐT: RecyclerView dùng giá trị trả về của hàm này
    // để quyết định tạo ViewHolder nào trong onCreateViewHolder.
    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    // ======================== onCreateViewHolder ========================
    // Tạo ViewHolder phù hợp dựa trên viewType.
    // Mỗi viewType → inflate 1 layout XML riêng → bọc trong ViewHolder riêng.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case BookingDetailItem.TYPE_HEADER_INFO:
                return new HeaderViewHolder(
                        inflater.inflate(R.layout.item_bd_header, parent, false));

            case BookingDetailItem.TYPE_FLIGHT_INFO:
                return new FlightViewHolder(
                        inflater.inflate(R.layout.item_bd_flight, parent, false));

            case BookingDetailItem.TYPE_SECTION_TITLE:
                return new SectionTitleViewHolder(
                        inflater.inflate(R.layout.item_bd_section_title, parent, false));

            case BookingDetailItem.TYPE_PASSENGER:
                return new PassengerViewHolder(
                        inflater.inflate(R.layout.item_bd_passenger, parent, false));

            case BookingDetailItem.TYPE_TICKET:
                return new TicketViewHolder(
                        inflater.inflate(R.layout.item_bd_ticket, parent, false));

            case BookingDetailItem.TYPE_PRICE_SUMMARY:
                return new PriceViewHolder(
                        inflater.inflate(R.layout.item_bd_price, parent, false));

            case BookingDetailItem.TYPE_TRANSACTION:
                return new TransactionViewHolder(
                        inflater.inflate(R.layout.item_bd_transaction, parent, false));

            case BookingDetailItem.TYPE_BOARDING_PASS:
                return new BoardingPassViewHolder(
                        inflater.inflate(R.layout.item_bd_boarding_pass, parent, false));

            default:
                // Fallback — không bao giờ xảy ra nếu code đúng
                return new SectionTitleViewHolder(
                        inflater.inflate(R.layout.item_bd_section_title, parent, false));
        }
    }

    // ======================== onBindViewHolder ========================
    // Gán dữ liệu vào ViewHolder. Kiểm tra instanceof để gọi đúng bind method.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BookingDetailItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            bindHeader((HeaderViewHolder) holder, item.getBookingData());

        } else if (holder instanceof FlightViewHolder) {
            bindFlight((FlightViewHolder) holder, item.getBookingData());

        } else if (holder instanceof SectionTitleViewHolder) {
            ((SectionTitleViewHolder) holder).tvTitle.setText(item.getSectionTitle());

        } else if (holder instanceof PassengerViewHolder) {
            bindPassenger((PassengerViewHolder) holder, item.getPassengerData(), item.getPassengerIndex());

        } else if (holder instanceof TicketViewHolder) {
            bindTicket((TicketViewHolder) holder, item.getTicketData());

        } else if (holder instanceof PriceViewHolder) {
            bindPrice((PriceViewHolder) holder, item.getBookingData());

        } else if (holder instanceof TransactionViewHolder) {
            bindTransaction((TransactionViewHolder) holder, item.getTransactionData());

        } else if (holder instanceof BoardingPassViewHolder) {
            bindBoardingPass((BoardingPassViewHolder) holder, item);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // ======================== BIND METHODS ========================

    /**
     * Bind dữ liệu cho Header (PNR, status, contact).
     */
    private void bindHeader(HeaderViewHolder h, BookingDetailResponse data) {
        if (data == null) return;

        // PNR Code
        h.tvPnrCode.setText("PNR: " + (data.getPnrCode() != null ? data.getPnrCode() : "N/A"));

        // Status badge (Việt hoá)
        setupStatusBadge(h.tvStatus, data.getStatus());

        // Contact info
        if (data.getContact() != null) {
            h.tvContactName.setText("👤 " + safeStr(data.getContact().getName()));
            h.tvContactEmail.setText("✉ " + safeStr(data.getContact().getEmail()));
            h.tvContactPhone.setText("📞 " + safeStr(data.getContact().getPhone()));
        } else {
            h.tvContactName.setText("👤 Không có thông tin");
            h.tvContactEmail.setVisibility(View.GONE);
            h.tvContactPhone.setVisibility(View.GONE);
        }

        // ======================== NÚT THANH TOÁN ========================
        // Chỉ hiển thị khi status là PENDING hoặc AWAITING_PAYMENT
        String status = data.getStatus() != null ? data.getStatus().toUpperCase() : "";
        boolean showPayButton = status.equals("PENDING") || status.equals("AWAITING_PAYMENT");

        h.btnPayNow.setVisibility(showPayButton ? View.VISIBLE : View.GONE);
        h.dividerPayment.setVisibility(showPayButton ? View.VISIBLE : View.GONE);

        if (showPayButton) {
            h.btnPayNow.setOnClickListener(v -> {
                if (payNowClickListener != null && data.getId() != null) {
                    payNowClickListener.onPayNowClick(data.getId());
                }
            });
        }
    }

    /**
     * Bind dữ liệu cho Flight Info Card.
     * Lấy thông tin chuyến bay từ vé đầu tiên của hành khách đầu tiên.
     */
    private void bindFlight(FlightViewHolder h, BookingDetailResponse data) {
        if (data == null || data.getPassengers() == null || data.getPassengers().isEmpty()) {
            h.tvDepTime.setText("--:--");
            h.tvArrTime.setText("--:--");
            h.tvDuration.setText("Chi tiết");
            h.tvOrigin.setText("N/A");
            h.tvDest.setText("N/A");
            h.tvAirline.setText("✈ Flight");
            return;
        }

        // Lấy vé đầu tiên để hiển thị route chính
        PassengerTicketResponse firstPax = data.getPassengers().get(0);
        if (firstPax.getTickets() != null && !firstPax.getTickets().isEmpty()) {
            TicketDetailResponse t = firstPax.getTickets().get(0);

            h.tvDepTime.setText(formatTime(t.getDepartureTime()));
            h.tvArrTime.setText(formatTime(t.getArrivalTime()));
            h.tvDuration.setText(calculateDuration(t.getDepartureTime(), t.getArrivalTime()));
            h.tvOrigin.setText(safeStr(t.getDepartureAirport()) + " Airport");
            h.tvDest.setText(safeStr(t.getArrivalAirport()) + " Airport");

            String className = t.getClassType() != null ? formatClass(t.getClassType()) : "Economy";
            h.tvAirline.setText("✈ " + safeStr(t.getFlightNumber()) + " • " + className);
        }
    }

    /**
     * Bind dữ liệu cho Passenger item.
     */
    private void bindPassenger(PassengerViewHolder h, PassengerTicketResponse p, int index) {
        if (p == null) return;

        // Tên đầy đủ với số thứ tự
        String fullName = p.getFullName();
        h.tvPassengerLabel.setText("#" + index + " — " + fullName);

        // ID rút gọn
        String id = p.getPassengerId() != null ? p.getPassengerId() : "N/A";
        String shortId = id.length() > 8 ? id.substring(0, 8).toUpperCase() : id.toUpperCase();
        h.tvPassengerId.setText("ID: " + shortId);

        // Badge loại hành khách (Việt hoá)
        setupPassengerBadge(h.tvPassengerBadge, p.getType());
    }

    /**
     * Bind dữ liệu cho Ticket item.
     */
    private void bindTicket(TicketViewHolder h, TicketDetailResponse t) {
        if (t == null) return;

        // Ticket number
        h.tvTicketNumber.setText("🎫 " + safeStr(t.getTicketNumber()));

        // Status badge
        setupTicketStatusBadge(h.tvTicketStatus, t.getStatus());

        // Flight + Class
        String className = t.getClassType() != null ? formatClass(t.getClassType()) : "Economy";
        h.tvFlightAndClass.setText("✈ " + safeStr(t.getFlightNumber()) + " • " + className);

        // Route
        h.tvRoute.setText(safeStr(t.getDepartureAirport()) + " → " + safeStr(t.getArrivalAirport()));

        // Times
        h.tvTicketTime.setText(formatTime(t.getDepartureTime()) + " → " + formatTime(t.getArrivalTime()));

        // Seat
        String seat = (t.getSeatNumber() != null && !t.getSeatNumber().isEmpty())
                ? t.getSeatNumber() : "TBD";
        h.tvSeat.setText("Ghế: " + seat);

        // Price
        h.tvTicketAmount.setText(formatVND.format(BigDecimal.valueOf(t.getTotalAmount())));
    }

    /**
     * Bind dữ liệu cho Price Summary.
     * Logic tính giá giống hệt FlightDetailActivity.calculatePriceBreakdown().
     */
    private void bindPrice(PriceViewHolder h, BookingDetailResponse data) {
        if (data == null) return;

        int adultQty = 0, childQty = 0, infantQty = 0;
        BigDecimal adultSum = BigDecimal.ZERO, childSum = BigDecimal.ZERO, infantSum = BigDecimal.ZERO;

        if (data.getPassengers() != null) {
            for (PassengerTicketResponse p : data.getPassengers()) {
                BigDecimal passengerTotalCost = BigDecimal.ZERO;
                if (p.getTickets() != null) {
                    for (TicketDetailResponse ticket : p.getTickets()) {
                        passengerTotalCost = passengerTotalCost.add(
                                BigDecimal.valueOf(ticket.getTotalAmount()));
                    }
                }

                String type = p.getType() != null ? p.getType().toUpperCase() : "ADULT";

                if (type.contains("ADULT")) {
                    adultQty++;
                    adultSum = adultSum.add(passengerTotalCost);
                } else if (type.contains("CHILD")) {
                    childQty++;
                    childSum = childSum.add(passengerTotalCost);
                } else {
                    infantQty++;
                    infantSum = infantSum.add(passengerTotalCost);
                }
            }
        }

        // Cập nhật UI
        updatePriceRow(h.layoutAdultPrice, h.tvAdultCount, h.tvAdultSum,
                adultQty, "Người lớn", adultSum);
        updatePriceRow(h.layoutChildPrice, h.tvChildCount, h.tvChildSum,
                childQty, "Trẻ em", childSum);
        updatePriceRow(h.layoutInfantPrice, h.tvInfantCount, h.tvInfantSum,
                infantQty, "Em bé", infantSum);

        // Tổng cộng
        BigDecimal grandTotal = adultSum.add(childSum).add(infantSum);
        h.tvTotalPrice.setText(formatVND.format(grandTotal));
    }

    /**
     * Bind dữ liệu cho Transaction item.
     */
    private void bindTransaction(TransactionViewHolder h, TransactionResponse tx) {
        if (tx == null) return;

        h.tvTransactionNo.setText(safeStr(tx.getTransactionNo()));
        h.tvPaymentMethod.setText("💳 " + safeStr(tx.getPaymentMethod()));
        h.tvTransactionDate.setText(formatDateTime(tx.getCreatedAt()));
        h.tvTransactionAmount.setText(formatVND.format(BigDecimal.valueOf(tx.getAmount())));

        // Status badge
        setupTransactionStatusBadge(h.tvTransactionStatus, tx.getStatus());
    }

    /**
     * Bind dữ liệu Boarding Pass và tạo QR Code bằng thư viện ZXing.
     */
    private void bindBoardingPass(BoardingPassViewHolder h, BookingDetailItem item) {
        h.tvBpPassengerName.setText(safeStr(item.getBpPassengerName()));
        h.tvBpFlightInfo.setText("✈ " + safeStr(item.getBpFlightNumber()) + " | " +
                safeStr(item.getBpDepartureAirport()) + " → " + safeStr(item.getBpArrivalAirport()));
        h.tvBpDate.setText(formatDateTime(item.getBpDepartureTime()));
        h.tvBpPnr.setText(safeStr(item.getBpPnrCode()));
        
        String seat = (item.getBpSeatNumber() != null && !item.getBpSeatNumber().isEmpty()) ? item.getBpSeatNumber() : "TBD";
        h.tvBpSeat.setText(seat);
        h.tvBpTicket.setText(safeStr(item.getBpTicketNumber()));

        // Tạo chuỗi JSON nhỏ gọn chứa thông tin vé
        String qrContent = String.format(Locale.US,
                "{\"PNR\":\"%s\",\"Tkt\":\"%s\",\"Seat\":\"%s\",\"Flight\":\"%s\",\"Date\":\"%s\"}",
                item.getBpPnrCode(), item.getBpTicketNumber(), seat,
                item.getBpFlightNumber(), item.getBpDepartureTime());

        // Sử dụng ZXing để tạo BitMatrix và vẽ ra Bitmap
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            h.ivQrCode.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
            h.ivQrCode.setBackgroundColor(Color.LTGRAY);
        }
    }

    // ======================== HELPER METHODS ========================

    // --- Hiện/ẩn dòng giá ---
    private void updatePriceRow(View layout, TextView tvCount, TextView tvSum,
                                int qty, String label, BigDecimal sum) {
        if (qty > 0) {
            layout.setVisibility(View.VISIBLE);
            tvCount.setText(qty + " " + label);
            tvSum.setText(formatVND.format(sum));
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    // --- Badge trạng thái booking (Việt hoá) ---
    private void setupStatusBadge(TextView tvBadge, String status) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);
        if (status == null) status = "";

        switch (status.toUpperCase()) {
            case "PAID":
            case "CONFIRMED":
                shape.setColor(Color.parseColor("#E8F5E9"));
                tvBadge.setTextColor(Color.parseColor("#2E7D32"));
                tvBadge.setText("Đã thanh toán");
                break;
            case "PENDING":
            case "AWAITING_PAYMENT":
                shape.setColor(Color.parseColor("#FFF3E0"));
                tvBadge.setTextColor(Color.parseColor("#E65100"));
                tvBadge.setText("Chờ thanh toán");
                break;
            case "CANCELLED":
            case "REFUNDED":
                shape.setColor(Color.parseColor("#FFEBEE"));
                tvBadge.setTextColor(Color.parseColor("#C62828"));
                tvBadge.setText("Đã huỷ");
                break;
            case "COMPLETED":
                shape.setColor(Color.parseColor("#E8F5E9"));
                tvBadge.setTextColor(Color.parseColor("#2E7D32"));
                tvBadge.setText("Hoàn thành");
                break;
            default:
                shape.setColor(Color.parseColor("#F5F5F5"));
                tvBadge.setTextColor(Color.parseColor("#9E9E9E"));
                tvBadge.setText(status);
                break;
        }
        tvBadge.setBackground(shape);
    }

    // --- Badge loại hành khách (Việt hoá) — logic giống PassengerAdapter ---
    private void setupPassengerBadge(TextView tvBadge, String type) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);
        String pType = type != null ? type.toUpperCase() : "ADULT";

        if (pType.contains("ADULT")) {
            shape.setColor(Color.parseColor("#E8F0FE"));
            tvBadge.setTextColor(Color.parseColor("#1A73E8"));
            tvBadge.setText("Người lớn");
        } else if (pType.contains("CHILD")) {
            shape.setColor(Color.parseColor("#FCE8E6"));
            tvBadge.setTextColor(Color.parseColor("#D93025"));
            tvBadge.setText("Trẻ em");
        } else if (pType.contains("INFANT")) {
            shape.setColor(Color.parseColor("#FEF3C7"));
            tvBadge.setTextColor(Color.parseColor("#D97706"));
            tvBadge.setText("Em bé");
        } else {
            shape.setColor(Color.parseColor("#F3F4F6"));
            tvBadge.setTextColor(Color.parseColor("#374151"));
            tvBadge.setText("Khác");
        }
        tvBadge.setBackground(shape);
    }

    // --- Badge trạng thái vé ---
    private void setupTicketStatusBadge(TextView tvBadge, String status) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(12f);
        if (status == null) status = "";

        switch (status.toUpperCase()) {
            case "RESERVED":
                shape.setColor(Color.parseColor("#E8F0FE"));
                tvBadge.setTextColor(Color.parseColor("#1A73E8"));
                tvBadge.setText("Đã đặt");
                break;
            case "ISSUED":
                shape.setColor(Color.parseColor("#E8F5E9"));
                tvBadge.setTextColor(Color.parseColor("#2E7D32"));
                tvBadge.setText("Đã xuất");
                break;
            case "CANCELLED":
                shape.setColor(Color.parseColor("#FFEBEE"));
                tvBadge.setTextColor(Color.parseColor("#C62828"));
                tvBadge.setText("Đã huỷ");
                break;
            default:
                shape.setColor(Color.parseColor("#F5F5F5"));
                tvBadge.setTextColor(Color.parseColor("#757575"));
                tvBadge.setText(status);
                break;
        }
        tvBadge.setBackground(shape);
    }

    // --- Badge trạng thái giao dịch ---
    private void setupTransactionStatusBadge(TextView tvBadge, String status) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(12f);
        if (status == null) status = "";

        switch (status.toUpperCase()) {
            case "SUCCESS":
                shape.setColor(Color.parseColor("#E8F5E9"));
                tvBadge.setTextColor(Color.parseColor("#2E7D32"));
                tvBadge.setText("Thành công");
                break;
            case "FAILED":
                shape.setColor(Color.parseColor("#FFEBEE"));
                tvBadge.setTextColor(Color.parseColor("#C62828"));
                tvBadge.setText("Thất bại");
                break;
            case "PENDING":
                shape.setColor(Color.parseColor("#FFF3E0"));
                tvBadge.setTextColor(Color.parseColor("#E65100"));
                tvBadge.setText("Đang xử lý");
                break;
            default:
                shape.setColor(Color.parseColor("#F5F5F5"));
                tvBadge.setTextColor(Color.parseColor("#757575"));
                tvBadge.setText(status);
                break;
        }
        tvBadge.setBackground(shape);
    }

    // --- Format thời gian: "2026-04-06T16:55:00" → "16:55" ---
    private String formatTime(String localDateTimeStr) {
        if (localDateTimeStr == null || localDateTimeStr.trim().isEmpty() || localDateTimeStr.equals("null")) return "--:--";
        try {
            int tIndex = localDateTimeStr.indexOf('T');
            if (tIndex != -1 && localDateTimeStr.length() >= tIndex + 6) {
                return localDateTimeStr.substring(tIndex + 1, tIndex + 6);
            }
        } catch (Exception ignored) {}
        return localDateTimeStr;
    }

    // --- Format ngày giờ đầy đủ: "2026-04-06T10:20:51.576Z" → "06/04/2026 10:20" ---
    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.trim().isEmpty()) return "N/A";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String safeIso = isoDateTime.length() > 19 ? isoDateTime.substring(0, 19) : isoDateTime;
            Date date = inputFormat.parse(safeIso);
            if (date != null) {
                return new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(date);
            }
        } catch (ParseException e) {
            // Fallback: hiển thị raw nếu parse lỗi
            try {
                return isoDateTime.length() > 16 ? isoDateTime.substring(0, 16).replace("T", " ") : isoDateTime;
            } catch (Exception ex) {
                return isoDateTime;
            }
        }
        return isoDateTime;
    }

    // --- Tính khoảng cách thời gian (giống FlightDetailActivity) ---
    private String calculateDuration(String departure, String arrival) {
        if (departure == null || departure.trim().isEmpty() || arrival == null || arrival.trim().isEmpty()) return "Chi tiết";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String safeDep = departure.length() > 19 ? departure.substring(0, 19) : departure;
            String safeArr = arrival.length() > 19 ? arrival.substring(0, 19) : arrival;
            
            Date depDate = format.parse(safeDep);
            Date arrDate = format.parse(safeArr);

            if (depDate != null && arrDate != null) {
                long diffInMillis = arrDate.getTime() - depDate.getTime();
                if (diffInMillis < 0) return "Chi tiết";
                long hours = diffInMillis / (60 * 60 * 1000);
                long minutes = (diffInMillis / (60 * 1000)) % 60;
                if (minutes == 0) return hours + "h";
                return hours + "h " + minutes + "m";
            }
        } catch (Exception e) {
            return "Chi tiết";
        }
        return "Chi tiết";
    }

    // --- Null-safe String ---
    private String safeStr(String val) {
        return val != null ? val : "N/A";
    }

    public String formatClass(String rawClass) {
        if (rawClass == null || rawClass.trim().isEmpty()) return "Economy";
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

    // ======================== VIEWHOLDER CLASSES ========================

    // --- 1. Header (PNR, status, contact, nút thanh toán) ---
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvPnrCode, tvStatus, tvContactName, tvContactEmail, tvContactPhone;
        Button btnPayNow;
        View dividerPayment;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPnrCode = itemView.findViewById(R.id.tvPnrCode);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactEmail = itemView.findViewById(R.id.tvContactEmail);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);
            btnPayNow = itemView.findViewById(R.id.btnPayNow);
            dividerPayment = itemView.findViewById(R.id.dividerPayment);
        }
    }

    // --- 2. Flight Info (route card) ---
    static class FlightViewHolder extends RecyclerView.ViewHolder {
        TextView tvDepTime, tvArrTime, tvDuration, tvOrigin, tvDest, tvAirline;

        FlightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDepTime = itemView.findViewById(R.id.tvDepTime);
            tvArrTime = itemView.findViewById(R.id.tvArrTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvOrigin = itemView.findViewById(R.id.tvOrigin);
            tvDest = itemView.findViewById(R.id.tvDest);
            tvAirline = itemView.findViewById(R.id.tvAirline);
        }
    }

    // --- 3. Section Title ---
    static class SectionTitleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        SectionTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSectionTitle);
        }
    }

    // --- 4. Passenger ---
    static class PassengerViewHolder extends RecyclerView.ViewHolder {
        TextView tvPassengerLabel, tvPassengerBadge, tvPassengerId;

        PassengerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPassengerLabel = itemView.findViewById(R.id.tvPassengerLabel);
            tvPassengerBadge = itemView.findViewById(R.id.tvPassengerBadge);
            tvPassengerId = itemView.findViewById(R.id.tvPassengerId);
        }
    }

    // --- 5. Ticket ---
    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketNumber, tvTicketStatus, tvFlightAndClass, tvRoute, tvTicketTime, tvSeat, tvTicketAmount;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketNumber = itemView.findViewById(R.id.tvTicketNumber);
            tvTicketStatus = itemView.findViewById(R.id.tvTicketStatus);
            tvFlightAndClass = itemView.findViewById(R.id.tvFlightAndClass);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvTicketTime = itemView.findViewById(R.id.tvTicketTime);
            tvSeat = itemView.findViewById(R.id.tvSeat);
            tvTicketAmount = itemView.findViewById(R.id.tvTicketAmount);
        }
    }

    // --- 6. Price Summary ---
    static class PriceViewHolder extends RecyclerView.ViewHolder {
        View layoutAdultPrice, layoutChildPrice, layoutInfantPrice;
        TextView tvAdultCount, tvAdultSum, tvChildCount, tvChildSum;
        TextView tvInfantCount, tvInfantSum, tvTotalPrice;

        PriceViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutAdultPrice = itemView.findViewById(R.id.layoutAdultPrice);
            layoutChildPrice = itemView.findViewById(R.id.layoutChildPrice);
            layoutInfantPrice = itemView.findViewById(R.id.layoutInfantPrice);
            tvAdultCount = itemView.findViewById(R.id.tvAdultCount);
            tvAdultSum = itemView.findViewById(R.id.tvAdultSum);
            tvChildCount = itemView.findViewById(R.id.tvChildCount);
            tvChildSum = itemView.findViewById(R.id.tvChildSum);
            tvInfantCount = itemView.findViewById(R.id.tvInfantCount);
            tvInfantSum = itemView.findViewById(R.id.tvInfantSum);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }
    }

    // --- 7. Transaction ---
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionNo, tvTransactionStatus, tvPaymentMethod, tvTransactionDate, tvTransactionAmount;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionNo = itemView.findViewById(R.id.tvTransactionNo);
            tvTransactionStatus = itemView.findViewById(R.id.tvTransactionStatus);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
        }
    }

    // --- 8. Boarding Pass ---
    static class BoardingPassViewHolder extends RecyclerView.ViewHolder {
        ImageView ivQrCode;
        TextView tvBpPassengerName, tvBpFlightInfo, tvBpDate, tvBpPnr, tvBpSeat, tvBpTicket;

        BoardingPassViewHolder(@NonNull View itemView) {
            super(itemView);
            ivQrCode = itemView.findViewById(R.id.ivQrCode);
            tvBpPassengerName = itemView.findViewById(R.id.tvBpPassengerName);
            tvBpFlightInfo = itemView.findViewById(R.id.tvBpFlightInfo);
            tvBpDate = itemView.findViewById(R.id.tvBpDate);
            tvBpPnr = itemView.findViewById(R.id.tvBpPnr);
            tvBpSeat = itemView.findViewById(R.id.tvBpSeat);
            tvBpTicket = itemView.findViewById(R.id.tvBpTicket);
        }
    }
}
