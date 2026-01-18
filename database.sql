-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Example Data Generation
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `travel_agency`
--
CREATE DATABASE IF NOT EXISTS `travel_agency`;
USE `travel_agency`;

-- --------------------------------------------------------

--
-- Table structure for table `flights`
--

CREATE TABLE `flights` (
  `flight_id` int(11) NOT NULL,
  `flight_code` varchar(10) NOT NULL,
  `airline` varchar(100) NOT NULL,
  `origin` varchar(100) NOT NULL,
  `destination` varchar(100) NOT NULL,
  `departure_date` date NOT NULL,
  `departure_time` time DEFAULT '08:00:00',
  `arrival_time` time DEFAULT '10:00:00',
  `class` enum('economy','business','first') DEFAULT 'economy',
  `base_price` decimal(10,2) NOT NULL,
  `total_seats` int(11) NOT NULL,
  `available_seats` int(11) NOT NULL,
  `active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `flights`
--

INSERT INTO `flights` (`flight_id`, `flight_code`, `airline`, `origin`, `destination`, `departure_date`, `departure_time`, `arrival_time`, `class`, `base_price`, `total_seats`, `available_seats`, `active`) VALUES
-- EUROPE
(1, 'AF001', 'Air France', 'Paris', 'Rome', '2026-06-01', '08:00:00', '10:00:00', 'economy', 49000.00, 150, 142, 1),
(2, 'AF002', 'Air France', 'Paris', 'Barcelona', '2026-06-02', '09:00:00', '11:00:00', 'economy', 39200.00, 120, 115, 1),
(3, 'BA101', 'British Airways', 'Paris', 'London', '2026-06-03', '10:00:00', '11:30:00', 'business', 58800.00, 80, 78, 1),
(4, 'RY201', 'Ryanair', 'Paris', 'Rome', '2026-06-01', '06:00:00', '08:00:00', 'economy', 25200.00, 180, 175, 1),
(5, 'RY202', 'Ryanair', 'Paris', 'Barcelona', '2026-06-02', '07:00:00', '09:00:00', 'economy', 21000.00, 180, 180, 1),
(6, 'EJ301', 'EasyJet', 'Paris', 'London', '2026-06-03', '11:00:00', '12:30:00', 'economy', 26600.00, 150, 140, 1),

-- MIDDLE EAST
(10, 'EM401', 'Emirates', 'Paris', 'Dubai', '2026-06-10', '14:00:00', '23:00:00', 'economy', 91000.00, 200, 198, 1),
(11, 'EM402', 'Emirates', 'Paris', 'Dubai', '2026-06-11', '13:00:00', '22:00:00', 'business', 168000.00, 100, 95, 1),
(12, 'EM403', 'Emirates', 'Paris', 'Dubai', '2026-06-12', '15:00:00', '00:00:00', 'first', 350000.00, 50, 48, 1),

-- USA (New)
(20, 'DL501', 'Delta Airlines', 'Paris', 'New York', '2026-06-15', '10:00:00', '13:00:00', 'economy', 85000.00, 250, 240, 1),
(21, 'AF502', 'Air France', 'Paris', 'New York', '2026-06-16', '14:00:00', '17:00:00', 'business', 195000.00, 80, 75, 1),
(22, 'AA503', 'American Airlines', 'Paris', 'New York', '2026-06-17', '11:00:00', '14:00:00', 'economy', 82000.00, 220, 200, 1),

-- ASIA (New)
(30, 'JL601', 'Japan Airlines', 'Paris', 'Tokyo', '2026-07-01', '13:00:00', '08:00:00', 'economy', 110000.00, 200, 190, 1),
(31, 'AF602', 'Air France', 'Paris', 'Tokyo', '2026-07-02', '23:00:00', '19:00:00', 'business', 250000.00, 60, 55, 1),
(32, 'NH603', 'All Nippon Airways', 'Paris', 'Tokyo', '2026-07-03', '12:00:00', '07:00:00', 'first', 450000.00, 40, 38, 1),

-- AUSTRALIA (New)
(40, 'QF701', 'Qantas', 'Paris', 'Sydney', '2026-08-01', '10:00:00', '18:00:00', 'economy', 140000.00, 250, 245, 1),
(41, 'QF702', 'Qantas', 'Paris', 'Sydney', '2026-08-05', '09:00:00', '17:00:00', 'business', 380000.00, 70, 68, 1);

-- --------------------------------------------------------

--
-- Table structure for table `hotels`
--

CREATE TABLE `hotels` (
  `hotel_id` int(11) NOT NULL,
  `hotel_code` varchar(10) NOT NULL,
  `name` varchar(255) NOT NULL,
  `city` varchar(100) NOT NULL,
  `address` varchar(500) DEFAULT NULL,
  `stars` int(11) DEFAULT NULL CHECK (`stars` between 1 and 5),
  `price_per_night` decimal(10,2) NOT NULL,
  `total_rooms` int(11) NOT NULL,
  `available_rooms` int(11) NOT NULL,
  `amenities` text DEFAULT NULL,
  `active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `hotels`
--

INSERT INTO `hotels` (`hotel_id`, `hotel_code`, `name`, `city`, `address`, `stars`, `price_per_night`, `total_rooms`, `available_rooms`, `amenities`, `active`) VALUES
-- ROME
(1, 'RM001', 'Grand Hotel Colosseo', 'Rome', 'Via del Colosseo 1', 5, 16800.00, 50, 45, '[\"WiFi\", \"Pool\", \"Spa\", \"Restaurant\", \"Gym\"]', 1),
(2, 'RM002', 'Rome Budget Inn', 'Rome', 'Via Nazionale 45', 3, 7000.00, 80, 70, '[\"WiFi\", \"Parking\"]', 1),

-- BARCELONA
(3, 'BC001', 'W Barcelona', 'Barcelona', 'Barceloneta Boardwalk 1', 5, 28000.00, 100, 95, '[\"WiFi\", \"Pool\", \"Beach\", \"Bar\"]', 1),
(4, 'BC002', 'Barcelona Hostel Central', 'Barcelona', 'Las Ramblas 20', 2, 4900.00, 100, 85, '[\"WiFi\", \"Common Kitchen\"]', 1),

-- LONDON
(5, 'LD001', 'The Ritz London', 'London', '150 Piccadilly', 5, 28000.00, 50, 48, '[\"WiFi\", \"Spa\", \"Fine Dining\", \"Concierge\"]', 1),
(6, 'LD002', 'Premier Inn London', 'London', 'Tower Bridge Road', 3, 9800.00, 150, 140, '[\"WiFi\", \"Breakfast\"]', 1),

-- DUBAI
(7, 'DB001', 'Burj Al Arab', 'Dubai', 'Jumeirah Beach', 5, 70000.00, 50, 45, '[\"WiFi\", \"Pool\", \"Butlers\", \"Helipad\"]', 1),
(8, 'DB002', 'Dubai City Max', 'Dubai', 'Bur Dubai', 3, 11200.00, 200, 180, '[\"WiFi\", \"Pool\", \"Parking\"]', 1),

-- NEW YORK (New)
(9, 'NY001', 'The Plaza', 'New York', '5th Avenue at Central Park S', 5, 65000.00, 100, 95, '[\"WiFi\", \"Spa\", \"Concierge\", \"Historic\"]', 1),
(10, 'NY002', 'Times Square Hotel', 'New York', '7th Ave', 4, 25000.00, 300, 280, '[\"WiFi\", \"Gym\", \"Business Center\"]', 1),
(11, 'NY003', 'Brooklyn Loft Stay', 'New York', 'Williamsburg', 3, 12000.00, 50, 48, '[\"WiFi\", \"Kitchenette\"]', 1),

-- TOKYO (New)
(12, 'TK001', 'Park Hyatt Tokyo', 'Tokyo', 'Shinjuku', 5, 55000.00, 80, 75, '[\"WiFi\", \"Pool\", \"Sky Bar\", \"Gym\"]', 1),
(13, 'TK002', 'Shibuya Granbell', 'Tokyo', 'Shibuya', 3, 14000.00, 100, 90, '[\"WiFi\", \"Modern Design\"]', 1),

-- SYDNEY (New)
(14, 'SY001', 'Sydney Opera View', 'Sydney', 'Circular Quay', 5, 45000.00, 60, 55, '[\"WiFi\", \"Opera View\", \"Pool\"]', 1),
(15, 'SY002', 'Bondi Beach House', 'Sydney', 'Bondi Beach', 4, 18000.00, 40, 35, '[\"WiFi\", \"Beach Access\", \"Surf Rendering\"]', 1);

-- --------------------------------------------------------

--
-- Table structure for table `reservations`
--

CREATE TABLE `reservations` (
  `reservation_id` int(11) NOT NULL,
  `booking_reference` varchar(20) DEFAULT NULL,
  `user_name` varchar(100) NOT NULL,
  `user_email` varchar(255) DEFAULT NULL,
  `user_phone` varchar(50) DEFAULT NULL,
  `flight_id` int(11) NOT NULL,
  `hotel_id` int(11) NOT NULL,
  `destination` varchar(100) DEFAULT NULL,
  `departure_date` date DEFAULT NULL,
  `return_date` date DEFAULT NULL,
  `adults` int(11) DEFAULT NULL,
  `children` int(11) DEFAULT NULL,
  `rooms` int(11) NOT NULL DEFAULT 1,
  `total_price` float NOT NULL,
  `booking_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` varchar(20) DEFAULT 'CONFIRMED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservations`
--

INSERT INTO `reservations` (`reservation_id`, `booking_reference`, `user_name`, `user_email`, `user_phone`, `flight_id`, `hotel_id`, `destination`, `departure_date`, `return_date`, `adults`, `children`, `rooms`, `total_price`, `booking_date`, `status`) VALUES
(1, 'BK-2026-X8Y2', 't_01', 't01@mail.com', '5550101', 1, 1, 'Rome', '2026-06-01', '2026-06-05', 2, 0, 1, 165200, '2026-05-15 10:00:00', 'CONFIRMED'),
(2, 'BK-2026-A9B3', 'f_99', 'f99@net.org', '5559999', 10, 7, 'Dubai', '2026-06-10', '2026-06-15', 1, 0, 1, 441000, '2026-05-20 14:30:00', 'CONFIRMED'),
(3, 'BK-2026-K2L5', 'u_777', 'u777@web.com', '5557777', 30, 12, 'Tokyo', '2026-07-01', '2026-07-07', 2, 1, 2, 440000, '2026-06-01 09:15:00', 'CONFIRMED');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT 'Guest User',
  `phone` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `last_login` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `email`, `full_name`, `phone`, `created_at`, `last_login`) VALUES
(1, 't01@mail.com', 't_01', '5550101', '2026-05-10 10:00:00', '2026-05-15 10:00:00'),
(2, 'f99@net.org', 'f_99', '5559999', '2026-05-12 11:20:00', '2026-05-20 14:30:00'),
(3, 'u777@web.com', 'u_777', '5557777', '2026-05-30 09:00:00', '2026-06-01 09:15:00'),
(4, 'guest@temp.com', 'g_123', '0000000', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `flights`
--
ALTER TABLE `flights`
  ADD PRIMARY KEY (`flight_id`),
  ADD UNIQUE KEY `flight_code` (`flight_code`),
  ADD KEY `idx_flights_destination` (`destination`,`departure_date`);

--
-- Indexes for table `hotels`
--
ALTER TABLE `hotels`
  ADD PRIMARY KEY (`hotel_id`),
  ADD UNIQUE KEY `hotel_code` (`hotel_code`),
  ADD KEY `idx_hotels_city` (`city`);

--
-- Indexes for table `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`reservation_id`),
  ADD UNIQUE KEY `booking_reference` (`booking_reference`),
  ADD KEY `flight_id` (`flight_id`),
  ADD KEY `hotel_id` (`hotel_id`),
  ADD KEY `idx_user_email` (`user_email`),
  ADD KEY `idx_booking_reference` (`booking_reference`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_departure_date` (`departure_date`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `flights`
--
ALTER TABLE `flights`
  MODIFY `flight_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=100;

--
-- AUTO_INCREMENT for table `hotels`
--
ALTER TABLE `hotels`
  MODIFY `hotel_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=100;

--
-- AUTO_INCREMENT for table `reservations`
--
ALTER TABLE `reservations`
  MODIFY `reservation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`flight_id`),
  ADD CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
