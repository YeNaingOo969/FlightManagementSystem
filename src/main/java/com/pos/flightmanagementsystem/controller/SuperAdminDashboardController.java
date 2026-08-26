package com.pos.flightmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class SuperAdminDashboardController implements Initializable {

    @FXML private Label lblCurrentDate;

    // --- Metric Labels ---
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblRevenueTrend;
    @FXML private SVGPath iconRevenueTrend;

    @FXML private Label lblNetProfit;
    @FXML private Label lblProfitTrend;
    @FXML private SVGPath iconProfitTrend;

    @FXML private Label lblOccupancyRate;
    @FXML private Label lblCancellations;
    @FXML private Label lblRefundAmount;

    // --- Charts ---
    @FXML private AreaChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> topRoutesChart;
    @FXML private PieChart revenueByClassChart;
    @FXML private LineChart<String, Number> bookingVsCancelChart;

    // --- Logs Table ---
    @FXML private TableView<LogData> logsTable;
    @FXML private TableColumn<LogData, String> colLogDate;
    @FXML private TableColumn<LogData, String> colLogUser;
    @FXML private TableColumn<LogData, String> colLogAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblCurrentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        setupTable();
        loadCEOMetricsFromDatabase();

        loadRevenueChart();
        loadTopRoutesChart();
        loadRevenueByClassChart();
        loadBookingVsCancelChart();

        loadRecentLogs();
    }

    private void setupTable() {
        colLogDate.setCellValueFactory(new PropertyValueFactory<>("timestampStr"));
        colLogUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colLogAction.setCellValueFactory(new PropertyValueFactory<>("action"));
    }

    private void loadCEOMetricsFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            LocalDate today = LocalDate.now();
            int currentMonth = today.getMonthValue();
            int currentYear = today.getYear();

            LocalDate lastMonthDate = today.minusMonths(1);
            int lastMonth = lastMonthDate.getMonthValue();
            int lastMonthYear = lastMonthDate.getYear();

            // 1. Gross Revenue
            double currentMonthRev = getMonthlyRevenue(conn, currentMonth, currentYear);
            double lastMonthRev = getMonthlyRevenue(conn, lastMonth, lastMonthYear);
            lblTotalRevenue.setText(String.format("%,.0f MMK", currentMonthRev));
            updateTrendUI(currentMonthRev, lastMonthRev, lblRevenueTrend, iconRevenueTrend);

            // 2. Net Profit (Assumed 35%)
            double currentProfit = currentMonthRev * 0.35;
            double lastProfit = lastMonthRev * 0.35;
            lblNetProfit.setText(String.format("%,.0f MMK", currentProfit));
            updateTrendUI(currentProfit, lastProfit, lblProfitTrend, iconProfitTrend);

            // 3. Occupancy Rate
            int totalCapacity = 0;
            int totalBooked = 0;
            String capQuery = "SELECT SUM(total_seats) FROM flights WHERE status = 'Scheduled'";
            try (PreparedStatement stmt = conn.prepareStatement(capQuery); ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) totalCapacity = rs.getInt(1);
            }
            String bookQuery = "SELECT COUNT(*) FROM bookings b JOIN flights f ON b.flight_id = f.flight_id WHERE f.status = 'Scheduled' AND b.status IN ('Confirmed', 'Completed')";
            try (PreparedStatement stmt = conn.prepareStatement(bookQuery); ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) totalBooked = rs.getInt(1);
            }
            double occupancy = (totalCapacity > 0) ? ((double) totalBooked / totalCapacity) * 100 : 0.0;
            lblOccupancyRate.setText(String.format("%.1f%%", occupancy));

            // 4. Cancellations & Refunds
            int cancelCount = 0;
            double refundAmount = 0.0;
            String cancelCountQuery = "SELECT COUNT(*) FROM bookings WHERE status = 'Cancelled' AND MONTH(booking_date) = ? AND YEAR(booking_date) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(cancelCountQuery)) {
                stmt.setInt(1, currentMonth); stmt.setInt(2, currentYear);
                try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) cancelCount = rs.getInt(1); }
            }
            String refundQuery = "SELECT SUM(b.total_price) FROM bookings b JOIN requests r ON b.booking_id = r.booking_id WHERE b.status = 'Cancelled' AND r.status = 'Cancelled by Admin' AND MONTH(b.booking_date) = ? AND YEAR(b.booking_date) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(refundQuery)) {
                stmt.setInt(1, currentMonth); stmt.setInt(2, currentYear);
                try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) refundAmount = rs.getDouble(1); }
            }
            lblCancellations.setText(cancelCount + " Bookings");
            lblRefundAmount.setText(String.format("Refunds: %,.0f MMK", refundAmount));

        } catch (Exception e) { e.printStackTrace(); }
    }

    private double getMonthlyRevenue(Connection conn, int month, int year) throws Exception {
        String query = "SELECT SUM(total_price) FROM bookings WHERE MONTH(booking_date) = ? AND YEAR(booking_date) = ? AND status IN ('Confirmed', 'Completed')";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, month); stmt.setInt(2, year);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getDouble(1); }
        }
        return 0.0;
    }

    private void updateTrendUI(double current, double previous, Label lblTrend, SVGPath icon) {
        if (previous == 0 && current > 0) {
            lblTrend.setText("+100.0% vs last month"); setTrendPositive(lblTrend, icon);
        } else if (previous == 0 && current == 0) {
            lblTrend.setText("0.0% vs last month"); setTrendNeutral(lblTrend, icon);
        } else {
            double percentChange = ((current - previous) / previous) * 100;
            if (percentChange > 0) { lblTrend.setText(String.format("+%.1f%% vs last month", percentChange)); setTrendPositive(lblTrend, icon); }
            else if (percentChange < 0) { lblTrend.setText(String.format("%.1f%% vs last month", percentChange)); setTrendNegative(lblTrend, icon); }
            else { lblTrend.setText("0.0% vs last month"); setTrendNeutral(lblTrend, icon); }
        }
    }

    private void setTrendPositive(Label lbl, SVGPath icon) {
        lbl.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 11px; -fx-font-weight: bold;");
        icon.setFill(Color.WHITE); icon.setScaleX(0.8); icon.setScaleY(0.8);
        icon.setContent("M16 18l2.29-2.29-4.88-4.88-4 4L2 7.41 3.41 6l6 6 4-4 6.3 6.29L22 12v6z");
    }

    private void setTrendNegative(Label lbl, SVGPath icon) {
        lbl.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.8); -fx-font-size: 11px; -fx-font-weight: bold;");
        icon.setFill(Color.web("rgba(255, 255, 255, 0.8)")); icon.setScaleX(0.8); icon.setScaleY(0.8);
        icon.setContent("M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z");
    }

    private void setTrendNeutral(Label lbl, SVGPath icon) {
        lbl.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-size: 11px;");
        icon.setFill(Color.web("rgba(255, 255, 255, 0.7)")); icon.setScaleX(0.8); icon.setScaleY(0.8);
        icon.setContent("M22 12l-4-4v3H3v2h15v3z");
    }

    private void loadRevenueChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String query = "SELECT YEAR(booking_date) as yr, MONTH(booking_date) as mth, SUM(total_price) as revenue FROM bookings WHERE booking_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 5 MONTH) AND status IN ('Confirmed', 'Completed') GROUP BY YEAR(booking_date), MONTH(booking_date) ORDER BY yr, mth";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String monthName = Month.of(rs.getInt("mth")).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + rs.getInt("yr");
                series.getData().add(new XYChart.Data<>(monthName, rs.getDouble("revenue")));
            }
            revenueChart.getData().add(series);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTopRoutesChart() {
        XYChart.Series<String, Number> routeSeries = new XYChart.Series<>();
        String topRouteQuery = "SELECT c1.city_name AS dep, c2.city_name AS arr, COUNT(b.booking_id) AS total_b FROM bookings b JOIN flights f ON b.flight_id = f.flight_id JOIN cities c1 ON f.departure_city_id = c1.city_id JOIN cities c2 ON f.arrival_city_id = c2.city_id WHERE b.status IN ('Confirmed', 'Completed') GROUP BY dep, arr ORDER BY total_b DESC LIMIT 4";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(topRouteQuery); ResultSet rs = stmt.executeQuery()) {
            while(rs.next()) routeSeries.getData().add(new XYChart.Data<>(rs.getString("dep") + " - " + rs.getString("arr"), rs.getInt("total_b")));
            topRoutesChart.getData().add(routeSeries);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 🌟 NEW: Load Revenue By Class Data (Pie Chart) 🌟
    private void loadRevenueByClassChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        String query = "SELECT s.seat_class, SUM(b.total_price) as revenue FROM bookings b JOIN seats s ON b.seat_id = s.seat_id WHERE b.status IN ('Confirmed', 'Completed') GROUP BY s.seat_class";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String className = rs.getString("seat_class");
                if (className == null) className = "Economy";
                pieChartData.add(new PieChart.Data(className, rs.getDouble("revenue")));
            }
            revenueByClassChart.setData(pieChartData);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 🌟 NEW: Load Bookings vs Cancellations Data (Line Chart) 🌟
    private void loadBookingVsCancelChart() {
        XYChart.Series<String, Number> seriesBooked = new XYChart.Series<>();
        seriesBooked.setName("Confirmed Bookings");

        XYChart.Series<String, Number> seriesCancelled = new XYChart.Series<>();
        seriesCancelled.setName("Cancellations");

        String query = "SELECT YEAR(booking_date) as yr, MONTH(booking_date) as mth, " +
                "SUM(CASE WHEN status IN ('Confirmed', 'Completed') THEN 1 ELSE 0 END) as booked, " +
                "SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END) as cancelled " +
                "FROM bookings WHERE booking_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 5 MONTH) " +
                "GROUP BY YEAR(booking_date), MONTH(booking_date) ORDER BY yr, mth";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String monthName = Month.of(rs.getInt("mth")).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + rs.getInt("yr");
                seriesBooked.getData().add(new XYChart.Data<>(monthName, rs.getInt("booked")));
                seriesCancelled.getData().add(new XYChart.Data<>(monthName, rs.getInt("cancelled")));
            }
            bookingVsCancelChart.getData().addAll(seriesBooked, seriesCancelled);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadRecentLogs() {
        ObservableList<LogData> logsList = FXCollections.observableArrayList();
        String query = "SELECT l.timestamp, u.username, l.action FROM logs l LEFT JOIN users u ON l.user_id = u.user_id ORDER BY l.timestamp DESC LIMIT 15";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("timestamp");
                String timeStr = (ts != null) ? ts.toLocalDateTime().format(formatter) : "Unknown";
                String user = rs.getString("username"); if (user == null) user = "System Process";
                logsList.add(new LogData(timeStr, user, rs.getString("action")));
            }
            logsTable.setItems(logsList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class LogData {
        private String timestampStr; private String username; private String action;
        public LogData(String timestampStr, String username, String action) { this.timestampStr = timestampStr; this.username = username; this.action = action; }
        public String getTimestampStr() { return timestampStr; } public String getUsername() { return username; } public String getAction() { return action; }
    }
}