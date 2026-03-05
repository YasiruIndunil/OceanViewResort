package com.oceanview.util;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for InputValidator.
 * TDD: Tests defined before implementation was finalised.
 */
@DisplayName("InputValidator Unit Tests")
class InputValidatorTest {

    // ── isNullOrEmpty ─────────────────────────────────────────

    @Test
    @DisplayName("isNullOrEmpty: null returns true")
    void testNullReturnsTrue() {
        assertTrue(InputValidator.isNullOrEmpty(null));
    }

    @Test
    @DisplayName("isNullOrEmpty: blank string returns true")
    void testBlankReturnsTrue() {
        assertTrue(InputValidator.isNullOrEmpty("   "));
    }

    @Test
    @DisplayName("isNullOrEmpty: non-empty string returns false")
    void testNonEmptyReturnsFalse() {
        assertFalse(InputValidator.isNullOrEmpty("Hello"));
    }

    // ── isValidName ───────────────────────────────────────────

    @Test
    @DisplayName("isValidName: valid Sri Lankan name passes")
    void testValidName() {
        assertTrue(InputValidator.isValidName("Kamal Perera"));
    }

    @Test
    @DisplayName("isValidName: name with hyphen passes")
    void testNameWithHyphen() {
        assertTrue(InputValidator.isValidName("Jean-Pierre"));
    }

    @Test
    @DisplayName("isValidName: single character fails")
    void testSingleCharFails() {
        assertFalse(InputValidator.isValidName("K"));
    }

    @Test
    @DisplayName("isValidName: name with numbers fails")
    void testNameWithNumbersFails() {
        assertFalse(InputValidator.isValidName("Kamal123"));
    }

    @Test
    @DisplayName("isValidName: null fails")
    void testNullNameFails() {
        assertFalse(InputValidator.isValidName(null));
    }

    // ── isValidPhone ──────────────────────────────────────────

    @Test
    @DisplayName("isValidPhone: Sri Lanka mobile format passes")
    void testValidSriLankaPhone() {
        assertTrue(InputValidator.isValidPhone("0771234567"));
    }

    @Test
    @DisplayName("isValidPhone: international format passes")
    void testInternationalPhone() {
        assertTrue(InputValidator.isValidPhone("+94771234567"));
    }

    @Test
    @DisplayName("isValidPhone: too short fails")
    void testTooShortPhone() {
        assertFalse(InputValidator.isValidPhone("123"));
    }

    @Test
    @DisplayName("isValidPhone: alphabetic characters fail")
    void testAlphaPhone() {
        assertFalse(InputValidator.isValidPhone("077ABCDEFG"));
    }

    // ── isValidEmail ──────────────────────────────────────────

    @Test
    @DisplayName("isValidEmail: null/empty returns true (optional field)")
    void testEmptyEmailIsOptional() {
        assertTrue(InputValidator.isValidEmail(null));
        assertTrue(InputValidator.isValidEmail(""));
    }

    @Test
    @DisplayName("isValidEmail: valid email passes")
    void testValidEmail() {
        assertTrue(InputValidator.isValidEmail("kamal@example.com"));
    }

    @Test
    @DisplayName("isValidEmail: missing @ fails")
    void testMissingAtEmail() {
        assertFalse(InputValidator.isValidEmail("kamalexample.com"));
    }

    @Test
    @DisplayName("isValidEmail: missing domain fails")
    void testMissingDomainEmail() {
        assertFalse(InputValidator.isValidEmail("kamal@"));
    }

    // ── isValidDateRange ──────────────────────────────────────

    @Test
    @DisplayName("isValidDateRange: future dates pass")
    void testFutureDatesPass() {
        LocalDate in  = LocalDate.now().plusDays(1);
        LocalDate out = LocalDate.now().plusDays(3);
        assertTrue(InputValidator.isValidDateRange(in, out));
    }

    @Test
    @DisplayName("isValidDateRange: same date fails")
    void testSameDateFails() {
        LocalDate today = LocalDate.now().plusDays(1);
        assertFalse(InputValidator.isValidDateRange(today, today));
    }

    @Test
    @DisplayName("isValidDateRange: checkout before checkin fails")
    void testCheckoutBeforeCheckin() {
        LocalDate in  = LocalDate.now().plusDays(5);
        LocalDate out = LocalDate.now().plusDays(2);
        assertFalse(InputValidator.isValidDateRange(in, out));
    }

    @Test
    @DisplayName("isValidDateRange: past check-in fails")
    void testPastCheckinFails() {
        LocalDate in  = LocalDate.now().minusDays(1);
        LocalDate out = LocalDate.now().plusDays(2);
        assertFalse(InputValidator.isValidDateRange(in, out));
    }

    @Test
    @DisplayName("isValidDateRange: null dates fail")
    void testNullDatesFail() {
        assertFalse(InputValidator.isValidDateRange(null, null));
    }

    // ── isValidNumGuests ─────────────────────────────────────

    @Test
    @DisplayName("isValidNumGuests: 1 guest with capacity 2 passes")
    void testValidGuestCount() {
        assertTrue(InputValidator.isValidNumGuests(1, 2));
    }

    @Test
    @DisplayName("isValidNumGuests: 0 guests fails")
    void testZeroGuestsFails() {
        assertFalse(InputValidator.isValidNumGuests(0, 2));
    }

    @Test
    @DisplayName("isValidNumGuests: exceeds capacity fails")
    void testExceedsCapacityFails() {
        assertFalse(InputValidator.isValidNumGuests(5, 2));
    }

    // ── validateReservationInput ─────────────────────────────

    @Test
    @DisplayName("validateReservationInput: all valid returns null (no error)")
    void testAllValidReturnsNull() {
        LocalDate in  = LocalDate.now().plusDays(1);
        LocalDate out = LocalDate.now().plusDays(3);
        String result = InputValidator.validateReservationInput(
                "Kamal Perera", "0771234567", "k@test.com",
                1, in, out, 2);
        assertNull(result, "Expected no error but got: " + result);
    }

    @Test
    @DisplayName("validateReservationInput: invalid name returns error")
    void testInvalidNameReturnsError() {
        LocalDate in  = LocalDate.now().plusDays(1);
        LocalDate out = LocalDate.now().plusDays(3);
        String result = InputValidator.validateReservationInput(
                "K", "0771234567", "", 1, in, out, 1);
        assertNotNull(result);
        assertTrue(result.contains("name"));
    }
}
