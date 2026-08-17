package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Backup;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SuperAdminSystemToolController implements Initializable {

    @FXML private Button btnBackupNow;
    @FXML private Button btnExportSql;

    // --- TableView Variables ---
    @FXML private TableView<Backup> tvBackupHistory;
    @FXML private TableColumn<Backup, LocalDateTime> colDate;
    @FXML private TableColumn<Backup, String> colFileName;
    @FXML private TableColumn<Backup, String> colSize;
    @FXML private TableColumn<Backup, String> colAction;
    private ObservableList<Backup> backupList = FXCollections.observableArrayList();

    // --- Toast Notification FXML Variables ---
    @FXML private HBox notificationToast;
    @FXML private StackPane toastIconBg;
    @FXML private SVGPath toastIconPath;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastMessage;

    // Scheduler for 24-hour Auto Backup
    private ScheduledExecutorService scheduler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDate.setCellValueFactory(new PropertyValueFactory<>("backupDate"));
        colFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("actionType"));

        tvBackupHistory.setItems(backupList);

        loadBackupHistory();
        startScheduledBackupTask();
    }

    // ==========================================
    // 24-HOUR SCHEDULED BACKUP LOGIC
    // ==========================================
    private void startScheduledBackupTask() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::performScheduledBackup, 24, 24, TimeUnit.HOURS);
    }

    private void performScheduledBackup() {
        String folderPath = "src/main/resources/com/pos/flightmanagementsystem/sql";
        File backupDir = new File(folderPath);
        if (!backupDir.exists()) backupDir.mkdirs();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        String fileName = "flight_db_scheduled_" + timestamp + ".sql";

        String fullOutputPath = backupDir.getAbsolutePath() + File.separator + fileName;
        processDatabaseExport(fullOutputPath, fileName, "Scheduled Backup");
    }

    // ==========================================
    // DATABASE HISTORY LOGIC
    // ==========================================
    private void loadBackupHistory() {
        backupList.clear();
        String query = "SELECT * FROM backups ORDER BY backup_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String fileName = rs.getString("file_name");
                String fileSize = rs.getString("file_size");
                String actionType = rs.getString("action_type");
                LocalDateTime backupDate = rs.getTimestamp("backup_date").toLocalDateTime();

                backupList.add(new Backup(id, fileName, fileSize, actionType, backupDate));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBackupHistory(String fileName, String fileSize, String actionType) {
        String query = "INSERT INTO backups (file_name, file_size, action_type, backup_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fileName);
            pstmt.setString(2, fileSize);
            pstmt.setString(3, actionType);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // UI BUTTON ACTIONS (Manual & Export)
    // ==========================================
    @FXML
    void handleBackupNow(ActionEvent event) {
        String folderPath = "src/main/resources/com/pos/flightmanagementsystem/sql";
        File backupDir = new File(folderPath);
        if (!backupDir.exists()) backupDir.mkdirs();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        String fileName = "flight_db_backup_" + timestamp + ".sql";

        String fullOutputPath = backupDir.getAbsolutePath() + File.separator + fileName;

        processDatabaseExport(fullOutputPath, fileName, "Auto Backup");
    }

    @FXML
    void handleExportSql(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Database SQL");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String defaultFileName = "flight_db_export_" + now.format(formatter) + ".sql";
        fileChooser.setInitialFileName(defaultFileName);

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files (*.sql)", "*.sql"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            processDatabaseExport(file.getAbsolutePath(), file.getName(), "Export SQL");
        }
    }

    // ==========================================
    // 🌟 SYSTEM LOG LOGIC 🌟
    // ==========================================
    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {
            if (userId > 0) pstmt.setInt(1, userId);
            else pstmt.setNull(1, java.sql.Types.INTEGER);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // CORE EXPORT LOGIC
    // ==========================================
    private void processDatabaseExport(String outputPath, String fileName, String actionType) {
        new Thread(() -> {
            boolean success = false;
            File outputFile = new File(outputPath);

            try (Connection conn = DatabaseConnection.getConnection();
                 PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

                String dbName = conn.getCatalog();
                writer.println("SET FOREIGN_KEY_CHECKS=0;\n");

                DatabaseMetaData metaData = conn.getMetaData();
                try (ResultSet tables = metaData.getTables(dbName, null, "%", new String[]{"TABLE"})) {
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        writer.println("DROP TABLE IF EXISTS `" + tableName + "`;");

                        try (Statement stmt = conn.createStatement();
                             ResultSet rsCreate = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {
                            if (rsCreate.next()) writer.println(rsCreate.getString(2) + ";\n");
                        }

                        try (Statement stmt = conn.createStatement();
                             ResultSet rsData = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {
                            ResultSetMetaData rsMetaData = rsData.getMetaData();
                            int columnCount = rsMetaData.getColumnCount();
                            while (rsData.next()) {
                                StringBuilder insertBuilder = new StringBuilder();
                                insertBuilder.append("INSERT INTO `").append(tableName).append("` VALUES (");
                                for (int i = 1; i <= columnCount; i++) {
                                    Object value = rsData.getObject(i);
                                    if (value == null) {
                                        insertBuilder.append("NULL");
                                    } else if (value instanceof Number) {
                                        insertBuilder.append(value);
                                    } else {
                                        String safeString = value.toString().replace("\\", "\\\\").replace("'", "\\'");
                                        insertBuilder.append("'").append(safeString).append("'");
                                    }
                                    if (i < columnCount) insertBuilder.append(", ");
                                }
                                insertBuilder.append(");");
                                writer.println(insertBuilder.toString());
                            }
                        }
                    }
                }
                writer.println("\nSET FOREIGN_KEY_CHECKS=1;");
                success = true;

            } catch (Exception e) {
                e.printStackTrace();
            }

            final boolean finalSuccess = success;
            Platform.runLater(() -> {
                if (finalSuccess) {
                    long length = outputFile.length();
                    String fileSize = (length / 1024) + " KB";

                    saveBackupHistory(fileName, fileSize, actionType);
                    loadBackupHistory();

                    // 🌟 ဤနေရာတွင် System Log သို့ မှတ်တမ်းတင်ပါမည် 🌟
                    int adminId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;
                    String logMessage;

                    if (actionType.equals("Export SQL")) {
                        logMessage = "Exported database SQL manually: " + fileName;
                    } else if (actionType.equals("Auto Backup")) {
                        logMessage = "Triggered manual database backup: " + fileName;
                    } else {
                        logMessage = "System performed scheduled backup: " + fileName;
                    }

                    insertLog(adminId, logMessage);

                    showToast("Backup Successful!", "Saved securely at:\n" + outputPath, true);
                } else {
                    showToast("Backup Failed!", "An error occurred while saving the backup. Check console.", false);
                }
            });
        }).start();
    }

    // ==========================================
    // TOAST NOTIFICATION
    // ==========================================
    private void showToast(String title, String message, boolean isSuccess) {
        lblToastTitle.setText(title);
        lblToastMessage.setText(message);

        if (isSuccess) {
            toastIconBg.setStyle("-fx-background-color: #10B981; -fx-background-radius: 50%; -fx-min-width: 38px; -fx-min-height: 38px; -fx-max-width: 38px; -fx-max-height: 38px;");
            toastIconPath.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
            toastIconPath.setStyle("-fx-fill: #FFFFFF;");
            notificationToast.setStyle("-fx-border-color: #10B981;");
        } else {
            toastIconBg.setStyle("-fx-background-color: #EF4444; -fx-background-radius: 50%; -fx-min-width: 38px; -fx-min-height: 38px; -fx-max-width: 38px; -fx-max-height: 38px;");
            toastIconPath.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
            toastIconPath.setStyle("-fx-fill: #FFFFFF;");
            notificationToast.setStyle("-fx-border-color: #EF4444;");
        }

        notificationToast.setVisible(true);
        notificationToast.setTranslateY(150);

        TranslateTransition popUp = new TranslateTransition(Duration.millis(600), notificationToast);
        popUp.setToY(0);
        popUp.setInterpolator(new Interpolator() {
            @Override
            protected double curve(double t) {
                double tension = 2.0;
                t -= 1.0;
                return t * t * ((tension + 1) * t + tension) + 1.0;
            }
        });
        popUp.play();

        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(6));
        delay.setOnFinished(e -> closeNotificationAnimation());
        delay.play();
    }

    @FXML
    void closeNotification(ActionEvent event) {
        closeNotificationAnimation();
    }

    private void closeNotificationAnimation() {
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(400), notificationToast);
        slideDown.setToY(150);
        slideDown.setInterpolator(Interpolator.EASE_IN);
        slideDown.setOnFinished(e -> notificationToast.setVisible(false));
        slideDown.play();
    }
}