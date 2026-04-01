package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

// Class này chứa TẤT CẢ thông tin của 1 chuyến bay, là "trái tim" của màn hình bạn đang làm
public class FlightDetail implements Serializable {

    @SerializedName("id")
    private String id; // Mã chuyến bay

    @SerializedName("flightNumber")
    private String flightNumber; // Số hiệu chuyến bay (VD: VJ123)

    @SerializedName("departureTime")
    private String departureTime; // Thời gian cất cánh

    @SerializedName("arrivalTime")
    private String arrivalTime; // Thời gian hạ cánh

    // Đây là lúc chúng ta tái sử dụng các class đã tạo!
    @SerializedName("airline")
    private Airline airline; // Thông tin Hãng bay

    @SerializedName("origin")
    private Location origin; // Thông tin Nơi đi

    @SerializedName("destination")
    private Location destination; // Thông tin Nơi đến

    // Chú ý: Đây là một DANH SÁCH các hạng vé (sẽ dùng để đổ vào ListView)
    @SerializedName("flightClasses")
    private List<FlightClass> flightClasses;

    // Constructor rỗng bắt buộc cho Gson
    public FlightDetail() {
    }

    public FlightDetail(String id, String flightNumber, String departureTime, String arrivalTime, Airline airline, Location origin, Location destination, List<FlightClass> flightClasses) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.flightClasses = flightClasses;
    }

    // --- GETTER & SETTER ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public List<FlightClass> getFlightClasses() {
        return flightClasses;
    }

    public void setFlightClasses(List<FlightClass> flightClasses) {
        this.flightClasses = flightClasses;
    }
}