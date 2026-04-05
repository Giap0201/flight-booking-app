package com.example.flight_booking_app.home.api;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.home.model.AirportPageData;
import com.example.flight_booking_app.home.model.FlightPageResponse;
import com.example.flight_booking_app.home.model.SearchRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HomeApiService {

    // Đường dẫn này nối tiếp vào BASE_URL trong ApiClient của bạn
    @POST("flights/search")
    Call<ApiResponse<FlightPageResponse>> searchFlights(@Body SearchRequest request);

    @GET("v1/airports")
    Call<ApiResponse<AirportPageData>> getAirports(@Query("size") int size);
}