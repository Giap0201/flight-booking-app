package com.example.flight_booking_app.ticket.model;


import com.google.gson.annotations.SerializedName;

public class Flight {
    @SerializedName("flightNo")
    private String flightNo; // Số hiệu chuyến bay (VD: IDN16821)

    @SerializedName("airlineName")
    private String airlineName; // Hãng bay (VD: Indonesia Air Asia)

    @SerializedName("departureCode")
    private String departureCode; // Mã sân bay đi (VD: CGK)

    @SerializedName("departureCity")
    private String departureCity; // Tên sân bay/thành phố đi (VD: Jakarta)

    @SerializedName("departureTime")
    private String departureTime; // Giờ bay (VD: 2024-05-07T16:55:00)

    @SerializedName("arrivalCode")
    private String arrivalCode; // Mã sân bay đến (VD: DPS)

    @SerializedName("arrivalCity")
    private String arrivalCity; // Tên sân bay/thành phố đến (VD: Bali)

    @SerializedName("arrivalTime")
    private String arrivalTime; // Giờ đến (VD: 2024-05-07T20:30:00)

    @SerializedName("duration")
    private String duration; // Thời gian bay (VD: 1h 50m)

    @SerializedName("seatClass")
    private String seatClass; // Hạng ghế (VD: Economy)

    // TODO: Bạn hãy Generate Getters và Setters
}