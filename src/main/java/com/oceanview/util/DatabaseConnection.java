package com.oceanview.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseConnection - Singleton pattern for DB connection management.
 * Design Pattern: Singleton
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Singleton instance
    private static DatabaseConnection instance;

    // Connection properties
    private static final String PROPERTIES_FILE = "/db.properties";
    private String url;
    private String username;
    private String password;

    // Private constructor - Singleton
    private DatabaseConnection() {
        loadProperties();
    }

    /**
     * Returns the single instance (Singleton pattern).
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                url      = prop.getProperty("db.url");
                username = prop.getProperty("db.username");
                password = prop.getProperty("db.password");
            } else {
                // Fallback defaults
                url      = "jdbc:mysql://localhost:3306/ocean_view_resort?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                username = "root";
                password = "root";
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load database properties", e);
        }
    }

    /**
     * Returns a new JDBC connection.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Safely close a connection.
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }
}
