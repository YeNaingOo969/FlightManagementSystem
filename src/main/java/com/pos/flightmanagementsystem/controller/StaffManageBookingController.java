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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
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

    // 🌟 ညာဘက်အပေါ်ထောင့်က Search Box
    @FXML private TextField searchField;

    // 🌟 Tabs များ
    @FXML private Label tabAllBookings;
    @FXML private Label tabCompleted;
    @FXML private Label tabCancelled;

    @FXML private SVGPath modalSpinnerIcon;
    private RotateTransition spinTransition;

    private BookingData pendingBookingData = null;
    private String currentFilter = "ALL";

    // --- Table Elements ---
    @FXML private TableView<BookingData> bookingTable;
    @FXML private TableColumn<BookingData, String> colBookingId;
    @FXML private TableColumn<BookingData, String> colPassenger;
    @FXML private TableColumn<BookingData, String> colRoute;
    @FXML private TableColumn<BookingData, String> colDate;
    @FXML private TableColumn<BookingData, String> colStatus;
    @FXML private TableColumn<BookingData, Void> colAction;

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
                        btn.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
                        btn.setOnAction((ActionEvent event) -> {
                            pendingBookingData = getTableView().getItems().get(getIndex());
                            openWaitingModal();
                            processDatabaseCancellation();
                        });

                        spinner.setPrefSize(16, 16);
                        spinner.setStyle("-fx-progress-color: #F59E0B;");
                        lblWaiting.getStyleClass().add("waiting-text");
                        waitingBox.setAlignment(Pos.CENTER_LEFT);
                        waitingBox.getChildren().addAll(spinner, lblWaiting);
                        setAlignment(Pos.CENTER_LEFT);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BookingData data = getTableView().getItems().get(getIndex());
                            if (data != null) {
                                if ("Cancelled".equalsIgnoreCase(data.getStatus())) {
                                    setGraphic(null);
                                } else if (data.isCancelPending()) {
                                    setGraphic(waitingBox);
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

        // 🌟 Search လုပ်ရန် FilteredList အသုံးပြုခြင်း
        setupSearchFilter();

        setupTabs();
        loadBookingsFromDatabase();
    }

    // ==========================================
    // 🌟 SEARCH FILTER (Booking ID & Passenger Name အစ စာလုံးဖြင့် ရှာရန်)
    // ==========================================
    private void setupSearchFilter() {
        FilteredList<BookingData> filteredData = new FilteredList<>(bookingList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(booking -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // ⚠️ startsWith ကိုပြောင်းလဲအသုံးပြုထားပါသည် ⚠️
                // PNR Code ရဲ့ အစစာလုံး/ဂဏန်းများဖြင့် တိုက်စစ်မည်
                if (booking.getPnrCode().toLowerCase().startsWith(lowerCaseFilter)) {
                    return true;
                }
                // Passenger နာမည်ရဲ့ အစစာလုံးဖြင့် တိုက်စစ်မည်
                else if (booking.getPassenger().toLowerCase().startsWith(lowerCaseFilter)) {
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
    // 🌟 TABS စီမံခန့်ခွဲမှု (Filter)
    // ==========================================
    private void setupTabs() {
        if (tabAllBookings != null) {
            tabAllBookings.setOnMouseClicked(e -> {
                updateTabStyles(tabAllBookings);
                currentFilter = "ALL";
                loadBookingsFromDatabase();
            });
        }

        if (tabCompleted != null) {
            tabCompleted.setOnMouseClicked(e -> {
                updateTabStyles(tabCompleted);
                currentFilter = "COMPLETED";
                loadBookingsFromDatabase();
            });
        }

        if (tabCancelled != null) {
            tabCancelled.setOnMouseClicked(e -> {
                updateTabStyles(tabCancelled);
                currentFilter = "CANCELLED";
                loadBookingsFromDatabase();
            });
        }

        if (tabAllBookings != null) {
            updateTabStyles(tabAllBookings);
        }
    }

    private void updateTabStyles(Label activeTab) {
        String normalStyle = "-fx-border-width: 0; -fx-text-fill: #7F8C8D; -fx-font-weight: normal; -fx-padding: 10; -fx-cursor: hand;";
        String activeStyle = "-fx-border-color: #0052CC; -fx-border-width: 0 0 2 0; -fx-text-fill: #0052CC; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;";

        if (tabAllBookings != null) tabAllBookings.setStyle(normalStyle);
        if (tabCompleted != null) tabCompleted.setStyle(normalStyle);
        if (tabCancelled != null) tabCancelled.setStyle(normalStyle);

        if (activeTab != null) {
            activeTab.setStyle(activeStyle);
        }
    }

    // ==========================================
    // 🌟 DATABASE မှ DATA များကို ဆွဲယူခြင်း
    // ==========================================
    private void loadBookingsFromDatabase() {
        bookingList.clear();

        String query = "SELECT b.booking_id, b.pnr_code, b.seat_id, b.status, " +
                "p.full_name AS passenger_name, " +
                "CONCAT(f.flight_number, ' (City ID: ', f.departure_city_id, '-', f.arrival_city_id, ')') AS route_info, " +
                "DATE(f.departure_time) AS flight_date " +
                "FROM bookings b " +
                "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                "JOIN flights f ON b.flight_id = f.flight_id";

        if (currentFilter.equals("COMPLETED")) {
            query += " WHERE b.status = 'Confirmed' OR b.status = 'Completed'";
        } else if (currentFilter.equals("CANCELLED")) {
            query += " WHERE b.status = 'Cancelled'";
        }

        query += " ORDER BY b.booking_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                BookingData booking = new BookingData(
                        rs.getInt("booking_id"),
                        rs.getString("pnr_code"),
                        rs.getString("passenger_name"),
                        rs.getString("route_info"),
                        rs.getString("flight_date"),
                        rs.getString("status"),
                        rs.getInt("seat_id")
                );
                bookingList.add(booking);
            }

            bookingTable.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 🌟 DATABASE တွင် CANCEL ပြုလုပ်ခြင်း
    // ==========================================
    private void processDatabaseCancellation() {
        if (pendingBookingData == null) return;

        new Thread(() -> {
            try {
                Thread.sleep(1000);

                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);

                    try {
                        String updateBooking = "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?";
                        try (PreparedStatement stmt1 = conn.prepareStatement(updateBooking)) {
                            stmt1.setInt(1, pendingBookingData.getBookingId());
                            stmt1.executeUpdate();
                        }

                        String updateSeat = "UPDATE seats SET is_available = true WHERE seat_id = ?";
                        try (PreparedStatement stmt2 = conn.prepareStatement(updateSeat)) {
                            stmt2.setInt(1, pendingBookingData.getSeatId());
                            stmt2.executeUpdate();
                        }

                        conn.commit();

                    } catch (Exception ex) {
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }

                Platform.runLater(() -> {
                    closeWaitingModalProgrammatically();
                    loadBookingsFromDatabase();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    closeWaitingModalProgrammatically();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Cancel လုပ်ရာတွင် အမှားအယွင်းရှိပါသည်။");
                    alert.show();
                });
            }
        }).start();
    }

    // ==========================================
    // 🌟 MODAL ANIMATIONS
    // ==========================================
    private void openWaitingModal() {
        waitingModal.setVisible(true);
        spinTransition.play();

        GaussianBlur blur = new GaussianBlur(0);
        mainContainer.setEffect(blur);
        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 15))
        );
        blurTimeline.play();

        waitingModal.setScaleX(0.5);
        waitingModal.setScaleY(0.5);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), waitingModal);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), waitingModal);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn);
        pt.setInterpolator(Interpolator.EASE_OUT);
        pt.play();
    }

    @FXML
    void closeWaitingModal(ActionEvent event) {
        if (pendingBookingData != null) {
            pendingBookingData.setCancelPending(true);
            bookingTable.refresh();
        }
        executeCloseModalAnimation();
    }

    private void closeWaitingModalProgrammatically() {
        pendingBookingData = null;
        executeCloseModalAnimation();
    }

    private void executeCloseModalAnimation() {
        if (mainContainer.getEffect() instanceof GaussianBlur) {
            GaussianBlur blur = (GaussianBlur) mainContainer.getEffect();
            Timeline blurOut = new Timeline(
                    new KeyFrame(Duration.millis(250), new KeyValue(blur.radiusProperty(), 0))
            );
            blurOut.setOnFinished(e -> mainContainer.setEffect(null));
            blurOut.play();
        }

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), waitingModal);
        scaleDown.setToX(0.5);
        scaleDown.setToY(0.5);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), waitingModal);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setInterpolator(Interpolator.EASE_IN);
        pt.setOnFinished(e -> {
            waitingModal.setVisible(false);
            spinTransition.stop();
        });
        pt.play();
    }

    // ==========================================
    // 🌟 DATA MODEL
    // ==========================================
    public static class BookingData {
        private int bookingId;
        private String pnrCode;
        private String passenger;
        private String route;
        private String date;
        private String status;
        private int seatId;
        private boolean cancelPending = false;

        public BookingData(int bookingId, String pnrCode, String passenger, String route, String date, String status, int seatId) {
            this.bookingId = bookingId;
            this.pnrCode = pnrCode;
            this.passenger = passenger;
            this.route = route;
            this.date = date;
            this.status = status;
            this.seatId = seatId;
        }

        public int getBookingId() { return bookingId; }
        public String getPnrCode() { return pnrCode; }
        public String getPassenger() { return passenger; }
        public String getRoute() { return route; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public int getSeatId() { return seatId; }

        public boolean isCancelPending() { return cancelPending; }
        public void setCancelPending(boolean cancelPending) { this.cancelPending = cancelPending; }
    }
}