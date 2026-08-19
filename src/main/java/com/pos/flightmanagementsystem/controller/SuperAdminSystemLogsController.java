package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Log;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SuperAdminSystemLogsController implements Initializable {

    // --- UI Fields ---
    @FXML private ComboBox<String> cbRoleFilter;
    @FXML private DatePicker dpDateFilter;

    // --- Table View Fields ---
    @FXML private TableView<Log> logsTable;

    @FXML private TableColumn<Log, LocalDateTime> colTimestamp;
    @FXML private TableColumn<Log, String> colUser;
    @FXML private TableColumn<Log, String> colRole;
    @FXML private TableColumn<Log, String> colAction;

    // --- Data Caching ---
    private Map<Integer, String> logUsernameMap = new HashMap<>();
    private Map<Integer, String> logRoleMap = new HashMap<>();

    // Master Data
    private ObservableList<Log> masterLogList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadLogsData();
        setupFilters();
    }

    // ==========================================
    // 🌟 SETUP TABLE COLUMNS
    // ==========================================
    private void setupTable() {

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

        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // 🌟 Action Column (Row အမြင့်မပျက်စေဘဲ အလယ်တည့်တည့်နှင့် Shrink-to-fit ပြုလုပ်ခြင်း) 🌟
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lblAction = new Label(action);

                    String lowerAction = action.toLowerCase();

                    // Base CSS Style
                    String baseStyle = "-fx-font-weight: bold; -fx-padding: 6px 12px; -fx-background-radius: 8px; -fx-font-size: 12px;";

                    // Keyword ပေါ်မူတည်၍ အရောင်သတ်မှတ်ခြင်း
                    if (lowerAction.contains("success") || lowerAction.contains("added") || lowerAction.contains("approve") || lowerAction.contains("created")) {
                        lblAction.setStyle(baseStyle + "-fx-text-fill: #31C4B4; -fx-background-color: rgba(49, 196, 180, 0.15);");
                    } else if (lowerAction.contains("fail") || lowerAction.contains("lock") || lowerAction.contains("reject") || lowerAction.contains("delete") || lowerAction.contains("cancel") || lowerAction.contains("inactive")) {
                        lblAction.setStyle(baseStyle + "-fx-text-fill: #FF4B72; -fx-background-color: rgba(255, 75, 114, 0.15);");
                    } else if (lowerAction.contains("update") || lowerAction.contains("edit") || lowerAction.contains("change") || lowerAction.contains("reschedule")) {
                        lblAction.setStyle(baseStyle + "-fx-text-fill: #3B82F6; -fx-background-color: rgba(59, 130, 246, 0.15);");
                    } else if (lowerAction.contains("log out") || lowerAction.contains("logout") || lowerAction.contains("pending")) {
                        lblAction.setStyle(baseStyle + "-fx-text-fill: #F59E0B; -fx-background-color: rgba(245, 158, 11, 0.15);");
                    } else {
                        lblAction.setStyle(baseStyle + "-fx-text-fill: #E2E8F0; -fx-background-color: rgba(226, 232, 240, 0.1);");
                    }

                    setGraphic(lblAction);
                    // 🌟 Cell တစ်ခုလုံးကို Center Alignment ပေးလိုက်ခြင်းဖြင့် Label သည် အလယ်သို့ ရောက်သွားမည် 🌟
                    setAlignment(Pos.CENTER);
                }
            }
        });
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
                "ORDER BY l.log_id DESC";

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
    // 🌟 FILTERING LOGIC
    // ==========================================
    private void setupFilters() {
        if (cbRoleFilter != null) {
            cbRoleFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyDataToTable());
            cbRoleFilter.getSelectionModel().select("All");
        }

        if (dpDateFilter != null) {
            dpDateFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyDataToTable());
            dpDateFilter.setValue(null);
        }
    }

    private void applyDataToTable() {
        FilteredList<Log> filteredData = new FilteredList<>(masterLogList, log -> {
            boolean matchesRole = true;
            if (cbRoleFilter != null) {
                String filterRole = cbRoleFilter.getValue();
                if (filterRole != null && !filterRole.isEmpty() && !filterRole.equalsIgnoreCase("All")) {
                    String logRole = logRoleMap.getOrDefault(log.getLogId(), "");
                    matchesRole = logRole.equalsIgnoreCase(filterRole);
                }
            }

            boolean matchesDate = true;
            if (dpDateFilter != null) {
                LocalDate filterDate = dpDateFilter.getValue();
                if (filterDate != null) {
                    LocalDateTime logTimestamp = log.getTimestamp();
                    if (logTimestamp != null) {
                        LocalDate logDate = logTimestamp.toLocalDate();
                        matchesDate = logDate.equals(filterDate);
                    } else {
                        matchesDate = false;
                    }
                }
            }

            return matchesRole && matchesDate;
        });

        SortedList<Log> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(logsTable.comparatorProperty());

        logsTable.setItems(sortedData);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}