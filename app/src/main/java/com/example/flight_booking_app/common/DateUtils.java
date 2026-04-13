package com.example.flight_booking_app.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String convertToApiFormat(String inputDate) {
        try {
            LocalDate date = LocalDate.parse(inputDate, INPUT_FORMAT);
            return date.toString(); // yyyy-MM-dd
        } catch (Exception e) {
            return null;
        }
    }
}