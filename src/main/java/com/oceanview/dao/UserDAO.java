package com.oceanview.dao;

import com.oceanview.model.Room;
import com.oceanview.model.User;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserDAO - handles user authentication.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private final DatabaseConnection dbConn;

    public UserDAO() {
        this.dbConn = DatabaseConnection.getInstance();
    }

    public UserDAO(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    /**
     * Authenticate user via stored procedure.
     * @return User object if valid, null otherwise.
     */
    public User authenticate(String username, String password) {
        String sql = "{CALL AuthenticateUser(?,?,?,?,?)}";
        try (Connection conn = dbConn.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, username);
            cs.setString(2, password);
            cs.registerOutParameter(3, Types.INTEGER); // p_user_id
            cs.registerOutParameter(4, Types.VARCHAR); // p_role
            cs.registerOutParameter(5, Types.VARCHAR); // p_name

            cs.execute();

            int userId = cs.getInt(3);
            if (userId > 0) {
                return new User(userId, username, cs.getString(5), cs.getString(4));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Authentication error", e);
        }
        return null;
    }
}
