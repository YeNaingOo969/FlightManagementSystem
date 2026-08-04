package com.pos.flightmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class AdminViewSalesDetailController implements Initializable {

    // Date Pickers
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;

    // KPI Labels
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblRevenuePercent;
    @FXML private Label lblTotalTickets;
    @FXML private Label lblTicketsPercent;
    @FXML private Label lblTopFlight;
    @FXML private Label lblTopStaff;
    @FXML private Label lblTopStaffRevenue;

    // Flight Sales Table
    @FXML private TableView<FlightSalesData> flightSalesTable;
    @FXML private TableColumn<FlightSalesData, String> colFlightNo;
    @FXML private TableColumn<FlightSalesData, String> colRoute;
    @FXML private TableColumn<FlightSalesData, Integer> colFlightTickets;
    @FXML private TableColumn<FlightSalesData, Double> colFlightRevenue;

    // Staff Sales Table
    @FXML private TableView<StaffSalesData> staffSalesTable;
    @FXML private TableColumn<StaffSalesData, String> colStaffName;
    @FXML private TableColumn<StaffSalesData, String> colStaffRole;
    @FXML private TableColumn<StaffSalesData, Integer> colStaffTickets;
    @FXML private TableColumn<StaffSalesData, Double> colStaffRevenue;

    // Database Connection URL (သင့်ရဲ့ DB အချက်အလက်နဲ့ ပြောင်းထည့်ပါ)
    private final String DB_URL = "jdbc:mysql://localhost:3306/flightmanagementsystem";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "password";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Table Columns များကို Model class များနှင့် ချိတ်ဆက်ခြင်း
        colFlightNo.setCellValueFactory(new PropertyValueFactory<>("flightNo"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colFlightTickets.setCellValueFactory(new PropertyValueFactory<>("ticketsSold"));
        colFlightRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));

        colStaffName.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        colStaffRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStaffTickets.setCellValueFactory(new PropertyValueFactory<>("ticketsSold"));
        colStaffRevenue.setCellValueFactory(new PropertyValueFactory<>("revenueGen"));

        // Default အနေဖြင့် Data မပြဘဲ Date ရွေးမှသာ ပြရန်အတွက် KPI များကို အလွတ်ထားပါမည်
        clearDashboard();

        // 2. Date Picker များတွင် Date ရွေးချယ်မှု ပြောင်းလဲတိုင်း Data ပြန်ဆွဲရန် Listener ထည့်ခြင်း
        dpFromDate.valueProperty().addListener((obs, oldVal, newVal) -> loadDashboardData());
        dpToDate.valueProperty().addListener((obs, oldVal, newVal) -> loadDashboardData());
    }

    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void clearDashboard() {
        lblTotalRevenue.setText("$0.00");
        lblTotalTickets.setText("0");
        lblTopFlight.setText("N/A");
        lblTopStaff.setText("N/A");
        lblTopStaffRevenue.setText("$0.00");
        lblRevenuePercent.setText("");
        lblTicketsPercent.setText("");
        flightSalesTable.setItems(FXCollections.observableArrayList());
        staffSalesTable.setItems(FXCollections.observableArrayList());
    }

    private void loadDashboardData() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();

        // Date နှစ်ခုလုံး ရွေးချယ်ပြီးမှသာ Database ကို Query လုပ်ပါမည်
        if (fromDate == null || toDate == null) {
            return;
        }

        try (Connection conn = getConnection()) {
            loadKPIs(conn, fromDate, toDate);
            loadFlightSales(conn, fromDate, toDate);
            loadStaffSales(conn, fromDate, toDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadKPIs(Connection conn, LocalDate fromDate, LocalDate toDate) throws Exception {
        // Current Period Queries
        String currentPeriodQuery = "SELECT SUM(total_price) AS rev, COUNT(booking_id) AS tix FROM bookings " +
                "WHERE status = 'Confirmed' AND DATE(booking_date) BETWEEN ? AND ?";

        double currentRev = 0;
        int currentTix = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(currentPeriodQuery)) {
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentRev = rs.getDouble("rev");
                currentTix = rs.getInt("tix");
            }
        }

        lblTotalRevenue.setText(String.format("$%,.2f", currentRev));
        lblTotalTickets.setText(String.format("%,d", currentTix));

        // Previous Period (For Percentage Calculation)
        long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        LocalDate prevFromDate = fromDate.minusDays(daysBetween);
        LocalDate prevToDate = toDate.minusDays(daysBetween);

        double prevRev = 0;
        int prevTix = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(currentPeriodQuery)) {
            pstmt.setString(1, prevFromDate.toString());
            pstmt.setString(2, prevToDate.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                prevRev = rs.getDouble("rev");
                prevTix = rs.getInt("tix");
            }
        }

        // Calculate and Set Percentages
        setPercentageLabel(lblRevenuePercent, currentRev, prevRev);
        setPercentageLabel(lblTicketsPercent, currentTix, prevTix);
    }

    private void setPercentageLabel(Label label, double current, double previous) {
        if (previous == 0) {
            label.setText(current > 0 ? "+100% (No previous data)" : "0%");
            label.setStyle("-fx-text-fill: #2ecc71;"); // Green
            return;
        }
        double percent = ((current - previous) / previous) * 100;
        if (percent >= 0) {
            label.setText(String.format("+%.1f%% from last period", percent));
            label.setStyle("-fx-text-fill: #2ecc71;"); // Green
        } else {
            label.setText(String.format("%.1f%% from last period", percent));
            label.setStyle("-fx-text-fill: #e74c3c;"); // Red
        }
    }

    private void loadFlightSales(Connection conn, LocalDate fromDate, LocalDate toDate) throws Exception {
        ObservableList<FlightSalesData> flightData = FXCollections.observableArrayList();
        String query = "SELECT f.flight_number, CONCAT(dc.city_code, ' - ', ac.city_code) AS route, " +
                "COUNT(b.booking_id) AS tickets, SUM(b.total_price) AS revenue " +
                "FROM flights f " +
                "JOIN bookings b ON f.flight_id = b.flight_id " +
                "JOIN cities dc ON f.departure_city_id = dc.city_id " +
                "JOIN cities ac ON f.arrival_city_id = ac.city_id " +
                "WHERE b.status = 'Confirmed' AND DATE(b.booking_date) BETWEEN ? AND ? " +
                "GROUP BY f.flight_id ORDER BY revenue DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            ResultSet rs = pstmt.executeQuery();

            boolean isFirst = true;
            while (rs.next()) {
                String flightNo = rs.getString("flight_number");
                String route = rs.getString("route");
                int tickets = rs.getInt("tickets");
                double revenue = rs.getDouble("revenue");

                flightData.add(new FlightSalesData(flightNo, route, tickets, revenue));

                // ပထမဆုံး row သည် Top Selling Flight ဖြစ်သည်
                if (isFirst) {
                    lblTopFlight.setText(route + " (" + flightNo + ")");
                    isFirst = false;
                }
            }
        }
        flightSalesTable.setItems(flightData);
    }

    private void loadStaffSales(Connection conn, LocalDate fromDate, LocalDate toDate) throws Exception {
        ObservableList<StaffSalesData> staffData = FXCollections.observableArrayList();
        String query = "SELECT u.username, u.role, COUNT(b.booking_id) AS tickets, SUM(b.total_price) AS revenue " +
                "FROM users u " +
                "JOIN bookings b ON u.user_id = b.staff_id " +
                "WHERE b.status = 'Confirmed' AND DATE(b.booking_date) BETWEEN ? AND ? " +
                "GROUP BY u.user_id ORDER BY revenue DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            ResultSet rs = pstmt.executeQuery();

            boolean isFirst = true;
            while (rs.next()) {
                String name = rs.getString("username");
                String role = rs.getString("role");
                int tickets = rs.getInt("tickets");
                double revenue = rs.getDouble("revenue");

                staffData.add(new StaffSalesData(name, role, tickets, revenue));

                // ပထမဆုံး row သည် Top Performing Staff ဖြစ်သည်
                if (isFirst) {
                    lblTopStaff.setText(name);
                    lblTopStaffRevenue.setText(String.format("$%,.2f generated", revenue));
                    isFirst = false;
                }
            }
        }
        staffSalesTable.setItems(staffData);
    }

    // ==========================================
    // Data Models for TableView
    // ==========================================
    public static class FlightSalesData {
        private String flightNo;
        private String route;
        private int ticketsSold;
        private double totalRevenue;

        public FlightSalesData(String flightNo, String route, int ticketsSold, double totalRevenue) {
            this.flightNo = flightNo;
            this.route = route;
            this.ticketsSold = ticketsSold;
            this.totalRevenue = totalRevenue;
        }

        public String getFlightNo() { return flightNo; }
        public String getRoute() { return route; }
        public int getTicketsSold() { return ticketsSold; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public static class StaffSalesData {
        private String staffName;
        private String role;
        private int ticketsSold;
        private double revenueGen;

        public StaffSalesData(String staffName, String role, int ticketsSold, double revenueGen) {
            this.staffName = staffName;
            this.role = role;
            this.ticketsSold = ticketsSold;
            this.revenueGen = revenueGen;
        }

        public String getStaffName() { return staffName; }
        public String getRole() { return role; }
        public int getTicketsSold() { return ticketsSold; }
        public double getRevenueGen() { return revenueGen; }
    }
}