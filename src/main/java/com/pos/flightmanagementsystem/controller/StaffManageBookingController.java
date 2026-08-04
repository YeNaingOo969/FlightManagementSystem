package com.pos.flightmanagementsystem.controller;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class StaffManageBookingController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;
    @FXML private StackPane waitingModal;

    @FXML private TextField searchField; // 🌟 Search Box

    // Tab Labels
    @FXML private Label lblTabAll;
    @FXML private Label lblTabCompleted;
    @FXML private Label lblTabCancelled;

    @FXML private SVGPath modalSpinnerIcon;
    private RotateTransition spinTransition;

    // --- Table Elements ---
    @FXML private TableView<BookingData> bookingTable;
    @FXML private TableColumn<BookingData, String> colBookingId;
    @FXML private TableColumn<BookingData, String> colPassenger;
    @FXML private TableColumn<BookingData, String> colRoute;
    @FXML private TableColumn<BookingData, String> colDate;
    @FXML private TableColumn<BookingData, String> colStatus;
    @FXML private TableColumn<BookingData, Void> colAction;

    // 🌟 Search အတွက် ObservableList သီးသန့်တည်ဆောက်ထားခြင်း
    private ObservableList<BookingData> bookingList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spinTransition = new RotateTransition(Duration.seconds(1.5), modalSpinnerIcon);
        spinTransition.setByAngle(360);
        spinTransition.setCycleCount(Animation.INDEFINITE);
        spinTransition.setInterpolator(Interpolator.LINEAR);

        colBookingId.setCellValueFactory(new PropertyValueFactory<>("pnrCode"));
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passenger"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        Callback<TableColumn<BookingData, Void>, TableCell<BookingData, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<BookingData, Void> call(final TableColumn<BookingData, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Cancel");
                    private final HBox waitingBox = new HBox(8);
                    private final ProgressIndicator spinner = new ProgressIndicator();
                    private final Label lblWaiting = new Label("Waiting");

                    {
                        btn.getStyleClass().add("btn-cancel-table");
                        btn.setOnAction((ActionEvent event) -> {
                            BookingData pendingBookingData = getTableView().getItems().get(getIndex());
                            openReasonModal(pendingBookingData);
                        });

                        spinner.setPrefSize(16, 16);
                        spinner.setStyle("-fx-progress-color: #F59E0B;");
                        lblWaiting.getStyleClass().add("waiting-text");

                        waitingBox.setAlignment(Pos.CENTER);
                        waitingBox.getChildren().addAll(spinner, lblWaiting);
                        setAlignment(Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BookingData data = getTableView().getItems().get(getIndex());
                            if (data != null) {
                                if (data.getStatus().equalsIgnoreCase("Cancelled")) {
                                    Label lblCancelled = new Label("Cancelled");
                                    lblCancelled.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                                    setGraphic(lblCancelled);
                                }
                                else if (data.isCancelPending() || data.getStatus().equalsIgnoreCase("Cancel Pending")) {
                                    setGraphic(waitingBox);
                                }
                                else {
                                    setGraphic(btn);
                                }
                            }
                        }
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);

        setupSearchFilter();
        loadBookingsFromDatabase("All");
    }

    // ==========================================
    // 🌟 SEARCH FILTER (Booking ID & Passenger Name)
    // ==========================================
    private void setupSearchFilter() {
        FilteredList<BookingData> filteredData = new FilteredList<>(bookingList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(booking -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (booking.getPnrCode().toLowerCase().startsWith(lowerCaseFilter)) {
                    return true;
                } else if (booking.getPassenger().toLowerCase().startsWith(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<BookingData> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(bookingTable.comparatorProperty());
        bookingTable.setItems(sortedData);
    }

    // ==========================================
    // 🌟 TAB CLICK ACTIONS
    // ==========================================
    @FXML void filterAll(MouseEvent event) { setActiveTab(lblTabAll); loadBookingsFromDatabase("All"); }
    @FXML void filterCompleted(MouseEvent event) { setActiveTab(lblTabCompleted); loadBookingsFromDatabase("Completed"); }
    @FXML void filterCancelled(MouseEvent event) { setActiveTab(lblTabCancelled); loadBookingsFromDatabase("Cancelled"); }

    private void setActiveTab(Label activeLabel) {
        lblTabAll.getStyleClass().remove("active-tab");
        lblTabCompleted.getStyleClass().remove("active-tab");
        lblTabCancelled.getStyleClass().remove("active-tab");
        activeLabel.getStyleClass().add("active-tab");
    }

    // ==========================================
    // 🌟 DATABASE မှ DATA ဆွဲထုတ်ခြင်း
    // ==========================================
    private void loadBookingsFromDatabase(String filterType) {
        bookingList.clear(); // 🌟 ObservableList အဟောင်းကိုရှင်းပြီး အသစ်ထည့်မည်

        String query = "SELECT b.booking_id, b.pnr_code, p.full_name AS passenger_name, " +
                "f.flight_number, dep_city.city_code AS dep_code, arr_city.city_code AS arr_code, " +
                "DATE(f.departure_time) AS dep_date, b.status " +
                "FROM bookings b " +
                "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                "JOIN flights f ON b.flight_id = f.flight_id " +
                "JOIN cities dep_city ON f.departure_city_id = dep_city.city_id " +
                "JOIN cities arr_city ON f.arrival_city_id = arr_city.city_id ";

        if (filterType.equals("Completed")) {
            query += "WHERE b.status IN ('Confirmed', 'Completed') ";
        } else if (filterType.equals("Cancelled")) {
            query += "WHERE b.status IN ('Cancelled', 'Cancel Pending') ";
        }

        query += "ORDER BY b.booking_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                BookingData data = new BookingData(
                        rs.getInt("booking_id"),
                        rs.getString("pnr_code"),
                        rs.getString("passenger_name"),
                        rs.getString("flight_number") + " (" + rs.getString("dep_code") + "-" + rs.getString("arr_code") + ")",
                        rs.getString("dep_date"),
                        rs.getString("status")
                );

                if (data.getStatus().equalsIgnoreCase("Cancel Pending")) {
                    data.setCancelPending(true);
                }
                bookingList.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bookingTable.refresh();
    }

    // ==========================================
    // 🌟 REASON MODAL & DATABASE UPDATE
    // ==========================================
    private void openReasonModal(BookingData data) {
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

            AdminCancelBookingReasonController controller = loader.getController();

            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

            GaussianBlur blur = new GaussianBlur(8);
            mainContainer.setEffect(blur);

            controller.setCallbacks(
                    (reason) -> {
                        processCancellationRequest(data, reason);
                        closeModal(overlay);
                        openWaitingModal();
                    },
                    () -> closeModal(overlay)
            );

            modalView.setScaleX(0.0);
            modalView.setScaleY(0.0);
            rootStackPane.getChildren().add(overlay);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), modalView);
            scaleUp.setToX(1.0); scaleUp.setToY(1.0);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
            fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn);
            pt.play();

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void processCancellationRequest(BookingData data, String reason) {
        int staffId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;

        String insertRequestQuery = "INSERT INTO requests (booking_id, staff_id, reason, status) VALUES (?, ?, ?, 'Pending')";
        String updateBookingQuery = "UPDATE bookings SET status = 'Cancel Pending' WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement insertStmt = conn.prepareStatement(insertRequestQuery);
                 PreparedStatement updateStmt = conn.prepareStatement(updateBookingQuery)) {
                insertStmt.setInt(1, data.getDbId());
                insertStmt.setInt(2, staffId);
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

            data.setCancelPending(true);
            data.setStatus("Cancel Pending");
            bookingTable.refresh();

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void closeModal(StackPane overlay) {
        Parent modalView = (Parent) overlay.getChildren().get(0);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), modalView);
        scaleDown.setToX(0.0); scaleDown.setToY(0.0); scaleDown.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), overlay); fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setOnFinished(e -> { rootStackPane.getChildren().remove(overlay); mainContainer.setEffect(null); });
        pt.play();
    }

    private void openWaitingModal() {
        waitingModal.setVisible(true);
        spinTransition.play();

        GaussianBlur blur = new GaussianBlur(15);
        mainContainer.setEffect(blur);

        waitingModal.setScaleX(0.5); waitingModal.setScaleY(0.5);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), waitingModal);
        scaleUp.setToX(1.0); scaleUp.setToY(1.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), waitingModal); fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);

        ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn); pt.setInterpolator(Interpolator.EASE_OUT); pt.play();
    }

    @FXML
    void closeWaitingModal(ActionEvent event) {
        if (mainContainer.getEffect() instanceof GaussianBlur) {
            GaussianBlur blur = (GaussianBlur) mainContainer.getEffect();
            Timeline blurOut = new Timeline(new KeyFrame(Duration.millis(250), new KeyValue(blur.radiusProperty(), 0)));
            blurOut.setOnFinished(e -> mainContainer.setEffect(null));
            blurOut.play();
        }

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), waitingModal);
        scaleDown.setToX(0.5); scaleDown.setToY(0.5);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), waitingModal); fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut); pt.setInterpolator(Interpolator.EASE_IN);
        pt.setOnFinished(e -> { waitingModal.setVisible(false); spinTransition.stop(); });
        pt.play();
    }

    public static class BookingData {
        private int dbId;
        private String pnrCode, passenger, route, date, status;
        private boolean cancelPending = false;

        public BookingData(int dbId, String pnrCode, String passenger, String route, String date, String status) {
            this.dbId = dbId; this.pnrCode = pnrCode; this.passenger = passenger; this.route = route; this.date = date; this.status = status;
        }

        public int getDbId() { return dbId; }
        public String getPnrCode() { return pnrCode; }
        public String getPassenger() { return passenger; }
        public String getRoute() { return route; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isCancelPending() { return cancelPending; }
        public void setCancelPending(boolean cancelPending) { this.cancelPending = cancelPending; }
    }
}