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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StaffSeatSelectionController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private ComboBox<String> classComboBox;

    @FXML private Pane viewportPane;
    @FXML private VBox sideViewPlane;
    @FXML private StackPane topViewPlane;

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
    @FXML private Button btnContinue;

    private boolean isTopViewVisible = false;
    private List<Button> selectedSeatsList = new ArrayList<>();
    private double totalSeatCharge = 0.0;

    private String currentFlightNo, currentFrom, currentTo, currentDate, currentPrice;
    private int currentPassengers = 1;
    private int currentFlightId = -1;

    private double ticketPriceValue = 0.0;
    private double currentTotalPrice = 0.0;
    private boolean isViewOnlyMode = false;

    // 🌟 List ပြောင်းထားသည် 🌟
    private boolean isChangeFlightMode = false;
    private List<Integer> oldBookingIds;

    class SeatData {
        String seatNumber, seatClass; boolean isAvailable;
        SeatData(String seatNumber, String seatClass, boolean isAvailable) { this.seatNumber = seatNumber; this.seatClass = seatClass; this.isAvailable = isAvailable; }
    }

    public void setViewOnlyMode(boolean isViewOnly) {
        this.isViewOnlyMode = isViewOnly;
        if (btnContinue != null) { btnContinue.setVisible(!isViewOnly); btnContinue.setManaged(!isViewOnly); }
    }

    public void initChangeFlightMode(List<Integer> oldBookingIds) {
        this.isChangeFlightMode = true;
        this.oldBookingIds = oldBookingIds;
    }

    public void setFlightData(String flightNo, String from, String to, String date, String price, int passengers) {
        this.currentFlightNo = flightNo; this.currentFrom = from; this.currentTo = to; this.currentDate = date; this.currentPrice = price; this.currentPassengers = passengers;
        lblTopFlight.setText("Flight: " + flightNo); lblTopRoute.setText(from + " ➔ " + to); lblTopDate.setText("Date: " + date); lblPassengerName.setText(passengers + " Pax");
        try {
            String numericPriceStr = price.replaceAll("[^\\d.]", ""); double basePrice = Double.parseDouble(numericPriceStr);
            ticketPriceValue = basePrice * passengers; currentTotalPrice = ticketPriceValue;
            lblTicketPrice.setText(String.format("%,.0f MMK", ticketPriceValue)); lblSeatCharge.setText("0 MMK"); lblTotal.setText(String.format("%,.0f MMK", ticketPriceValue));
        } catch (Exception e) { lblTicketPrice.setText("--"); lblSeatCharge.setText("--"); lblTotal.setText("--"); }
        fetchFlightDetailsAndBookedSeats();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Rectangle clipRect = new Rectangle(); clipRect.widthProperty().bind(viewportPane.widthProperty()); clipRect.heightProperty().bind(viewportPane.heightProperty()); viewportPane.setClip(clipRect);
        classComboBox.setOnAction(e -> animatePlaneToSection(classComboBox.getValue()));
    }

    private void fetchFlightDetailsAndBookedSeats() {
        String flightQuery = "SELECT flight_id, TIME_FORMAT(departure_time, '%h:%i %p') as time_str FROM flights WHERE flight_number = ? AND DATE(departure_time) = ?";
        String seatsQuery = "SELECT seat_number, seat_class, is_available FROM seats WHERE flight_id = ? ORDER BY seat_id ASC";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            try (PreparedStatement flightStmt = conn.prepareStatement(flightQuery)) {
                flightStmt.setString(1, currentFlightNo); flightStmt.setString(2, currentDate);
                try (ResultSet rs = flightStmt.executeQuery()) { if (rs.next()) { currentFlightId = rs.getInt("flight_id"); lblTopTime.setText("Time: " + rs.getString("time_str")); } }
            }
            if (currentFlightId == -1) return;
            Map<String, Map<Integer, List<SeatData>>> seatMap = new LinkedHashMap<>();
            try (PreparedStatement seatStmt = conn.prepareStatement(seatsQuery)) {
                seatStmt.setInt(1, currentFlightId);
                try (ResultSet rs = seatStmt.executeQuery()) {
                    while (rs.next()) {
                        String sNum = rs.getString("seat_number"); String sClass = rs.getString("seat_class"); boolean isAvail = rs.getBoolean("is_available");
                        int rowNum = extractRowNumber(sNum);
                        seatMap.putIfAbsent(sClass, new LinkedHashMap<>()); seatMap.get(sClass).putIfAbsent(rowNum, new ArrayList<>()); seatMap.get(sClass).get(rowNum).add(new SeatData(sNum, sClass, isAvail));
                    }
                }
            }
            Platform.runLater(() -> buildDynamicSeatUI(seatMap));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int extractRowNumber(String seatNum) { String num = seatNum.replaceAll("[^0-9]", ""); return num.isEmpty() ? 0 : Integer.parseInt(num); }

    private void buildDynamicSeatUI(Map<String, Map<Integer, List<SeatData>>> seatMap) {
        businessSection.getChildren().clear(); premiumSection.getChildren().clear(); economySection.getChildren().clear(); classComboBox.getItems().clear();
        for (String className : seatMap.keySet()) {
            classComboBox.getItems().add(className); VBox targetSection = getSectionForClass(className);
            if (targetSection != businessSection) { Separator sep = new Separator(); sep.getStyleClass().add("class-separator"); targetSection.getChildren().add(sep); }
            Label titleLbl = new Label(className); titleLbl.getStyleClass().add("class-title"); targetSection.getChildren().add(titleLbl); targetSection.setSpacing(15);
            Map<Integer, List<SeatData>> rows = seatMap.get(className);
            for (Integer rowNum : rows.keySet()) {
                HBox rowBox = new HBox(15); rowBox.setAlignment(Pos.CENTER);
                List<SeatData> seatsInRow = rows.get(rowNum);
                HBox leftBox = new HBox(15); leftBox.setAlignment(Pos.CENTER_RIGHT); leftBox.setPrefWidth(105);
                HBox rightBox = new HBox(15); rightBox.setAlignment(Pos.CENTER_LEFT); rightBox.setPrefWidth(105);
                for (SeatData seat : seatsInRow) {
                    Button btn = createDynamicSeatButton(seat);
                    if (seat.seatNumber.contains("A") || seat.seatNumber.contains("B")) leftBox.getChildren().add(btn); else rightBox.getChildren().add(btn);
                }
                Label rowLbl = new Label(String.valueOf(rowNum)); rowLbl.setPrefWidth(30); rowLbl.setAlignment(Pos.CENTER); rowLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
                rowBox.getChildren().addAll(leftBox, rowLbl, rightBox); targetSection.getChildren().add(rowBox);
            }
        }
        businessSection.setVisible(!businessSection.getChildren().isEmpty()); businessSection.setManaged(!businessSection.getChildren().isEmpty());
        premiumSection.setVisible(!premiumSection.getChildren().isEmpty()); premiumSection.setManaged(!premiumSection.getChildren().isEmpty());
        economySection.setVisible(!economySection.getChildren().isEmpty()); economySection.setManaged(!economySection.getChildren().isEmpty());
        if (!classComboBox.getItems().isEmpty()) { classComboBox.getSelectionModel().selectFirst(); Platform.runLater(() -> animatePlaneToSection(classComboBox.getValue())); }
    }

    private VBox getSectionForClass(String className) { String lower = className.toLowerCase(); if (lower.contains("business")) return businessSection; if (lower.contains("premium")) return premiumSection; return economySection; }

    private Button createDynamicSeatButton(SeatData seat) {
        Button btn = new Button(seat.seatNumber); btn.setUserData(seat.seatClass + "_" + seat.seatNumber); btn.setPrefSize(45, 45); btn.setStyle("-fx-cursor: hand; -fx-background-radius: 5;");
        if (seat.isAvailable) { btn.getStyleClass().add("seat-available"); btn.setOnAction(this::selectSeat); } else { btn.getStyleClass().add("seat-booked"); btn.setDisable(true); }
        return btn;
    }

    private void animatePlaneToSection(String selectedClass) {
        if (selectedClass == null) return; topViewPlane.applyCss(); topViewPlane.layout();
        double targetY = 0; String lower = selectedClass.toLowerCase();
        if (lower.contains("business")) targetY = -15; else if (lower.contains("premium")) targetY = -(premiumSection.getLayoutY() - 30); else targetY = -(economySection.getLayoutY() - 30);
        ParallelTransition pt = new ParallelTransition();
        if (!isTopViewVisible) {
            FadeTransition fadeOutSide = new FadeTransition(Duration.millis(500), sideViewPlane); fadeOutSide.setToValue(0);
            FadeTransition fadeInTop = new FadeTransition(Duration.millis(800), topViewPlane); fadeInTop.setToValue(1);
            pt.getChildren().addAll(fadeOutSide, fadeInTop); isTopViewVisible = true;
        }
        TranslateTransition slidePlane = new TranslateTransition(Duration.millis(800), topViewPlane); slidePlane.setToY(targetY); pt.getChildren().add(slidePlane); pt.play();
    }

    @FXML void selectSeat(ActionEvent event) {
        Button clickedButton = (Button) event.getSource(); if (clickedButton.getStyleClass().contains("seat-booked") || clickedButton.isDisabled()) return;
        String[] seatData = clickedButton.getUserData().toString().split("_"); String selectedClass = seatData[0];
        double thisSeatCharge = 0.0; if (selectedClass.toLowerCase().contains("business")) thisSeatCharge = 676000.0; else if (selectedClass.toLowerCase().contains("premium")) thisSeatCharge = 418000.0;
        if (selectedSeatsList.contains(clickedButton)) {
            selectedSeatsList.remove(clickedButton); clickedButton.getStyleClass().remove("seat-selected"); clickedButton.getStyleClass().add("seat-available"); totalSeatCharge -= thisSeatCharge;
        } else {
            if (selectedSeatsList.size() >= currentPassengers) { showWarningModal("Maximum Seats Reached", "You can only select " + currentPassengers + " seat(s)."); return; }
            selectedSeatsList.add(clickedButton); clickedButton.getStyleClass().remove("seat-available"); clickedButton.getStyleClass().add("seat-selected"); totalSeatCharge += thisSeatCharge;
        }
        updateSummaryLabels();
    }

    private void updateSummaryLabels() {
        if (selectedSeatsList.isEmpty()) lblSelectedSeat.setText("--"); else { List<String> seatNumbers = selectedSeatsList.stream().map(Button::getText).collect(Collectors.toList()); lblSelectedSeat.setText(String.join(", ", seatNumbers)); }
        lblSeatCharge.setText(String.format("%,.0f MMK", totalSeatCharge)); currentTotalPrice = ticketPriceValue + totalSeatCharge; lblTotal.setText(String.format("%,.0f MMK", currentTotalPrice));
    }

    @FXML void clickBack(ActionEvent event) {
        try {
            String targetFxml = isViewOnlyMode ? "/com/pos/flightmanagementsystem/view/StaffFlightSchedules.fxml" : "/com/pos/flightmanagementsystem/view/StaffCreateNewBooking.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml)); Parent previousView = loader.load(); applyThemeRecursively(previousView, checkIsDarkMode());

            if (targetFxml.contains("StaffCreateNewBooking") && isChangeFlightMode) {
                StaffCreateNewBookingController controller = loader.getController();
                controller.initChangeFlightMode(oldBookingIds);
            }

            Pane parentContainer = (Pane) rootStackPane.getParent();
            if (parentContainer != null) {
                previousView.setScaleX(0.8); previousView.setScaleY(0.8); previousView.setOpacity(0.0); parentContainer.getChildren().add(0, previousView);
                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), rootStackPane); scaleOut.setToX(1.2); scaleOut.setToY(1.2); FadeTransition fadeOut = new FadeTransition(Duration.millis(400), rootStackPane); fadeOut.setToValue(0.0);
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), previousView); scaleIn.setToX(1.0); scaleIn.setToY(1.0); FadeTransition fadeIn = new FadeTransition(Duration.millis(400), previousView); fadeIn.setToValue(1.0);
                ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn); pt.setInterpolator(Interpolator.EASE_BOTH); pt.setOnFinished(e -> parentContainer.getChildren().remove(rootStackPane)); pt.play();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void applyThemeRecursively(Parent node, boolean isDarkMode) {
        if (node.getStylesheets() != null && !node.getStylesheets().isEmpty()) { ObservableList<String> styles = node.getStylesheets(); for (int i = 0; i < styles.size(); i++) { String css = styles.get(i); if (isDarkMode && css.contains("Light")) styles.set(i, css.replace("Light", "Dark")); else if (!isDarkMode && css.contains("Dark")) styles.set(i, css.replace("Dark", "Light")); } }
        for (javafx.scene.Node child : node.getChildrenUnmodifiable()) { if (child instanceof Parent) applyThemeRecursively((Parent) child, isDarkMode); }
    }
    private boolean checkIsDarkMode() { boolean isDarkMode = true; Parent current = rootStackPane; while (current != null) { String currentStyles = current.getStylesheets().toString(); if (currentStyles.contains("Dark")) return true; else if (currentStyles.contains("Light")) return false; current = current.getParent(); } return isDarkMode; }

    @FXML void clickContinue(ActionEvent event) {
        if (selectedSeatsList.size() < currentPassengers) { showWarningModal("Incomplete Seat Selection", "Please select exactly " + currentPassengers + " seat(s) for your passengers."); return; }
        loadPassengerDetailsPage();
    }

    private void showWarningModal(String title, String message) {
        // ... (Warning Modal as before) ...
        StackPane overlay = new StackPane(); overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); BoxBlur blur = new BoxBlur(8, 8, 3); mainContainer.setEffect(blur);
        VBox modalCard = new VBox(20); modalCard.setMaxSize(420, Region.USE_PREF_SIZE); modalCard.setStyle("-fx-background-color: #0B0E14; -fx-border-color: #F59E0B; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 24px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 10);");
        HBox header = new HBox(); header.setAlignment(Pos.CENTER_LEFT); Label titleLabel = new Label(title); titleLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 16px; -fx-font-weight: bold;"); Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); StackPane closeBtn = new StackPane(); SVGPath closeIcon = new SVGPath(); closeIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"); closeIcon.setStyle("-fx-fill: #8B95A5; -fx-scale-x: 0.8; -fx-scale-y: 0.8;"); closeBtn.getChildren().add(closeIcon); closeBtn.setCursor(Cursor.HAND); closeBtn.setOnMouseEntered(e -> closeIcon.setStyle("-fx-fill: #FFFFFF; -fx-scale-x: 0.8; -fx-scale-y: 0.8;")); closeBtn.setOnMouseExited(e -> closeIcon.setStyle("-fx-fill: #8B95A5; -fx-scale-x: 0.8; -fx-scale-y: 0.8;")); header.getChildren().addAll(titleLabel, spacer, closeBtn);
        HBox body = new HBox(15); body.setAlignment(Pos.CENTER_LEFT); SVGPath warningIcon = new SVGPath(); warningIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"); warningIcon.setStyle("-fx-fill: #F59E0B; -fx-scale-x: 1.5; -fx-scale-y: 1.5;"); StackPane iconPane = new StackPane(warningIcon); iconPane.setPadding(new Insets(0, 10, 0, 10)); Label messageLabel = new Label(message); messageLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;"); messageLabel.setWrapText(true); messageLabel.setMaxWidth(300); body.getChildren().addAll(iconPane, messageLabel);
        HBox footer = new HBox(); footer.setAlignment(Pos.CENTER_RIGHT); Button okBtn = new Button("OK"); okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #F59E0B; -fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;"); okBtn.setOnMouseEntered(e -> okBtn.setStyle("-fx-background-color: rgba(245, 158, 11, 0.15); -fx-border-color: #F59E0B; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;")); okBtn.setOnMouseExited(e -> okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #F59E0B; -fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-border-radius: 6px; -background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;")); footer.getChildren().add(okBtn);
        modalCard.getChildren().addAll(header, body, footer); overlay.getChildren().add(modalCard); rootStackPane.getChildren().add(overlay); modalCard.setScaleX(0.5); modalCard.setScaleY(0.5); modalCard.setOpacity(0); overlay.setOpacity(0);
        ScaleTransition stIn = new ScaleTransition(Duration.millis(300), modalCard); stIn.setToX(1); stIn.setToY(1); stIn.setInterpolator(Interpolator.EASE_OUT); FadeTransition ftIn = new FadeTransition(Duration.millis(300), modalCard); ftIn.setToValue(1); FadeTransition bgFtIn = new FadeTransition(Duration.millis(300), overlay); bgFtIn.setToValue(1); ParallelTransition ptIn = new ParallelTransition(stIn, ftIn, bgFtIn); ptIn.play();
        Runnable closeAction = () -> { ScaleTransition stOut = new ScaleTransition(Duration.millis(200), modalCard); stOut.setToX(0.5); stOut.setToY(0.5); stOut.setInterpolator(Interpolator.EASE_IN); FadeTransition ftOut = new FadeTransition(Duration.millis(200), modalCard); ftOut.setToValue(0); FadeTransition bgFtOut = new FadeTransition(Duration.millis(200), overlay); bgFtOut.setToValue(0); ParallelTransition ptOut = new ParallelTransition(stOut, ftOut, bgFtOut); ptOut.setOnFinished(ev -> { rootStackPane.getChildren().remove(overlay); mainContainer.setEffect(null); }); ptOut.play(); };
        closeBtn.setOnMouseClicked(e -> closeAction.run()); okBtn.setOnAction(e -> closeAction.run());
    }

    private void loadPassengerDetailsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffPassengerDetails.fxml"));
            Parent nextView = loader.load(); applyThemeRecursively(nextView, checkIsDarkMode());
            StaffPassengerDetailsController controller = loader.getController();

            List<String> seatStrings = selectedSeatsList.stream().map(Button::getText).collect(Collectors.toList());
            String timeString = lblTopTime.getText().replace("Time: ", "").trim();
            controller.setBookingData( currentFlightNo, currentFrom, currentTo, currentDate, timeString, seatStrings, currentTotalPrice, currentPassengers );

            if (isChangeFlightMode) {
                controller.initChangeFlightMode(oldBookingIds);
            }

            nextView.setScaleX(0.8); nextView.setScaleY(0.8); nextView.setOpacity(0.0); rootStackPane.getChildren().add(nextView);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer); scaleOut.setToX(1.2); scaleOut.setToY(1.2); FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer); fadeOut.setToValue(0.0);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView); scaleIn.setToX(1.0); scaleIn.setToY(1.0); FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView); fadeIn.setToValue(1.0);
            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn); pt.setInterpolator(Interpolator.EASE_BOTH); pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer)); pt.play();
        } catch (Exception e) { e.printStackTrace(); }
    }
}