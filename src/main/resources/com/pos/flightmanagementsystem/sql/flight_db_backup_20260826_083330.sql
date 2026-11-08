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
  KEY `flight_id` (`flight_id`),
  KEY `passenger_id` (`passenger_id`),
  KEY `seat_id` (`seat_id`),
  KEY `staff_id` (`staff_id`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`flight_id`),
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`passenger_id`) REFERENCES `passengers` (`passenger_id`),
  CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`seat_id`),
  CONSTRAINT `bookings_ibfk_4` FOREIGN KEY (`staff_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=269 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `bookings` VALUES (1, 'PNR33611', 6, 4, 38, 9, 210000.00, 'KPay', 'Confirmed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (2, 'PNR05784', 1, 10, 82, 10, 130000.00, 'WavePay', 'Confirmed', '2026-06-19 00:00:00.0');
INSERT INTO `bookings` VALUES (3, 'PNR96925', 8, 1, 44, 10, 120000.00, 'Visa', 'Completed', '2026-06-24 00:00:00.0');
INSERT INTO `bookings` VALUES (4, 'PNR03825', 2, 4, 47, 10, 210000.00, 'KPay', 'Confirmed', '2026-07-15 00:00:00.0');
INSERT INTO `bookings` VALUES (5, 'PNR64338', 11, 3, 72, 9, 120000.00, 'WavePay', 'Cancelled', '2026-06-21 00:00:00.0');
INSERT INTO `bookings` VALUES (6, 'PNR95034', 7, 10, 11, 9, 210000.00, 'Visa', 'Confirmed', '2026-07-25 00:00:00.0');
INSERT INTO `bookings` VALUES (7, 'PNR90606', 6, 7, 92, 9, 130000.00, 'Visa', 'Cancelled', '2026-07-24 00:00:00.0');
INSERT INTO `bookings` VALUES (8, 'PNR84481', 2, 1, 10, 10, 190000.00, 'Visa', 'Cancelled', '2026-08-15 00:00:00.0');
INSERT INTO `bookings` VALUES (9, 'PNR14015', 5, 3, 29, 9, 160000.00, 'KPay', 'Cancelled', '2026-06-05 00:00:00.0');
INSERT INTO `bookings` VALUES (10, 'PNR74692', 1, 8, 83, 9, 190000.00, 'KPay', 'Completed', '2026-07-20 00:00:00.0');
INSERT INTO `bookings` VALUES (11, 'PNR97674', 9, 5, 33, 10, 120000.00, 'WavePay', 'Cancelled', '2026-06-21 00:00:00.0');
INSERT INTO `bookings` VALUES (12, 'PNR91477', 5, 2, 76, 10, 200000.00, 'WavePay', 'Confirmed', '2026-07-19 00:00:00.0');
INSERT INTO `bookings` VALUES (13, 'PNR84460', 12, 4, 71, 9, 190000.00, 'Visa', 'Completed', '2026-08-02 00:00:00.0');
INSERT INTO `bookings` VALUES (14, 'PNR08061', 8, 10, 74, 9, 140000.00, 'Visa', 'Confirmed', '2026-07-10 00:00:00.0');
INSERT INTO `bookings` VALUES (15, 'PNR38228', 5, 7, 26, 10, 180000.00, 'WavePay', 'Cancelled', '2026-07-31 00:00:00.0');
INSERT INTO `bookings` VALUES (16, 'PNR80257', 2, 4, 50, 10, 210000.00, 'Visa', 'Cancelled', '2026-06-04 00:00:00.0');
INSERT INTO `bookings` VALUES (17, 'PNR16149', 1, 9, 87, 9, 210000.00, 'KPay', 'Completed', '2026-07-27 00:00:00.0');
INSERT INTO `bookings` VALUES (18, 'PNR77023', 11, 2, 89, 10, 190000.00, 'WavePay', 'Cancelled', '2026-07-29 00:00:00.0');
INSERT INTO `bookings` VALUES (19, 'PNR99443', 1, 4, 43, 10, 160000.00, 'Visa', 'Completed', '2026-06-03 00:00:00.0');
INSERT INTO `bookings` VALUES (20, 'PNR57053', 1, 6, 67, 9, 120000.00, 'WavePay', 'Confirmed', '2026-06-02 00:00:00.0');
INSERT INTO `bookings` VALUES (21, 'PNR15514', 12, 3, 56, 9, 210000.00, 'KPay', 'Confirmed', '2026-08-25 00:00:00.0');
INSERT INTO `bookings` VALUES (22, 'PNR40500', 1, 8, 96, 10, 130000.00, 'WavePay', 'Confirmed', '2026-06-25 00:00:00.0');
INSERT INTO `bookings` VALUES (23, 'PNR72997', 8, 8, 11, 10, 190000.00, 'KPay', 'Confirmed', '2026-08-03 00:00:00.0');
INSERT INTO `bookings` VALUES (24, 'PNR01131', 4, 5, 23, 9, 180000.00, 'Visa', 'Confirmed', '2026-07-07 00:00:00.0');
INSERT INTO `bookings` VALUES (25, 'PNR08536', 10, 6, 47, 9, 120000.00, 'KPay', 'Confirmed', '2026-08-07 00:00:00.0');
INSERT INTO `bookings` VALUES (26, 'PNR77958', 4, 1, 50, 10, 210000.00, 'Visa', 'Cancelled', '2026-05-31 00:00:00.0');
INSERT INTO `bookings` VALUES (27, 'PNR33958', 10, 1, 2, 9, 200000.00, 'Visa', 'Confirmed', '2026-06-14 00:00:00.0');
INSERT INTO `bookings` VALUES (28, 'PNR44864', 10, 8, 41, 9, 140000.00, 'Visa', 'Completed', '2026-06-26 00:00:00.0');
INSERT INTO `bookings` VALUES (29, 'PNR38913', 12, 5, 40, 9, 130000.00, 'Visa', 'Confirmed', '2026-06-04 00:00:00.0');
INSERT INTO `bookings` VALUES (30, 'PNR14937', 12, 5, 38, 9, 150000.00, 'WavePay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (31, 'PNR59155', 9, 6, 93, 9, 170000.00, 'KPay', 'Completed', '2026-05-29 00:00:00.0');
INSERT INTO `bookings` VALUES (32, 'PNR97530', 12, 7, 63, 10, 170000.00, 'WavePay', 'Cancelled', '2026-07-19 00:00:00.0');
INSERT INTO `bookings` VALUES (33, 'PNR78704', 9, 10, 94, 9, 190000.00, 'Visa', 'Confirmed', '2026-06-14 00:00:00.0');
INSERT INTO `bookings` VALUES (34, 'PNR49516', 1, 8, 79, 9, 160000.00, 'WavePay', 'Confirmed', '2026-05-31 00:00:00.0');
INSERT INTO `bookings` VALUES (35, 'PNR95201', 11, 7, 34, 9, 140000.00, 'Visa', 'Confirmed', '2026-08-23 00:00:00.0');
INSERT INTO `bookings` VALUES (36, 'PNR54231', 8, 5, 59, 10, 170000.00, 'KPay', 'Confirmed', '2026-08-14 00:00:00.0');
INSERT INTO `bookings` VALUES (37, 'PNR54733', 5, 2, 88, 9, 150000.00, 'WavePay', 'Cancelled', '2026-08-04 00:00:00.0');
INSERT INTO `bookings` VALUES (38, 'PNR55774', 1, 7, 8, 10, 200000.00, 'KPay', 'Cancelled', '2026-06-04 00:00:00.0');
INSERT INTO `bookings` VALUES (39, 'PNR06622', 8, 8, 89, 10, 170000.00, 'KPay', 'Completed', '2026-06-18 00:00:00.0');
INSERT INTO `bookings` VALUES (40, 'PNR96080', 7, 8, 16, 9, 130000.00, 'WavePay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (41, 'PNR75468', 6, 3, 62, 10, 140000.00, 'KPay', 'Confirmed', '2026-07-14 00:00:00.0');
INSERT INTO `bookings` VALUES (42, 'PNR36046', 5, 9, 9, 9, 130000.00, 'Visa', 'Completed', '2026-07-22 00:00:00.0');
INSERT INTO `bookings` VALUES (43, 'PNR56425', 9, 7, 22, 10, 120000.00, 'Visa', 'Cancelled', '2026-07-20 00:00:00.0');
INSERT INTO `bookings` VALUES (44, 'PNR08149', 3, 8, 30, 10, 200000.00, 'Visa', 'Confirmed', '2026-08-11 00:00:00.0');
INSERT INTO `bookings` VALUES (45, 'PNR64647', 9, 8, 58, 9, 150000.00, 'Visa', 'Confirmed', '2026-05-29 00:00:00.0');
INSERT INTO `bookings` VALUES (46, 'PNR04049', 4, 2, 14, 10, 130000.00, 'KPay', 'Confirmed', '2026-05-28 00:00:00.0');
INSERT INTO `bookings` VALUES (47, 'PNR39577', 1, 9, 26, 9, 190000.00, 'Visa', 'Confirmed', '2026-07-03 00:00:00.0');
INSERT INTO `bookings` VALUES (48, 'PNR18057', 2, 2, 16, 10, 190000.00, 'WavePay', 'Confirmed', '2026-08-17 00:00:00.0');
INSERT INTO `bookings` VALUES (49, 'PNR55720', 6, 9, 56, 10, 130000.00, 'WavePay', 'Confirmed', '2026-08-24 00:00:00.0');
INSERT INTO `bookings` VALUES (50, 'PNR79525', 12, 3, 52, 9, 160000.00, 'Visa', 'Confirmed', '2026-07-21 00:00:00.0');
INSERT INTO `bookings` VALUES (51, 'PNR26845', 2, 10, 43, 10, 170000.00, 'WavePay', 'Confirmed', '2026-08-24 00:00:00.0');
INSERT INTO `bookings` VALUES (52, 'PNR37554', 10, 10, 34, 9, 120000.00, 'WavePay', 'Confirmed', '2026-06-13 00:00:00.0');
INSERT INTO `bookings` VALUES (53, 'PNR64990', 10, 1, 89, 10, 180000.00, 'WavePay', 'Cancelled', '2026-07-28 00:00:00.0');
INSERT INTO `bookings` VALUES (54, 'PNR97749', 12, 8, 79, 9, 160000.00, 'KPay', 'Completed', '2026-07-11 00:00:00.0');
INSERT INTO `bookings` VALUES (55, 'PNR28042', 11, 7, 58, 9, 200000.00, 'WavePay', 'Confirmed', '2026-06-20 00:00:00.0');
INSERT INTO `bookings` VALUES (56, 'PNR91646', 5, 1, 4, 10, 160000.00, 'Visa', 'Cancelled', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (57, 'PNR45420', 2, 5, 62, 9, 140000.00, 'Visa', 'Completed', '2026-07-10 00:00:00.0');
INSERT INTO `bookings` VALUES (58, 'PNR38473', 5, 7, 19, 9, 130000.00, 'KPay', 'Confirmed', '2026-08-07 00:00:00.0');
INSERT INTO `bookings` VALUES (59, 'PNR64769', 8, 2, 92, 10, 200000.00, 'Visa', 'Cancelled', '2026-06-19 00:00:00.0');
INSERT INTO `bookings` VALUES (60, 'PNR12830', 5, 5, 34, 10, 120000.00, 'WavePay', 'Cancelled', '2026-08-16 00:00:00.0');
INSERT INTO `bookings` VALUES (61, 'PNR23414', 11, 6, 9, 9, 210000.00, 'WavePay', 'Confirmed', '2026-06-30 00:00:00.0');
INSERT INTO `bookings` VALUES (62, 'PNR59450', 1, 7, 80, 10, 150000.00, 'KPay', 'Confirmed', '2026-06-22 00:00:00.0');
INSERT INTO `bookings` VALUES (63, 'PNR37076', 9, 5, 2, 9, 210000.00, 'WavePay', 'Completed', '2026-08-14 00:00:00.0');
INSERT INTO `bookings` VALUES (64, 'PNR75993', 6, 9, 90, 9, 140000.00, 'WavePay', 'Cancelled', '2026-06-23 00:00:00.0');
INSERT INTO `bookings` VALUES (65, 'PNR66108', 3, 1, 47, 10, 120000.00, 'WavePay', 'Confirmed', '2026-07-06 00:00:00.0');
INSERT INTO `bookings` VALUES (66, 'PNR61378', 5, 1, 92, 9, 130000.00, 'Visa', 'Cancelled', '2026-08-05 00:00:00.0');
INSERT INTO `bookings` VALUES (67, 'PNR25301', 7, 2, 95, 10, 180000.00, 'WavePay', 'Completed', '2026-07-23 00:00:00.0');
INSERT INTO `bookings` VALUES (68, 'PNR77343', 10, 5, 99, 9, 210000.00, 'KPay', 'Completed', '2026-07-03 00:00:00.0');
INSERT INTO `bookings` VALUES (69, 'PNR92396', 11, 5, 72, 10, 210000.00, 'KPay', 'Confirmed', '2026-06-24 00:00:00.0');
INSERT INTO `bookings` VALUES (70, 'PNR93254', 7, 1, 42, 10, 200000.00, 'KPay', 'Cancelled', '2026-07-11 00:00:00.0');
INSERT INTO `bookings` VALUES (71, 'PNR86920', 11, 6, 39, 10, 180000.00, 'Visa', 'Confirmed', '2026-07-05 00:00:00.0');
INSERT INTO `bookings` VALUES (72, 'PNR81555', 5, 4, 57, 9, 170000.00, 'WavePay', 'Cancelled', '2026-06-10 00:00:00.0');
INSERT INTO `bookings` VALUES (73, 'PNR31259', 1, 2, 69, 9, 200000.00, 'WavePay', 'Completed', '2026-08-09 00:00:00.0');
INSERT INTO `bookings` VALUES (74, 'PNR12691', 2, 1, 16, 9, 120000.00, 'Visa', 'Completed', '2026-06-02 00:00:00.0');
INSERT INTO `bookings` VALUES (75, 'PNR65928', 6, 5, 83, 9, 150000.00, 'KPay', 'Confirmed', '2026-06-02 00:00:00.0');
INSERT INTO `bookings` VALUES (76, 'PNR97474', 1, 4, 79, 9, 150000.00, 'WavePay', 'Confirmed', '2026-07-27 00:00:00.0');
INSERT INTO `bookings` VALUES (77, 'PNR27027', 5, 1, 34, 10, 210000.00, 'WavePay', 'Confirmed', '2026-06-10 00:00:00.0');
INSERT INTO `bookings` VALUES (78, 'PNR23666', 8, 6, 68, 9, 120000.00, 'Visa', 'Confirmed', '2026-07-26 00:00:00.0');
INSERT INTO `bookings` VALUES (79, 'PNR49812', 6, 9, 1, 10, 190000.00, 'WavePay', 'Confirmed', '2026-06-28 00:00:00.0');
INSERT INTO `bookings` VALUES (80, 'PNR25926', 5, 9, 47, 9, 200000.00, 'KPay', 'Completed', '2026-07-20 00:00:00.0');
INSERT INTO `bookings` VALUES (81, 'PNR89166', 3, 6, 80, 10, 120000.00, 'Visa', 'Confirmed', '2026-07-15 00:00:00.0');
INSERT INTO `bookings` VALUES (82, 'PNR62964', 10, 10, 34, 9, 170000.00, 'Visa', 'Cancelled', '2026-07-16 00:00:00.0');
INSERT INTO `bookings` VALUES (83, 'PNR80321', 9, 10, 68, 9, 200000.00, 'WavePay', 'Completed', '2026-08-02 00:00:00.0');
INSERT INTO `bookings` VALUES (84, 'PNR54299', 12, 1, 41, 9, 140000.00, 'WavePay', 'Confirmed', '2026-07-21 00:00:00.0');
INSERT INTO `bookings` VALUES (85, 'PNR24821', 1, 6, 62, 10, 120000.00, 'KPay', 'Cancelled', '2026-06-20 00:00:00.0');
INSERT INTO `bookings` VALUES (86, 'PNR05814', 1, 2, 73, 10, 120000.00, 'KPay', 'Cancelled', '2026-06-20 00:00:00.0');
INSERT INTO `bookings` VALUES (87, 'PNR07972', 2, 6, 45, 10, 210000.00, 'WavePay', 'Cancelled', '2026-07-08 00:00:00.0');
INSERT INTO `bookings` VALUES (88, 'PNR95751', 2, 10, 18, 10, 200000.00, 'Visa', 'Completed', '2026-06-28 00:00:00.0');
INSERT INTO `bookings` VALUES (89, 'PNR32485', 9, 5, 1, 9, 120000.00, 'WavePay', 'Confirmed', '2026-08-04 00:00:00.0');
INSERT INTO `bookings` VALUES (90, 'PNR45563', 7, 5, 70, 10, 130000.00, 'WavePay', 'Completed', '2026-07-04 00:00:00.0');
INSERT INTO `bookings` VALUES (91, 'PNR53083', 11, 10, 82, 10, 150000.00, 'WavePay', 'Confirmed', '2026-08-06 00:00:00.0');
INSERT INTO `bookings` VALUES (92, 'PNR47104', 9, 2, 41, 9, 150000.00, 'WavePay', 'Cancelled', '2026-06-11 00:00:00.0');
INSERT INTO `bookings` VALUES (93, 'PNR44733', 9, 2, 81, 10, 210000.00, 'WavePay', 'Cancelled', '2026-06-24 00:00:00.0');
INSERT INTO `bookings` VALUES (94, 'PNR60777', 12, 10, 72, 9, 120000.00, 'Visa', 'Completed', '2026-07-01 00:00:00.0');
INSERT INTO `bookings` VALUES (95, 'PNR08332', 7, 6, 99, 10, 180000.00, 'WavePay', 'Confirmed', '2026-06-26 00:00:00.0');
INSERT INTO `bookings` VALUES (96, 'PNR74880', 9, 4, 66, 10, 200000.00, 'Visa', 'Completed', '2026-07-18 00:00:00.0');
INSERT INTO `bookings` VALUES (97, 'PNR07362', 2, 3, 76, 10, 170000.00, 'WavePay', 'Confirmed', '2026-07-17 00:00:00.0');
INSERT INTO `bookings` VALUES (98, 'PNR89144', 2, 10, 45, 10, 140000.00, 'KPay', 'Confirmed', '2026-07-26 00:00:00.0');
INSERT INTO `bookings` VALUES (99, 'PNR20151', 12, 4, 71, 9, 160000.00, 'Visa', 'Completed', '2026-07-21 00:00:00.0');
INSERT INTO `bookings` VALUES (100, 'PNR01153', 11, 4, 3, 10, 170000.00, 'KPay', 'Cancelled', '2026-06-29 00:00:00.0');
INSERT INTO `bookings` VALUES (101, 'PNR45901', 5, 6, 63, 10, 150000.00, 'KPay', 'Completed', '2026-06-20 00:00:00.0');
INSERT INTO `bookings` VALUES (102, 'PNR81293', 11, 8, 44, 9, 170000.00, 'WavePay', 'Completed', '2026-08-23 00:00:00.0');
INSERT INTO `bookings` VALUES (103, 'PNR95961', 9, 8, 69, 10, 150000.00, 'WavePay', 'Confirmed', '2026-07-23 00:00:00.0');
INSERT INTO `bookings` VALUES (104, 'PNR69203', 5, 7, 12, 9, 130000.00, 'WavePay', 'Confirmed', '2026-07-09 00:00:00.0');
INSERT INTO `bookings` VALUES (105, 'PNR61283', 6, 6, 24, 9, 130000.00, 'KPay', 'Confirmed', '2026-07-01 00:00:00.0');
INSERT INTO `bookings` VALUES (106, 'PNR07784', 7, 4, 34, 9, 180000.00, 'WavePay', 'Completed', '2026-06-14 00:00:00.0');
INSERT INTO `bookings` VALUES (107, 'PNR93028', 4, 5, 55, 10, 130000.00, 'Visa', 'Completed', '2026-07-09 00:00:00.0');
INSERT INTO `bookings` VALUES (108, 'PNR51167', 12, 4, 90, 10, 140000.00, 'KPay', 'Cancelled', '2026-07-05 00:00:00.0');
INSERT INTO `bookings` VALUES (109, 'PNR53847', 12, 2, 11, 9, 150000.00, 'Visa', 'Cancelled', '2026-07-28 00:00:00.0');
INSERT INTO `bookings` VALUES (110, 'PNR81108', 2, 1, 10, 10, 200000.00, 'WavePay', 'Confirmed', '2026-07-05 00:00:00.0');
INSERT INTO `bookings` VALUES (111, 'PNR82717', 5, 6, 57, 10, 120000.00, 'Visa', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (112, 'PNR90698', 5, 1, 22, 9, 210000.00, 'Visa', 'Confirmed', '2026-06-23 00:00:00.0');
INSERT INTO `bookings` VALUES (113, 'PNR52073', 6, 8, 57, 10, 170000.00, 'KPay', 'Confirmed', '2026-08-14 00:00:00.0');
INSERT INTO `bookings` VALUES (114, 'PNR99571', 7, 10, 83, 10, 160000.00, 'KPay', 'Cancelled', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (115, 'PNR24646', 2, 9, 76, 10, 160000.00, 'KPay', 'Cancelled', '2026-06-27 00:00:00.0');
INSERT INTO `bookings` VALUES (116, 'PNR43131', 3, 7, 53, 9, 140000.00, 'KPay', 'Cancelled', '2026-07-19 00:00:00.0');
INSERT INTO `bookings` VALUES (117, 'PNR81636', 10, 7, 97, 9, 200000.00, 'KPay', 'Confirmed', '2026-07-21 00:00:00.0');
INSERT INTO `bookings` VALUES (118, 'PNR80595', 11, 9, 85, 9, 150000.00, 'KPay', 'Confirmed', '2026-05-31 00:00:00.0');
INSERT INTO `bookings` VALUES (119, 'PNR09964', 8, 9, 29, 9, 200000.00, 'KPay', 'Cancelled', '2026-06-03 00:00:00.0');
INSERT INTO `bookings` VALUES (120, 'PNR77185', 1, 1, 15, 9, 120000.00, 'Visa', 'Completed', '2026-06-22 00:00:00.0');
INSERT INTO `bookings` VALUES (121, 'PNR73298', 7, 4, 11, 9, 160000.00, 'WavePay', 'Confirmed', '2026-06-22 00:00:00.0');
INSERT INTO `bookings` VALUES (122, 'PNR85813', 2, 1, 93, 10, 170000.00, 'KPay', 'Completed', '2026-07-25 00:00:00.0');
INSERT INTO `bookings` VALUES (123, 'PNR58918', 11, 8, 18, 9, 140000.00, 'WavePay', 'Confirmed', '2026-07-23 00:00:00.0');
INSERT INTO `bookings` VALUES (124, 'PNR24426', 2, 9, 70, 10, 150000.00, 'WavePay', 'Cancelled', '2026-06-16 00:00:00.0');
INSERT INTO `bookings` VALUES (125, 'PNR92285', 4, 6, 9, 9, 140000.00, 'KPay', 'Completed', '2026-07-15 00:00:00.0');
INSERT INTO `bookings` VALUES (128, 'PNR07103', 9, 5, 4, 9, 200000.00, 'WavePay', 'Confirmed', '2026-06-14 00:00:00.0');
INSERT INTO `bookings` VALUES (129, 'PNR45169', 11, 9, 57, 9, 160000.00, 'Visa', 'Confirmed', '2026-07-06 00:00:00.0');
INSERT INTO `bookings` VALUES (130, 'PNR03907', 7, 6, 3, 9, 160000.00, 'Visa', 'Confirmed', '2026-07-12 00:00:00.0');
INSERT INTO `bookings` VALUES (131, 'PNR59749', 6, 7, 65, 9, 160000.00, 'KPay', 'Confirmed', '2026-07-01 00:00:00.0');
INSERT INTO `bookings` VALUES (132, 'PNR01883', 4, 3, 22, 9, 200000.00, 'WavePay', 'Confirmed', '2026-06-07 00:00:00.0');
INSERT INTO `bookings` VALUES (133, 'PNR06769', 9, 3, 20, 9, 160000.00, 'KPay', 'Confirmed', '2026-06-13 00:00:00.0');
INSERT INTO `bookings` VALUES (134, 'PNR27066', 11, 7, 53, 9, 180000.00, 'KPay', 'Completed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (135, 'PNR98327', 9, 5, 7, 9, 140000.00, 'Visa', 'Confirmed', '2026-07-17 00:00:00.0');
INSERT INTO `bookings` VALUES (136, 'PNR79573', 8, 9, 23, 9, 120000.00, 'WavePay', 'Confirmed', '2026-06-05 00:00:00.0');
INSERT INTO `bookings` VALUES (137, 'PNR75861', 1, 1, 68, 9, 180000.00, 'Visa', 'Confirmed', '2026-07-16 00:00:00.0');
INSERT INTO `bookings` VALUES (138, 'PNR62129', 10, 10, 46, 9, 140000.00, 'WavePay', 'Confirmed', '2026-07-28 00:00:00.0');
INSERT INTO `bookings` VALUES (139, 'PNR90034', 7, 2, 67, 9, 140000.00, 'KPay', 'Confirmed', '2026-06-19 00:00:00.0');
INSERT INTO `bookings` VALUES (140, 'PNR17169', 8, 6, 57, 9, 160000.00, 'Visa', 'Pending', '2026-07-31 00:00:00.0');
INSERT INTO `bookings` VALUES (141, 'PNR47308', 7, 2, 23, 9, 120000.00, 'WavePay', 'Completed', '2026-07-28 00:00:00.0');
INSERT INTO `bookings` VALUES (142, 'PNR40016', 1, 2, 31, 9, 200000.00, 'Visa', 'Completed', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (143, 'PNR92964', 5, 3, 69, 9, 120000.00, 'KPay', 'Confirmed', '2026-06-27 00:00:00.0');
INSERT INTO `bookings` VALUES (144, 'PNR65050', 4, 5, 23, 9, 140000.00, 'WavePay', 'Confirmed', '2026-06-20 00:00:00.0');
INSERT INTO `bookings` VALUES (145, 'PNR35102', 7, 7, 40, 9, 200000.00, 'Visa', 'Confirmed', '2026-06-24 00:00:00.0');
INSERT INTO `bookings` VALUES (146, 'PNR86077', 3, 6, 67, 9, 120000.00, 'Visa', 'Confirmed', '2026-08-11 00:00:00.0');
INSERT INTO `bookings` VALUES (147, 'PNR24296', 9, 10, 26, 9, 120000.00, 'KPay', 'Confirmed', '2026-07-04 00:00:00.0');
INSERT INTO `bookings` VALUES (148, 'PNR72039', 11, 1, 57, 9, 200000.00, 'WavePay', 'Confirmed', '2026-06-20 00:00:00.0');
INSERT INTO `bookings` VALUES (149, 'PNR52708', 5, 5, 12, 9, 160000.00, 'WavePay', 'Completed', '2026-07-14 00:00:00.0');
INSERT INTO `bookings` VALUES (150, 'PNR11467', 3, 5, 68, 9, 140000.00, 'Visa', 'Confirmed', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (151, 'PNR28408', 4, 7, 21, 9, 160000.00, 'Visa', 'Confirmed', '2026-08-09 00:00:00.0');
INSERT INTO `bookings` VALUES (152, 'PNR93738', 2, 8, 44, 9, 180000.00, 'Visa', 'Confirmed', '2026-07-02 00:00:00.0');
INSERT INTO `bookings` VALUES (153, 'PNR31827', 10, 10, 19, 9, 160000.00, 'Visa', 'Confirmed', '2026-06-20 00:00:00.0');
INSERT INTO `bookings` VALUES (154, 'PNR31914', 5, 1, 61, 9, 140000.00, 'Visa', 'Completed', '2026-08-15 00:00:00.0');
INSERT INTO `bookings` VALUES (155, 'PNR58968', 8, 3, 44, 9, 140000.00, 'WavePay', 'Confirmed', '2026-06-24 00:00:00.0');
INSERT INTO `bookings` VALUES (156, 'PNR96663', 9, 9, 64, 9, 120000.00, 'WavePay', 'Pending', '2026-08-23 00:00:00.0');
INSERT INTO `bookings` VALUES (157, 'PNR16531', 9, 3, 58, 9, 160000.00, 'Visa', 'Pending', '2026-07-15 00:00:00.0');
INSERT INTO `bookings` VALUES (158, 'PNR95273', 5, 10, 53, 9, 200000.00, 'KPay', 'Pending', '2026-06-29 00:00:00.0');
INSERT INTO `bookings` VALUES (159, 'PNR87981', 6, 8, 24, 9, 140000.00, 'Visa', 'Confirmed', '2026-07-03 00:00:00.0');
INSERT INTO `bookings` VALUES (160, 'PNR87703', 8, 4, 54, 9, 200000.00, 'KPay', 'Confirmed', '2026-05-28 00:00:00.0');
INSERT INTO `bookings` VALUES (161, 'PNR29353', 6, 7, 39, 9, 200000.00, 'Visa', 'Confirmed', '2026-06-28 00:00:00.0');
INSERT INTO `bookings` VALUES (162, 'PNR87728', 6, 7, 53, 9, 200000.00, 'KPay', 'Completed', '2026-07-06 00:00:00.0');
INSERT INTO `bookings` VALUES (163, 'PNR44907', 7, 5, 55, 9, 160000.00, 'Visa', 'Completed', '2026-06-05 00:00:00.0');
INSERT INTO `bookings` VALUES (164, 'PNR27437', 8, 5, 10, 9, 160000.00, 'Visa', 'Completed', '2026-07-06 00:00:00.0');
INSERT INTO `bookings` VALUES (165, 'PNR74673', 1, 1, 13, 9, 180000.00, 'Visa', 'Pending', '2026-08-16 00:00:00.0');
INSERT INTO `bookings` VALUES (166, 'PNR89623', 2, 1, 58, 9, 200000.00, 'KPay', 'Confirmed', '2026-06-01 00:00:00.0');
INSERT INTO `bookings` VALUES (167, 'PNR08981', 8, 7, 41, 9, 200000.00, 'Visa', 'Confirmed', '2026-06-09 00:00:00.0');
INSERT INTO `bookings` VALUES (168, 'PNR26199', 9, 9, 66, 9, 140000.00, 'WavePay', 'Confirmed', '2026-06-24 00:00:00.0');
INSERT INTO `bookings` VALUES (169, 'PNR97413', 10, 1, 54, 9, 180000.00, 'KPay', 'Completed', '2026-07-25 00:00:00.0');
INSERT INTO `bookings` VALUES (170, 'PNR33023', 8, 1, 45, 9, 200000.00, 'WavePay', 'Pending', '2026-08-06 00:00:00.0');
INSERT INTO `bookings` VALUES (171, 'PNR18864', 4, 10, 69, 9, 200000.00, 'Visa', 'Pending', '2026-08-18 00:00:00.0');
INSERT INTO `bookings` VALUES (172, 'PNR84603', 12, 4, 1, 9, 200000.00, 'KPay', 'Completed', '2026-06-02 00:00:00.0');
INSERT INTO `bookings` VALUES (173, 'PNR85555', 6, 8, 18, 9, 120000.00, 'WavePay', 'Confirmed', '2026-06-26 00:00:00.0');
INSERT INTO `bookings` VALUES (174, 'PNR32891', 8, 2, 58, 9, 180000.00, 'KPay', 'Completed', '2026-08-15 00:00:00.0');
INSERT INTO `bookings` VALUES (175, 'PNR75921', 6, 10, 37, 9, 180000.00, 'Visa', 'Completed', '2026-06-26 00:00:00.0');
INSERT INTO `bookings` VALUES (176, 'PNR90522', 7, 9, 50, 9, 200000.00, 'Visa', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (177, 'PNR36317', 8, 3, 5, 9, 180000.00, 'WavePay', 'Confirmed', '2026-07-31 00:00:00.0');
INSERT INTO `bookings` VALUES (178, 'PNR39302', 2, 4, 45, 9, 200000.00, 'KPay', 'Completed', '2026-06-28 00:00:00.0');
INSERT INTO `bookings` VALUES (179, 'PNR14759', 10, 5, 59, 9, 200000.00, 'Visa', 'Confirmed', '2026-07-27 00:00:00.0');
INSERT INTO `bookings` VALUES (180, 'PNR93332', 9, 7, 14, 9, 120000.00, 'WavePay', 'Confirmed', '2026-08-04 00:00:00.0');
INSERT INTO `bookings` VALUES (181, 'PNR48413', 9, 2, 36, 9, 120000.00, 'KPay', 'Confirmed', '2026-06-20 00:00:00.0');
INSERT INTO `bookings` VALUES (182, 'PNR64813', 1, 2, 50, 9, 120000.00, 'KPay', 'Pending', '2026-06-18 00:00:00.0');
INSERT INTO `bookings` VALUES (183, 'PNR18098', 8, 5, 45, 9, 180000.00, 'Visa', 'Completed', '2026-07-24 00:00:00.0');
INSERT INTO `bookings` VALUES (184, 'PNR93706', 8, 3, 23, 9, 200000.00, 'Visa', 'Pending', '2026-07-31 00:00:00.0');
INSERT INTO `bookings` VALUES (185, 'PNR68973', 8, 10, 68, 9, 200000.00, 'Visa', 'Confirmed', '2026-08-21 00:00:00.0');
INSERT INTO `bookings` VALUES (186, 'PNR04072', 1, 2, 28, 9, 180000.00, 'KPay', 'Confirmed', '2026-07-21 00:00:00.0');
INSERT INTO `bookings` VALUES (187, 'PNR33396', 6, 5, 67, 9, 140000.00, 'WavePay', 'Confirmed', '2026-06-12 00:00:00.0');
INSERT INTO `bookings` VALUES (188, 'PNR16281', 4, 2, 53, 9, 140000.00, 'KPay', 'Completed', '2026-07-04 00:00:00.0');
INSERT INTO `bookings` VALUES (189, 'PNR46109', 7, 4, 23, 9, 160000.00, 'KPay', 'Pending', '2026-07-14 00:00:00.0');
INSERT INTO `bookings` VALUES (190, 'PNR84223', 10, 6, 7, 9, 200000.00, 'WavePay', 'Confirmed', '2026-07-29 00:00:00.0');
INSERT INTO `bookings` VALUES (191, 'PNR87236', 6, 7, 58, 9, 140000.00, 'WavePay', 'Completed', '2026-08-10 00:00:00.0');
INSERT INTO `bookings` VALUES (192, 'PNR63921', 9, 6, 48, 9, 180000.00, 'WavePay', 'Completed', '2026-06-05 00:00:00.0');
INSERT INTO `bookings` VALUES (193, 'PNR06407', 8, 8, 69, 9, 180000.00, 'KPay', 'Completed', '2026-07-01 00:00:00.0');
INSERT INTO `bookings` VALUES (194, 'PNR29292', 8, 3, 14, 9, 140000.00, 'KPay', 'Pending', '2026-07-13 00:00:00.0');
INSERT INTO `bookings` VALUES (195, 'PNR02064', 8, 2, 3, 9, 160000.00, 'Visa', 'Pending', '2026-06-21 00:00:00.0');
INSERT INTO `bookings` VALUES (196, 'PNR66871', 2, 9, 41, 9, 160000.00, 'WavePay', 'Pending', '2026-06-06 00:00:00.0');
INSERT INTO `bookings` VALUES (197, 'PNR17933', 3, 6, 5, 9, 180000.00, 'KPay', 'Completed', '2026-07-20 00:00:00.0');
INSERT INTO `bookings` VALUES (198, 'PNR61721', 11, 5, 60, 9, 180000.00, 'KPay', 'Confirmed', '2026-07-30 00:00:00.0');
INSERT INTO `bookings` VALUES (199, 'PNR44686', 5, 4, 54, 9, 180000.00, 'WavePay', 'Completed', '2026-06-24 00:00:00.0');
INSERT INTO `bookings` VALUES (200, 'PNR04812', 2, 7, 54, 9, 200000.00, 'KPay', 'Confirmed', '2026-07-22 00:00:00.0');
INSERT INTO `bookings` VALUES (201, 'PNR77144', 9, 3, 66, 9, 120000.00, 'WavePay', 'Confirmed', '2026-08-19 00:00:00.0');
INSERT INTO `bookings` VALUES (202, 'PNR72195', 5, 8, 54, 9, 160000.00, 'Visa', 'Completed', '2026-06-13 00:00:00.0');
INSERT INTO `bookings` VALUES (203, 'PNR14331', 4, 9, 26, 9, 140000.00, 'WavePay', 'Completed', '2026-07-31 00:00:00.0');
INSERT INTO `bookings` VALUES (204, 'PNR66806', 6, 5, 42, 9, 180000.00, 'Visa', 'Completed', '2026-07-01 00:00:00.0');
INSERT INTO `bookings` VALUES (205, 'PNR62613', 4, 5, 40, 9, 140000.00, 'KPay', 'Pending', '2026-07-03 00:00:00.0');
INSERT INTO `bookings` VALUES (206, 'PNR46183', 7, 3, 56, 9, 120000.00, 'KPay', 'Confirmed', '2026-06-01 00:00:00.0');
INSERT INTO `bookings` VALUES (207, 'PNR21374', 3, 5, 29, 9, 200000.00, 'Visa', 'Confirmed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (208, 'PNR77343', 9, 2, 45, 9, 180000.00, 'Visa', 'Confirmed', '2026-06-22 00:00:00.0');
INSERT INTO `bookings` VALUES (209, 'PNR81106', 11, 1, 30, 9, 120000.00, 'Visa', 'Pending', '2026-08-08 00:00:00.0');
INSERT INTO `bookings` VALUES (210, 'PNR47141', 10, 5, 51, 9, 160000.00, 'Visa', 'Confirmed', '2026-06-11 00:00:00.0');
INSERT INTO `bookings` VALUES (211, 'PNR93551', 2, 1, 44, 9, 120000.00, 'KPay', 'Completed', '2026-06-01 00:00:00.0');
INSERT INTO `bookings` VALUES (212, 'PNR64457', 5, 10, 50, 9, 180000.00, 'KPay', 'Confirmed', '2026-08-13 00:00:00.0');
INSERT INTO `bookings` VALUES (213, 'PNR95711', 5, 10, 60, 9, 140000.00, 'Visa', 'Confirmed', '2026-06-30 00:00:00.0');
INSERT INTO `bookings` VALUES (214, 'PNR91880', 9, 8, 49, 9, 140000.00, 'Visa', 'Confirmed', '2026-08-25 00:00:00.0');
INSERT INTO `bookings` VALUES (215, 'PNR50695', 7, 1, 38, 9, 180000.00, 'Visa', 'Confirmed', '2026-07-04 00:00:00.0');
INSERT INTO `bookings` VALUES (216, 'PNR27118', 8, 3, 42, 9, 120000.00, 'Visa', 'Confirmed', '2026-06-12 00:00:00.0');
INSERT INTO `bookings` VALUES (217, 'PNR60340', 7, 9, 39, 9, 140000.00, 'Visa', 'Pending', '2026-06-01 00:00:00.0');
INSERT INTO `bookings` VALUES (218, 'PNR24535', 5, 3, 61, 9, 180000.00, 'Visa', 'Completed', '2026-08-20 00:00:00.0');
INSERT INTO `bookings` VALUES (219, 'PNR29229', 4, 5, 34, 9, 120000.00, 'WavePay', 'Confirmed', '2026-06-03 00:00:00.0');
INSERT INTO `bookings` VALUES (220, 'PNR14099', 11, 2, 65, 9, 140000.00, 'WavePay', 'Confirmed', '2026-07-22 00:00:00.0');
INSERT INTO `bookings` VALUES (221, 'PNR66431', 2, 9, 44, 9, 180000.00, 'WavePay', 'Confirmed', '2026-08-07 00:00:00.0');
INSERT INTO `bookings` VALUES (222, 'PNR75839', 3, 7, 38, 9, 200000.00, 'Visa', 'Pending', '2026-07-27 00:00:00.0');
INSERT INTO `bookings` VALUES (223, 'PNR00229', 1, 1, 29, 9, 180000.00, 'Visa', 'Confirmed', '2026-08-03 00:00:00.0');
INSERT INTO `bookings` VALUES (224, 'PNR06433', 7, 7, 50, 9, 160000.00, 'KPay', 'Pending', '2026-08-22 00:00:00.0');
INSERT INTO `bookings` VALUES (225, 'PNR23445', 1, 7, 5, 9, 140000.00, 'WavePay', 'Confirmed', '2026-05-29 00:00:00.0');
INSERT INTO `bookings` VALUES (226, 'PNR72233', 8, 2, 56, 9, 160000.00, 'KPay', 'Pending', '2026-07-06 00:00:00.0');
INSERT INTO `bookings` VALUES (227, 'PNR89857', 10, 4, 34, 9, 140000.00, 'WavePay', 'Confirmed', '2026-06-28 00:00:00.0');
INSERT INTO `bookings` VALUES (228, 'PNR75101', 10, 8, 12, 9, 180000.00, 'KPay', 'Confirmed', '2026-08-15 00:00:00.0');
INSERT INTO `bookings` VALUES (229, 'PNR45444', 12, 3, 34, 9, 180000.00, 'Visa', 'Completed', '2026-06-09 00:00:00.0');
INSERT INTO `bookings` VALUES (230, 'PNR45781', 9, 2, 43, 9, 160000.00, 'KPay', 'Confirmed', '2026-05-31 00:00:00.0');
INSERT INTO `bookings` VALUES (231, 'PNR51594', 9, 9, 30, 9, 160000.00, 'Visa', 'Pending', '2026-06-12 00:00:00.0');
INSERT INTO `bookings` VALUES (232, 'PNR54087', 3, 6, 5, 9, 180000.00, 'KPay', 'Pending', '2026-07-05 00:00:00.0');
INSERT INTO `bookings` VALUES (233, 'PNR42866', 6, 9, 54, 9, 160000.00, 'Visa', 'Pending', '2026-06-24 00:00:00.0');
INSERT INTO `bookings` VALUES (234, 'PNR96914', 10, 10, 26, 9, 120000.00, 'Visa', 'Pending', '2026-07-13 00:00:00.0');
INSERT INTO `bookings` VALUES (235, 'PNR74018', 4, 1, 40, 9, 180000.00, 'WavePay', 'Confirmed', '2026-06-10 00:00:00.0');
INSERT INTO `bookings` VALUES (255, 'PNR77111', 1, 1, 1, 9, 450000.00, 'KPay', 'Completed', '2026-08-25 10:15:00.0');
INSERT INTO `bookings` VALUES (256, 'PNR77112', 2, 2, 2, 9, 210000.00, 'WavePay', 'Confirmed', '2026-08-25 14:30:00.0');
INSERT INTO `bookings` VALUES (257, 'PNR77113', 3, 3, 3, 9, 320000.00, 'Visa', 'Completed', '2026-08-24 09:20:00.0');
INSERT INTO `bookings` VALUES (258, 'PNR77114', 4, 4, 4, 9, 150000.00, 'KPay', 'Confirmed', '2026-08-24 16:45:00.0');
INSERT INTO `bookings` VALUES (259, 'PNR77115', 1, 5, 5, 9, 680000.00, 'WavePay', 'Completed', '2026-08-23 11:10:00.0');
INSERT INTO `bookings` VALUES (260, 'PNR77116', 2, 6, 6, 9, 120000.00, 'Visa', 'Confirmed', '2026-08-23 15:00:00.0');
INSERT INTO `bookings` VALUES (261, 'PNR77117', 3, 7, 7, 9, 480000.00, 'KPay', 'Completed', '2026-08-22 08:30:00.0');
INSERT INTO `bookings` VALUES (262, 'PNR77118', 4, 8, 8, 9, 240000.00, 'WavePay', 'Confirmed', '2026-08-22 13:20:00.0');
INSERT INTO `bookings` VALUES (263, 'PNR77119', 1, 9, 9, 9, 350000.00, 'Visa', 'Completed', '2026-08-21 10:05:00.0');
INSERT INTO `bookings` VALUES (264, 'PNR77120', 2, 10, 10, 9, 180000.00, 'KPay', 'Confirmed', '2026-08-21 17:50:00.0');
INSERT INTO `bookings` VALUES (265, 'PNR77121', 3, 1, 11, 9, 540000.00, 'WavePay', 'Completed', '2026-08-20 09:40:00.0');
INSERT INTO `bookings` VALUES (266, 'PNR77122', 4, 2, 12, 9, 210000.00, 'Visa', 'Confirmed', '2026-08-20 12:15:00.0');
INSERT INTO `bookings` VALUES (267, 'PNR77123', 1, 3, 13, 9, 720000.00, 'KPay', 'Completed', '2026-08-19 08:00:00.0');
INSERT INTO `bookings` VALUES (268, 'PNR77124', 2, 4, 14, 9, 160000.00, 'WavePay', 'Confirmed', '2026-08-19 14:10:00.0');
DROP TABLE IF EXISTS `cities`;
CREATE TABLE `cities` (
  `city_id` int NOT NULL AUTO_INCREMENT,
  `city_code` varchar(10) NOT NULL,
  `city_name` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`city_id`),
  UNIQUE KEY `city_code` (`city_code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `cities` VALUES (1, 'RGN', 'Yangon', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (2, 'MDL', 'Mandalay', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (3, 'NYT', 'Naypyidaw', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (4, 'NYU', 'Bagan (Nyaung-U)', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (5, 'HEH', 'Heho (Taunggyi)', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (6, 'SNW', 'Thandwe (Ngapali)', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (7, 'STW', 'Sittwe', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (8, 'MYT', 'Myitkyina', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (9, 'THL', 'Tachileik', 'Myanmar', '2026-08-25 12:22:29.0');
INSERT INTO `cities` VALUES (10, 'KAW', 'Kawthaung', 'Myanmar', '2026-08-25 12:22:29.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `flights` VALUES (1, 'FL-101', 'FlyPlan Air', 1, 2, '2026-08-25T08:00', '2026-08-25T09:30', 70, 120000.00, 'Departed');
INSERT INTO `flights` VALUES (2, 'FL-102', 'FlyPlan Air', 1, 4, '2026-08-25T10:00', '2026-08-25T11:20', 70, 150000.00, 'Scheduled');
INSERT INTO `flights` VALUES (3, 'FL-103', 'FlyPlan Air', 2, 1, '2026-08-25T14:00', '2026-08-25T15:30', 70, 120000.00, 'Scheduled');
INSERT INTO `flights` VALUES (4, 'FL-104', 'FlyPlan Air', 1, 6, '2026-08-25T16:30', '2026-08-25T17:30', 70, 180000.00, 'Delayed');
INSERT INTO `flights` VALUES (5, 'FL-201', 'FlyPlan Air', 1, 5, '2026-08-24T09:00', '2026-08-24T10:15', 70, 140000.00, 'Completed');
INSERT INTO `flights` VALUES (6, 'FL-202', 'FlyPlan Air', 3, 1, '2026-08-23T11:00', '2026-08-23T12:00', 70, 95000.00, 'Completed');
INSERT INTO `flights` VALUES (7, 'FL-203', 'FlyPlan Air', 1, 7, '2026-08-22T13:00', '2026-08-22T14:15', 70, 160000.00, 'Completed');
INSERT INTO `flights` VALUES (8, 'FL-204', 'FlyPlan Air', 1, 8, '2026-08-20T08:30', '2026-08-20T10:30', 70, 250000.00, 'Completed');
INSERT INTO `flights` VALUES (9, 'FL-301', 'FlyPlan Air', 2, 4, '2026-08-10T10:00', '2026-08-10T10:45', 70, 75000.00, 'Completed');
INSERT INTO `flights` VALUES (10, 'FL-302', 'FlyPlan Air', 1, 9, '2026-07-26T14:00', '2026-07-26T16:00', 70, 220000.00, 'Completed');
INSERT INTO `flights` VALUES (11, 'FL-303', 'FlyPlan Air', 1, 10, '2026-07-11T07:00', '2026-07-11T09:15', 70, 280000.00, 'Completed');
INSERT INTO `flights` VALUES (12, 'FL-304', 'FlyPlan Air', 2, 5, '2026-06-26T11:30', '2026-06-26T12:15', 70, 85000.00, 'Completed');
DROP TABLE IF EXISTS `logs`;
CREATE TABLE `logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `action` varchar(255) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
INSERT INTO `logs` VALUES (26, 5, 'Triggered manual database backup: flight_db_backup_20260825_115607.sql', '2026-08-25 11:56:07.0');
INSERT INTO `logs` VALUES (27, 8, 'Log out', '2026-08-14 00:00:00.0');
INSERT INTO `logs` VALUES (28, 5, 'Log out', '2026-06-22 00:00:00.0');
INSERT INTO `logs` VALUES (29, 8, 'Login Successful', '2026-07-26 00:00:00.0');
INSERT INTO `logs` VALUES (30, 8, 'Login Successful', '2026-06-30 00:00:00.0');
INSERT INTO `logs` VALUES (31, 9, 'Login Successful', '2026-07-29 00:00:00.0');
INSERT INTO `logs` VALUES (32, 10, 'Login Successful', '2026-07-25 00:00:00.0');
INSERT INTO `logs` VALUES (33, 8, 'Login Successful', '2026-08-25 00:00:00.0');
INSERT INTO `logs` VALUES (34, 10, 'Login Successful', '2026-08-04 00:00:00.0');
INSERT INTO `logs` VALUES (35, 8, 'Login Successful', '2026-07-04 00:00:00.0');
INSERT INTO `logs` VALUES (36, 9, 'Log out', '2026-07-16 00:00:00.0');
INSERT INTO `logs` VALUES (37, 10, 'Login Successful', '2026-07-02 00:00:00.0');
INSERT INTO `logs` VALUES (38, 10, 'Login Successful', '2026-07-23 00:00:00.0');
INSERT INTO `logs` VALUES (39, 5, 'Login Successful', '2026-08-04 00:00:00.0');
INSERT INTO `logs` VALUES (40, 10, 'Login Successful', '2026-06-21 00:00:00.0');
INSERT INTO `logs` VALUES (41, 8, 'Login Successful', '2026-06-29 00:00:00.0');
INSERT INTO `logs` VALUES (42, 10, 'Log out', '2026-06-23 00:00:00.0');
INSERT INTO `logs` VALUES (43, 9, 'Login Successful', '2026-06-05 00:00:00.0');
INSERT INTO `logs` VALUES (44, 9, 'Login Successful', '2026-06-10 00:00:00.0');
INSERT INTO `logs` VALUES (45, 9, 'Login Successful', '2026-06-23 00:00:00.0');
INSERT INTO `logs` VALUES (46, 9, 'Login Successful', '2026-05-31 00:00:00.0');
INSERT INTO `logs` VALUES (47, 5, 'Login Successful', '2026-08-23 00:00:00.0');
INSERT INTO `logs` VALUES (48, 5, 'Login Successful', '2026-08-17 00:00:00.0');
INSERT INTO `logs` VALUES (49, 9, 'Login Successful', '2026-07-09 00:00:00.0');
INSERT INTO `logs` VALUES (50, 8, 'Log out', '2026-06-24 00:00:00.0');
INSERT INTO `logs` VALUES (51, 9, 'Login Successful', '2026-07-08 00:00:00.0');
INSERT INTO `logs` VALUES (58, 5, 'Login Successful', '2026-08-25 12:23:01.0');
INSERT INTO `logs` VALUES (59, 5, 'Log out', '2026-08-25 12:23:40.0');
INSERT INTO `logs` VALUES (60, 8, 'Login Successful', '2026-08-25 12:23:49.0');
INSERT INTO `logs` VALUES (61, 8, 'Log out', '2026-08-25 12:24:43.0');
INSERT INTO `logs` VALUES (62, 9, 'Login Successful', '2026-08-25 12:24:53.0');
INSERT INTO `logs` VALUES (63, 9, 'Login Successful', '2026-08-25 12:28:03.0');
INSERT INTO `logs` VALUES (64, 9, 'Login Successful', '2026-08-25 14:03:47.0');
INSERT INTO `logs` VALUES (65, 9, 'Login Successful', '2026-08-25 14:08:04.0');
INSERT INTO `logs` VALUES (66, 9, 'Login Successful', '2026-08-25 14:12:36.0');
INSERT INTO `logs` VALUES (67, 9, 'Login Successful', '2026-08-25 14:13:55.0');
INSERT INTO `logs` VALUES (68, 9, 'Login Successful', '2026-08-25 14:18:48.0');
INSERT INTO `logs` VALUES (69, 9, 'Log out', '2026-08-25 14:19:39.0');
INSERT INTO `logs` VALUES (70, 8, 'Login Successful', '2026-08-25 14:19:50.0');
INSERT INTO `logs` VALUES (71, 8, 'Login Successful', '2026-08-25 14:24:50.0');
INSERT INTO `logs` VALUES (72, 8, 'Login Successful', '2026-08-25 14:26:40.0');
INSERT INTO `logs` VALUES (73, 5, 'Login Successful', '2026-08-26 08:33:19.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `passengers` VALUES (1, 'Aung Aung', '12/YGN(N)123456', '0912345678', 'aung@gmail.com', 'Male', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (2, 'Su Su', '12/YGN(N)654321', '0987654321', 'susu@gmail.com', 'Female', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (3, 'Kyaw Kyaw', '9/MDY(N)111222', '0944445555', 'kyaw@gmail.com', 'Male', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (4, 'Mya Mya', '9/MDY(N)333444', '0955556666', 'mya@gmail.com', 'Female', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (5, 'Zaw Zaw', '1/MYT(N)999888', '0977778888', 'zaw@gmail.com', 'Male', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (6, 'Hla Hla', '5/SGG(N)222111', '0999990000', 'hla@gmail.com', 'Female', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (7, 'Tun Tun', '14/PYP(N)555777', '0922223333', 'tun@gmail.com', 'Male', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (8, 'Nilar', '8/YNG(N)444333', '0944443333', 'nilar@gmail.com', 'Female', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (9, 'Win Min', '3/KRN(N)888999', '0966667777', 'win@gmail.com', 'Male', NULL, NULL, '2026-08-25 12:22:29.0');
INSERT INTO `passengers` VALUES (10, 'Khin Khin', '7/PNN(N)111000', '0988889999', 'khin@gmail.com', 'Female', NULL, NULL, '2026-08-25 12:22:29.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `requests` VALUES (1, 5, 9, 8, 'Change of travel plans', 'Pending', '2026-06-22 00:00:00.0');
INSERT INTO `requests` VALUES (2, 6, 9, 8, 'Medical emergency', 'Pending', '2026-07-26 00:00:00.0');
INSERT INTO `requests` VALUES (3, 7, 9, 8, 'Medical emergency', 'Approved', '2026-07-25 00:00:00.0');
INSERT INTO `requests` VALUES (4, 8, 10, 8, 'Medical emergency', 'Approved', '2026-08-16 00:00:00.0');
INSERT INTO `requests` VALUES (5, 9, 9, 8, 'Change of travel plans', 'Pending', '2026-06-06 00:00:00.0');
INSERT INTO `requests` VALUES (6, 10, 9, 8, 'Change of travel plans', 'Approved', '2026-07-21 00:00:00.0');
INSERT INTO `requests` VALUES (7, 11, 10, 8, 'Change of travel plans', 'Approved', '2026-06-22 00:00:00.0');
INSERT INTO `requests` VALUES (8, 12, 10, 8, 'Flight was delayed', 'Pending', '2026-07-20 00:00:00.0');
INSERT INTO `requests` VALUES (9, 15, 10, 8, 'Medical emergency', 'Pending', '2026-08-01 00:00:00.0');
INSERT INTO `requests` VALUES (10, 16, 10, 8, 'Booked by mistake', 'Approved', '2026-06-05 00:00:00.0');
INSERT INTO `requests` VALUES (11, 18, 10, 8, 'Change of travel plans', 'Approved', '2026-07-30 00:00:00.0');
INSERT INTO `requests` VALUES (12, 20, 9, 8, 'Change of travel plans', 'Approved', '2026-06-03 00:00:00.0');
INSERT INTO `requests` VALUES (13, 26, 10, 8, 'Flight was delayed', 'Approved', '2026-06-01 00:00:00.0');
INSERT INTO `requests` VALUES (14, 32, 10, 8, 'Medical emergency', 'Pending', '2026-07-20 00:00:00.0');
INSERT INTO `requests` VALUES (15, 34, 9, 8, 'Medical emergency', 'Pending', '2026-06-01 00:00:00.0');
INSERT INTO `requests` VALUES (16, 35, 9, 8, 'Flight was delayed', 'Approved', '2026-08-24 00:00:00.0');
INSERT INTO `requests` VALUES (17, 36, 10, 8, 'Medical emergency', 'Approved', '2026-08-15 00:00:00.0');
INSERT INTO `requests` VALUES (18, 37, 9, 8, 'Booked by mistake', 'Approved', '2026-08-05 00:00:00.0');
INSERT INTO `requests` VALUES (19, 38, 10, 8, 'Booked by mistake', 'Pending', '2026-06-05 00:00:00.0');
INSERT INTO `requests` VALUES (20, 43, 10, 8, 'Change of travel plans', 'Approved', '2026-07-21 00:00:00.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `seats` VALUES (1, 1, '12D', 'Premium', 'false');
INSERT INTO `seats` VALUES (2, 1, '7D', 'Premium', 'false');
INSERT INTO `seats` VALUES (3, 1, '3D', 'Premium', 'false');
INSERT INTO `seats` VALUES (4, 1, '1A', 'Business', 'false');
INSERT INTO `seats` VALUES (5, 1, '4A', 'Premium', 'false');
INSERT INTO `seats` VALUES (6, 1, '1B', 'Business', 'false');
INSERT INTO `seats` VALUES (7, 1, '12D', 'Business', 'false');
INSERT INTO `seats` VALUES (8, 1, '5B', 'Business', 'false');
INSERT INTO `seats` VALUES (9, 1, '3D', 'Business', 'false');
INSERT INTO `seats` VALUES (10, 1, '7D', 'Economy', 'false');
INSERT INTO `seats` VALUES (11, 1, '4A', 'Business', 'false');
INSERT INTO `seats` VALUES (12, 1, '3D', 'Economy', 'false');
INSERT INTO `seats` VALUES (13, 1, '6C', 'Economy', 'false');
INSERT INTO `seats` VALUES (14, 1, '9B', 'Economy', 'false');
INSERT INTO `seats` VALUES (15, 1, '6C', 'Premium', 'false');
INSERT INTO `seats` VALUES (16, 2, '2C', 'Premium', 'false');
INSERT INTO `seats` VALUES (17, 2, '9B', 'Premium', 'false');
INSERT INTO `seats` VALUES (18, 2, '8A', 'Economy', 'false');
INSERT INTO `seats` VALUES (19, 2, '4A', 'Economy', 'false');
INSERT INTO `seats` VALUES (20, 2, '4A', 'Economy', 'false');
INSERT INTO `seats` VALUES (21, 2, '5B', 'Premium', 'false');
INSERT INTO `seats` VALUES (22, 2, '5B', 'Premium', 'false');
INSERT INTO `seats` VALUES (23, 2, '1B', 'Business', 'false');
INSERT INTO `seats` VALUES (24, 2, '10C', 'Premium', 'false');
INSERT INTO `seats` VALUES (25, 2, '4A', 'Business', 'false');
INSERT INTO `seats` VALUES (26, 2, '2C', 'Economy', 'false');
INSERT INTO `seats` VALUES (27, 2, '1B', 'Premium', 'false');
INSERT INTO `seats` VALUES (28, 2, '4A', 'Business', 'false');
INSERT INTO `seats` VALUES (29, 2, '1B', 'Economy', 'false');
INSERT INTO `seats` VALUES (30, 2, '3D', 'Economy', 'false');
INSERT INTO `seats` VALUES (31, 3, '8A', 'Business', 'false');
INSERT INTO `seats` VALUES (32, 3, '4A', 'Economy', 'false');
INSERT INTO `seats` VALUES (33, 3, '2C', 'Premium', 'false');
INSERT INTO `seats` VALUES (34, 3, '1A', 'Economy', 'false');
INSERT INTO `seats` VALUES (35, 4, '3D', 'Business', 'false');
INSERT INTO `seats` VALUES (36, 4, '8A', 'Economy', 'false');
INSERT INTO `seats` VALUES (37, 4, '3D', 'Business', 'false');
INSERT INTO `seats` VALUES (38, 4, '9B', 'Economy', 'false');
INSERT INTO `seats` VALUES (39, 4, '10C', 'Economy', 'false');
INSERT INTO `seats` VALUES (40, 5, '2C', 'Business', 'false');
INSERT INTO `seats` VALUES (41, 5, '9B', 'Economy', 'false');
INSERT INTO `seats` VALUES (42, 5, '8A', 'Business', 'false');
INSERT INTO `seats` VALUES (43, 5, '6C', 'Business', 'false');
INSERT INTO `seats` VALUES (44, 5, '3D', 'Economy', 'false');
INSERT INTO `seats` VALUES (45, 5, '9B', 'Business', 'false');
INSERT INTO `seats` VALUES (46, 5, '3D', 'Business', 'false');
INSERT INTO `seats` VALUES (47, 5, '1B', 'Economy', 'false');
INSERT INTO `seats` VALUES (48, 5, '12D', 'Premium', 'false');
INSERT INTO `seats` VALUES (49, 5, '10C', 'Business', 'false');
INSERT INTO `seats` VALUES (50, 5, '12D', 'Economy', 'false');
INSERT INTO `seats` VALUES (51, 5, '8A', 'Premium', 'false');
INSERT INTO `seats` VALUES (52, 5, '2C', 'Business', 'false');
INSERT INTO `seats` VALUES (53, 5, '1A', 'Economy', 'false');
INSERT INTO `seats` VALUES (54, 5, '1B', 'Business', 'false');
INSERT INTO `seats` VALUES (55, 5, '8A', 'Premium', 'false');
INSERT INTO `seats` VALUES (56, 6, '1A', 'Business', 'false');
INSERT INTO `seats` VALUES (57, 6, '1B', 'Economy', 'false');
INSERT INTO `seats` VALUES (58, 6, '9B', 'Premium', 'false');
INSERT INTO `seats` VALUES (59, 6, '4A', 'Business', 'false');
INSERT INTO `seats` VALUES (60, 6, '9B', 'Premium', 'false');
INSERT INTO `seats` VALUES (61, 6, '7D', 'Economy', 'false');
INSERT INTO `seats` VALUES (62, 6, '5B', 'Economy', 'false');
INSERT INTO `seats` VALUES (63, 6, '2C', 'Economy', 'false');
INSERT INTO `seats` VALUES (64, 6, '6C', 'Business', 'false');
INSERT INTO `seats` VALUES (65, 7, '10C', 'Premium', 'false');
INSERT INTO `seats` VALUES (66, 7, '6C', 'Premium', 'false');
INSERT INTO `seats` VALUES (67, 7, '9B', 'Premium', 'false');
INSERT INTO `seats` VALUES (68, 7, '1B', 'Business', 'false');
INSERT INTO `seats` VALUES (69, 7, '2C', 'Economy', 'false');
INSERT INTO `seats` VALUES (70, 7, '9B', 'Business', 'false');
INSERT INTO `seats` VALUES (71, 7, '9B', 'Economy', 'false');
INSERT INTO `seats` VALUES (72, 7, '9B', 'Premium', 'false');
INSERT INTO `seats` VALUES (73, 7, '4A', 'Business', 'false');
INSERT INTO `seats` VALUES (74, 8, '9B', 'Premium', 'false');
INSERT INTO `seats` VALUES (75, 8, '3D', 'Economy', 'false');
INSERT INTO `seats` VALUES (76, 8, '6C', 'Economy', 'false');
INSERT INTO `seats` VALUES (77, 8, '2C', 'Economy', 'false');
INSERT INTO `seats` VALUES (78, 8, '6C', 'Business', 'false');
INSERT INTO `seats` VALUES (79, 8, '3D', 'Economy', 'false');
INSERT INTO `seats` VALUES (80, 8, '3D', 'Business', 'false');
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
