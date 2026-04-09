package com.example.flight_booking_app.search.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class FlightPageResponse implements Serializable {
    @SerializedName("currentPage")
    private int currentPage;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("pageSize")
    private int pageSize;

    @SerializedName("totalElements")
    private long totalElements;

    @SerializedName("data")
    private List<Flight> data;

    // Getters
    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public List<Flight> getData() { return data; }
}