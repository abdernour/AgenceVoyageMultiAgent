package com.agencevoyage.utils;

import java.sql.*;
import java.util.Date;

public class ReservationManager {

    /**
     * Create a new reservation - UPDATED to use explicit room count
     */
    public static boolean createReservation(
            String userName,
            String email,
            String phone,
            int flightId,
            int hotelId,
            String destination,
            Date departureDate,
            Date returnDate,
            int adults,
            int children,
            int rooms,              // NEW PARAMETER
            float totalPrice
    ) {
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) {
            System.err.println("ERROR: Database connection failed");
            return false;
        }

        try {
            conn.setAutoCommit(false);

            // 1. Create/Get User
            int userId = getOrCreateUser(conn, userName, email, phone);
            if (userId == -1) {
                conn.rollback();
                return false;
            }

            // 2. Check availability before booking
            if (!checkAvailability(conn, flightId, hotelId, adults + children, rooms)) {
                conn.rollback();
                System.err.println("ERROR: Insufficient availability");
                return false;
            }

            // 3. Generate booking reference
            String bookingRef = generateBookingReference(conn);

            // 4. Create Reservation - UPDATED to include rooms
            String resSQL = "INSERT INTO reservations (booking_reference, user_name, user_email, user_phone, " +
                    "flight_id, hotel_id, destination, departure_date, return_date, " +
                    "adults, children, rooms, total_price, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";

            PreparedStatement resStmt = conn.prepareStatement(resSQL, Statement.RETURN_GENERATED_KEYS);
            resStmt.setString(1, bookingRef);
            resStmt.setString(2, userName);
            resStmt.setString(3, email);
            resStmt.setString(4, phone);
            resStmt.setInt(5, flightId);
            resStmt.setInt(6, hotelId);
            resStmt.setString(7, destination);
            resStmt.setDate(8, new java.sql.Date(departureDate.getTime()));
            resStmt.setDate(9, new java.sql.Date(returnDate.getTime()));
            resStmt.setInt(10, adults);
            resStmt.setInt(11, children);
            resStmt.setInt(12, rooms);  // NEW
            resStmt.setFloat(13, totalPrice);

            if (resStmt.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            ResultSet keys = resStmt.getGeneratedKeys();
            int reservationId = 0;
            if (keys.next()) reservationId = keys.getInt(1);

            System.out.println("✓ Reservation created: " + bookingRef);

            // 5. Update Flight Seats
            int totalPassengers = adults + children;
            String updateFlightSQL = "UPDATE flights SET available_seats = available_seats - ? " +
                    "WHERE flight_id = ? AND available_seats >= ?";
            PreparedStatement flightStmt = conn.prepareStatement(updateFlightSQL);
            flightStmt.setInt(1, totalPassengers);
            flightStmt.setInt(2, flightId);
            flightStmt.setInt(3, totalPassengers);

            if (flightStmt.executeUpdate() == 0) {
                conn.rollback();
                System.err.println("ERROR: Could not update flight seats");
                return false;
            }

            // 6. Update Hotel Rooms - UPDATED to use explicit room count
            String updateHotelSQL = "UPDATE hotels SET available_rooms = available_rooms - ? " +
                    "WHERE hotel_id = ? AND available_rooms >= ?";
            PreparedStatement hotelStmt = conn.prepareStatement(updateHotelSQL);
            hotelStmt.setInt(1, rooms);  // CHANGED: Use explicit rooms instead of calculation
            hotelStmt.setInt(2, hotelId);
            hotelStmt.setInt(3, rooms);

            if (hotelStmt.executeUpdate() == 0) {
                conn.rollback();
                System.err.println("ERROR: Could not update hotel rooms");
                return false;
            }

            conn.commit();
            System.out.println("✓ Booking completed successfully!");
            System.out.println("  " + totalPassengers + " passengers, " + rooms + " rooms");
            return true;

        } catch (SQLException e) {
            System.err.println("ERROR: Reservation failed - " + e.getMessage());
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static String generateBookingReference(Connection conn) throws SQLException {
        String sql = "SELECT MAX(reservation_id) as max_id FROM reservations";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        int nextId = 1;
        if (rs.next()) {
            nextId = rs.getInt("max_id") + 1;
        }

        rs.close();
        stmt.close();

        return String.format("BK-%d-%06d", java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), nextId);
    }

    private static int getOrCreateUser(Connection conn, String name, String email, String phone)
            throws SQLException {
        String checkSQL = "SELECT user_id FROM users WHERE email = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
        checkStmt.setString(1, email);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int userId = rs.getInt("user_id");
            rs.close();

            String updateSQL = "UPDATE users SET full_name = ?, phone = ?, last_login = NOW() WHERE user_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSQL);
            updateStmt.setString(1, name);
            updateStmt.setString(2, phone);
            updateStmt.setInt(3, userId);
            updateStmt.executeUpdate();

            return userId;
        }

        String insertSQL = "INSERT INTO users (full_name, email, phone, created_at) " +
                "VALUES (?, ?, ?, NOW())";
        PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, name);
        insertStmt.setString(2, email);
        insertStmt.setString(3, phone);
        insertStmt.executeUpdate();

        ResultSet keys = insertStmt.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }

        return -1;
    }

    /**
     * Check availability - UPDATED to use explicit room count
     */
    private static boolean checkAvailability(Connection conn, int flightId, int hotelId,
                                             int passengers, int rooms) throws SQLException {
        String sql = "SELECT " +
                "(SELECT available_seats FROM flights WHERE flight_id = ?) as flight_seats, " +
                "(SELECT available_rooms FROM hotels WHERE hotel_id = ?) as hotel_rooms";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, flightId);
        stmt.setInt(2, hotelId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int availableSeats = rs.getInt("flight_seats");
            int availableRooms = rs.getInt("hotel_rooms");
            boolean available = availableSeats >= passengers && availableRooms >= rooms;

            if (!available) {
                System.err.println("Not enough availability: Seats=" + availableSeats +
                        "/" + passengers + ", Rooms=" + availableRooms + "/" + rooms);
            }

            return available;
        }

        return false;
    }

    /**
     * Cancel reservation - UPDATED to restore explicit room count
     */
    public static boolean cancelReservation(int reservationId) {
        Connection conn = DatabaseManager.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);

            // Get reservation details - UPDATED to include rooms
            String selectSQL = "SELECT * FROM reservations WHERE reservation_id = ? AND status = 'CONFIRMED'";
            PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
            selectStmt.setInt(1, reservationId);
            ResultSet rs = selectStmt.executeQuery();

            if (!rs.next()) {
                System.err.println("Reservation not found or already cancelled");
                conn.rollback();
                return false;
            }

            int flightId = rs.getInt("flight_id");
            int hotelId = rs.getInt("hotel_id");
            int adults = rs.getInt("adults");
            int children = rs.getInt("children");
            int rooms = rs.getInt("rooms");  // NEW
            String bookingRef = rs.getString("booking_reference");

            // Update reservation status
            String updateResSQL = "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?";
            PreparedStatement updateResStmt = conn.prepareStatement(updateResSQL);
            updateResStmt.setInt(1, reservationId);
            updateResStmt.executeUpdate();

            // Restore flight seats
            int totalPassengers = adults + children;
            String updateFlightSQL = "UPDATE flights SET available_seats = available_seats + ? WHERE flight_id = ?";
            PreparedStatement updateFlightStmt = conn.prepareStatement(updateFlightSQL);
            updateFlightStmt.setInt(1, totalPassengers);
            updateFlightStmt.setInt(2, flightId);
            updateFlightStmt.executeUpdate();

            // Restore hotel rooms - UPDATED to use explicit room count
            String updateHotelSQL = "UPDATE hotels SET available_rooms = available_rooms + ? WHERE hotel_id = ?";
            PreparedStatement updateHotelStmt = conn.prepareStatement(updateHotelSQL);
            updateHotelStmt.setInt(1, rooms);  // CHANGED
            updateHotelStmt.setInt(2, hotelId);
            updateHotelStmt.executeUpdate();

            conn.commit();
            System.out.println("✓ Booking " + bookingRef + " cancelled successfully");
            System.out.println("  Restored: " + totalPassengers + " seats, " + rooms + " rooms");
            return true;

        } catch (SQLException e) {
            System.err.println("ERROR: Cancellation failed - " + e.getMessage());
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ResultSet getUserBookings(String email) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String sql = "SELECT r.*, f.airline, f.flight_code, h.name as hotel_name " +
                    "FROM reservations r " +
                    "LEFT JOIN flights f ON r.flight_id = f.flight_id " +
                    "LEFT JOIN hotels h ON r.hotel_id = h.hotel_id " +
                    "WHERE r.user_email = ? " +
                    "ORDER BY r.booking_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}