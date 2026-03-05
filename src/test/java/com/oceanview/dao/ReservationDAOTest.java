package com.oceanview.dao;

import com.oceanview.model.Reservation;
import com.oceanview.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReservationDAO Tests using Mockito for DB mocking.
 * Demonstrates TDD approach – tests written to define expected behaviour.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationDAO Unit Tests")
class ReservationDAOTest {

    @Mock private DatabaseConnection mockDbConn;
    @Mock private Connection         mockConn;
    @Mock private CallableStatement  mockCs;
    @Mock private PreparedStatement  mockPs;
    @Mock private ResultSet          mockRs;

    private ReservationDAO reservationDAO;

    @BeforeEach
    void setUp() throws SQLException {
        reservationDAO = new ReservationDAO(mockDbConn);
        when(mockDbConn.getConnection()).thenReturn(mockConn);
    }

    // ── addReservation ────────────────────────────────────────

    @Test
    @DisplayName("addReservation: SUCCESS returns message with reservation number")
    void testAddReservationSuccess() throws SQLException {
        when(mockConn.prepareCall(anyString())).thenReturn(mockCs);
        when(mockCs.getString(11)).thenReturn("OVR-20250101-123456");
        when(mockCs.getString(13)).thenReturn("SUCCESS: Reservation created successfully.");

        Reservation res = createTestReservation();
        String result   = reservationDAO.addReservation(res, 1);

        assertTrue(result.startsWith("SUCCESS"));
        assertEquals("OVR-20250101-123456", res.getReservationNumber());
        verify(mockCs).execute();
    }

    @Test
    @DisplayName("addReservation: ERROR when room not available")
    void testAddReservationRoomNotAvailable() throws SQLException {
        when(mockConn.prepareCall(anyString())).thenReturn(mockCs);
        when(mockCs.getString(11)).thenReturn(null);
        when(mockCs.getString(13)).thenReturn("ERROR: Room is not available.");

        Reservation res = createTestReservation();
        String result   = reservationDAO.addReservation(res, 1);

        assertTrue(result.startsWith("ERROR"));
        assertNull(res.getReservationNumber());
    }

    @Test
    @DisplayName("addReservation: SQL exception returns ERROR message")
    void testAddReservationSQLException() throws SQLException {
        when(mockConn.prepareCall(anyString())).thenThrow(new SQLException("Connection refused"));

        Reservation res = createTestReservation();
        String result   = reservationDAO.addReservation(res, 1);

        assertTrue(result.startsWith("ERROR"));
    }

    // ── getReservationByNumber ────────────────────────────────

    @Test
    @DisplayName("getReservationByNumber: returns Reservation when found")
    void testGetReservationFound() throws SQLException {
        when(mockConn.prepareCall(anyString())).thenReturn(mockCs);
        when(mockCs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("reservation_number")).thenReturn("OVR-20250101-123456");
        when(mockRs.getString("guest_name")).thenReturn("Kamal Perera");
        when(mockRs.getString("address")).thenReturn("Galle, Sri Lanka");
        when(mockRs.getString("contact_number")).thenReturn("0771234567");
        when(mockRs.getString("email")).thenReturn("kamal@test.com");
        when(mockRs.getString("room_number")).thenReturn("301");
        when(mockRs.getString("room_type")).thenReturn("OCEAN_VIEW");
        when(mockRs.getBigDecimal("rate_per_night")).thenReturn(java.math.BigDecimal.valueOf(12000));
        when(mockRs.getDate("check_in_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(mockRs.getDate("check_out_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(3)));
        when(mockRs.getBigDecimal("total_amount")).thenReturn(java.math.BigDecimal.valueOf(24000));
        when(mockRs.getInt("num_guests")).thenReturn(2);
        when(mockRs.getString("status")).thenReturn("CONFIRMED");
        when(mockRs.getString("special_requests")).thenReturn("Sea view preferred");
        when(mockRs.getString("created_at")).thenReturn("2025-01-01 10:00:00");

        Reservation result = reservationDAO.getReservationByNumber("OVR-20250101-123456");

        assertNotNull(result);
        assertEquals("Kamal Perera", result.getGuestName());
        assertEquals("OVR-20250101-123456", result.getReservationNumber());
        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    @DisplayName("getReservationByNumber: returns null when not found")
    void testGetReservationNotFound() throws SQLException {
        when(mockConn.prepareCall(anyString())).thenReturn(mockCs);
        when(mockCs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        Reservation result = reservationDAO.getReservationByNumber("OVR-INVALID-000000");
        assertNull(result);
    }

    // ── cancelReservation ─────────────────────────────────────

    @Test
    @DisplayName("cancelReservation: SUCCESS response")
    void testCancelReservationSuccess() throws SQLException {
        when(mockConn.prepareCall(anyString())).thenReturn(mockCs);
        when(mockCs.getString(2)).thenReturn("SUCCESS: Reservation cancelled successfully.");

        String result = reservationDAO.cancelReservation("OVR-20250101-123456");

        assertTrue(result.startsWith("SUCCESS"));
        verify(mockCs).execute();
    }

    @Test
    @DisplayName("cancelReservation: ERROR for invalid number")
    void testCancelReservationError() throws SQLException {
        when(mockConn.prepareCall(anyString())).thenReturn(mockCs);
        when(mockCs.getString(2)).thenReturn("ERROR: Reservation not found or cannot be cancelled.");

        String result = reservationDAO.cancelReservation("OVR-INVALID");
        assertTrue(result.startsWith("ERROR"));
    }

    // ── generateBill ──────────────────────────────────────────

    @Test
    @DisplayName("generateBill: returns SUCCESS")
    void testGenerateBillSuccess() throws SQLException {
        when(mockConn.prepareCall(anyString())).thenReturn(mockCs);
        when(mockCs.getString(2)).thenReturn("SUCCESS: Bill generated successfully.");

        String result = reservationDAO.generateBill("OVR-20250101-123456");
        assertTrue(result.startsWith("SUCCESS"));
    }

    // ── Helper ───────────────────────────────────────────────

    private Reservation createTestReservation() {
        return new Reservation(
            "Kamal Perera", "Galle, Sri Lanka", "0771234567",
            "kamal@test.com", 1,
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
            2, "No special requests"
        );
    }
}
