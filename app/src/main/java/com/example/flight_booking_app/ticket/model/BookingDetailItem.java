package com.example.flight_booking_app.ticket.model;

import com.example.flight_booking_app.ticket.response.BookingDetailResponse;
import com.example.flight_booking_app.ticket.response.PassengerTicketResponse;
import com.example.flight_booking_app.ticket.response.TicketDetailResponse;
import com.example.flight_booking_app.ticket.response.TransactionResponse;

/**
 * =====================================================================================
 * BookingDetailItem — Mô hình "phẳng hóa" (flattened) cho RecyclerView đa kiểu (multi-ViewType).
 * =====================================================================================
 *
 * VẤN ĐỀ: JSON response của Booking Detail có cấu trúc lồng nhau sâu:
 *     Booking → passengers[] → tickets[] → ancillaries[]
 *
 * Nếu dùng RecyclerView lồng RecyclerView → hiệu năng kém, khó quản lý scroll.
 *
 * GIẢI PHÁP: "Phẳng hóa" toàn bộ dữ liệu thành 1 danh sách List<BookingDetailItem>.
 * Mỗi item có 1 trường `type` để Adapter biết inflate layout nào.
 *
 * Ví dụ danh sách sau khi phẳng hóa:
 *   [0] TYPE_HEADER_INFO      → Thông tin chung (PNR, trạng thái, liên hệ)
 *   [1] TYPE_FLIGHT_INFO      → Thẻ chuyến bay (giờ bay, sân bay)
 *   [2] TYPE_SECTION_TITLE    → "Passengers"
 *   [3] TYPE_PASSENGER        → Hành khách #1
 *   [4] TYPE_TICKET           → Vé #1 của hành khách #1
 *   [5] TYPE_TICKET           → Vé #2 của hành khách #1
 *   [6] TYPE_PASSENGER        → Hành khách #2
 *   [7] TYPE_TICKET           → Vé #1 của hành khách #2
 *   [8] TYPE_PRICE_SUMMARY    → Bảng giá tổng hợp
 *   [9] TYPE_SECTION_TITLE    → "Transactions"
 *  [10] TYPE_TRANSACTION      → Giao dịch #1
 * =====================================================================================
 */
public class BookingDetailItem {

    // --- HẰNG SỐ LOẠI ITEM (dùng trong Adapter.getItemViewType()) ---
    public static final int TYPE_HEADER_INFO    = 0; // Thông tin booking chung
    public static final int TYPE_FLIGHT_INFO    = 1; // Thẻ chuyến bay
    public static final int TYPE_SECTION_TITLE  = 2; // Tiêu đề phân đoạn ("Passengers", "Transactions")
    public static final int TYPE_PASSENGER      = 3; // 1 hành khách
    public static final int TYPE_TICKET         = 4; // 1 vé (con của hành khách)
    public static final int TYPE_PRICE_SUMMARY  = 5; // Bảng giá
    public static final int TYPE_TRANSACTION    = 6; // 1 giao dịch
    public static final int TYPE_BOARDING_PASS  = 7; // [MỚI] Boarding Pass với QR Code

    private int type; // Loại item hiện tại

    // Các trường dữ liệu — chỉ trường tương ứng với `type` mới có giá trị, còn lại null
    private BookingDetailResponse bookingData;      // Dùng cho TYPE_HEADER_INFO, TYPE_FLIGHT_INFO, TYPE_PRICE_SUMMARY
    private String sectionTitle;                     // Dùng cho TYPE_SECTION_TITLE
    private PassengerTicketResponse passengerData;  // Dùng cho TYPE_PASSENGER
    private TicketDetailResponse ticketData;        // Dùng cho TYPE_TICKET
    private TransactionResponse transactionData;    // Dùng cho TYPE_TRANSACTION
    private int passengerIndex;                     // Thứ tự hành khách (1-based, cho UI "Hành khách #1")

    // [MỚI] Các trường dữ liệu riêng cho TYPE_BOARDING_PASS
    // Lý do tạo field riêng thay vì dùng lại bookingData:
    //   → Boarding Pass chỉ cần 1 subset nhỏ của data (1 hành khách, 1 vé cụ thể)
    //   → Tránh phụ thuộc vào cấu trúc lồng nhau trong Adapter (không muốn Adapter tự traverse)
    private String bpPnrCode;        // Mã PNR của booking
    private String bpPassengerName;  // Tên đầy đủ của hành khách (để in trên boarding pass)
    private String bpTicketNumber;   // Số vé
    private String bpSeatNumber;     // Số ghế
    private String bpFlightNumber;   // Số hiệu chuyến bay
    private String bpDepartureTime;  // Giờ cất cánh (ISO format)
    private String bpDepartureAirport; // Sân bay đi
    private String bpArrivalAirport;   // Sân bay đến

    // ======================== CONSTRUCTOR RIÊNG CHO TỪNG LOẠI ========================

    // Private constructor — bắt buộc dùng factory method bên dưới
    private BookingDetailItem(int type) {
        this.type = type;
    }

    // --- Factory method: Tạo item thông tin header (PNR, status, contact) ---
    public static BookingDetailItem createHeaderInfo(BookingDetailResponse data) {
        BookingDetailItem item = new BookingDetailItem(TYPE_HEADER_INFO);
        item.bookingData = data;
        return item;
    }

    // --- Factory method: Tạo item thẻ chuyến bay (giờ bay, sân bay) ---
    public static BookingDetailItem createFlightInfo(BookingDetailResponse data) {
        BookingDetailItem item = new BookingDetailItem(TYPE_FLIGHT_INFO);
        item.bookingData = data;
        return item;
    }

    // --- Factory method: Tạo item tiêu đề phân đoạn ---
    public static BookingDetailItem createSectionTitle(String title) {
        BookingDetailItem item = new BookingDetailItem(TYPE_SECTION_TITLE);
        item.sectionTitle = title;
        return item;
    }

    // --- Factory method: Tạo item hành khách ---
    public static BookingDetailItem createPassenger(PassengerTicketResponse passenger, int index) {
        BookingDetailItem item = new BookingDetailItem(TYPE_PASSENGER);
        item.passengerData = passenger;
        item.passengerIndex = index;
        return item;
    }

    // --- Factory method: Tạo item vé ---
    public static BookingDetailItem createTicket(TicketDetailResponse ticket) {
        BookingDetailItem item = new BookingDetailItem(TYPE_TICKET);
        item.ticketData = ticket;
        return item;
    }

    // --- Factory method: Tạo item bảng giá ---
    public static BookingDetailItem createPriceSummary(BookingDetailResponse data) {
        BookingDetailItem item = new BookingDetailItem(TYPE_PRICE_SUMMARY);
        item.bookingData = data;
        return item;
    }

    // --- Factory method: Tạo item giao dịch ---
    public static BookingDetailItem createTransaction(TransactionResponse transaction) {
        BookingDetailItem item = new BookingDetailItem(TYPE_TRANSACTION);
        item.transactionData = transaction;
        return item;
    }

    // --- [MỚI] Factory method: Tạo item Boarding Pass ---
    // Nhận toàn bộ dữ liệu cần thiết dưới dạng primitive/String,
    // không phụ thuộc vào cấu trúc lồng nhau của BookingDetailResponse.
    // @param pnrCode         Mã PNR của booking
    // @param passengerName   Tên hành khách đầu tiên
    // @param ticketNumber    Số vé của hành khách đầu tiên
    // @param seatNumber      Số ghế
    // @param flightNumber    Số hiệu chuyến bay
    // @param departureTime   Giờ cất cánh (ISO 8601 string)
    // @param departureAirport Mã/tên sân bay đi
    // @param arrivalAirport   Mã/tên sân bay đến
    public static BookingDetailItem createBoardingPass(
            String pnrCode,
            String passengerName,
            String ticketNumber,
            String seatNumber,
            String flightNumber,
            String departureTime,
            String departureAirport,
            String arrivalAirport) {
        BookingDetailItem item = new BookingDetailItem(TYPE_BOARDING_PASS);
        item.bpPnrCode = pnrCode;
        item.bpPassengerName = passengerName;
        item.bpTicketNumber = ticketNumber;
        item.bpSeatNumber = seatNumber;
        item.bpFlightNumber = flightNumber;
        item.bpDepartureTime = departureTime;
        item.bpDepartureAirport = departureAirport;
        item.bpArrivalAirport = arrivalAirport;
        return item;
    }

    // ======================== GETTERS ========================

    public int getType() { return type; }

    public BookingDetailResponse getBookingData() { return bookingData; }

    public String getSectionTitle() { return sectionTitle; }

    public PassengerTicketResponse getPassengerData() { return passengerData; }

    public TicketDetailResponse getTicketData() { return ticketData; }

    public TransactionResponse getTransactionData() { return transactionData; }

    public int getPassengerIndex() { return passengerIndex; }

    // [MỚI] Getters cho Boarding Pass fields
    public String getBpPnrCode() { return bpPnrCode; }
    public String getBpPassengerName() { return bpPassengerName; }
    public String getBpTicketNumber() { return bpTicketNumber; }
    public String getBpSeatNumber() { return bpSeatNumber; }
    public String getBpFlightNumber() { return bpFlightNumber; }
    public String getBpDepartureTime() { return bpDepartureTime; }
    public String getBpDepartureAirport() { return bpDepartureAirport; }
    public String getBpArrivalAirport() { return bpArrivalAirport; }
}
