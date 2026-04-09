package com.example.flight_booking_app.ticket.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.ticket.api.TicketApiService;
import com.example.flight_booking_app.ticket.model.FlightDetail;
import com.example.flight_booking_app.ticket.response.client.BookingDetailResponse;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.common.AppConfig;
import com.example.flight_booking_app.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightRepository {
    private static final String TAG = "FlightRepository";
    private TicketApiService apiService;

    public FlightRepository(Context context) {
        // Fix: Pass the 'context' parameter, NOT 'this'
        apiService = ApiClient.getClient(context).create(TicketApiService.class);
    }

    public MutableLiveData<FlightDetail> getFlightDetail(String flightId) {
        MutableLiveData<FlightDetail> flightData = new MutableLiveData<>();

        apiService.getFlightDetail(flightId).enqueue(new Callback<ApiResponse<FlightDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<FlightDetail>> call, Response<ApiResponse<FlightDetail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getCode() == 1000) {
                        flightData.setValue(response.body().getResult());
                    } else {
                        flightData.setValue(null);
                    }
                } else {
                    flightData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FlightDetail>> call, Throwable t) {
                flightData.setValue(null);
            }
        });

        return flightData;
    }

    public MutableLiveData<BookingDetailResponse> getBookingDetail(String bookingId) {
        MutableLiveData<BookingDetailResponse> bookingData = new MutableLiveData<>();

        apiService.getBookingById(AppConfig.TOKEN, bookingId)
                .enqueue(new Callback<ApiResponse<BookingDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDetailResponse>> call,
                                   Response<ApiResponse<BookingDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getCode() == 1000) {
                        bookingData.setValue(response.body().getResult());
                    } else {
                        Log.w(TAG, "API Error Code: " + response.body().getCode());
                        bookingData.setValue(null);
                    }
                } else {
                    bookingData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDetailResponse>> call, Throwable t) {
                Log.e(TAG, "Network Error: " + t.getMessage());
                bookingData.setValue(null);
            }
        });

        return bookingData;
    }
}
