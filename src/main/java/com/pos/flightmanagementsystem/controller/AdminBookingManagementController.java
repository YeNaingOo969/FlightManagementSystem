package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminBookingManagementController implements Initializable {

    @FXML private StackPane rootStackPane;

    @FXML private TextField searchField;
    @FXML private Label lblTabAll;
    @FXML private Label lblTabCompleted;
    @FXML private Label lblTabCancelled;

    @FXML private TableView<BookingData> bookingTable;
    @FXML private TableColumn<BookingData, String> colBookingId;
    @FXML private TableColumn<BookingData, String> colCustomer;
    @FXML private TableColumn<BookingData, String> colFlight;
    @FXML private TableColumn<BookingData, String> colDate;
    @FXML private TableColumn<BookingData, String> colStatus;
    @FXML private TableColumn<BookingData, Void> colAction;

    private final ObservableList<BookingData> masterBookingList = FXCollections.observableArrayList();
    private String currentTabFilter = "ALL";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("pnrCode"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("passenger"));
        colFlight.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        if (lblTabAll != null) lblTabAll.setCursor(Cursor.HAND);
        if (lblTabCompleted != null) lblTabCompleted.setCursor(Cursor.HAND);
        if (lblTabCancelled != null) lblTabCancelled.setCursor(Cursor.HAND);

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }

        // 🌟 Status Column ကို Staff Portal ပုံစံ Pill Badge (Border + Background) ဖြင့် ပြသခြင်း 🌟
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lblStatus = new Label(status);
                    String lowerStatus = status.toLowerCase();

                    String baseStyle = "-fx-font-weight: bold; -fx-padding: 4px 14px; -fx-background-radius: 20px; -fx-border-radius: 20px; -fx-border-width: 1px; -fx-font-size: 12px;";

                    if (lowerStatus.contains("confirm") || lowerStatus.contains("complete") || lowerStatus.contains("success")) {
                        lblStatus.setStyle(baseStyle + "-fx-text-fill: #31C4B4; -fx-background-color: rgba(49, 196, 180, 0.12); -fx-border-color: #31C4B4;");
                    } else if (lowerStatus.contains("cancel")) {
                        lblStatus.setStyle(baseStyle + "-fx-text-fill: #FF4B72; -fx-background-color: rgba(255, 75, 114, 0.12); -fx-border-color: #FF4B72;");
                    } else if (lowerStatus.contains("reschedule")) {
                        lblStatus.setStyle(baseStyle + "-fx-text-fill: #3B82F6; -fx-background-color: rgba(59, 130, 246, 0.12); -fx-border-color: #3B82F6;");
                    } else if (lowerStatus.contains("pending")) {
                        lblStatus.setStyle(baseStyle + "-fx-text-fill: #94A3B8; -fx-background-color: rgba(148, 163, 184, 0.12); -fx-border-color: #64748B;");
                    } else {
                        lblStatus.setStyle(baseStyle + "-fx-text-fill: #E2E8F0; -fx-background-color: rgba(226, 232, 240, 0.1); -fx-border-color: #475569;");
                    }

                    setGraphic(lblStatus);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // 🌟 Action Column (Cancel ခလုတ် နှင့် Cancelled အနီရောင်စာသား) 🌟
        Callback<TableColumn<BookingData, Void>, TableCell<BookingData, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<BookingData, Void> call(final TableColumn<BookingData, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Cancel");

                    {
                        btn.getStyleClass().add("btn-cancel-table");
                        setAlignment(Pos.CENTER);

                        btn.setOnAction((ActionEvent event) -> {
                            BookingData data = getTableView().getItems().get(getIndex());
                            openCancelModal(data, btn);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BookingData data = getTableView().getItems().get(getIndex());
                            if (data != null) {
                                if (data.getStatus() != null && data.getStatus().toLowerCase().contains("cancel")) {
                                    Label lblActionStatus = new Label("Cancelled");
                                    lblActionStatus.setStyle("-fx-text-fill: #FF4B72; -fx-font-weight: bold; -fx-font-size: 12px;");
                                    setGraphic(lblActionStatus);
                                } else {
                                    setGraphic(btn);
                                }
                            }
                        }
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);

        loadBookingsFromDatabase();
    }

    @FXML
    void filterAll(MouseEvent event) {
        currentTabFilter = "ALL";
        updateActiveTabStyle(lblTabAll);
        applyFilters();
    }

    @FXML
    void filterCompleted(MouseEvent event) {
        currentTabFilter = "COMPLETED";
        updateActiveTabStyle(lblTabCompleted);
        applyFilters();
    }

    @FXML
    void filterCancelled(MouseEvent event) {
        currentTabFilter = "CANCELLED";
        updateActiveTabStyle(lblTabCancelled);
        applyFilters();
    }

    private void updateActiveTabStyle(Label activeLabel) {
        if (lblTabAll != null) lblTabAll.getStyleClass().remove("active-tab");
        if (lblTabCompleted != null) lblTabCompleted.getStyleClass().remove("active-tab");
        if (lblTabCancelled != null) lblTabCancelled.getStyleClass().remove("active-tab");

        if (activeLabel != null && !activeLabel.getStyleClass().contains("active-tab")) {
            activeLabel.getStyleClass().add("active-tab");
        }
    }

    private void applyFilters() {
        String keyword = (searchField != null && searchField.getText() != null)
                ? searchField.getText().trim().toLowerCase()
                : "";

        ObservableList<BookingData> filteredList = FXCollections.observableArrayList();

        for (BookingData item : masterBookingList) {
            String statusLower = item.getStatus() != null ? item.getStatus().toLowerCase() : "";

            boolean matchesTab = switch (currentTabFilter) {
                case "COMPLETED" -> statusLower.contains("confirm") || statusLower.contains("complete") || statusLower.contains("success");
                case "CANCELLED" -> statusLower.contains("cancel");
                default -> true;
            };

            if (!matchesTab) continue;

            boolean matchesKeyword = keyword.isEmpty()
                    || (item.getPnrCode() != null && item.getPnrCode().toLowerCase().contains(keyword))
                    || (item.getPassenger() != null && item.getPassenger().toLowerCase().contains(keyword))
                    || (item.getRoute() != null && item.getRoute().toLowerCase().contains(keyword))
                    || (item.getDate() != null && item.getDate().toLowerCase().contains(keyword));

            if (matchesKeyword) {
                filteredList.add(item);
            }
        }

        bookingTable.setItems(filteredList);
    }

    private void loadBookingsFromDatabase() {
        masterBookingList.clear();

        String query = "SELECT b.booking_id, b.pnr_code, p.full_name AS passenger_name, " +
                "f.flight_number, dep_city.city_code AS dep_code, arr_city.city_code AS arr_code, " +
                "DATE(f.departure_time) AS dep_date, b.status " +
                "FROM bookings b " +
                "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                "JOIN flights f ON b.flight_id = f.flight_id " +
                "JOIN cities dep_city ON f.departure_city_id = dep_city.city_id " +
                "JOIN cities arr_city ON f.arrival_city_id = arr_city.city_id " +
                "ORDER BY b.booking_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int dbId = rs.getInt("booking_id");
                String pnrCode = rs.getString("pnr_code");
                String passengerName = rs.getString("passenger_name");
                String route = rs.getString("flight_number") + " (" + rs.getString("dep_code") + "-" + rs.getString("arr_code") + ")";
                String date = rs.getString("dep_date");
                String status = rs.getString("status");

                masterBookingList.add(new BookingData(dbId, pnrCode, passengerName, route, date, status));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        applyFilters();
    }

    private void openCancelModal(BookingData data, Button sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminCancelBookingReason.fxml"));
            Parent modalView = loader.load();

            if (rootStackPane.getStylesheets() != null && !rootStackPane.getStylesheets().isEmpty()) {
                String currentMainStyle = rootStackPane.getStylesheets().get(0);
                modalView.getStylesheets().clear();
                if (currentMainStyle.contains("Light")) {
                    modalView.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/AdminBookingLight.css").toExternalForm());
                } else {
                    modalView.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/AdminBookingDark.css").toExternalForm());
                }
            }

            modalView.setStyle("-fx-background-color: transparent; -fx-max-width: USE_PREF_SIZE; -fx-max-height: USE_PREF_SIZE;");
            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

            Node mainContent = rootStackPane.getChildren().get(0);
            BoxBlur blur = new BoxBlur(8, 8, 3);
            mainContent.setEffect(blur);

            AdminCancelBookingReasonController controller = loader.getController();

            controller.setCallbacks(
                    (reason) -> {
                        processAdminCancellation(data, reason);
                        closeModal(overlay, mainContent);
                    },
                    () -> closeModal(overlay, mainContent)
            );

            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    closeModal(overlay, mainContent);
                }
            });

            rootStackPane.getChildren().add(overlay);

            modalView.setScaleX(0.5);
            modalView.setScaleY(0.5);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn);
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAdminCancellation(BookingData data, String reason) {
        int adminId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;

        String insertRequestQuery = "INSERT INTO requests (booking_id, admin_id, reason, status) VALUES (?, ?, ?, 'Cancelled by Admin')";
        String updateBookingQuery = "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement insertStmt = conn.prepareStatement(insertRequestQuery);
                 PreparedStatement updateStmt = conn.prepareStatement(updateBookingQuery)) {

                insertStmt.setInt(1, data.getDbId());
                insertStmt.setInt(2, adminId);
                insertStmt.setString(3, reason);
                insertStmt.executeUpdate();

                updateStmt.setInt(1, data.getDbId());
                updateStmt.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            insertLog(adminId, "Cancelled booking PNR: " + data.getPnrCode() + " (Reason: " + reason + ")");

            data.setStatus("Cancelled");
            applyFilters();
            bookingTable.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private void closeModal(StackPane overlay, Node mainContent) {
        Node modalView = overlay.getChildren().get(0);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), modalView);
        scaleDown.setToX(0.5);
        scaleDown.setToY(0.5);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), overlay);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setOnFinished(e -> {
            rootStackPane.getChildren().remove(overlay);
            mainContent.setEffect(null);
        });
        pt.play();
    }

    public static class BookingData {
        private int dbId;
        private String pnrCode, passenger, route, date, status;

        public BookingData(int dbId, String pnrCode, String passenger, String route, String date, String status) {
            this.dbId = dbId;
            this.pnrCode = pnrCode;
            this.passenger = passenger;
            this.route = route;
            this.date = date;
            this.status = status;
        }

        public int getDbId() { return dbId; }
        public String getPnrCode() { return pnrCode; }
        public String getPassenger() { return passenger; }
        public String getRoute() { return route; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}