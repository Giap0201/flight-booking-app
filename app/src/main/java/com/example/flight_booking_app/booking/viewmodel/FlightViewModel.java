package com.example.flight_booking_app.booking.viewmodel; // Đổi package cho khớp

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flight_booking_app.booking.model.BookingDetailItem;
import com.example.flight_booking_app.booking.model.FlightDetail;
import com.example.flight_booking_app.booking.repository.FlightRepository;
import com.example.flight_booking_app.booking.response.client.BookingDetailResponse;
import com.example.flight_booking_app.booking.response.client.PassengerTicketResponse;
import com.example.flight_booking_app.booking.response.client.TicketDetailResponse;
import com.example.flight_booking_app.booking.response.client.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel dùng chung cho cả FlightDetailActivity (cũ) và BookingDetailActivity (mới).
 *
 * Phần MỚI:
 * - fetchBookingDetail()      → gọi API lấy chi tiết booking
 * - flattenBookingDetail()    → phẳng hóa dữ liệu lồng nhau thành List<BookingDetailItem>
 * - bookingDetailItems        → LiveData chứa danh sách phẳng cho RecyclerView
 * - isLoading / errorMessage  → trạng thái UI
 */
public class FlightViewModel extends ViewModel {

    private FlightRepository repository;

    // --- LiveData cho trang Flight Detail (CŨ — giữ nguyên) ---
    // (Không sửa đổi gì để đảm bảo backward compatibility)

    // --- LiveData cho trang Booking Detail (MỚI) ---
    private final MutableLiveData<List<BookingDetailItem>> bookingDetailItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public FlightViewModel() {
        repository = new FlightRepository();
    }

    // ======================== HÀM CŨ — KHÔNG SỬA ========================

    // Activity sẽ gọi hàm này để "quan sát" dữ liệu
    public LiveData<FlightDetail> getFlightDetailLiveData(String flightId) {
        return repository.getFlightDetail(flightId);
    }

    // ======================== HÀM MỚI — BOOKING DETAIL ========================

    /**
     * Getter cho LiveData danh sách item phẳng.
     * Activity observe LiveData này để cập nhật RecyclerView.
     */
    public LiveData<List<BookingDetailItem>> getBookingDetailItems() {
        return bookingDetailItems;
    }

    /**
     * Getter cho trạng thái loading.
     * Activity observe để hiện/ẩn ProgressBar.
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Getter cho thông báo lỗi.
     * Activity observe để hiện Toast khi có lỗi.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gọi API lấy chi tiết booking, sau đó phẳng hóa dữ liệu.
     *
     * Flow:
     * 1. Bật trạng thái loading
     * 2. Gọi Repository.getBookingDetail() → trả về LiveData<BookingDetailResponse>
     * 3. Observe kết quả:
     *    - Nếu có data → gọi flattenBookingDetail() → đẩy vào bookingDetailItems
     *    - Nếu null → đẩy errorMessage
     * 4. Tắt trạng thái loading
     *
     * @param bookingId UUID dạng String của booking cần lấy
     */
    public void fetchBookingDetail(String bookingId) {
        isLoading.setValue(true);

        // Observe kết quả từ Repository
        // Lưu ý: observeForever vì ViewModel không có LifecycleOwner
        // Sẽ tự remove observer sau khi nhận được data
        repository.getBookingDetail(bookingId).observeForever(new androidx.lifecycle.Observer<BookingDetailResponse>() {
            @Override
            public void onChanged(BookingDetailResponse response) {
                isLoading.setValue(false);

                if (response != null) {
                    // Phẳng hóa dữ liệu và đẩy vào LiveData cho UI
                    List<BookingDetailItem> flatList = flattenBookingDetail(response);
                    bookingDetailItems.setValue(flatList);
                } else {
                    errorMessage.setValue("Không thể tải chi tiết booking. Vui lòng thử lại.");
                }

                // Tự remove observer để tránh memory leak
                repository.getBookingDetail(bookingId).removeObserver(this);
            }
        });
    }

    // ======================== LOGIC PHẲNG HÓA (FLATTEN) ========================

    /**
     * Phẳng hóa cấu trúc lồng nhau của BookingDetailResponse thành danh sách tuyến tính.
     *
     * Cấu trúc JSON gốc (lồng nhau):
     *    Booking
     *    ├── passengers[0]
     *    │   ├── tickets[0]
     *    │   └── tickets[1]
     *    ├── passengers[1]
     *    │   └── tickets[0]
     *    └── transactions[0]
     *
     * Kết quả sau khi phẳng hóa (tuyến tính):
     *    [HEADER_INFO]         → PNR, status, contact
     *    [FLIGHT_INFO]         → Thẻ chuyến bay (lấy từ vé đầu tiên)
     *    [SECTION: Passengers]
     *    [PASSENGER #1]        → passengers[0]
     *    [TICKET]              → passengers[0].tickets[0]
     *    [TICKET]              → passengers[0].tickets[1]
     *    [PASSENGER #2]        → passengers[1]
     *    [TICKET]              → passengers[1].tickets[0]
     *    [PRICE_SUMMARY]       → Bảng giá tổng hợp
     *    [SECTION: Transactions]
     *    [TRANSACTION]         → transactions[0]
     *
     * @param data BookingDetailResponse đầy đủ từ API
     * @return Danh sách phẳng List<BookingDetailItem> cho RecyclerView
     */
    private List<BookingDetailItem> flattenBookingDetail(BookingDetailResponse data) {
        List<BookingDetailItem> items = new ArrayList<>();

        // ---- 1. HEADER: Thông tin booking chung (PNR, status, contact) ----
        items.add(BookingDetailItem.createHeaderInfo(data));

        // ---- 2. FLIGHT INFO: Lấy thông tin chuyến bay từ vé đầu tiên ----
        // (Vì API trả flight info nằm bên trong ticket, không phải ở booking level)
        items.add(BookingDetailItem.createFlightInfo(data));

        // ---- 3. PASSENGERS + TICKETS ----
        List<PassengerTicketResponse> passengers = data.getPassengers();
        if (passengers != null && !passengers.isEmpty()) {
            // Tiêu đề phân đoạn "Hành khách"
            items.add(BookingDetailItem.createSectionTitle(
                    "Hành khách (" + passengers.size() + ")"));

            // Duyệt từng hành khách
            for (int i = 0; i < passengers.size(); i++) {
                PassengerTicketResponse passenger = passengers.get(i);

                // Thêm item hành khách (index 1-based cho UI)
                items.add(BookingDetailItem.createPassenger(passenger, i + 1));

                // Duyệt từng vé của hành khách này và thêm vào danh sách
                List<TicketDetailResponse> tickets = passenger.getTickets();
                if (tickets != null) {
                    for (TicketDetailResponse ticket : tickets) {
                        items.add(BookingDetailItem.createTicket(ticket));
                    }
                }
            }
        }

        // ---- 3.5. BOARDING PASS (Dành cho chuyến bay đã thanh toán/xác nhận) ----
        // Lấy thông tin vé đầu tiên của hành khách đầu tiên làm Boarding Pass đại diện
        if (passengers != null && !passengers.isEmpty() &&
            passengers.get(0).getTickets() != null && !passengers.get(0).getTickets().isEmpty()) {
            
            String status = data.getStatus() != null ? data.getStatus().toUpperCase() : "";
            if (status.equals("PAID") || status.equals("CONFIRMED") || status.equals("COMPLETED")) {
                PassengerTicketResponse firstPax = passengers.get(0);
                TicketDetailResponse firstTicket = firstPax.getTickets().get(0);
                
                items.add(BookingDetailItem.createBoardingPass(
                        data.getPnrCode(),
                        firstPax.getFullName(),
                        firstTicket.getTicketNumber(),
                        firstTicket.getSeatNumber(),
                        firstTicket.getFlightNumber(),
                        firstTicket.getDepartureTime(),
                        firstTicket.getDepartureAirport(),
                        firstTicket.getArrivalAirport()
                ));
            }
        }

        // ---- 4. PRICE SUMMARY: Bảng giá tổng hợp ----
        items.add(BookingDetailItem.createPriceSummary(data));

        // ---- 5. TRANSACTIONS ----
        List<TransactionResponse> transactions = data.getTransactions();
        if (transactions != null && !transactions.isEmpty()) {
            // Tiêu đề phân đoạn "Lịch sử giao dịch"
            items.add(BookingDetailItem.createSectionTitle(
                    "Lịch sử giao dịch (" + transactions.size() + ")"));

            for (TransactionResponse transaction : transactions) {
                items.add(BookingDetailItem.createTransaction(transaction));
            }
        }

        return items;
    }
}