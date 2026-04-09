package com.example.flight_booking_app.ticket.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookingDetailResponse {

    public static class Contact {
        @com.google.gson.annotations.SerializedName("name")
        private String name;

        @com.google.gson.annotations.SerializedName("email")
        private String email;

        @com.google.gson.annotations.SerializedName("phone")
        private String phone;

        // Getters
        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        // Setters
        public void setName(String name) {
            this.name = name;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    @SerializedName("id")
    private String id;

    @SerializedName("pnrCode")
    private String pnrCode;

    @SerializedName("status")
    private String status;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("contact")
    private Contact contact; // Now refers to the top-level Contact class

    @SerializedName("passengers")
    private List<PassengerTicketResponse> passengers;

    @SerializedName("transactions")
    private List<TransactionResponse> transactions;

    // Getters
    public String getId() {
        return id;
    }

    public String getPnrCode() {
        return pnrCode;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public Contact getContact() {
        return contact;
    }

    public List<PassengerTicketResponse> getPassengers() {
        return passengers;
    }

    public List<TransactionResponse> getTransactions() {
        return transactions;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setPnrCode(String pnrCode) {
        this.pnrCode = pnrCode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setPassengers(List<PassengerTicketResponse> passengers) {
        this.passengers = passengers;
    }

    public void setTransactions(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }
}
