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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class AdminBookingManagementController implements Initializable {

    @FXML private StackPane rootStackPane;

    @FXML private TableView<BookingData> bookingTable;
    @FXML private TableColumn<BookingData, String> colBookingId;
    @FXML private TableColumn<BookingData, String> colCustomer;
    @FXML private TableColumn<BookingData, String> colFlight;
    @FXML private TableColumn<BookingData, String> colDate;
    @FXML private TableColumn<BookingData, String> colStatus;
    @FXML private TableColumn<BookingData, Void> colAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("pnrCode"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("passenger"));
        colFlight.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

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
                                // 🌟 Cancel ဖြစ်ပြီးသားဆိုလျှင် ခလုတ်အစား စာသားပြမည် 🌟
                                if (data.getStatus().equalsIgnoreCase("Cancelled")) {
                                    Label lblCancelled = new Label("Cancelled");
                                    lblCancelled.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                                    setGraphic(lblCancelled);
                                } else if (data.getStatus().equalsIgnoreCase("Cancel Pending")) {
                                    Label lblPending = new Label("Pending Cancel");
                                    lblPending.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                                    setGraphic(lblPending);
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

    // ==========================================
    // 🌟 DATABASE မှ DATA အစစ်များကို ဆွဲထုတ်ခြင်း 🌟
    // ==========================================
    private void loadBookingsFromDatabase() {
        ObservableList<BookingData> dbDataList = FXCollections.observableArrayList();

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

                dbDataList.add(new BookingData(dbId, pnrCode, passengerName, route, date, status));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bookingTable.setItems(dbDataList);
    }

    // ==========================================
    // 🌟 CANCEL MODAL ဖွင့်ခြင်း နှင့် REASON တောင်းခြင်း 🌟
    // ==========================================
    private void openCancelModal(BookingData data, Button sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminCancelBookingReason.fxml"));
            Parent modalView = loader.load();

            // Dynamic Theme လိုက်ပြောင်းရန်
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

            // 🌟 Callbacks သတ်မှတ်ခြင်း 🌟
            controller.setCallbacks(
                    (reason) -> {
                        processAdminCancellation(data, reason); // DB Update လုပ်မည်
                        closeModal(overlay, mainContent);       // Modal ပိတ်မည်
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

    // ==========================================
    // 🌟 DATABASE UPDATE (Admin မှ တိုက်ရိုက် Cancel လုပ်ခြင်း) 🌟
    // ==========================================
    private void processAdminCancellation(BookingData data, String reason) {
        int adminId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;

        // Admin Cancel ဖြစ်သောကြောင့် Status ကို 'Cancelled by Admin' ဟု သိမ်းပါမည်
        String insertRequestQuery = "INSERT INTO requests (booking_id, admin_id, reason, status) VALUES (?, ?, ?, 'Cancelled by Admin')";
        String updateBookingQuery = "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement insertStmt = conn.prepareStatement(insertRequestQuery);
                 PreparedStatement updateStmt = conn.prepareStatement(updateBookingQuery)) {

                // 1. Insert into requests table
                insertStmt.setInt(1, data.getDbId());
                insertStmt.setInt(2, adminId);
                insertStmt.setString(3, reason);
                insertStmt.executeUpdate();

                // 2. Update bookings table
                updateStmt.setInt(1, data.getDbId());
                updateStmt.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            // Table တွင် ချက်ချင်း Update ပြရန်
            data.setStatus("Cancelled");
            bookingTable.refresh();

        } catch (Exception e) {
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

    // ==========================================
    // 🌟 Booking Data Model
    // ==========================================
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