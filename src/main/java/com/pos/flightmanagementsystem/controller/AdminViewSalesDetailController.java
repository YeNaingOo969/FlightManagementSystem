package com.pos.flightmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

// 🔴 သင့်ရဲ့ Database Connection Class ကို Import လုပ်ပါ
// import com.pos.flightmanagementsystem.utils.DBConnection;

public class AdminViewSalesDetailController implements Initializable {

    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblRevenuePercent;
    @FXML private Label lblTotalTickets;
    @FXML private Label lblTicketsPercent;
    @FXML private Label lblTopFlight;
    @FXML private Label lblTopStaff;
    @FXML private Label lblTopStaffRevenue;

    @FXML private TableView<FlightSalesData> flightSalesTable;
    @FXML private TableColumn<FlightSalesData, String> colFlightNo;
    @FXML private TableColumn<FlightSalesData, String> colRoute;
    @FXML private TableColumn<FlightSalesData, Integer> colFlightTickets;
    @FXML private TableColumn<FlightSalesData, Double> colFlightRevenue;

    @FXML private TableView<StaffSalesData> staffSalesTable;
    @FXML private TableColumn<StaffSalesData, String> colStaffName;
    @FXML private TableColumn<StaffSalesData, String> colStaffRole;
    @FXML private TableColumn<StaffSalesData, Integer> colStaffTickets;
    @FXML private TableColumn<StaffSalesData, Double> colStaffRevenue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Table Column များကို Model လေးများနှင့် ချိတ်ဆက်ခြင်း
        colFlightNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFlightNo()));
        colRoute.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoute()));
        colFlightTickets.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTicketsSold()).asObject());
        colFlightRevenue.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalRevenue()).asObject());

        colStaffName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStaffName()));
        colStaffRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        colStaffTickets.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTicketsSold()).asObject());
        colStaffRevenue.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRevenueGen()).asObject());

        // Date ရွေးလိုက်တာနဲ့ Data ဆွဲထုတ်မည့် Listeners များ
        dpFromDate.valueProperty().addListener((obs, oldVal, newVal) -> loadDashboardData());
        dpToDate.valueProperty().addListener((obs, oldVal, newVal) -> loadDashboardData());

        dpFromDate.setOnAction(e -> loadDashboardData());
        dpToDate.setOnAction(e -> loadDashboardData());

        // 🌟 လက်ရှိရောက်နေသော လ၏ ၁ ရက်နေ့ မှ လကုန်ရက် အထိ Default ပြပေးရန် သတ်မှတ်ခြင်း 🌟
        LocalDate today = LocalDate.now();
        dpFromDate.setValue(today.withDayOfMonth(1)); // ဒီလ၏ ၁ ရက်နေ့
        dpToDate.setValue(today.withDayOfMonth(today.lengthOfMonth())); // ဒီလ၏ နောက်ဆုံးရက် (လကုန်ရက်)
    }

    private void clearDashboard() {
        lblTotalRevenue.setText("0.00 MMK");
        lblTotalTickets.setText("0");
        lblTopFlight.setText("N/A");
        lblTopStaff.setText("N/A");
        lblTopStaffRevenue.setText("0.00 MMK generated");
        lblRevenuePercent.setText("");
        lblTicketsPercent.setText("");
        flightSalesTable.setItems(FXCollections.observableArrayList());
        staffSalesTable.setItems(FXCollections.observableArrayList());
    }

    private void loadDashboardData() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();

        // Date ၂ ခုလုံး မရွေးရသေးရင် အလုပ်မလုပ်ဘဲ ရပ်နေပါမည် (သို့မဟုတ်) ရှင်းထုတ်ပစ်ပါမည်
        if (fromDate == null || toDate == null) {
            clearDashboard();
            return;
        }

        // 🔴 အောက်ပါ DBConnection နေရာတွင် သင့် Connection Method ကို ပြောင်းသုံးပါ
        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn == null) {
                System.out.println("❌ Database ချိတ်ဆက်မှု မအောင်မြင်ပါ။ (Connection is null)");
                return;
            }

            loadKPIs(conn, fromDate, toDate);
            loadFlightSales(conn, fromDate, toDate);
            loadStaffSales(conn, fromDate, toDate);

        } catch (Exception e) {
            System.out.println("❌ Database Error ဖြစ်နေပါသည်: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Database Error", "Error: " + e.getMessage());
        }
    }

    private void loadKPIs(Connection conn, LocalDate fromDate, LocalDate toDate) throws Exception {
        String currentQuery = "SELECT SUM(total_price) AS rev, COUNT(booking_id) AS tix FROM bookings " +
                "WHERE status = 'Confirmed' AND DATE(booking_date) >= ? AND DATE(booking_date) <= ?";

        double currentRev = 0;
        int currentTix = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(currentQuery)) {
            pstmt.setDate(1, Date.valueOf(fromDate));
            pstmt.setDate(2, Date.valueOf(toDate));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentRev = rs.getDouble("rev");
                currentTix = rs.getInt("tix");
            }
        }
        lblTotalRevenue.setText(String.format("%,.2f MMK", currentRev));
        lblTotalTickets.setText(String.format("%,d", currentTix));

        // အရင်လကထက် Percent % ဘယ်လောက်တိုးလာလဲ တွက်ရန်
        LocalDate prevFromDate = fromDate.minusMonths(1);
        LocalDate prevToDate = toDate.minusMonths(1);

        double prevRev = 0;
        int prevTix = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(currentQuery)) {
            pstmt.setDate(1, Date.valueOf(prevFromDate));
            pstmt.setDate(2, Date.valueOf(prevToDate));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                prevRev = rs.getDouble("rev");
                prevTix = rs.getInt("tix");
            }
        }

        setPercentageLabel(lblRevenuePercent, currentRev, prevRev);
        setPercentageLabel(lblTicketsPercent, currentTix, prevTix);
    }

    private void setPercentageLabel(Label label, double current, double previous) {
        if (previous == 0) {
            label.setText(current > 0 ? "+100% from last month" : "0%");
            label.setStyle("-fx-text-fill: #2ecc71;");
            return;
        }
        double percent = ((current - previous) / previous) * 100;
        if (percent >= 0) {
            label.setText(String.format("+%.1f%% from last month", percent));
            label.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            label.setText(String.format("%.1f%% from last month", percent));
            label.setStyle("-fx-text-fill: #e74c3c;");
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
                "WHERE b.status = 'Confirmed' AND DATE(b.booking_date) >= ? AND DATE(b.booking_date) <= ? " +
                "GROUP BY f.flight_id ORDER BY revenue DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(fromDate));
            pstmt.setDate(2, Date.valueOf(toDate));
            ResultSet rs = pstmt.executeQuery();

            boolean isFirst = true;
            lblTopFlight.setText("N/A");

            while (rs.next()) {
                String flightNo = rs.getString("flight_number");
                String route = rs.getString("route");
                int tickets = rs.getInt("tickets");
                double revenue = rs.getDouble("revenue");

                flightData.add(new FlightSalesData(flightNo, route, tickets, revenue));

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
                "WHERE b.status = 'Confirmed' AND DATE(b.booking_date) >= ? AND DATE(b.booking_date) <= ? " +
                "GROUP BY u.user_id ORDER BY revenue DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(fromDate));
            pstmt.setDate(2, Date.valueOf(toDate));
            ResultSet rs = pstmt.executeQuery();

            boolean isFirst = true;
            lblTopStaff.setText("N/A");
            lblTopStaffRevenue.setText("0.00 MMK generated");

            while (rs.next()) {
                String name = rs.getString("username");
                String role = rs.getString("role");
                int tickets = rs.getInt("tickets");
                double revenue = rs.getDouble("revenue");

                staffData.add(new StaffSalesData(name, role, tickets, revenue));

                if (isFirst) {
                    lblTopStaff.setText(name);
                    lblTopStaffRevenue.setText(String.format("%,.2f MMK generated", revenue));
                    isFirst = false;
                }
            }
        }
        staffSalesTable.setItems(staffData);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class FlightSalesData {
        private final String flightNo;
        private final String route;
        private final int ticketsSold;
        private final double totalRevenue;

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
        private final String staffName;
        private final String role;
        private final int ticketsSold;
        private final double revenueGen;

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