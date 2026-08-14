package com.pos.flightmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // Metric Labels
    @FXML private Label lblPendingRequests;
    @FXML private Label lblTodayFlights;
    @FXML private Label lblTotalBookings;
    @FXML private Label lblTotalRevenue;

    // Flight Status Labels
    @FXML private Label lblScheduled;
    @FXML private Label lblDeparted;
    @FXML private Label lblDelayed;

    // Table elements
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
        loadRecentRequests();
    }

    private void setupTableColumns() {
        colReqId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        colPnr.setCellValueFactory(new PropertyValueFactory<>("pnrCode"));
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 🌟 Status များကို အရောင်လေးများနှင့် Pill ပုံစံပြရန် Custom Cell Factory သုံးခြင်း 🌟
        colStatus.setCellFactory(column -> new TableCell<RequestData, String>() {
            private final Label statusLabel = new Label();
            {
                statusLabel.getStyleClass().add("table-status-pill");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    statusLabel.setText(item);
                    // ရှိပြီးသား အရောင် Class များကို အရင်ဖျက်ပါမည်
                    statusLabel.getStyleClass().removeAll("pill-pending", "pill-approved", "pill-cancelled");

                    // စာသားပေါ်မူတည်၍ သက်ဆိုင်ရာ အရောင် Class ကို ထည့်ပါမည်
                    if (item.toLowerCase().contains("pending")) {
                        statusLabel.getStyleClass().add("pill-pending");
                    } else if (item.toLowerCase().contains("approve")) {
                        statusLabel.getStyleClass().add("pill-approved");
                    } else { // Cancelled
                        statusLabel.getStyleClass().add("pill-cancelled");
                    }

                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
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

            // 2. Today's Flights (Using DATE function)
            String flightsSql = "SELECT COUNT(*) FROM flights WHERE DATE(departure_time) = CURDATE()";
            try (PreparedStatement ps = conn.prepareStatement(flightsSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblTodayFlights.setText(String.valueOf(rs.getInt(1)));
            }

            // 3. Monthly Bookings (Current Month & Year)
            String bookingsSql = "SELECT COUNT(*) FROM bookings WHERE MONTH(booking_date) = MONTH(CURDATE()) AND YEAR(booking_date) = YEAR(CURDATE())";
            try (PreparedStatement ps = conn.prepareStatement(bookingsSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblTotalBookings.setText(String.valueOf(rs.getInt(1)));
            }

            // 4. Monthly Revenue
            String revenueSql = "SELECT SUM(total_price) FROM bookings WHERE status = 'Confirmed' AND MONTH(booking_date) = MONTH(CURDATE()) AND YEAR(booking_date) = YEAR(CURDATE())";
            try (PreparedStatement ps = conn.prepareStatement(revenueSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double rev = rs.getDouble(1);
                    lblTotalRevenue.setText(String.format("%,.0f MMK", rev));
                }
            }

            // 5. Today's Flight Status Breakdowns
            String statusSql = "SELECT status, COUNT(*) FROM flights WHERE DATE(departure_time) = CURDATE() GROUP BY status";
            try (PreparedStatement ps = conn.prepareStatement(statusSql); ResultSet rs = ps.executeQuery()) {
                int sch = 0, dep = 0, del = 0;
                while (rs.next()) {
                    String status = rs.getString(1);
                    int count = rs.getInt(2);
                    if ("Scheduled".equalsIgnoreCase(status)) sch = count;
                    else if ("Departed".equalsIgnoreCase(status)) dep = count;
                    else if ("Delayed".equalsIgnoreCase(status)) del = count;
                }
                lblScheduled.setText("Scheduled Flights: " + sch);
                lblDeparted.setText("Departed Flights: " + dep);
                lblDelayed.setText("Delayed Flights: " + del);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRecentRequests() {
        requestList.clear();
        // ER Diagram အရ Bookings နှင့် Passengers Table ကို JOIN လုပ်၍ အချက်အလက်ဆွဲထုတ်ခြင်း
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🌟 TableView အတွက် Inner Data Model Class 🌟
    public static class RequestData {
        private int requestId;
        private String pnrCode;
        private String passengerName;
        private String reason;
        private String status;

        public RequestData(int requestId, String pnrCode, String passengerName, String reason, String status) {
            this.requestId = requestId;
            this.pnrCode = pnrCode;
            this.passengerName = passengerName;
            this.reason = reason;
            this.status = status;
        }

        public int getRequestId() { return requestId; }
        public String getPnrCode() { return pnrCode; }
        public String getPassengerName() { return passengerName; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
    }
}