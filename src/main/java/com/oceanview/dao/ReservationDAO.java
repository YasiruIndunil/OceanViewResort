package com.oceanview.dao;

import com.oceanview.model.Bill;
import com.oceanview.model.Reservation;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReservationDAO - handles all reservation DB operations via Stored Procedures.
 * Design Pattern: DAO (Data Access Object)
 */
public class ReservationDAO {

    private static final Logger LOGGER = Logger.getLogger(ReservationDAO.class.getName());
    private final DatabaseConnection dbConn;

    public ReservationDAO() {
        this.dbConn = DatabaseConnection.getInstance();
    }

    // Constructor injection for testing
    public ReservationDAO(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    /**
     * Add a new reservation using the AddReservation stored procedure.
     * @return result message (starts with "SUCCESS:" or "ERROR:")
     */
    public String addReservation(Reservation res, int createdBy) {
        String sql = "{CALL AddReservation(?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (Connection conn = dbConn.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, res.getGuestName());
            cs.setString(2, res.getAddress());
            cs.setString(3, res.getContactNumber());
            cs.setString(4, res.getEmail());
            cs.setInt(5, res.getRoomId());
            cs.setDate(6, Date.valueOf(res.getCheckInDate()));
            cs.setDate(7, Date.valueOf(res.getCheckOutDate()));
            cs.setInt(8, res.getNumGuests());
            cs.setString(9, res.getSpecialRequests());
            cs.setInt(10, createdBy);

            cs.registerOutParameter(11, Types.VARCHAR);  // p_res_number
            cs.registerOutParameter(12, Types.DECIMAL);  // p_total_amount
            cs.registerOutParameter(13, Types.VARCHAR);  // p_result_msg

            cs.execute();

            String resNumber = cs.getString(11);
            String resultMsg = cs.getString(13);

            if (resultMsg != null && resultMsg.startsWith("SUCCESS")) {
                res.setReservationNumber(resNumber);
            }
            return resultMsg;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding reservation", e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Get reservation details by reservation number.
     */
    public Reservation getReservationByNumber(String reservationNumber) {
        String sql = "{CALL GetReservationDetails(?)}";
        try (Connection conn = dbConn.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, reservationNumber);
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservation", e);
        }
        return null;
    }

    /**
     * Get all reservations.
     */
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "{CALL GetAllReservations()}";
        try (Connection conn = dbConn.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setReservationNumber(rs.getString("reservation_number"));
                r.setGuestName(rs.getString("guest_name"));
                r.setContactNumber(rs.getString("contact_number"));
                r.setRoomNumber(rs.getString("room_number"));
                r.setRoomType(rs.getString("room_type"));
                r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                r.setTotalAmount(rs.getBigDecimal("total_amount"));
                r.setStatus(rs.getString("status"));
                list.add(r);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all reservations", e);
        }
        return list;
    }

    /**
     * Cancel a reservation.
     */
    public String cancelReservation(String reservationNumber) {
        String sql = "{CALL CancelReservation(?,?)}";
        try (Connection conn = dbConn.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, reservationNumber);
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            return cs.getString(2);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cancelling reservation", e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Generate bill for a reservation.
     */
    public String generateBill(String reservationNumber) {
        String sql = "{CALL GenerateBill(?,?)}";
        try (Connection conn = dbConn.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, reservationNumber);
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            return cs.getString(2);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating bill", e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Get bill details for a reservation.
     */
    public Bill getBillDetails(String reservationNumber) {
        String sql = "SELECT b.*, r.reservation_number, g.guest_name, " +
                     "rm.room_number, rm.room_type " +
                     "FROM bills b " +
                     "JOIN reservations r ON b.reservation_id = r.reservation_id " +
                     "JOIN guests g       ON r.guest_id = g.guest_id " +
                     "JOIN rooms rm       ON r.room_id  = rm.room_id " +
                     "WHERE r.reservation_number = ?";

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservationNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                com.oceanview.model.Bill bill = new com.oceanview.model.Bill();
                bill.setBillId(rs.getInt("bill_id"));
                bill.setReservationNumber(rs.getString("reservation_number"));
                bill.setGuestName(rs.getString("guest_name"));
                bill.setRoomNumber(rs.getString("room_number"));
                bill.setRoomType(rs.getString("room_type"));
                bill.setNumNights(rs.getInt("num_nights"));
                bill.setRoomCharge(rs.getBigDecimal("room_charge"));
                bill.setTaxAmount(rs.getBigDecimal("tax_amount"));
                bill.setTotalAmount(rs.getBigDecimal("total_amount"));
                bill.setPaid(rs.getBoolean("is_paid"));
                bill.setGeneratedAt(rs.getString("generated_at"));
                return bill;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting bill", e);
        }
        return null;
    }

    // ── Private helpers ───────────────────────────────────────

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationNumber(rs.getString("reservation_number"));
        r.setGuestName(rs.getString("guest_name"));
        r.setAddress(rs.getString("address"));
        r.setContactNumber(rs.getString("contact_number"));
        r.setEmail(rs.getString("email"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(rs.getString("room_type"));
        r.setRatePerNight(rs.getBigDecimal("rate_per_night"));
        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setTotalAmount(rs.getBigDecimal("total_amount"));
        r.setNumGuests(rs.getInt("num_guests"));
        r.setStatus(rs.getString("status"));
        r.setSpecialRequests(rs.getString("special_requests"));
        r.setCreatedAt(rs.getString("created_at"));
        return r;
    }
}
