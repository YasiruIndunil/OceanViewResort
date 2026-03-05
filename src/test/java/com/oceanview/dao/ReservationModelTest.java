package com.oceanview.dao;

import com.oceanview.model.Reservation;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reservation Model Unit Tests.
 */
@DisplayName("Reservation Model Unit Tests")
class ReservationModelTest {

    @Test
    @DisplayName("getNumNights: correctly calculates 3 nights")
    void testNumNightsCalculation() {
        Reservation r = new Reservation();
        r.setCheckInDate(LocalDate.of(2025, 6, 1));
        r.setCheckOutDate(LocalDate.of(2025, 6, 4));
        assertEquals(3, r.getNumNights());
    }

    @Test
    @DisplayName("getNumNights: returns 0 for null dates")
    void testNumNightsNullDates() {
        Reservation r = new Reservation();
        assertEquals(0, r.getNumNights());
    }

    @Test
    @DisplayName("constructor: sets all fields correctly")
    void testConstructorSetsFields() {
        LocalDate in  = LocalDate.now().plusDays(1);
        LocalDate out = LocalDate.now().plusDays(5);
        Reservation r = new Reservation(
            "Kamal Perera", "Galle", "0771234567",
            "k@test.com", 3, in, out, 2, "Extra towels"
        );

        assertEquals("Kamal Perera", r.getGuestName());
        assertEquals("Galle", r.getAddress());
        assertEquals("0771234567", r.getContactNumber());
        assertEquals(3, r.getRoomId());
        assertEquals(2, r.getNumGuests());
        assertEquals("Extra towels", r.getSpecialRequests());
    }

    @Test
    @DisplayName("setters and getters work correctly")
    void testSettersAndGetters() {
        Reservation r = new Reservation();
        r.setReservationNumber("OVR-20250101-123456");
        r.setStatus("CONFIRMED");
        r.setTotalAmount(BigDecimal.valueOf(36000.00));

        assertEquals("OVR-20250101-123456", r.getReservationNumber());
        assertEquals("CONFIRMED", r.getStatus());
        assertEquals(BigDecimal.valueOf(36000.00), r.getTotalAmount());
    }

    @Test
    @DisplayName("toString: includes reservation number and guest name")
    void testToString() {
        Reservation r = new Reservation();
        r.setReservationNumber("OVR-20250101-123456");
        r.setGuestName("Kamal Perera");
        r.setStatus("CONFIRMED");

        String str = r.toString();
        assertTrue(str.contains("OVR-20250101-123456"));
        assertTrue(str.contains("Kamal Perera"));
    }
}
