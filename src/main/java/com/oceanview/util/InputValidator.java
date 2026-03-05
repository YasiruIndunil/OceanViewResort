package com.oceanview.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * InputValidator - validates all user inputs.
 * Prevents invalid data from entering the system.
 */
public class InputValidator {

    private static final Pattern PHONE_PATTERN  = Pattern.compile("^[0-9+\\-\\s]{7,20}$");
    private static final Pattern EMAIL_PATTERN  = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN   = Pattern.compile("^[A-Za-z\\s.'-]{2,100}$");

    private InputValidator() {}

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        return !isNullOrEmpty(name) && NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return !isNullOrEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return true; // email is optional
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return false;
        return checkIn.isBefore(checkOut) && !checkIn.isBefore(LocalDate.now());
    }

    public static boolean isValidNumGuests(int numGuests, int maxCapacity) {
        return numGuests >= 1 && numGuests <= maxCapacity;
    }

    public static boolean isValidRoomId(int roomId) {
        return roomId > 0;
    }

    /**
     * Returns a descriptive error message for invalid reservation fields.
     */
    public static String validateReservationInput(String guestName, String contact,
                                                   String email, int roomId,
                                                   LocalDate checkIn, LocalDate checkOut,
                                                   int numGuests) {
        if (!isValidName(guestName))
            return "Guest name is invalid. Use letters only (2-100 characters).";
        if (!isValidPhone(contact))
            return "Contact number is invalid. Use 7-20 digits.";
        if (!isValidEmail(email))
            return "Email address format is invalid.";
        if (!isValidRoomId(roomId))
            return "Please select a valid room.";
        if (!isValidDateRange(checkIn, checkOut))
            return "Check-out date must be after check-in date, and check-in cannot be in the past.";
        if (numGuests < 1)
            return "Number of guests must be at least 1.";
        return null; // no error
    }
}
