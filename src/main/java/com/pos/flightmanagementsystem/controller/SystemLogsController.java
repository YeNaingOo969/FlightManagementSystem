package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Log;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SystemLogsController implements Initializable {

    // --- UI Fields ---
    @FXML private ComboBox<String> cbRoleFilter;
    @FXML private DatePicker dpDateFilter; // 🌟 DatePicker ကို ချိတ်ဆက်ထားပါသည်

    // --- Table View Fields ---
    @FXML private TableView<Log> logsTable;
    @FXML private TableColumn<Log, Integer> colLogId;
    @FXML private TableColumn<Log, LocalDateTime> colTimestamp;
    @FXML private TableColumn<Log, String> colUser;
    @FXML private TableColumn<Log, String> colRole;
    @FXML private TableColumn<Log, String> colAction;

    // --- Data Caching (Model မပြင်ဘဲ သုံးရန်) ---
    private Map<Integer, String> logUsernameMap = new HashMap<>();
    private Map<Integer, String> logRoleMap = new HashMap<>();

    // Master Data သိမ်းထားရန်
    private ObservableList<Log> masterLogList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadLogsData();
        setupFilters(); // 🌟 Filters များကို စတင်ရန်
    }

    // ==========================================
    // 🌟 SETUP TABLE COLUMNS
    // ==========================================
    private void setupTable() {
        colLogId.setCellValueFactory(new PropertyValueFactory<>("logId"));

        colUser.setCellValueFactory(cellData -> {
            int logId = cellData.getValue().getLogId();
            String username = logUsernameMap.getOrDefault(logId, "System / Unknown");
            return new SimpleStringProperty(username);
        });

        colRole.setCellValueFactory(cellData -> {
            int logId = cellData.getValue().getLogId();
            String role = logRoleMap.getOrDefault(logId, "-");
            return new SimpleStringProperty(role);
        });

        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
    }

    // ==========================================
    // 🌟 LOAD DATA FROM DATABASE
    // ==========================================
    private void loadLogsData() {
        masterLogList.clear();
        logUsernameMap.clear();
        logRoleMap.clear();

        String query = "SELECT l.log_id, l.user_id, l.action, l.timestamp, u.username, u.role " +
                "FROM logs l " +
                "LEFT JOIN users u ON l.user_id = u.user_id " +
                "ORDER BY l.log_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Log log = new Log();
                int logId = rs.getInt("log_id");

                log.setLogId(logId);
                log.setUserId(rs.getInt("user_id"));
                log.setAction(rs.getString("action"));

                java.sql.Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) {
                    log.setTimestamp(ts.toLocalDateTime());
                }

                String username = rs.getString("username");
                if (username == null || username.trim().isEmpty()) {
                    username = "System / Unknown";
                }
                logUsernameMap.put(logId, username);

                String role = rs.getString("role");
                if (role == null || role.trim().isEmpty()) {
                    role = "-";
                }
                logRoleMap.put(logId, role);

                masterLogList.add(log);
            }

            applyDataToTable();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load system logs from database.");
        }
    }

    // ==========================================
    // 🌟 FILTERING LOGIC (Role & Date)
    // ==========================================
    // ==========================================
    // 🌟 FILTERING LOGIC (Role & Date)
    // ==========================================
    private void setupFilters() {
        // ComboBox ပြောင်းလဲမှု နားထောင်ရန်
        if (cbRoleFilter != null) {
            cbRoleFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyDataToTable());
            cbRoleFilter.getSelectionModel().select("All");
        }

        // DatePicker ပြောင်းလဲမှု နားထောင်ရန်
        if (dpDateFilter != null) {
            dpDateFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyDataToTable());
            dpDateFilter.setValue(java.time.LocalDate.now());
        }
    }

    private void applyDataToTable() {
        FilteredList<Log> filteredData = new FilteredList<>(masterLogList, log -> {

            // 1. Role ကို စစ်ဆေးခြင်း
            boolean matchesRole = true;
            if (cbRoleFilter != null) {
                String filterRole = cbRoleFilter.getValue();
                if (filterRole != null && !filterRole.isEmpty() && !filterRole.equalsIgnoreCase("All")) {
                    String logRole = logRoleMap.getOrDefault(log.getLogId(), "");
                    matchesRole = logRole.equalsIgnoreCase(filterRole);
                }
            }

            // 2. Date ကို စစ်ဆေးခြင်း
            boolean matchesDate = true;
            if (dpDateFilter != null) {
                LocalDate filterDate = dpDateFilter.getValue(); // ရွေးချယ်ထားသော Date ကို ယူမည်
                if (filterDate != null) {
                    LocalDateTime logTimestamp = log.getTimestamp();
                    if (logTimestamp != null) {
                        LocalDate logDate = logTimestamp.toLocalDate(); // Timestamp ထဲမှ Date အပိုင်းကိုသာ ဆွဲထုတ်မည်
                        matchesDate = logDate.equals(filterDate); // နေ့စွဲချင်း တူ/မတူ စစ်ဆေးမည်
                    } else {
                        matchesDate = false; // Timestamp မရှိပါက ဤ Date Filter ဖြင့် မပေါ်စေရန်
                    }
                }
            }

            // Role ရော Date ရော နှစ်ခုလုံး သတ်မှတ်ချက်ကိုက်ညီမှသာ (true) Table တွင် ပြမည်
            return matchesRole && matchesDate;
        });

        SortedList<Log> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(logsTable.comparatorProperty());

        logsTable.setItems(sortedData);
    }

    // ==========================================
    // 🌟 ERROR ALERT DIALOG
    // ==========================================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}