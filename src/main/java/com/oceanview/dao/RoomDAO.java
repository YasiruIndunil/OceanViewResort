package com.oceanview.dao;

import com.oceanview.model.Room;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RoomDAO - handles room data access.
 */
public class RoomDAO {

    private static final Logger LOGGER = Logger.getLogger(RoomDAO.class.getName());
    private final DatabaseConnection dbConn;

    public RoomDAO() {
        this.dbConn = DatabaseConnection.getInstance();
    }

    public RoomDAO(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    /** Get all available rooms. */
    public List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE is_available = 1 ORDER BY room_type, room_number";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching available rooms", e);
        }
        return rooms;
    }

    /** Get all rooms. */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching rooms", e);
        }
        return rooms;
    }

    /** Get room by ID. */
    public Room getRoomById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching room by id", e);
        }
        return null;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(rs.getString("room_type"));
        r.setRatePerNight(rs.getBigDecimal("rate_per_night"));
        r.setMaxGuests(rs.getInt("max_guests"));
        r.setAvailable(rs.getBoolean("is_available"));
        return r;
    }
}
