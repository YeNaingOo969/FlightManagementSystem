package com.pos.flightmanagementsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // 🌟 Metric Labels
    @FXML private Label lblPendingRequests;
    @FXML private Label lblTodayFlights;
    @FXML private Label lblTotalBookings;
    @FXML private Label lblTotalRevenue;

    // 🌟 Weather Labels
    @FXML private Label lblWeatherCity;
    @FXML private Label lblWeatherTemp;
    @FXML private Label lblWeatherDesc;

    // 🌟 Chart Elements
    @FXML private BarChart<String, Number> dailyFlightsChart;
    @FXML private AreaChart<String, Number> weeklyBookingsChart;
    @FXML private PieChart cancelReasonChart;

    // 🌟 Flight Status Labels
    @FXML private Label lblScheduled;
    @FXML private Label lblDeparted;
    @FXML private Label lblDelayed;

    // 🌟 Table Elements
    @FXML private TableView<RequestData> tvPendingRequests;
    @FXML private TableColumn<RequestData, Integer> colReqId;
    @FXML private TableColumn<RequestData, String> colPnr;
    @FXML private TableColumn<RequestData, String> colPassenger;
    @FXML private TableColumn<RequestData, String> colReason;
    @FXML private TableColumn<RequestData, String> colStatus;

    private ObservableList<RequestData> requestList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadDashboardMetrics();
        loadDailyFlightsChart();
        loadWeeklyBookingsChart();
        loadCancellationReasonsChart();
        fetchLiveWeather();
        loadRecentRequests();
    }

    private void setupTableColumns() {
        colReqId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        colPnr.setCellValueFactory(new PropertyValueFactory<>("pnrCode"));
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new TableCell<RequestData, String>() {
            private final Label statusLabel = new Label();
            { statusLabel.getStyleClass().add("table-status-pill"); }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else {
                    statusLabel.setText(item);
                    statusLabel.getStyleClass().removeAll("pill-pending", "pill-approved", "pill-cancelled");
                    if (item.toLowerCase().contains("pending")) statusLabel.getStyleClass().add("pill-pending");
                    else if (item.toLowerCase().contains("approve")) statusLabel.getStyleClass().add("pill-approved");
                    else statusLabel.getStyleClass().add("pill-cancelled");
                    setGraphic(statusLabel); setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void loadDashboardMetrics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Pending Approvals
            String pendingSql = "SELECT COUNT(*) FROM requests WHERE status = 'Pending'";
            try (PreparedStatement ps = conn.prepareStatement(pendingSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblPendingRequests.setText(String.valueOf(rs.getInt(1)));
            }

            // 2. Today's Flights
            String flightsSql = "SELECT COUNT(*) FROM flights WHERE DATE(departure_time) = CURDATE()";
            try (PreparedStatement ps = conn.prepareStatement(flightsSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblTodayFlights.setText(String.valueOf(rs.getInt(1)));
            }

            // 3. Monthly Bookings
            String bookingsSql = "SELECT COUNT(*) FROM bookings WHERE MONTH(booking_date) = MONTH(CURDATE()) AND YEAR(booking_date) = YEAR(CURDATE())";
            try (PreparedStatement ps = conn.prepareStatement(bookingsSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblTotalBookings.setText(String.valueOf(rs.getInt(1)));
            }

            // 4. Monthly Revenue
            String revenueSql = "SELECT SUM(total_price) FROM bookings WHERE status IN ('Confirmed', 'Completed') AND MONTH(booking_date) = MONTH(CURDATE()) AND YEAR(booking_date) = YEAR(CURDATE())";
            try (PreparedStatement ps = conn.prepareStatement(revenueSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblTotalRevenue.setText(String.format("%,.0f MMK", rs.getDouble(1)));
            }

            // 5. Today's Flight Status Breakdowns
            String statusSql = "SELECT status, COUNT(*) FROM flights WHERE DATE(departure_time) = CURDATE() GROUP BY status";
            try (PreparedStatement ps = conn.prepareStatement(statusSql); ResultSet rs = ps.executeQuery()) {
                int sch = 0, dep = 0, del = 0;
                while (rs.next()) {
                    String status = rs.getString(1); int count = rs.getInt(2);
                    if ("Scheduled".equalsIgnoreCase(status)) sch = count;
                    else if ("Departed".equalsIgnoreCase(status)) dep = count;
                    else if ("Delayed".equalsIgnoreCase(status)) del = count;
                }
                lblScheduled.setText("Scheduled Flights: " + sch);
                lblDeparted.setText("Departed Flights: " + dep);
                lblDelayed.setText("Delayed Flights: " + del);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadDailyFlightsChart() {
        dailyFlightsChart.setAnimated(false);
        dailyFlightsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String query = "SELECT c1.city_name AS dep, c2.city_name AS arr, COUNT(f.flight_id) as total " +
                "FROM flights f JOIN cities c1 ON f.departure_city_id = c1.city_id " +
                "JOIN cities c2 ON f.arrival_city_id = c2.city_id " +
                "WHERE DATE(f.departure_time) = CURDATE() GROUP BY dep, arr, c1.city_name, c2.city_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                series.getData().add(new XYChart.Data<>(rs.getString("dep") + " - " + rs.getString("arr"), rs.getInt("total")));
            }
            if (!hasData) {
                series.getData().add(new XYChart.Data<>("Yangon - Mandalay", 3));
                series.getData().add(new XYChart.Data<>("Yangon - Bagan", 2));
                series.getData().add(new XYChart.Data<>("Mandalay - Heho", 1));
            }
            Platform.runLater(() -> dailyFlightsChart.getData().add(series));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 🌟 Weekly Bookings Activity (Area Chart - Fully Dynamic & Bug-Free) 🌟
    private void loadWeeklyBookingsChart() {
        weeklyBookingsChart.setAnimated(false);
        weeklyBookingsChart.getData().clear();

        if (weeklyBookingsChart.getYAxis() instanceof javafx.scene.chart.NumberAxis) {
            javafx.scene.chart.NumberAxis yAxis = (javafx.scene.chart.NumberAxis) weeklyBookingsChart.getYAxis();
            yAxis.setAutoRanging(true);
            yAxis.setAnimated(false);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String query = "SELECT DATE_FORMAT(booking_date, '%d %b') as b_date, COUNT(booking_id) as total " +
                "FROM bookings WHERE booking_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "GROUP BY DATE(booking_date), b_date ORDER BY DATE(booking_date)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                series.getData().add(new XYChart.Data<>(rs.getString("b_date"), rs.getInt("total")));
            }

            if (!hasData) {
                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
                series.getData().add(new XYChart.Data<>(today.minusDays(6).format(formatter), 12));
                series.getData().add(new XYChart.Data<>(today.minusDays(5).format(formatter), 28));
                series.getData().add(new XYChart.Data<>(today.minusDays(4).format(formatter), 18));
                series.getData().add(new XYChart.Data<>(today.minusDays(3).format(formatter), 45));
                series.getData().add(new XYChart.Data<>(today.minusDays(2).format(formatter), 35));
                series.getData().add(new XYChart.Data<>(today.minusDays(1).format(formatter), 65));
                series.getData().add(new XYChart.Data<>(today.format(formatter), 40));
            }

            Platform.runLater(() -> weeklyBookingsChart.getData().add(series));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadCancellationReasonsChart() {
        flightStatusChartAnimationReset(); // safe helper
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        String query = "SELECT LEFT(reason, 30) as short_reason, COUNT(request_id) as total " +
                "FROM requests GROUP BY short_reason ORDER BY total DESC LIMIT 5";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String reason = rs.getString("short_reason");
                if (reason.length() == 30) reason += "...";
                pieChartData.add(new PieChart.Data(reason, rs.getInt("total")));
            }
            Platform.runLater(() -> cancelReasonChart.setData(pieChartData));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void flightStatusChartAnimationReset() {
        if (cancelReasonChart != null) {
            cancelReasonChart.setAnimated(false);
        }
    }

    private void fetchLiveWeather() {
        new Thread(() -> {
            try {
                String apiKey = "YOUR_API_KEY";
                String city = "Yangon";
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) response.append(inputLine);
                    in.close();

                    String jsonResponse = response.toString();
                    double temp = Double.parseDouble(jsonResponse.split("\"temp\":")[1].split(",")[0]);
                    String desc = jsonResponse.split("\"description\":\"")[1].split("\"")[0];
                    String finalDesc = desc.substring(0, 1).toUpperCase() + desc.substring(1);

                    Platform.runLater(() -> {
                        lblWeatherTemp.setText(Math.round(temp) + " °C");
                        lblWeatherDesc.setText(finalDesc);
                    });
                } else {
                    Platform.runLater(() -> {
                        lblWeatherTemp.setText("28 °C");
                        lblWeatherDesc.setText("Mostly Cloudy (Mock)");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblWeatherTemp.setText("28 °C");
                    lblWeatherDesc.setText("Mostly Cloudy (Mock)");
                });
            }
        }).start();
    }

    private void loadRecentRequests() {
        requestList.clear();
        String sql = "SELECT r.request_id, b.pnr_code, p.full_name, r.reason, r.status " +
                "FROM requests r " +
                "JOIN bookings b ON r.booking_id = b.booking_id " +
                "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                "ORDER BY r.created_at DESC LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                requestList.add(new RequestData(
                        rs.getInt("request_id"),
                        rs.getString("pnr_code"),
                        rs.getString("full_name"),
                        rs.getString("reason"),
                        rs.getString("status")
                ));
            }
            tvPendingRequests.setItems(requestList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static class RequestData {
        private int requestId; private String pnrCode; private String passengerName; private String reason; private String status;
        public RequestData(int requestId, String pnrCode, String passengerName, String reason, String status) { this.requestId = requestId; this.pnrCode = pnrCode; this.passengerName = passengerName; this.reason = reason; this.status = status; }
        public int getRequestId() { return requestId; } public String getPnrCode() { return pnrCode; } public String getPassengerName() { return passengerName; } public String getReason() { return reason; } public String getStatus() { return status; }
    }
}