SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `backup_history`;
CREATE TABLE `backup_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `backup_date` datetime NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_size` varchar(50) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `booking`;
CREATE TABLE `booking` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `flight_id` int NOT NULL,
  `passenger_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `seat_number` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `booking_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pnr_code` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `staff_id` int DEFAULT NULL,
  `seat_id` int DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `nrc_no` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'Confirmed',
  PRIMARY KEY (`booking_id`),
  KEY `flight_id` (`flight_id`),
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flight` (`flight_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `flight`;
CREATE TABLE `flight` (
  `flight_id` int NOT NULL AUTO_INCREMENT,
  `flight_number` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `departure_location` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `arrival_location` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `departure_time` datetime NOT NULL,
  `arrival_time` datetime NOT NULL,
  `total_seats` int NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'Scheduled',
  PRIMARY KEY (`flight_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `pending_request`;
CREATE TABLE `pending_request` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `staff_id` int DEFAULT NULL,
  `admin_id` int DEFAULT NULL,
  `reason` text COLLATE utf8mb4_general_ci,
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'Pending',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`),
  KEY `booking_id` (`booking_id`),
  CONSTRAINT `pending_request_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
  `seat_id` int NOT NULL AUTO_INCREMENT,
  `flight_id` int NOT NULL,
  `airline` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `seat_number` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `is_available` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`seat_id`),
  KEY `flight_id` (`flight_id`),
  CONSTRAINT `seat_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flight` (`flight_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `role` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `users` VALUES (1, 'SuperAdmin', 'luuarr344@gmail.com', 'superadmin', '2026-07-17 04:22:26.0', 'superadmin');

SET FOREIGN_KEY_CHECKS=1;
