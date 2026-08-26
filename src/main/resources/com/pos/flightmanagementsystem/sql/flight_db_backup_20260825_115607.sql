SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `backups`;
CREATE TABLE `backups` (
  `id` int NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL,
  `file_size` varchar(50) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  `backup_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `backups` VALUES (1, 'flight_db_backup_20260816_200529.sql', '157 KB', 'Auto Backup', '2026-08-16 20:05:30.0');
INSERT INTO `backups` VALUES (2, 'flight_db_export_20260816_200533.sql.sql', '158 KB', 'Export SQL', '2026-08-16 20:05:46.0');
INSERT INTO `backups` VALUES (3, 'flight_db_backup_20260816_200942.sql', '158 KB', 'Auto Backup', '2026-08-16 20:09:42.0');
INSERT INTO `backups` VALUES (4, 'flight_db_export_20260816_200943.sql.sql', '158 KB', 'Export SQL', '2026-08-16 20:09:46.0');
INSERT INTO `backups` VALUES (5, 'flight_db_backup_20260819_091026.sql', '163 KB', 'Auto Backup', '2026-08-19 09:10:27.0');
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
  KEY `flight_id` (`flight_id`),
  KEY `passenger_id` (`passenger_id`),
  KEY `seat_id` (`seat_id`),
  KEY `staff_id` (`staff_id`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`flight_id`),
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`passenger_id`) REFERENCES `passengers` (`passenger_id`),
  CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`seat_id`),
  CONSTRAINT `bookings_ibfk_4` FOREIGN KEY (`staff_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `bookings` VALUES (1, 'PNR901', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 09:00:00.0');
INSERT INTO `bookings` VALUES (2, 'PNR902', 2, 2, 2, 1, 150000.00, 'Visa', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (3, 'PNR903', 3, 3, 3, 1, 120000.00, 'WavePay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (4, 'PNR904', 4, 1, 4, 1, 130000.00, 'Cash', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (5, 'PNR905', 4, 2, 5, 1, 130000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (6, 'PNR906', 4, 3, 6, 1, 130000.00, 'Visa', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (7, 'PNR-03942', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (8, 'PNR-21267', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (9, 'PNR-94509', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (10, 'PNR-08749', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (11, 'PNR-60215', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (12, 'PNR-74829', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (13, 'PNR-93502', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (14, 'PNR-43028', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (15, 'PNR-34633', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (16, 'PNR-44080', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (17, 'PNR-16502', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (18, 'PNR-50269', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (19, 'PNR-01842', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (20, 'PNR-58402', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (21, 'PNR-86487', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (22, 'PNR-57233', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (23, 'PNR-26702', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (24, 'PNR-61812', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (25, 'PNR-28954', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (26, 'PNR-59336', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (27, 'PNR-09819', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (28, 'PNR-71087', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (29, 'PNR-25980', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (30, 'PNR-16637', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (31, 'PNR-05248', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (32, 'PNR-76329', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (33, 'PNR-65904', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (34, 'PNR-00536', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (35, 'PNR-04966', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (36, 'PNR-23222', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (37, 'PNR-01214', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (38, 'PNR-36402', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (39, 'PNR-78369', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (40, 'PNR-82643', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (41, 'PNR-78110', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (42, 'PNR-42621', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (43, 'PNR-78773', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (44, 'PNR-66008', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (45, 'PNR-93720', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (46, 'PNR-70579', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (47, 'PNR-71735', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (48, 'PNR-46940', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (49, 'PNR-19494', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (50, 'PNR-56652', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (51, 'PNR-24779', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (52, 'PNR-53940', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (53, 'PNR-95365', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (54, 'PNR-15008', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (55, 'PNR-88941', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (56, 'PNR-99684', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (57, 'PNR-31602', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (58, 'PNR-58957', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (59, 'PNR-99980', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (60, 'PNR-23031', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (61, 'PNR-15212', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (62, 'PNR-06970', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (63, 'PNR-89212', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (64, 'PNR-25155', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (65, 'PNR-58139', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (66, 'PNR-15233', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (67, 'PNR-01747', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (68, 'PNR-63034', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (69, 'PNR-09933', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (70, 'PNR-60560', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (71, 'PNR-73003', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (72, 'PNR-83338', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (73, 'PNR-97680', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (74, 'PNR-38391', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (75, 'PNR-98913', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (76, 'PNR-79395', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (77, 'PNR-00236', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (78, 'PNR-62995', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (79, 'PNR-14270', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (80, 'PNR-82364', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (81, 'PNR-69011', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (82, 'PNR-97965', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (83, 'PNR-82793', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (84, 'PNR-20073', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (85, 'PNR-51983', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (86, 'PNR-99699', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (87, 'PNR-42550', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (88, 'PNR-13652', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (89, 'PNR-40611', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (90, 'PNR-62100', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (91, 'PNR-88668', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (92, 'PNR-57043', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (93, 'PNR-19213', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (94, 'PNR-24934', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (95, 'PNR-67031', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (96, 'PNR-60358', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (97, 'PNR-00695', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (98, 'PNR-22402', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (99, 'PNR-09928', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (100, 'PNR-82430', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (101, 'PNR-82371', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (102, 'PNR-64568', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (103, 'PNR-75725', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (104, 'PNR-84922', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (105, 'PNR-97438', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (106, 'PNR-32424', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (107, 'PNR-69806', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (108, 'PNR-51761', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (109, 'PNR-49389', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (110, 'PNR-91660', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (111, 'PNR-10139', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (112, 'PNR-75713', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (113, 'PNR-48152', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (114, 'PNR-13620', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (115, 'PNR-23644', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-16 00:00:00.0');
INSERT INTO `bookings` VALUES (116, 'PNR-77360', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-16 00:00:00.0');
INSERT INTO `bookings` VALUES (117, 'PNR-15872', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-16 00:00:00.0');
INSERT INTO `bookings` VALUES (118, 'PNR-47280', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-16 00:00:00.0');
INSERT INTO `bookings` VALUES (119, 'PNR-88782', 1, 1, 1, 1, 120000.00, 'KPay', 'Confirmed', '2026-08-16 00:00:00.0');
DROP TABLE IF EXISTS `cities`;
CREATE TABLE `cities` (
  `city_id` int NOT NULL AUTO_INCREMENT,
  `city_code` varchar(10) NOT NULL,
  `city_name` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`city_id`),
  UNIQUE KEY `city_code` (`city_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `cities` VALUES (1, 'RGN', 'Yangon', 'Myanmar', '2026-08-22 19:20:52.0');
INSERT INTO `cities` VALUES (2, 'MDL', 'Mandalay', 'Myanmar', '2026-08-22 19:20:52.0');
INSERT INTO `cities` VALUES (3, 'NYT', 'Naypyidaw', 'Myanmar', '2026-08-22 19:20:52.0');
INSERT INTO `cities` VALUES (4, 'NYU', 'Bagan (Nyaung-U)', 'Myanmar', '2026-08-22 19:20:52.0');
INSERT INTO `cities` VALUES (5, 'HEH', 'Heho (Taunggyi)', 'Myanmar', '2026-08-22 19:20:52.0');
INSERT INTO `cities` VALUES (6, 'SNW', 'Thandwe (Ngapali)', 'Myanmar', '2026-08-22 19:20:52.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `flights` VALUES (1, 'FL-801', 'FlyPlan Air', 1, 2, '2026-08-22T08:00', '2026-08-22T09:30', 150, 120000.00, 'Departed');
INSERT INTO `flights` VALUES (2, 'FL-802', 'FlyPlan Air', 1, 4, '2026-08-22T14:00', '2026-08-22T15:20', 150, 150000.00, 'Scheduled');
INSERT INTO `flights` VALUES (3, 'FL-803', 'FlyPlan Air', 2, 1, '2026-08-22T18:00', '2026-08-22T19:30', 150, 120000.00, 'Delayed');
INSERT INTO `flights` VALUES (4, 'FL-804', 'FlyPlan Air', 1, 6, '2026-08-22T10:00', '2026-08-22T11:00', 150, 180000.00, 'Scheduled');
INSERT INTO `flights` VALUES (5, 'FL-805', 'FlyPlan Air', 1, 2, '2026-08-22T12:00', '2026-08-22T13:30', 150, 120000.00, 'Scheduled');
INSERT INTO `flights` VALUES (6, 'FL-806', 'FlyPlan Air', 1, 2, '2026-08-22T16:00', '2026-08-22T17:30', 150, 120000.00, 'Scheduled');
INSERT INTO `flights` VALUES (7, 'FL-807', 'FlyPlan Air', 1, 4, '2026-08-22T09:00', '2026-08-22T10:20', 150, 150000.00, 'Departed');
INSERT INTO `flights` VALUES (8, 'FL-808', 'FlyPlan Air', 2, 1, '2026-08-22T11:00', '2026-08-22T12:30', 150, 120000.00, 'Scheduled');
DROP TABLE IF EXISTS `logs`;
CREATE TABLE `logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `action` varchar(255) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `logs` VALUES (1, 8, 'Login Successful', '2026-08-22 19:21:15.0');
INSERT INTO `logs` VALUES (2, 8, 'Login Successful', '2026-08-22 19:26:03.0');
INSERT INTO `logs` VALUES (3, 8, 'Login Successful', '2026-08-22 19:38:33.0');
INSERT INTO `logs` VALUES (4, 9, 'Login Successful', '2026-08-22 19:53:08.0');
INSERT INTO `logs` VALUES (5, 5, 'Login Successful', '2026-08-24 22:27:07.0');
INSERT INTO `logs` VALUES (6, 5, 'Log out', '2026-08-24 22:27:45.0');
INSERT INTO `logs` VALUES (7, 8, 'Login Successful', '2026-08-24 22:27:57.0');
INSERT INTO `logs` VALUES (8, 8, 'Login Failed (1/3 attempts)', '2026-08-25 08:53:11.0');
INSERT INTO `logs` VALUES (9, 8, 'Login Successful', '2026-08-25 08:53:19.0');
INSERT INTO `logs` VALUES (10, 8, 'Login Successful', '2026-08-25 09:06:37.0');
INSERT INTO `logs` VALUES (11, 8, 'Login Successful', '2026-08-25 09:59:38.0');
INSERT INTO `logs` VALUES (12, 8, 'Login Successful', '2026-08-25 10:01:49.0');
INSERT INTO `logs` VALUES (13, 8, 'Log out', '2026-08-25 10:02:04.0');
INSERT INTO `logs` VALUES (14, 5, 'Login Successful', '2026-08-25 10:02:11.0');
INSERT INTO `logs` VALUES (15, 5, 'Login Successful', '2026-08-25 10:37:02.0');
INSERT INTO `logs` VALUES (16, 5, 'Log out', '2026-08-25 10:37:18.0');
INSERT INTO `logs` VALUES (17, 9, 'Login Successful', '2026-08-25 10:37:25.0');
INSERT INTO `logs` VALUES (18, 9, 'Login Successful', '2026-08-25 10:42:07.0');
INSERT INTO `logs` VALUES (19, 9, 'Login Successful', '2026-08-25 10:54:13.0');
INSERT INTO `logs` VALUES (20, 9, 'Login Successful', '2026-08-25 11:08:30.0');
INSERT INTO `logs` VALUES (21, 9, 'Login Successful', '2026-08-25 11:33:33.0');
INSERT INTO `logs` VALUES (22, 9, 'Login Successful', '2026-08-25 11:36:26.0');
INSERT INTO `logs` VALUES (23, 9, 'Login Successful', '2026-08-25 11:53:35.0');
INSERT INTO `logs` VALUES (24, 9, 'Log out', '2026-08-25 11:55:52.0');
INSERT INTO `logs` VALUES (25, 5, 'Login Successful', '2026-08-25 11:56:02.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `passengers` VALUES (1, 'Aung Aung', '12/YGN(N)123456', '0912345678', 'aung@gmail.com', 'Male', '1990-01-01', 'Myanmar', '2026-08-22 19:20:52.0');
INSERT INTO `passengers` VALUES (2, 'Su Su', '12/YGN(N)654321', '0987654321', 'susu@gmail.com', 'Female', '1995-05-05', 'Myanmar', '2026-08-22 19:20:52.0');
INSERT INTO `passengers` VALUES (3, 'Kyaw Kyaw', '9/MDY(N)111222', '0944445555', 'kyaw@gmail.com', 'Male', '1988-10-10', 'Myanmar', '2026-08-22 19:20:52.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `requests` VALUES (1, 1, 1, 2, 'Change of travel plans', 'Pending', '2026-08-22 19:20:52.0');
INSERT INTO `requests` VALUES (2, 2, 1, 2, 'Medical emergency', 'Pending', '2026-08-22 19:20:52.0');
INSERT INTO `requests` VALUES (3, 3, 1, 2, 'Flight was delayed', 'Pending', '2026-08-22 19:20:52.0');
INSERT INTO `requests` VALUES (4, 4, 1, 2, 'Change of travel plans', 'Pending', '2026-08-22 19:20:52.0');
INSERT INTO `requests` VALUES (5, 5, 1, 2, 'Booked by mistake', 'Pending', '2026-08-22 19:20:52.0');
INSERT INTO `requests` VALUES (6, 6, 1, 2, 'Medical emergency', 'Pending', '2026-08-22 19:20:52.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `seats` VALUES (1, 1, '1A', 'Business', 'false');
INSERT INTO `seats` VALUES (2, 2, '5C', 'Premium', 'false');
INSERT INTO `seats` VALUES (3, 3, '2A', 'Business', 'false');
INSERT INTO `seats` VALUES (4, 4, '15A', 'Economy', 'false');
INSERT INTO `seats` VALUES (5, 4, '15B', 'Economy', 'false');
INSERT INTO `seats` VALUES (6, 4, '15C', 'Economy', 'false');
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
  `failed_attempts` int DEFAULT '0',
  `locked_until` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `users` VALUES (1, 'StaffUser', 'staff@flyplan.com', '123456', 'Staff', 'Active', NULL, '2026-08-22 19:20:24.0', 0, NULL);
INSERT INTO `users` VALUES (2, 'AdminUser', 'admin@flyplan.com', '123456', 'Admin', 'Active', NULL, '2026-08-22 19:20:24.0', 0, NULL);
INSERT INTO `users` VALUES (5, 'SuperAdmin', 'luuarr344@gmail.com', '186cf774c97b60a1c106ef718d10970a6a06e06bef89553d9ae65d938a886eae', 'superadmin', 'Active', NULL, '2026-08-13 21:27:41.0', 0, NULL);
INSERT INTO `users` VALUES (6, 'Admin', NULL, 'admin', 'admin', 'Inactive', NULL, '2026-08-13 21:27:41.0', 0, NULL);
INSERT INTO `users` VALUES (7, 'Staff', NULL, 'staff', 'staff', 'Inactive', NULL, '2026-08-13 21:27:41.0', 0, NULL);
INSERT INTO `users` VALUES (8, 'Ye Naing Oo', 'yno@gmail.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'admin', 'Active', 'profile_1786866643672.jpg', '2026-08-16 14:20:43.0', 0, NULL);
INSERT INTO `users` VALUES (9, 'Nay Linn Oo', 'nlo@gmail.com', '1562206543da764123c21bd524674f0a8aaf49c8a89744c97352fe677f7e4006', 'staff', 'Active', 'profile_1786866686771.jpg', '2026-08-16 14:21:26.0', 0, NULL);
INSERT INTO `users` VALUES (10, 'Soe Thiha Paing', 'soe@gmail.com', '1562206543da764123c21bd524674f0a8aaf49c8a89744c97352fe677f7e4006', 'staff', 'Active', 'profile_1786885793598.jpg', '2026-08-16 19:39:53.0', 0, NULL);

SET FOREIGN_KEY_CHECKS=1;
