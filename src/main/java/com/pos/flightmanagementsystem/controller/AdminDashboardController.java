package com.pos.flightmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

// 🔴 သင့် Database Connection Class အမည်ကို အောက်တွင် ပြင်သုံးပါ။ (ဥပမာ DBConnection)
// import com.pos.flightmanagementsystem.utils.DBConnection;

public class AdminDashboardController implements Initializable {

    // 🌟 Top Metric Cards Labels
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalStaff;
    @FXML private Label lblActiveFlights;
    @FXML private Label lblTotalBookings;

    // 🌟 Active Flights Table & Columns
    @FXML private TableView<ActiveFlight> tblActiveFlights;
    @FXML private TableColumn<ActiveFlight, String> colFlightNo;
    @FXML private TableColumn<ActiveFlight, String> colAirline;
    @FXML private TableColumn<ActiveFlight, String> colRoute;
    @FXML private TableColumn<ActiveFlight, String> colDepTime;
    @FXML private TableColumn<ActiveFlight, String> colArrTime;
    @FXML private TableColumn<ActiveFlight, String> colStatus;

    // Sidebar Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnFlightSchedule;
    @FXML private Button btnBookingManagement;
    @FXML private Button btnReport;
    @FXML private Button btnLogout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ဇယား (Table) ကော်လံများကို Model နှင့် ချိတ်ဆက်ခြင်း
        colFlightNo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFlightNo()));
        colAirline.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAirline()));
        colRoute.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoute()));
        colDepTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDepTime()));
        colArrTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArrTime()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

        // UI အရင်ပွင့်လာစေရန် Platform.runLater ဖြင့် Database Data ကို နောက်မှဆွဲမည်
        Platform.runLater(() -> loadDashboardData());
    }

    private void loadDashboardData() {
        // 🔴 ဤနေရာရှိ DatabaseConnection ကို မိမိ DB Class အမည်ပြောင်းပါ
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection is null!");
                return;
            }

            loadMetrics(conn);
            loadActiveFlightsTable(conn);

        } catch (Exception e) {
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMetrics(Connection conn) {
        // 1. Total Revenue
        try (PreparedStatement ps = conn.prepareStatement("SELECT SUM(total_price) FROM bookings WHERE status = 'Confirmed'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lblTotalRevenue.setText(String.format("%,.0f MMK", rs.getDouble(1)));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2. Total Staff
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'Staff' OR role = 'staff'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lblTotalStaff.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 3. Active Flights
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM flights WHERE status != 'Cancelled'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lblActiveFlights.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 4. Total Bookings
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM bookings");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lblTotalBookings.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadActiveFlightsTable(Connection conn) {
        ObservableList<ActiveFlight> flightList = FXCollections.observableArrayList();

        // Flights နှင့် Cities များကို Join ၍ ခရီးစဉ် (Route) ကို ဆွဲထုတ်ခြင်း
        String query = "SELECT f.flight_number, f.airline, CONCAT(dc.city_code, ' - ', ac.city_code) AS route, " +
                "f.departure_time, f.arrival_time, f.status " +
                "FROM flights f " +
                "JOIN cities dc ON f.departure_city_id = dc.city_id " +
                "JOIN cities ac ON f.arrival_city_id = ac.city_id " +
                "WHERE f.status != 'Cancelled' ORDER BY f.departure_time ASC LIMIT 30";

        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                flightList.add(new ActiveFlight(
                        rs.getString("flight_number"),
                        rs.getString("airline"),
                        rs.getString("route"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getString("status")
                ));
            }
            tblActiveFlights.setItems(flightList);

        } catch (Exception e) {
            System.err.println("Table Data Load Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // TableView တွင် ပြသရန်အတွက် Model Class
    // ==========================================
    public static class ActiveFlight {
        private final String flightNo;
        private final String airline;
        private final String route;
        private final String depTime;
        private final String arrTime;
        private final String status;

        public ActiveFlight(String flightNo, String airline, String route, String depTime, String arrTime, String status) {
            this.flightNo = flightNo;
            this.airline = airline;
            this.route = route;
            this.depTime = depTime;
            this.arrTime = arrTime;
            this.status = status;
        }

        public String getFlightNo() { return flightNo; }
        public String getAirline() { return airline; }
        public String getRoute() { return route; }
        public String getDepTime() { return depTime; }
        public String getArrTime() { return arrTime; }
        public String getStatus() { return status; }
    }
}