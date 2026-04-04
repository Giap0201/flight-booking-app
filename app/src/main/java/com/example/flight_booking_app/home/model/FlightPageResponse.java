package com.example.flight_booking_app.home.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FlightPageResponse {
    @SerializedName("currentPage")
    private int currentPage;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("pageSize")
    private int pageSize;

    @SerializedName("totalElements")
    private int totalElements;

    @SerializedName("data")
    private List<Flight> data;

    public List<Flight> getData() { return data; }
    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public int getTotalElements() { return totalElements; }
}