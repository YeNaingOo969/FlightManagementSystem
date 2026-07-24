SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `backups`;
CREATE TABLE `backups` (
  `id` int NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL,
  `file_size` varchar(50) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  `backup_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `bookings`;
CREATE TABLE `bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `pnr_code` varchar(10) NOT NULL,
  `flight_id` int NOT NULL,
  `passenger_id` int NOT NULL,
  `seat_id` int NOT NULL,
  `staff_id` int NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Confirmed',
  `booking_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `pnr_code` (`pnr_code`),
  KEY `flight_id` (`flight_id`),
  KEY `passenger_id` (`passenger_id`),
  KEY `seat_id` (`seat_id`),
  KEY `staff_id` (`staff_id`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`flight_id`),
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`passenger_id`) REFERENCES `passengers` (`passenger_id`),
  CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`seat_id`),
  CONSTRAINT `bookings_ibfk_4` FOREIGN KEY (`staff_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `cities`;
CREATE TABLE `cities` (
  `city_id` int NOT NULL AUTO_INCREMENT,
  `city_code` varchar(10) NOT NULL,
  `city_name` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`city_id`),
  UNIQUE KEY `city_code` (`city_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `cities` VALUES (1, 'RGN', 'Yangon', 'Myanmar', '2026-07-22 21:25:23.0');
INSERT INTO `cities` VALUES (2, 'MDL', 'Mandalay', 'Myanmar', '2026-07-22 21:25:23.0');
INSERT INTO `cities` VALUES (3, 'BKK', 'Bangkok', 'Thailand', '2026-07-22 21:25:23.0');
INSERT INTO `cities` VALUES (4, 'SIN', 'Singapore', 'Singapore', '2026-07-22 21:25:23.0');
DROP TABLE IF EXISTS `flights`;
CREATE TABLE `flights` (
  `flight_id` int NOT NULL AUTO_INCREMENT,
  `flight_number` varchar(10) NOT NULL,
  `airline` varchar(100) NOT NULL,
  `departure_city_id` int NOT NULL,
  `arrival_city_id` int NOT NULL,
  `departure_time` datetime NOT NULL,
  `arrival_time` datetime NOT NULL,
  `total_seats` int NOT NULL,
  `base_price` decimal(10,2) NOT NULL,
  `status` varchar(20) DEFAULT 'Scheduled',
  PRIMARY KEY (`flight_id`),
  KEY `departure_city_id` (`departure_city_id`),
  KEY `arrival_city_id` (`arrival_city_id`),
  CONSTRAINT `flights_ibfk_1` FOREIGN KEY (`departure_city_id`) REFERENCES `cities` (`city_id`),
  CONSTRAINT `flights_ibfk_2` FOREIGN KEY (`arrival_city_id`) REFERENCES `cities` (`city_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `logs`;
CREATE TABLE `logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `action` varchar(255) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `passengers`;
CREATE TABLE `passengers` (
  `passenger_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `nrc_passport_no` varchar(50) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `nationality` varchar(50) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`passenger_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `requests`;
CREATE TABLE `requests` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `staff_id` int DEFAULT NULL,
  `admin_id` int DEFAULT NULL,
  `reason` text,
  `status` varchar(20) DEFAULT 'Pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`),
  KEY `booking_id` (`booking_id`),
  KEY `staff_id` (`staff_id`),
  KEY `admin_id` (`admin_id`),
  CONSTRAINT `requests_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `requests_ibfk_2` FOREIGN KEY (`staff_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `requests_ibfk_3` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `seats`;
CREATE TABLE `seats` (
  `seat_id` int NOT NULL AUTO_INCREMENT,
  `flight_id` int NOT NULL,
  `seat_number` varchar(10) NOT NULL,
  `seat_class` varchar(50) NOT NULL,
  `is_available` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`seat_id`),
  KEY `flight_id` (`flight_id`),
  CONSTRAINT `seats_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`flight_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  `status` varchar(20) DEFAULT 'Active',
  `image_path` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `users` VALUES (8, 'SuperAdmin', NULL, 'superadmin', 'superadmin', 'Active', NULL, '2026-07-22 21:32:18.0');
INSERT INTO `users` VALUES (9, 'Admin', NULL, 'admin', 'admin', 'Active', NULL, '2026-07-22 21:32:18.0');
INSERT INTO `users` VALUES (10, 'Staff', NULL, 'staff', 'staff', 'Active', NULL, '2026-07-22 21:32:18.0');

SET FOREIGN_KEY_CHECKS=1;
