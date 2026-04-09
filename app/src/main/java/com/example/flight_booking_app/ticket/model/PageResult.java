package com.example.flight_booking_app.ticket.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PageResult<T> {
    @SerializedName("currentPage")
    private int currentPage;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalElements")
    private int totalElements;

    @SerializedName("data")
    private List<T> data;

    // Getters and Setters
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
}
