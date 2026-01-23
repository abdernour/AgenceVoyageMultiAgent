package com.agencevoyage;

import com.agencevoyage.utils.DatabaseManager;
import java.sql.*;

public class TestDatabase {
    public static void main(String[] args) {
        System.out.println("🔍 Testing Database Connection...\n");

        // Test connection
        boolean connected = DatabaseManager.testConnection();

        if (connected) {
            System.out.println("\nFetching sample data...\n");

            // Test flights
            testFlights();

            // Test hotels
            testHotels();

            // Test users
            testUsers();

        } else {
            System.err.println("❌ Cannot proceed without database connection!");
        }

        DatabaseManager.closeConnection();
    }

    private static void testFlights() {
        String sql = "SELECT flight_code, airline, destination, base_price " +
                "FROM flights WHERE active = TRUE LIMIT 5";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("✈️  FLIGHTS:");
            System.out.println("─".repeat(60));

            while (rs.next()) {
                System.out.printf("%-10s %-20s %-15s %8.2f€\n",
                        rs.getString("flight_code"),
                        rs.getString("airline"),
                        rs.getString("destination"),
                        rs.getFloat("base_price"));
            }
            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testHotels() {
        String sql = "SELECT hotel_code, name, city, stars, price_per_night " +
                "FROM hotels WHERE active = TRUE LIMIT 5";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("🏨 HOTELS:");
            System.out.println("─".repeat(60));

            while (rs.next()) {
                System.out.printf("%-10s %-25s %-12s %d★ %8.2f€/night\n",
                        rs.getString("hotel_code"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getInt("stars"),
                        rs.getFloat("price_per_night"));
            }
            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testUsers() {
        String sql = "SELECT email, full_name FROM users";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("👤 USERS:");
            System.out.println("─".repeat(60));

            while (rs.next()) {
                System.out.printf("%-30s %s\n",
                        rs.getString("email"),
                        rs.getString("full_name"));
            }
            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}