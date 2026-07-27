package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StaffSeatSelectionController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private ComboBox<String> classComboBox;
    @FXML private Pane viewportPane;
    @FXML private VBox sideViewPlane;
    @FXML private Pane topViewPlane;

    @FXML private VBox businessSection;
    @FXML private VBox premiumSection;
    @FXML private VBox economySection;

    @FXML private Label lblTopFlight;
    @FXML private Label lblTopRoute;
    @FXML private Label lblTopDate;
    @FXML private Label lblTopTime;

    @FXML private Label lblSelectedSeat;
    @FXML private Label lblPassengerName;
    @FXML private Label lblTicketPrice;
    @FXML private Label lblSeatCharge;
    @FXML private Label lblTotal;

    @FXML private Button btnBack;

    private boolean isTopViewVisible = false;
    private Button currentlySelectedSeat = null;

    private String currentFlightNo;
    private String currentFrom;
    private String currentTo;
    private String currentDate;
    private String currentPrice;
    private int currentPassengers;
    private String currentPassengerName;

    private int currentFlightId = -1;
    private double ticketPriceValue = 0.0;

    // ==========================================
    // 🌟 Data လက်ခံခြင်း (MMK ဖြင့် Default Price သတ်မှတ်ခြင်း)
    // ==========================================
    public void setFlightData(String flightNo, String from, String to, String date, String price, int passengers, String passengerName) {
        this.currentFlightNo = flightNo;
        this.currentFrom = from;
        this.currentTo = to;
        this.currentDate = date;
        this.currentPrice = price; // ရှေ့ကပို့လိုက်တဲ့ String (အသုံးမပြုတော့ပါ)
        this.currentPassengers = passengers;
        this.currentPassengerName = passengerName;

        lblTopFlight.setText("Flight: " + flightNo);
        lblTopRoute.setText(from + " ➔ " + to);
        lblTopDate.setText("Date: " + date);
        lblPassengerName.setText(passengerName);

        try {
            // 🌟 ဈေးနှုန်းကို Economic Base Price အဖြစ် 980,000 MMK ဖြင့် အသေသတ်မှတ်မည်
            double basePrice = 980000.0;
            ticketPriceValue = basePrice * passengers;

            lblTicketPrice.setText(String.format("%,.0f MMK", ticketPriceValue));
            lblSeatCharge.setText("0 MMK");
            lblTotal.setText(String.format("%,.0f MMK", ticketPriceValue));
        } catch (Exception e) {
            ticketPriceValue = 980000.0;
        }

        fetchFlightDetailsAndBookedSeats();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        classComboBox.getItems().addAll("Business", "Premium Economic", "Economic");
        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(viewportPane.widthProperty());
        clipRect.heightProperty().bind(viewportPane.heightProperty());
        viewportPane.setClip(clipRect);

        classComboBox.setOnAction(e -> animatePlaneToSection(classComboBox.getValue()));
    }

    private void fetchFlightDetailsAndBookedSeats() {
        String flightQuery = "SELECT flight_id, TIME_FORMAT(departure_time, '%h:%i %p') as time_str FROM flights WHERE flight_number = ? AND DATE(departure_time) = ?";
        String seatsQuery = "SELECT seat_number FROM seats WHERE flight_id = ? AND is_available = false";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            try (PreparedStatement flightStmt = conn.prepareStatement(flightQuery)) {
                flightStmt.setString(1, currentFlightNo);
                flightStmt.setString(2, currentDate);
                try (ResultSet rs = flightStmt.executeQuery()) {
                    if (rs.next()) {
                        currentFlightId = rs.getInt("flight_id");
                        lblTopTime.setText("Time: " + rs.getString("time_str"));
                    }
                }
            }
            if (currentFlightId == -1) return;

            List<String> bookedSeats = new ArrayList<>();
            try (PreparedStatement seatStmt = conn.prepareStatement(seatsQuery)) {
                seatStmt.setInt(1, currentFlightId);
                try (ResultSet rs = seatStmt.executeQuery()) {
                    while (rs.next()) {
                        bookedSeats.add(rs.getString("seat_number"));
                    }
                }
            }
            disableBookedSeats(businessSection, bookedSeats);
            disableBookedSeats(premiumSection, bookedSeats);
            disableBookedSeats(economySection, bookedSeats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableBookedSeats(Pane parent, List<String> bookedSeats) {
        for (Node node : parent.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (bookedSeats.contains(btn.getText())) {
                    btn.getStyleClass().remove("seat-available");
                    btn.getStyleClass().add("seat-booked");
                    btn.setDisable(true);
                }
            } else if (node instanceof Pane) {
                disableBookedSeats((Pane) node, bookedSeats);
            }
        }
    }

    private void animatePlaneToSection(String selectedClass) {
        double targetY = 0;
        if (selectedClass.equals("Business")) targetY = -15;
        else if (selectedClass.equals("Premium Economic")) targetY = -(premiumSection.getLayoutY() - 30);
        else if (selectedClass.equals("Economic")) targetY = -(economySection.getLayoutY() - 30);

        ParallelTransition parallelTransition = new ParallelTransition();
        if (!isTopViewVisible) {
            FadeTransition fadeOutSide = new FadeTransition(Duration.millis(500), sideViewPlane);
            fadeOutSide.setToValue(0);
            FadeTransition fadeInTop = new FadeTransition(Duration.millis(800), topViewPlane);
            fadeInTop.setToValue(1);
            parallelTransition.getChildren().addAll(fadeOutSide, fadeInTop);
            isTopViewVisible = true;
        }

        TranslateTransition slidePlane = new TranslateTransition(Duration.millis(800), topViewPlane);
        slidePlane.setToY(targetY);
        parallelTransition.getChildren().add(slidePlane);
        parallelTransition.play();
    }

    // ==========================================
    // 🌟 Seat ရွေးချယ်မှုအလိုက် MMK ဖြင့် Charge တွက်ချက်ခြင်း
    // ==========================================
    @FXML
    void selectSeat(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if (clickedButton.getStyleClass().contains("seat-booked") || clickedButton.isDisabled()) return;

        if (currentlySelectedSeat != null) {
            currentlySelectedSeat.getStyleClass().remove("seat-selected");
            currentlySelectedSeat.getStyleClass().add("seat-available");
        }

        clickedButton.getStyleClass().remove("seat-available");
        clickedButton.getStyleClass().add("seat-selected");
        currentlySelectedSeat = clickedButton;

        String selectedClass = classComboBox.getValue() != null ? classComboBox.getValue() : "Economic";
        lblSelectedSeat.setText(selectedClass + " - " + clickedButton.getText());

        // 🌟 ဈေးနှုန်း Logic အသစ် (MMK ဖြင့်)
        double seatCharge = 0.0;
        if (selectedClass.equals("Business")) {
            seatCharge = 676000.0 * currentPassengers; // 980K + 676K = 1656K
        } else if (selectedClass.equals("Premium Economic")) {
            seatCharge = 418000.0 * currentPassengers; // 980K + 418K = 1398K
        } else {
            seatCharge = 0.0; // Economy ဆိုလျှင် Charge မရှိပါ (980K)
        }

        lblSeatCharge.setText(String.format("%,.0f MMK", seatCharge));
        double total = ticketPriceValue + seatCharge;
        lblTotal.setText(String.format("%,.0f MMK", total));
    }

    @FXML
    void clickBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffPassengerDetails.fxml"));
            Parent previousView = loader.load();
            StaffPassengerDetailsController controller = loader.getController();
            controller.setFlightData(currentFlightNo, currentFrom, currentTo, currentDate, currentPrice, currentPassengers);

            previousView.setScaleX(1.2);
            previousView.setScaleY(1.2);
            previousView.setOpacity(0.0);
            rootStackPane.getChildren().add(previousView);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer);
            scaleOut.setToX(0.8);
            scaleOut.setToY(0.8);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), previousView);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), previousView);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
            pt.setInterpolator(Interpolator.EASE_BOTH);
            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer));
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clickContinue(ActionEvent event) {
        if (currentlySelectedSeat == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Seat Selected");
            alert.setHeaderText(null);
            alert.setContentText("ကျေးဇူးပြု၍ ထိုင်ခုံတစ်ခု ရွေးချယ်ပါ။");
            alert.showAndWait();
            return;
        }

        String seatNumber = currentlySelectedSeat.getText();
        String seatClass = classComboBox.getValue();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database Connection မရရှိပါ။");

            boolean exists = false;
            boolean isAvailable = true;
            String checkQuery = "SELECT is_available FROM seats WHERE flight_id = ? AND seat_number = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, currentFlightId);
                checkStmt.setString(2, seatNumber);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                        isAvailable = rs.getBoolean("is_available");
                    }
                }
            }

            if (exists && !isAvailable) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Seat Unavailable");
                alert.setContentText("တောင်းပန်ပါသည်။ ဤခုံမှာ ယခုလေးတင် အခြားသူ ဝယ်ယူသွားပါပြီ။ အခြားခုံ ပြောင်းလဲရွေးချယ်ပေးပါ။");
                alert.showAndWait();

                currentlySelectedSeat.getStyleClass().remove("seat-selected");
                currentlySelectedSeat.getStyleClass().add("seat-booked");
                currentlySelectedSeat.setDisable(true);
                currentlySelectedSeat = null;
                lblSelectedSeat.setText("--");
                return;
            }

            if (exists) {
                String updateQuery = "UPDATE seats SET is_available = false, seat_class = ? WHERE flight_id = ? AND seat_number = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, seatClass);
                    updateStmt.setInt(2, currentFlightId);
                    updateStmt.setString(3, seatNumber);
                    updateStmt.executeUpdate();
                }
            } else {
                String insertQuery = "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) VALUES (?, ?, ?, false)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, currentFlightId);
                    insertStmt.setString(2, seatNumber);
                    insertStmt.setString(3, seatClass);
                    insertStmt.executeUpdate();
                }
            }

            String timeString = lblTopTime.getText().replace("Time: ", "").trim();
            loadBoardingPassPage(seatClass, timeString, seatNumber);

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("ထိုင်ခုံသိမ်းဆည်းရာတွင် အမှားအယွင်းရှိနေပါသည်။");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void loadBoardingPassPage(String seatClass, String timeString, String seatNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffBoardingPass.fxml"));
            Parent modalView = loader.load();

            StaffBoardingPassController controller = loader.getController();
            controller.setBoardingPassData(
                    currentPassengerName,
                    seatClass,
                    currentFrom,
                    currentTo,
                    currentDate,
                    timeString,
                    currentFlightNo,
                    seatNumber
            );

            modalView.setScaleX(0.5);
            modalView.setScaleY(0.5);
            modalView.setOpacity(0.0);
            rootStackPane.getChildren().add(modalView);

            GaussianBlur blur = new GaussianBlur(0);
            mainContainer.setEffect(blur);
            Timeline blurTimeline = new Timeline(
                    new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 15))
            );
            blurTimeline.play();

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), modalView);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn);
            pt.setInterpolator(Interpolator.EASE_OUT);
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}