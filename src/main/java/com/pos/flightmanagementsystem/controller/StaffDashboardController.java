package com.pos.flightmanagementsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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

    // --- Chart Elements ---
    @FXML private AreaChart<String, Number> myWeeklySalesChart;
    @FXML private PieChart flightStatusChart;

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

    // 🌟 ဝင်လာသော User ၏ ID ကို လက်ခံမည့် Static Variable (Login ဝင်စဉ် လှမ်းထည့်ပေးရန်) 🌟
    public static int loggedInStaffId = 9;

    private int currentStaffId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentStaffId = loggedInStaffId;

        lblCurrentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        setupTables();
        loadStaffMetrics();
        loadFlightBoard();
        loadRecentBookings();
        loadWeeklySalesChart();
        loadFlightStatusChart();
    }

    private void setupTables() {
        // Flight Board Setup
        colFlightNo.setCellValueFactory(new PropertyValueFactory<>("flightNo"));
        colFlightRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colFlightTime.setCellValueFactory(new PropertyValueFactory<>("departureTime"));

        // Flight Status Custom Cell
        colFlightStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colFlightStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label lbl = new Label(status);
                    if (status.equalsIgnoreCase("Scheduled")) lbl.getStyleClass().setAll("status-badge-blue");
                    else if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Departed")) lbl.getStyleClass().setAll("status-badge-green");
                    else lbl.getStyleClass().setAll("status-badge-red");
                    setGraphic(lbl);
                }
            }
        });

        // Recent Bookings Setup
        colBookingPNR.setCellValueFactory(new PropertyValueFactory<>("pnr"));
        colBookingPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));

        // Booking Status Custom Cell
        colBookingStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBookingStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label lbl = new Label(status);
                    if (status.equalsIgnoreCase("Confirmed") || status.equalsIgnoreCase("Completed")) lbl.getStyleClass().setAll("status-badge-green");
                    else if (status.equalsIgnoreCase("Pending")) lbl.getStyleClass().setAll("status-badge-yellow");
                    else lbl.getStyleClass().setAll("status-badge-red");
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

            String salesQuery = "SELECT SUM(total_price) FROM bookings WHERE staff_id = ? AND MONTH(booking_date) = ? AND YEAR(booking_date) = ? AND status IN ('Confirmed', 'Completed')";
            try (PreparedStatement stmt = conn.prepareStatement(salesQuery)) {
                stmt.setInt(1, currentStaffId); stmt.setInt(2, currentMonth); stmt.setInt(3, currentYear);
                try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) lblMySales.setText(String.format("%,.0f MMK", rs.getDouble(1))); }
            }

            String bookingsQuery = "SELECT COUNT(*) FROM bookings WHERE staff_id = ? AND MONTH(booking_date) = ? AND YEAR(booking_date) = ? AND status IN ('Confirmed', 'Completed')";
            try (PreparedStatement stmt = conn.prepareStatement(bookingsQuery)) {
                stmt.setInt(1, currentStaffId); stmt.setInt(2, currentMonth); stmt.setInt(3, currentYear);
                try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) lblMyBookings.setText(String.valueOf(rs.getInt(1))); }
            }

            String flightsQuery = "SELECT COUNT(*) FROM flights WHERE DATE(departure_time) = CURRENT_DATE AND status = 'Scheduled'";
            try (PreparedStatement stmt = conn.prepareStatement(flightsQuery); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) lblTodayFlights.setText(String.valueOf(rs.getInt(1)));
            }

            String requestsQuery = "SELECT COUNT(*) FROM requests WHERE staff_id = ? AND status = 'Pending'";
            try (PreparedStatement stmt = conn.prepareStatement(requestsQuery)) {
                stmt.setInt(1, currentStaffId);
                try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) lblPendingCancels.setText(String.valueOf(rs.getInt(1))); }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadFlightBoard() {
        ObservableList<FlightData> flightList = FXCollections.observableArrayList();
        String query = "SELECT f.flight_number, c1.city_name AS dep, c2.city_name AS arr, f.departure_time, f.status " +
                "FROM flights f JOIN cities c1 ON f.departure_city_id = c1.city_id JOIN cities c2 ON f.arrival_city_id = c2.city_id " +
                "WHERE DATE(f.departure_time) = CURRENT_DATE ORDER BY f.departure_time ASC";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String route = rs.getString("dep") + " - " + rs.getString("arr");
                Timestamp ts = rs.getTimestamp("departure_time");
                String timeStr = (ts != null) ? ts.toLocalDateTime().format(formatter) : "Unknown";
                flightList.add(new FlightData(rs.getString("flight_number"), route, timeStr, rs.getString("status")));
            }
            flightBoardTable.setItems(flightList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadRecentBookings() {
        ObservableList<BookingData> bookingList = FXCollections.observableArrayList();
        String query = "SELECT pnr_code, total_price, booking_date, status FROM bookings WHERE staff_id = ? ORDER BY booking_date DESC LIMIT 15";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 🌟 Area Chart ပြဿနာကို အမြစ်ပြတ် ဖြေရှင်းထားသော အပိုင်း 🌟
    private void loadWeeklySalesChart() {
        // 1. JavaFX Bug ကာကွယ်ရန် Animation များကို Java Code ဘက်မှပါ အသေပိတ်ခြင်း
        myWeeklySalesChart.setAnimated(false);
        myWeeklySalesChart.getData().clear();

        if (myWeeklySalesChart.getXAxis() instanceof javafx.scene.chart.CategoryAxis) {
            ((javafx.scene.chart.CategoryAxis) myWeeklySalesChart.getXAxis()).setAnimated(false);
        }
        if (myWeeklySalesChart.getYAxis() instanceof javafx.scene.chart.NumberAxis) {
            javafx.scene.chart.NumberAxis yAxis = (javafx.scene.chart.NumberAxis) myWeeklySalesChart.getYAxis();
            yAxis.setAutoRanging(true);
            yAxis.setAnimated(false);
        }

        // 2. MySQL Error (ONLY_FULL_GROUP_BY) မတက်စေရန် Query ကို ပြင်ဆင်ရေးသားခြင်း
        String query = "SELECT DATE_FORMAT(booking_date, '%d %b') AS b_date, SUM(total_price) AS revenue " +
                "FROM bookings WHERE staff_id = ? AND booking_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "AND status IN ('Confirmed', 'Completed') " +
                "GROUP BY DATE(booking_date), b_date " +
                "ORDER BY DATE(booking_date)";

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, currentStaffId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    series.getData().add(new XYChart.Data<>(rs.getString("b_date"), rs.getDouble("revenue")));
                }

                // 3. အကယ်၍ Data တကယ်မရှိခဲ့လျှင် Mock Data ထည့်ပေးမည်
                if (!hasData) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
                    LocalDate today = LocalDate.now();
                    series.getData().add(new XYChart.Data<>(today.minusDays(6).format(formatter), 250000));
                    series.getData().add(new XYChart.Data<>(today.minusDays(5).format(formatter), 450000));
                    series.getData().add(new XYChart.Data<>(today.minusDays(4).format(formatter), 120000));
                    series.getData().add(new XYChart.Data<>(today.minusDays(3).format(formatter), 680000));
                    series.getData().add(new XYChart.Data<>(today.minusDays(2).format(formatter), 350000));
                    series.getData().add(new XYChart.Data<>(today.minusDays(1).format(formatter), 540000));
                    series.getData().add(new XYChart.Data<>(today.format(formatter), 400000));
                }

                // 4. UI Thread ပေါ်တွင် Chart သို့ Data သေချာပေါက် ဝင်စေရန် Platform.runLater ဖြင့် ဆွဲတင်ခြင်း
                Platform.runLater(() -> {
                    myWeeklySalesChart.getData().add(series);
                });
            }
        } catch (Exception e) {
            // Error တက်ပါက သိရှိနိုင်ရန် Console တွင် ပြပေးမည်
            System.err.println("❌ Error loading Weekly Sales Chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFlightStatusChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        String query = "SELECT status, COUNT(flight_id) as total FROM flights WHERE DATE(departure_time) = CURRENT_DATE GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                pieChartData.add(new PieChart.Data(rs.getString("status"), rs.getInt("total")));
            }
            if (!hasData) {
                pieChartData.add(new PieChart.Data("Scheduled", 4));
                pieChartData.add(new PieChart.Data("Departed", 2));
                pieChartData.add(new PieChart.Data("Delayed", 1));
            }
            flightStatusChart.setData(pieChartData);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class FlightData {
        private String flightNo, route, departureTime, status;
        public FlightData(String flightNo, String route, String departureTime, String status) { this.flightNo = flightNo; this.route = route; this.departureTime = departureTime; this.status = status; }
        public String getFlightNo() { return flightNo; } public String getRoute() { return route; } public String getDepartureTime() { return departureTime; } public String getStatus() { return status; }
    }

    public static class BookingData {
        private String pnr, price, bookingDate, status;
        public BookingData(String pnr, String price, String bookingDate, String status) { this.pnr = pnr; this.price = price; this.bookingDate = bookingDate; this.status = status; }
        public String getPnr() { return pnr; } public String getPrice() { return price; } public String getBookingDate() { return bookingDate; } public String getStatus() { return status; }
    }
}