package com.example.flight_booking_app.common;

import java.util.List;

public class PageResponse<T> {
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private long totalElements;
    private List<T> data;

    // Getters
    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public List<T> getData() { return data; }
}