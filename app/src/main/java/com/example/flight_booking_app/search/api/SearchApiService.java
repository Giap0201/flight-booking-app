package com.example.flight_booking_app.search.api;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.common.PageResponse;
import com.example.flight_booking_app.search.model.Airline;
import com.example.flight_booking_app.search.model.CheapestDate;
import com.example.flight_booking_app.search.model.FlightPageResponse;
import com.example.flight_booking_app.search.model.SearchRequest;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SearchApiService {

    // 1. Lấy danh sách chuyến bay (Đã có - Giữ nguyên)
    @POST("flights/search")
    Call<ApiResponse<FlightPageResponse>> searchFlights(
            @Body SearchRequest request,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir
    );

    // 2. Dải ngày giá rẻ: GET /flights/cheapest-dates
    @GET("flights/cheapest-dates")
    Call<ApiResponse<List<CheapestDate>>> getCheapestDates(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("year") int year,
            @Query("month") int month
    );

    // 3. Lấy danh sách hãng bay: GET /v1/airlines (Để đổ vào bộ lọc)
    @GET("v1/airlines")
    Call<ApiResponse<PageResponse<Airline>>> getAirlines(@Query("size") int size);
}