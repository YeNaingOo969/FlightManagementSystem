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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StaffSeatSelectionController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private ComboBox<String> classComboBox;
    @FXML private ComboBox<String> cmbPaymentMethod;

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
    private double currentTotalPrice = 0.0;

    private int currentPassengerId;
    private int currentStaffId;

    public void setFlightData(String flightNo, String from, String to, String date, String price, int passengers, String passengerName, int passengerId) {
        this.currentFlightNo = flightNo;
        this.currentFrom = from;
        this.currentTo = to;
        this.currentDate = date;
        this.currentPrice = price;
        this.currentPassengers = passengers;
        this.currentPassengerName = passengerName;
        this.currentPassengerId = passengerId;

        if (LoginController.loggedInUser != null) {
            this.currentStaffId = LoginController.loggedInUser.getUserId();
        } else {
            this.currentStaffId = 1;
        }

        lblTopFlight.setText("Flight: " + flightNo);
        lblTopRoute.setText(from + " ➔ " + to);
        lblTopDate.setText("Date: " + date);
        lblPassengerName.setText(passengerName);

        try {
            double basePrice = 980000.0;
            ticketPriceValue = basePrice * passengers;
            currentTotalPrice = ticketPriceValue;

            lblTicketPrice.setText(String.format("%,.0f MMK", ticketPriceValue));
            lblSeatCharge.setText("0 MMK");
            lblTotal.setText(String.format("%,.0f MMK", ticketPriceValue));
        } catch (Exception e) {
            ticketPriceValue = 980000.0;
            currentTotalPrice = ticketPriceValue;
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

        setupPaymentMethods();
    }

    private void setupPaymentMethods() {
        ObservableList<String> paymentOptions = FXCollections.observableArrayList(
                "Cash", "KBZ Pay", "Wave Pay", "CB Pay", "AYA Pay", "Credit/Debit Card"
        );
        cmbPaymentMethod.setItems(paymentOptions);
        cmbPaymentMethod.getSelectionModel().selectFirst();

        Callback<ListView<String>, ListCell<String>> cellFactory = param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(12);
                    box.setAlignment(Pos.CENTER_LEFT);
                    SVGPath icon = new SVGPath();

                    switch (item) {
                        case "Cash":
                            icon.setContent("M2 4v16h20V4H2zm18 14H4V6h16v12zM12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zm0 8c-1.65 0-3-1.35-3-3s1.35-3 3-3 3 1.35 3 3-1.35 3-3 3z");
                            icon.setFill(Color.web("#10B981")); // Green
                            break;
                        case "KBZ Pay":
                            icon.setContent("M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z");
                            icon.setFill(Color.web("#2196F3")); // Blue
                            break;
                        case "Wave Pay":
                            icon.setContent("M2 12c2 0 2-2 4-2s2 2 4 2 2-2 4-2 2 2 4 2v6H2v-6z");
                            icon.setFill(Color.web("#FFC107")); // Yellow
                            break;
                        case "CB Pay":
                            icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z");
                            icon.setFill(Color.web("#FF9800")); // Orange
                            break;
                        case "AYA Pay":
                            icon.setContent("M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");
                            icon.setFill(Color.web("#E53935")); // Red
                            break;
                        case "Credit/Debit Card":
                            icon.setContent("M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z");
                            icon.setFill(Color.web("#9C27B0")); // Purple
                            break;
                    }
                    icon.setScaleX(0.9);
                    icon.setScaleY(0.9);

                    Label label = new Label(item);
                    // 🌟 Light Mode တွင် စာသားကို ရှင်းလင်းစွာ မြင်ရစေရန် #1E293B သို့ ပြောင်းထားပါသည် 🌟
                    label.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 14px; -fx-font-weight: bold;");
                    box.getChildren().addAll(icon, label);
                    setGraphic(box);
                }
            }
        };

        cmbPaymentMethod.setCellFactory(cellFactory);
        cmbPaymentMethod.setButtonCell(cellFactory.call(null));
    }

    private void applyThemeRecursively(Parent node, boolean isDarkMode) {
        if (node.getStylesheets() != null && !node.getStylesheets().isEmpty()) {
            ObservableList<String> styles = node.getStylesheets();
            for (int i = 0; i < styles.size(); i++) {
                String css = styles.get(i);
                if (isDarkMode && css.contains("Light")) {
                    styles.set(i, css.replace("Light", "Dark"));
                } else if (!isDarkMode && css.contains("Dark")) {
                    styles.set(i, css.replace("Dark", "Light"));
                }
            }
        }
        for (Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                applyThemeRecursively((Parent) child, isDarkMode);
            }
        }
    }

    private boolean checkIsDarkMode() {
        boolean isDarkMode = true;
        Parent current = rootStackPane;
        while (current != null) {
            String currentStyles = current.getStylesheets().toString();
            if (currentStyles.contains("Dark")) {
                return true;
            } else if (currentStyles.contains("Light")) {
                return false;
            }
            current = current.getParent();
        }
        return isDarkMode;
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

        double seatCharge = 0.0;
        if (selectedClass.equals("Business")) {
            seatCharge = 676000.0 * currentPassengers;
        } else if (selectedClass.equals("Premium Economic")) {
            seatCharge = 418000.0 * currentPassengers;
        } else {
            seatCharge = 0.0;
        }

        lblSeatCharge.setText(String.format("%,.0f MMK", seatCharge));
        currentTotalPrice = ticketPriceValue + seatCharge;
        lblTotal.setText(String.format("%,.0f MMK", currentTotalPrice));
    }

    @FXML
    void clickBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffPassengerDetails.fxml"));
            Parent previousView = loader.load();

            applyThemeRecursively(previousView, checkIsDarkMode());

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
        String timeString = lblTopTime.getText().replace("Time: ", "").trim();

        String selectedPayment = cmbPaymentMethod.getValue();
        if (selectedPayment == null) {
            selectedPayment = "Cash";
        }

        loadBoardingPassPage(seatClass, timeString, seatNumber, selectedPayment);
    }

    private void loadBoardingPassPage(String seatClass, String timeString, String seatNumber, String paymentMethod) {
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

            controller.setBookingDetailsForDatabase(currentFlightId, currentPassengerId, currentStaffId, currentTotalPrice, paymentMethod);

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