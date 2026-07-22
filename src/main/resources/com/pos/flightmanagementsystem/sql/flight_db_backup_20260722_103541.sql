SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `backup_history`;
CREATE TABLE `backup_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `backup_date` datetime NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_size` varchar(50) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `backup_history` VALUES (1, '2026-07-20T22:50:50', 'flight_db_backup_20260720_225049.sql', '3 KB', 'Auto Backup');
INSERT INTO `backup_history` VALUES (2, '2026-07-20T22:51:16', 'flight_db_export_20260720_225105.sql', '4 KB', 'Export SQL');
INSERT INTO `backup_history` VALUES (3, '2026-07-20T23:07:21', 'flight_db_backup_20260720_230721.sql', '4 KB', 'Auto Backup');
INSERT INTO `backup_history` VALUES (4, '2026-07-21T10:05:28', 'flight_db_export_20260721_100516.sql', '4 KB', 'Export SQL');
DROP TABLE IF EXISTS `booking`;
CREATE TABLE `booking` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `flight_id` int NOT NULL,
  `passenger_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `seat_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `booking_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pnr_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `staff_id` int DEFAULT NULL,
  `seat_id` int DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `nrc_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Confirmed',
  PRIMARY KEY (`booking_id`),
  KEY `flight_id` (`flight_id`),
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flight` (`flight_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `flight`;
CREATE TABLE `flight` (
  `flight_id` int NOT NULL AUTO_INCREMENT,
  `flight_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `departure_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `arrival_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `departure_time` datetime NOT NULL,
  `arrival_time` datetime NOT NULL,
  `total_seats` int NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Scheduled',
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
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Pending',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`),
  KEY `booking_id` (`booking_id`),
  CONSTRAINT `pending_request_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
  `seat_id` int NOT NULL AUTO_INCREMENT,
  `flight_id` int NOT NULL,
  `airline` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `seat_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `is_available` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`seat_id`),
  KEY `flight_id` (`flight_id`),
  CONSTRAINT `seat_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flight` (`flight_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `image_path` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `users` VALUES (1, 'SuperAdmin', 'luuarr344@gmail.com', 'superadmin', '2026-07-17 04:22:26.0', 'superadmin', NULL);
INSERT INTO `users` VALUES (3, 'Nay Linn Oo', 'nayh72335@gmail.com', '12345', '2026-07-21 10:59:07.0', 'admin', 'src/main/resources/com/pos/flightmanagementsystem/image/profile_1784608147787.png');
INSERT INTO `users` VALUES (4, 'admin', 'admin@gmail.com', 'admin', '2026-07-21 11:07:59.0', 'admin', 'profile_1784608679154.jpg');
INSERT INTO `users` VALUES (5, 'Ye Naing Oo', 'yenaing123@gmail.com', '1234', '2026-07-21 22:04:00.0', 'admin', NULL);
INSERT INTO `users` VALUES (6, 'Soe Thiha Paing', 'thiha123@gmail.com', '1234', '2026-07-21 22:05:46.0', 'staff', 'profile_1784648146003.jpg');
INSERT INTO `users` VALUES (7, 'Sayar Htoo', 'htoohtoo123@gmail.com', '1234', '2026-07-21 22:41:08.0', 'staff', 'profile_1784650267990.jpg');
INSERT INTO `users` VALUES (8, 'Pyae Phyo Aung Khant', 'pyaephyo123@gmail.com', '1234', '2026-07-22 09:14:07.0', 'staff', NULL);
INSERT INTO `users` VALUES (9, 'Lwin Lwin Htun', 'Lwin123@gmail.com', '1234', '2026-07-22 09:15:15.0', 'staff', NULL);
INSERT INTO `users` VALUES (10, 'Khin Khin Khant', 'Khin123@gmail.com', '1234', '2026-07-22 09:15:47.0', 'staff', NULL);
INSERT INTO `users` VALUES (11, 'Ngu War Han', 'ngu123@gmail.com', '1234', '2026-07-22 09:16:19.0', 'admin', NULL);
INSERT INTO `users` VALUES (12, 'Nan Htet Thiri Naing', 'nahhtet123@gmail.com', '1234', '2026-07-22 09:16:58.0', 'admin', NULL);
INSERT INTO `users` VALUES (13, 'Aung Phone Pyae', 'aung123@gmail.com', '1234', '2026-07-22 10:34:42.0', 'staff', NULL);

SET FOREIGN_KEY_CHECKS=1;
