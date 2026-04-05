package com.example.flight_booking_app.home.model;

import java.util.List;

public class AirportPageData {
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private int totalElements;
    private List<ApiAirport> data; // Mảng danh sách sân bay thực sự

    public List<ApiAirport> getData() {
        return data;
    }

    // Class đại diện cho 1 Object sân bay trả về từ API
    public static class ApiAirport {
        private String code;
        private String name;
        private String cityCode;
        private String countryCode;

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getCityCode() { return cityCode; }
        public String getCountryCode() { return countryCode; }
    }
}