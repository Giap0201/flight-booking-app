package com.example.flight_booking_app.ticket.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.ticket.model.BookingDetailItem;
import com.example.flight_booking_app.ticket.model.FlightDetail;
import com.example.flight_booking_app.ticket.repository.FlightRepository;
import com.example.flight_booking_app.ticket.response.client.BookingDetailResponse;
import com.example.flight_booking_app.ticket.response.client.PassengerTicketResponse;
import com.example.flight_booking_app.ticket.response.client.TicketDetailResponse;
import com.example.flight_booking_app.ticket.response.client.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

public class FlightViewModel extends AndroidViewModel {

    private FlightRepository repository;

    private final MutableLiveData<List<BookingDetailItem>> bookingDetailItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public FlightViewModel(@NonNull Application application) {
        super(application);
        repository = new FlightRepository(application);
    }

    public LiveData<FlightDetail> getFlightDetailLiveData(String flightId) {
        return repository.getFlightDetail(flightId);
    }

    public LiveData<List<BookingDetailItem>> getBookingDetailItems() {
        return bookingDetailItems;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchBookingDetail(String bookingId) {
        isLoading.setValue(true);
        repository.getBookingDetail(bookingId).observeForever(new androidx.lifecycle.Observer<BookingDetailResponse>() {
            @Override
            public void onChanged(BookingDetailResponse response) {
                isLoading.setValue(false);
                if (response != null) {
                    List<BookingDetailItem> flatList = flattenBookingDetail(response);
                    bookingDetailItems.setValue(flatList);
                } else {
                    errorMessage.setValue("Không thể tải chi tiết booking. Vui lòng thử lại.");
                }
                repository.getBookingDetail(bookingId).removeObserver(this);
            }
        });
    }

    private List<BookingDetailItem> flattenBookingDetail(BookingDetailResponse data) {
        List<BookingDetailItem> items = new ArrayList<>();
        items.add(BookingDetailItem.createHeaderInfo(data));
        items.add(BookingDetailItem.createFlightInfo(data));

        List<PassengerTicketResponse> passengers = data.getPassengers();
        if (passengers != null && !passengers.isEmpty()) {
            items.add(BookingDetailItem.createSectionTitle("Hành khách (" + passengers.size() + ")"));
            for (int i = 0; i < passengers.size(); i++) {
                PassengerTicketResponse passenger = passengers.get(i);
                items.add(BookingDetailItem.createPassenger(passenger, i + 1));
                List<TicketDetailResponse> tickets = passenger.getTickets();
                if (tickets != null) {
                    for (TicketDetailResponse ticket : tickets) {
                        items.add(BookingDetailItem.createTicket(ticket));
                    }
                }
            }
        }

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

        items.add(BookingDetailItem.createPriceSummary(data));

        return items;
    }

    // ======================== PAYMENT URL ========================

    /**
     * Gọi API tạo đường dẫn thanh toán VNPay.
     * Dùng khi người dùng nhấn nút "Thanh toán" trên BookingDetailActivity.
     *
     * @param bookingId Mã booking (UUID)
     * @param platform  Nền tảng ("android")
     * @return LiveData chứa URL thanh toán VNPay
     */
    public LiveData<String> createPaymentUrl(String bookingId, String platform) {
        return repository.createPaymentUrl(bookingId, platform);
    }
}
