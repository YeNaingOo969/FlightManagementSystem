SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `backups`;
CREATE TABLE `backups` (
  `id` int NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL,
  `file_size` varchar(50) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  `backup_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `backups` VALUES (1, 'flight_db_backup_20260724_220440.sql', '6 KB', 'Auto Backup', '2026-07-24 22:04:41.0');
INSERT INTO `backups` VALUES (2, 'flight_db_export_20260724_220447.sql', '6 KB', 'Export SQL', '2026-07-24 22:04:53.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `bookings` VALUES (3, 'F0FF75', 2, 3, 3, 12, 1656000.00, 'Cash', 'Confirmed', '2026-08-01 04:12:46.0');
INSERT INTO `bookings` VALUES (4, 'E4D9C6', 2, 4, 4, 12, 1398000.00, 'Cash', 'Confirmed', '2026-08-01 04:29:43.0');
INSERT INTO `bookings` VALUES (5, '4CD533', 2, 5, 5, 12, 1656000.00, 'Cash', 'Confirmed', '2026-08-01 04:50:23.0');
INSERT INTO `bookings` VALUES (6, 'BDF337', 2, 6, 6, 12, 980000.00, 'Cash', 'Confirmed', '2026-08-01 04:56:37.0');
INSERT INTO `bookings` VALUES (7, '13A37C', 2, 7, 7, 12, 980000.00, 'Cash', 'Confirmed', '2026-08-01 05:00:46.0');
INSERT INTO `bookings` VALUES (8, 'BDCE04', 2, 8, 8, 12, 1398000.00, 'Cash', 'Confirmed', '2026-08-01 05:05:58.0');
INSERT INTO `bookings` VALUES (9, '17AF6E', 2, 11, 9, 12, 1656000.00, 'CB Pay', 'Confirmed', '2026-08-01 07:14:15.0');
DROP TABLE IF EXISTS `cities`;
CREATE TABLE `cities` (
  `city_id` int NOT NULL AUTO_INCREMENT,
  `city_code` varchar(10) NOT NULL,
  `city_name` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`city_id`),
  UNIQUE KEY `city_code` (`city_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `cities` VALUES (1, 'RGN', 'Yangon', 'Myanmar', '2026-07-22 21:25:23.0');
INSERT INTO `cities` VALUES (2, 'MDL', 'Mandalay', 'Myanmar', '2026-07-22 21:25:23.0');
INSERT INTO `cities` VALUES (3, 'BKK', 'Bangkok', 'Thailand', '2026-07-22 21:25:23.0');
INSERT INTO `cities` VALUES (4, 'SIN', 'Singapore', 'Singapore', '2026-07-22 21:25:23.0');
INSERT INTO `cities` VALUES (5, 'Chee', 'Chee', 'Myanmar', '2026-07-29 21:37:54.0');
INSERT INTO `cities` VALUES (6, 'adaf', 'adaf', 'adaf', '2026-07-29 21:39:27.0');
INSERT INTO `cities` VALUES (7, 'kdkd', 'kdkd', 'kdkd', '2026-07-29 21:54:44.0');
INSERT INTO `cities` VALUES (8, 'aaa', 'aaa', 'aaa', '2026-07-29 22:01:19.0');
INSERT INTO `cities` VALUES (9, 'kfkfk', 'vvvv', 'vvvv', '2026-07-29 22:07:41.0');
INSERT INTO `cities` VALUES (10, 'ddd', 'ddddd', 'dddd', '2026-07-30 11:02:24.0');
INSERT INTO `cities` VALUES (11, 'ffff', 'fff', 'ffff', '2026-07-30 11:02:53.0');
INSERT INTO `cities` VALUES (12, 'nnn', 'mn', 'nnn', '2026-07-30 11:17:46.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `flights` VALUES (1, 'Fl-101', 'Myanmar', 4, 2, '2026-08-06T06:40', '2026-08-06T09:45', 100, 2500000.00, 'Scheduled');
INSERT INTO `flights` VALUES (2, 'ffff', 'ffff', 6, 3, '2026-07-29T08:30', '2026-07-30T07:40', 100, 12330.00, 'Scheduled');
INSERT INTO `flights` VALUES (3, 'kdkd', 'adjdj', 2, 3, '2026-08-05T06:45', '2026-08-08T07:35', 100, 1000000.00, 'Scheduled');
DROP TABLE IF EXISTS `logs`;
CREATE TABLE `logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `action` varchar(255) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `logs` VALUES (1, 11, 'Login Successful', '2026-07-30 09:29:50.0');
INSERT INTO `logs` VALUES (2, 11, 'Login Successful', '2026-07-30 09:32:47.0');
INSERT INTO `logs` VALUES (3, 11, 'Login Successful', '2026-07-30 09:38:05.0');
INSERT INTO `logs` VALUES (4, 11, 'Login Successful', '2026-07-30 09:40:55.0');
INSERT INTO `logs` VALUES (5, 11, 'Login Successful', '2026-07-30 09:50:08.0');
INSERT INTO `logs` VALUES (6, 11, 'Log out', '2026-07-30 09:50:49.0');
INSERT INTO `logs` VALUES (7, 8, 'Login Successful', '2026-07-30 09:50:57.0');
INSERT INTO `logs` VALUES (8, 8, 'Login Successful', '2026-07-30 09:54:16.0');
INSERT INTO `logs` VALUES (9, 8, 'Log out', '2026-07-30 09:54:29.0');
INSERT INTO `logs` VALUES (10, 11, 'Login Successful', '2026-07-30 09:54:42.0');
INSERT INTO `logs` VALUES (11, 11, 'Log out', '2026-07-30 09:54:47.0');
INSERT INTO `logs` VALUES (12, 8, 'Login Successful', '2026-07-30 09:54:57.0');
INSERT INTO `logs` VALUES (13, 9, 'Login Successful', '2026-07-30 10:14:50.0');
INSERT INTO `logs` VALUES (14, 9, 'Log out', '2026-07-30 10:15:05.0');
INSERT INTO `logs` VALUES (15, 8, 'Login Successful', '2026-07-30 10:15:23.0');
INSERT INTO `logs` VALUES (16, 8, 'Login Successful', '2026-07-30 10:21:01.0');
INSERT INTO `logs` VALUES (17, 8, 'Log out', '2026-07-30 10:21:42.0');
INSERT INTO `logs` VALUES (18, 9, 'Login Successful', '2026-07-30 10:21:52.0');
INSERT INTO `logs` VALUES (19, 9, 'Log out', '2026-07-30 10:21:55.0');
INSERT INTO `logs` VALUES (20, 11, 'Login Successful', '2026-07-30 10:22:03.0');
INSERT INTO `logs` VALUES (21, 11, 'Login Successful', '2026-07-30 10:29:05.0');
INSERT INTO `logs` VALUES (22, 9, 'Login Successful', '2026-07-30 10:33:41.0');
INSERT INTO `logs` VALUES (23, 9, 'Login Successful', '2026-07-30 10:44:10.0');
INSERT INTO `logs` VALUES (24, 11, 'Login Successful', '2026-07-30 10:49:38.0');
INSERT INTO `logs` VALUES (25, 11, 'Login Successful', '2026-07-30 10:54:26.0');
INSERT INTO `logs` VALUES (26, 11, 'Login Successful', '2026-07-30 11:01:34.0');
INSERT INTO `logs` VALUES (27, 11, 'Login Successful', '2026-07-30 11:14:07.0');
INSERT INTO `logs` VALUES (28, 11, 'Log out', '2026-07-30 11:19:13.0');
INSERT INTO `logs` VALUES (29, 10, 'Login Successful', '2026-07-30 11:19:21.0');
INSERT INTO `logs` VALUES (30, 10, 'Log out', '2026-07-30 11:19:31.0');
INSERT INTO `logs` VALUES (31, 11, 'Login Successful', '2026-07-30 11:20:09.0');
INSERT INTO `logs` VALUES (32, 11, 'Login Successful', '2026-07-31 13:49:19.0');
INSERT INTO `logs` VALUES (33, 11, 'Log out', '2026-07-31 13:58:38.0');
INSERT INTO `logs` VALUES (34, 12, 'Login Successful', '2026-07-31 13:58:47.0');
INSERT INTO `logs` VALUES (35, 12, 'Login Successful', '2026-07-31 14:43:35.0');
INSERT INTO `logs` VALUES (36, 12, 'Log out', '2026-07-31 14:59:11.0');
INSERT INTO `logs` VALUES (37, 11, 'Login Successful', '2026-07-31 14:59:19.0');
INSERT INTO `logs` VALUES (38, 12, 'Login Successful', '2026-07-31 15:02:36.0');
INSERT INTO `logs` VALUES (39, 12, 'Log out', '2026-07-31 15:04:08.0');
INSERT INTO `logs` VALUES (40, 11, 'Login Successful', '2026-07-31 15:05:28.0');
INSERT INTO `logs` VALUES (41, 12, 'Login Successful', '2026-08-01 03:01:30.0');
INSERT INTO `logs` VALUES (42, 12, 'Login Successful', '2026-08-01 03:12:13.0');
INSERT INTO `logs` VALUES (43, 12, 'Login Successful', '2026-08-01 03:25:42.0');
INSERT INTO `logs` VALUES (44, 12, 'Login Successful', '2026-08-01 03:35:07.0');
INSERT INTO `logs` VALUES (45, 12, 'Login Successful', '2026-08-01 03:42:55.0');
INSERT INTO `logs` VALUES (46, 12, 'Login Successful', '2026-08-01 03:50:01.0');
INSERT INTO `logs` VALUES (47, 12, 'Login Successful', '2026-08-01 04:00:16.0');
INSERT INTO `logs` VALUES (48, 12, 'Login Successful', '2026-08-01 04:11:46.0');
INSERT INTO `logs` VALUES (49, 12, 'Login Successful', '2026-08-01 04:28:27.0');
INSERT INTO `logs` VALUES (50, 12, 'Login Successful', '2026-08-01 04:49:13.0');
INSERT INTO `logs` VALUES (51, 12, 'Login Successful', '2026-08-01 04:56:05.0');
INSERT INTO `logs` VALUES (52, 12, 'Login Successful', '2026-08-01 04:59:52.0');
INSERT INTO `logs` VALUES (53, 12, 'Login Successful', '2026-08-01 05:04:42.0');
INSERT INTO `logs` VALUES (54, 12, 'Login Successful', '2026-08-01 06:40:10.0');
INSERT INTO `logs` VALUES (55, 12, 'Login Successful', '2026-08-01 06:50:27.0');
INSERT INTO `logs` VALUES (56, 12, 'Login Successful', '2026-08-01 06:53:25.0');
INSERT INTO `logs` VALUES (57, 12, 'Login Successful', '2026-08-01 07:06:18.0');
INSERT INTO `logs` VALUES (58, 12, 'Login Successful', '2026-08-01 07:12:36.0');
INSERT INTO `logs` VALUES (59, 12, 'Login Successful', '2026-08-01 07:20:04.0');
INSERT INTO `logs` VALUES (60, 12, 'Login Successful', '2026-08-01 07:40:52.0');
INSERT INTO `logs` VALUES (61, 12, 'Login Successful', '2026-08-01 08:05:45.0');
INSERT INTO `logs` VALUES (62, 12, 'Login Successful', '2026-08-01 09:39:59.0');
INSERT INTO `logs` VALUES (63, 12, 'Login Successful', '2026-08-02 12:29:43.0');
INSERT INTO `logs` VALUES (64, 12, 'Login Successful', '2026-08-02 12:34:25.0');
INSERT INTO `logs` VALUES (65, 12, 'Login Successful', '2026-08-02 12:46:54.0');
INSERT INTO `logs` VALUES (66, 12, 'Login Successful', '2026-08-02 12:54:08.0');
INSERT INTO `logs` VALUES (67, 12, 'Log out', '2026-08-02 12:54:27.0');
INSERT INTO `logs` VALUES (68, 11, 'Login Successful', '2026-08-02 13:31:16.0');
INSERT INTO `logs` VALUES (69, 11, 'Log out', '2026-08-02 13:31:29.0');
INSERT INTO `logs` VALUES (70, 11, 'Login Successful', '2026-08-02 13:31:40.0');
INSERT INTO `logs` VALUES (71, 11, 'Log out', '2026-08-02 13:31:50.0');
INSERT INTO `logs` VALUES (72, 11, 'Login Successful', '2026-08-03 00:03:15.0');
INSERT INTO `logs` VALUES (73, 11, 'Login Successful', '2026-08-03 00:16:11.0');
INSERT INTO `logs` VALUES (74, 11, 'Log out', '2026-08-03 00:16:19.0');
INSERT INTO `logs` VALUES (75, 11, 'Login Successful', '2026-08-03 00:16:29.0');
INSERT INTO `logs` VALUES (76, 11, 'Log out', '2026-08-03 00:16:36.0');
INSERT INTO `logs` VALUES (77, 11, 'Login Successful', '2026-08-03 00:16:48.0');
INSERT INTO `logs` VALUES (78, 11, 'Log out', '2026-08-03 00:17:21.0');
INSERT INTO `logs` VALUES (79, 11, 'Login Successful', '2026-08-03 00:17:32.0');
INSERT INTO `logs` VALUES (80, 11, 'Login Successful', '2026-08-03 00:27:42.0');
INSERT INTO `logs` VALUES (81, 11, 'Login Successful', '2026-08-03 00:32:20.0');
INSERT INTO `logs` VALUES (82, 11, 'Login Successful', '2026-08-03 00:32:40.0');
INSERT INTO `logs` VALUES (83, 11, 'Login Successful', '2026-08-03 01:22:52.0');
INSERT INTO `logs` VALUES (84, 11, 'Log out', '2026-08-03 01:22:56.0');
INSERT INTO `logs` VALUES (85, 11, 'Login Successful', '2026-08-03 01:23:36.0');
INSERT INTO `logs` VALUES (86, 11, 'Log out', '2026-08-03 01:23:40.0');
INSERT INTO `logs` VALUES (87, 11, 'Login Successful', '2026-08-03 01:29:17.0');
INSERT INTO `logs` VALUES (88, 11, 'Log out', '2026-08-03 01:29:20.0');
INSERT INTO `logs` VALUES (89, 11, 'Login Successful', '2026-08-03 01:35:04.0');
INSERT INTO `logs` VALUES (90, 11, 'Log out', '2026-08-03 01:35:08.0');
INSERT INTO `logs` VALUES (91, 11, 'Login Successful', '2026-08-03 01:39:28.0');
INSERT INTO `logs` VALUES (92, 11, 'Log out', '2026-08-03 01:39:31.0');
INSERT INTO `logs` VALUES (93, 11, 'Login Successful', '2026-08-03 01:55:17.0');
INSERT INTO `logs` VALUES (94, 11, 'Log out', '2026-08-03 01:55:21.0');
INSERT INTO `logs` VALUES (95, 11, 'Login Successful', '2026-08-03 03:02:57.0');
INSERT INTO `logs` VALUES (96, 11, 'Log out', '2026-08-03 03:03:11.0');
INSERT INTO `logs` VALUES (97, 11, 'Login Successful', '2026-08-03 03:04:24.0');
INSERT INTO `logs` VALUES (98, 11, 'Log out', '2026-08-03 03:04:28.0');
INSERT INTO `logs` VALUES (99, 11, 'Login Successful', '2026-08-03 03:10:13.0');
INSERT INTO `logs` VALUES (100, 11, 'Log out', '2026-08-03 03:10:18.0');
INSERT INTO `logs` VALUES (101, 11, 'Login Successful', '2026-08-03 03:10:29.0');
INSERT INTO `logs` VALUES (102, 11, 'Login Successful', '2026-08-03 03:11:22.0');
INSERT INTO `logs` VALUES (103, 11, 'Log out', '2026-08-03 03:11:27.0');
INSERT INTO `logs` VALUES (104, 11, 'Login Successful', '2026-08-03 03:21:42.0');
INSERT INTO `logs` VALUES (105, 11, 'Log out', '2026-08-03 03:21:48.0');
INSERT INTO `logs` VALUES (106, 11, 'Login Successful', '2026-08-03 03:22:09.0');
INSERT INTO `logs` VALUES (107, 11, 'Log out', '2026-08-03 03:22:17.0');
INSERT INTO `logs` VALUES (108, 11, 'Login Successful', '2026-08-03 03:28:29.0');
INSERT INTO `logs` VALUES (109, 11, 'Login Successful', '2026-08-03 04:00:26.0');
INSERT INTO `logs` VALUES (110, 11, 'Log out', '2026-08-03 04:00:31.0');
INSERT INTO `logs` VALUES (111, 11, 'Login Successful', '2026-08-03 04:00:42.0');
INSERT INTO `logs` VALUES (112, 11, 'Log out', '2026-08-03 04:00:46.0');
INSERT INTO `logs` VALUES (113, 11, 'Login Successful', '2026-08-03 04:01:05.0');
INSERT INTO `logs` VALUES (114, 11, 'Log out', '2026-08-03 04:01:09.0');
INSERT INTO `logs` VALUES (115, 8, 'Login Successful', '2026-08-03 04:21:40.0');
INSERT INTO `logs` VALUES (116, 8, 'Login Successful', '2026-08-03 04:24:50.0');
INSERT INTO `logs` VALUES (117, 8, 'Log out', '2026-08-03 04:24:53.0');
INSERT INTO `logs` VALUES (118, 8, 'Login Successful', '2026-08-03 04:45:23.0');
INSERT INTO `logs` VALUES (119, 8, 'Log out', '2026-08-03 04:45:34.0');
INSERT INTO `logs` VALUES (120, 11, 'Login Successful', '2026-08-03 04:45:44.0');
INSERT INTO `logs` VALUES (121, 11, 'Log out', '2026-08-03 04:45:49.0');
INSERT INTO `logs` VALUES (122, 8, 'Login Successful', '2026-08-03 04:45:57.0');
INSERT INTO `logs` VALUES (123, 8, 'Login Successful', '2026-08-03 05:05:03.0');
INSERT INTO `logs` VALUES (124, 8, 'Login Successful', '2026-08-03 05:06:40.0');
INSERT INTO `logs` VALUES (125, 8, 'Log out', '2026-08-03 05:06:48.0');
INSERT INTO `logs` VALUES (126, 11, 'Login Successful', '2026-08-03 05:06:57.0');
INSERT INTO `logs` VALUES (127, 11, 'Log out', '2026-08-03 05:07:01.0');
INSERT INTO `logs` VALUES (128, 12, 'Login Successful', '2026-08-03 05:16:21.0');
INSERT INTO `logs` VALUES (129, 12, 'Log out', '2026-08-03 05:16:26.0');
INSERT INTO `logs` VALUES (130, 12, 'Login Successful', '2026-08-03 05:16:34.0');
INSERT INTO `logs` VALUES (131, 12, 'Log out', '2026-08-03 05:16:37.0');
INSERT INTO `logs` VALUES (132, 12, 'Login Successful', '2026-08-03 05:16:46.0');
INSERT INTO `logs` VALUES (133, 8, 'Login Successful', '2026-08-04 03:41:54.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `passengers` VALUES (1, 'adad', 'adadad', '23444332', 'adadad', 'Female', '2026-07-30', 'Foreigner', '2026-08-01 03:54:27.0');
INSERT INTO `passengers` VALUES (2, 'adkadkf', 'akdjlakfd', '2048720750', 'kadfladfkaf', 'Male', '2026-08-12', 'Myanmar', '2026-08-01 04:00:49.0');
INSERT INTO `passengers` VALUES (3, 'akldflakf', 'kaldlajfkd', '09842905', 'a;dkfalfj', 'Male', '2026-08-19', 'Myanmar', '2026-08-01 04:12:31.0');
INSERT INTO `passengers` VALUES (4, 'akldflakdfj', 'alkdfjalfj', 'akljdlajflajfl', 'alkjdflajf', 'Male', '2026-08-12', 'Foreigner', '2026-08-01 04:29:22.0');
INSERT INTO `passengers` VALUES (5, 'aldkjalkf', 'akdfjlakjfl', '908205', 'ajdfkjaklfjlaf', 'Male', '2026-08-18', 'Myanmar', '2026-08-01 04:50:02.0');
INSERT INTO `passengers` VALUES (6, 'aldjlakf', 'akldjflajfl', '09805454', 'adklajdfladfk', 'Male', '2026-08-12', 'Myanmar', '2026-08-01 04:56:29.0');
INSERT INTO `passengers` VALUES (7, 'adjflaf', 'ajdkjadf', '0849754', 'kladfjklajf', 'Male', '2026-08-05', 'Myanmar', '2026-08-01 05:00:14.0');
INSERT INTO `passengers` VALUES (8, 'alkdfjalkjf', 'akldjladjf', '20458025', 'adjjahklajsdlf', 'Male', '2026-08-05', 'Myanmar', '2026-08-01 05:05:49.0');
INSERT INTO `passengers` VALUES (9, 'lakjfklaf', 'adkfjalfja', '656151', 'adkfjalf', 'Male', '2026-08-18', 'Myanmar', '2026-08-01 06:56:09.0');
INSERT INTO `passengers` VALUES (10, 'akdjflajfdkl', 'adlkfaljkfl', '98374149', 'akdjflajfla', 'Male', '2026-08-10', 'Myanmar', '2026-08-01 07:06:40.0');
INSERT INTO `passengers` VALUES (11, 'a;dfkajflaj', 'adfafdaf', '1514515', 'dafaf', 'Male', '2026-08-11', 'Myanmar', '2026-08-01 07:13:33.0');
INSERT INTO `passengers` VALUES (12, 'afdadfaf', 'adfadfaf', '25415', 'afaf', 'Male', '2026-08-12', 'Myanmar', '2026-08-01 07:20:35.0');
INSERT INTO `passengers` VALUES (13, 'kjljkj', 'adfaf', '24525', 'afdafa', 'Male', '2026-08-05', 'Myanmar', '2026-08-01 07:43:04.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `seats` VALUES (3, 2, '1C', 'Business', 'false');
INSERT INTO `seats` VALUES (4, 2, '4D', 'Premium Economic', 'false');
INSERT INTO `seats` VALUES (5, 2, '3C', 'Business', 'false');
INSERT INTO `seats` VALUES (6, 2, '2C', 'Economic', 'false');
INSERT INTO `seats` VALUES (7, 2, '4C', 'Economic', 'false');
INSERT INTO `seats` VALUES (8, 2, '3D', 'Premium Economic', 'false');
INSERT INTO `seats` VALUES (9, 2, '2D', 'Business', 'false');
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `users` VALUES (8, 'SuperAdmin', NULL, 'superadmin', 'superadmin', 'Active', NULL, '2026-07-22 21:32:18.0');
INSERT INTO `users` VALUES (9, 'Admin', NULL, 'admin', 'admin', 'Active', NULL, '2026-07-22 21:32:18.0');
INSERT INTO `users` VALUES (10, 'Staff', NULL, 'staff', 'staff', 'Active', NULL, '2026-07-22 21:32:18.0');
INSERT INTO `users` VALUES (11, 'Ye Naing Oo', 'yno@gmail.com', '1234', 'admin', 'Active', 'profile_1785257806386.jpeg', '2026-07-28 23:26:46.0');
INSERT INTO `users` VALUES (12, 'Chee', 'chee@gmaill.com', '12234', 'staff', 'Active', 'profile_1785295775224.jpg', '2026-07-29 09:59:35.0');
INSERT INTO `users` VALUES (13, 'Gojo', 'gojo@gmail.com', '6789', 'admin', 'Active', 'profile_1785296534322.jpg', '2026-07-29 10:12:14.0');
INSERT INTO `users` VALUES (14, 'add', 'add', 'add', 'admin', 'Active', 'profile_1785338728043.jpg', '2026-07-29 21:55:28.0');
INSERT INTO `users` VALUES (15, 'Bruh', 'bruh@gmail.com', '12345', 'admin', 'Active', 'profile_1785791564423.jpg', '2026-08-04 03:42:44.0');

SET FOREIGN_KEY_CHECKS=1;
