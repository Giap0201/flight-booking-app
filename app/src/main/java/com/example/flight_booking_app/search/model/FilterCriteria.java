package com.example.flight_booking_app.search.model;

import java.util.HashSet;
import java.util.Set;

public class FilterCriteria {
    private boolean nonstopOnly = false;
    private Set<String> selectedAirlines = new HashSet<>();
    private float departureTimeStart = 0; // 0.0 to 24.0
    private float departureTimeEnd = 24;
    private Set<String> selectedCabins = new HashSet<>();

    // Getters và Setters
    public boolean isNonstopOnly() { return nonstopOnly; }
    public void setNonstopOnly(boolean nonstopOnly) { this.nonstopOnly = nonstopOnly; }
    public Set<String> getSelectedAirlines() { return selectedAirlines; }
    public void setSelectedAirlines(Set<String> airlines) { this.selectedAirlines = airlines; }
    public float getDepartureTimeStart() { return departureTimeStart; }
    public float getDepartureTimeEnd() { return departureTimeEnd; }
    public void setDepartureTimeRange(float start, float end) {
        this.departureTimeStart = start;
        this.departureTimeEnd = end;
    }
    public Set<String> getSelectedCabins() { return selectedCabins; }
    public void setSelectedCabins(Set<String> cabins) { this.selectedCabins = cabins; }
}