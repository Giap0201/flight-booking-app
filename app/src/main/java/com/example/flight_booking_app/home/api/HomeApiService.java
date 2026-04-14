package com.example.flight_booking_app.home.api;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.home.model.AirportPageData;
import com.example.flight_booking_app.search.model.FlightPageResponse;
import com.example.flight_booking_app.search.model.SearchRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HomeApiService {
    @GET("v1/airports")
    Call<ApiResponse<AirportPageData>> getAirports(@Query("size") int size);
}