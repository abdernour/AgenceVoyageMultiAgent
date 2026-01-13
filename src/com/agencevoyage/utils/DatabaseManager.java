package com.agencevoyage.utils;

import java.sql.*;

public class DatabaseManager {

    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/travel_agency";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // XAMPP default is empty

    private static Connection connection;

    /**
     * Get database connection (singleton pattern)
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Database connected successfully");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database test successful!");

                // Test query
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM flights");
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("   Found " + count + " flights in database");
                }
                rs.close();
                stmt.close();

                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Database test failed!");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Execute a SELECT query and return ResultSet
     */
    public static ResultSet executeQuery(String sql) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Execute INSERT, UPDATE, DELETE
     */
    public static boolean executeUpdate(String sql) {
        try {
            Statement stmt = getConnection().createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get PreparedStatement for parameterized queries
     */
    public static PreparedStatement prepareStatement(String sql) {
        try {
            return getConnection().prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}