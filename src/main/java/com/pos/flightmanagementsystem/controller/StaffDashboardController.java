package com.pos.flightmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class StaffDashboardController implements Initializable {

    @FXML private Label lblCurrentDate;

    // --- Metric Labels ---
    @FXML private Label lblMySales;
    @FXML private Label lblMyBookings;
    @FXML private Label lblTodayFlights;
    @FXML private Label lblPendingCancels;

    // --- Flight Board Table ---
    @FXML private TableView<FlightData> flightBoardTable;
    @FXML private TableColumn<FlightData, String> colFlightNo;
    @FXML private TableColumn<FlightData, String> colFlightRoute;
    @FXML private TableColumn<FlightData, String> colFlightTime;
    @FXML private TableColumn<FlightData, String> colFlightStatus;

    // --- Recent Bookings Table ---
    @FXML private TableView<BookingData> recentBookingsTable;
    @FXML private TableColumn<BookingData, String> colBookingPNR;
    @FXML private TableColumn<BookingData, String> colBookingPrice;
    @FXML private TableColumn<BookingData, String> colBookingDate;
    @FXML private TableColumn<BookingData, String> colBookingStatus;

    private final int currentStaffId = 12;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblCurrentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        setupTables();
        loadStaffMetrics();
        loadFlightBoard();
        loadRecentBookings();
    }

    private void setupTables() {
        // Flight Board Setup
        colFlightNo.setCellValueFactory(new PropertyValueFactory<>("flightNo"));
        colFlightRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colFlightTime.setCellValueFactory(new PropertyValueFactory<>("departureTime"));

        // Flight Status Custom Cell (Color Styling)
        colFlightStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colFlightStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(status);
                    if (status.equalsIgnoreCase("Scheduled")) {
                        lbl.getStyleClass().setAll("status-badge-blue");
                    } else if (status.equalsIgnoreCase("Completed")) {
                        lbl.getStyleClass().setAll("status-badge-green");
                    } else {
                        lbl.getStyleClass().setAll("status-badge-red");
                    }
                    setGraphic(lbl);
                }
            }
        });

        // Recent Bookings Setup
        colBookingPNR.setCellValueFactory(new PropertyValueFactory<>("pnr"));
        colBookingPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));

        // Booking Status Custom Cell (Color Styling)
        colBookingStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBookingStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(status);
                    if (status.equalsIgnoreCase("Confirmed") || status.equalsIgnoreCase("Completed")) {
                        lbl.getStyleClass().setAll("status-badge-green");
                    } else if (status.equalsIgnoreCase("Pending")) {
                        lbl.getStyleClass().setAll("status-badge-yellow");
                    } else {
                        lbl.getStyleClass().setAll("status-badge-red");
                    }
                    setGraphic(lbl);
                }
            }
        });
    }

    private void loadStaffMetrics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            int currentMonth = LocalDate.now().getMonthValue();
            int currentYear = LocalDate.now().getYear();

            // 1. My Sales
            String salesQuery = "SELECT SUM(total_price) FROM bookings WHERE staff_id = ? AND MONTH(booking_date) = ? AND YEAR(booking_date) = ? AND status IN ('Confirmed', 'Completed')";
            try (PreparedStatement stmt = conn.prepareStatement(salesQuery)) {
                stmt.setInt(1, currentStaffId);
                stmt.setInt(2, currentMonth);
                stmt.setInt(3, currentYear);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) lblMySales.setText(String.format("%,.0f MMK", rs.getDouble(1)));
                }
            }

            // 2. My Bookings
            String bookingsQuery = "SELECT COUNT(*) FROM bookings WHERE staff_id = ? AND MONTH(booking_date) = ? AND YEAR(booking_date) = ? AND status IN ('Confirmed', 'Completed')";
            try (PreparedStatement stmt = conn.prepareStatement(bookingsQuery)) {
                stmt.setInt(1, currentStaffId);
                stmt.setInt(2, currentMonth);
                stmt.setInt(3, currentYear);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) lblMyBookings.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // 3. Today's Flights
            String flightsQuery = "SELECT COUNT(*) FROM flights WHERE DATE(departure_time) = CURRENT_DATE AND status = 'Scheduled'";
            try (PreparedStatement stmt = conn.prepareStatement(flightsQuery); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) lblTodayFlights.setText(String.valueOf(rs.getInt(1)));
            }

            // 4. Pending Cancel Requests
            String requestsQuery = "SELECT COUNT(*) FROM requests WHERE staff_id = ? AND status = 'Pending'";
            try (PreparedStatement stmt = conn.prepareStatement(requestsQuery)) {
                stmt.setInt(1, currentStaffId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) lblPendingCancels.setText(String.valueOf(rs.getInt(1)));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFlightBoard() {
        ObservableList<FlightData> flightList = FXCollections.observableArrayList();
        String query = "SELECT f.flight_number, c1.city_name AS dep, c2.city_name AS arr, f.departure_time, f.status " +
                "FROM flights f JOIN cities c1 ON f.departure_city_id = c1.city_id " +
                "JOIN cities c2 ON f.arrival_city_id = c2.city_id " +
                "WHERE DATE(f.departure_time) = CURRENT_DATE ORDER BY f.departure_time ASC";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String route = rs.getString("dep") + " - " + rs.getString("arr");
                Timestamp ts = rs.getTimestamp("departure_time");
                String timeStr = (ts != null) ? ts.toLocalDateTime().format(formatter) : "Unknown";
                flightList.add(new FlightData(rs.getString("flight_number"), route, timeStr, rs.getString("status")));
            }
            flightBoardTable.setItems(flightList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecentBookings() {
        ObservableList<BookingData> bookingList = FXCollections.observableArrayList();
        String query = "SELECT pnr_code, total_price, booking_date, status FROM bookings " +
                "WHERE staff_id = ? ORDER BY booking_date DESC LIMIT 15";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentStaffId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("booking_date");
                    String dateStr = (ts != null) ? ts.toLocalDateTime().format(formatter) : "Unknown";
                    String priceStr = String.format("%,.0f", rs.getDouble("total_price"));
                    bookingList.add(new BookingData(rs.getString("pnr_code"), priceStr, dateStr, rs.getString("status")));
                }
            }
            recentBookingsTable.setItems(bookingList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FlightData {
        private String flightNo, route, departureTime, status;
        public FlightData(String flightNo, String route, String departureTime, String status) {
            this.flightNo = flightNo; this.route = route; this.departureTime = departureTime; this.status = status;
        }
        public String getFlightNo() { return flightNo; }
        public String getRoute() { return route; }
        public String getDepartureTime() { return departureTime; }
        public String getStatus() { return status; }
    }

    public static class BookingData {
        private String pnr, price, bookingDate, status;
        public BookingData(String pnr, String price, String bookingDate, String status) {
            this.pnr = pnr; this.price = price; this.bookingDate = bookingDate; this.status = status;
        }
        public String getPnr() { return pnr; }
        public String getPrice() { return price; }
        public String getBookingDate() { return bookingDate; }
        public String getStatus() { return status; }
    }
}