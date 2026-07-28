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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

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
    @FXML private Button btnContinue;

    private boolean isTopViewVisible = false;
    private Button currentlySelectedSeat = null;

    private String currentFlightNo;
    private String currentFrom;
    private String currentTo;
    private String currentDate;
    private String currentPrice;
    private int currentPassengers = 1;
    private String currentPassengerName;
    private int currentFlightId = -1;
    private double ticketPriceValue = 0.0;
    private double currentTotalPrice = 0.0;

    private int currentPassengerId = 1;
    private int currentStaffId = 4;

    private boolean isViewOnlyMode = false;

    public void setViewOnlyMode(boolean isViewOnly) {
        this.isViewOnlyMode = isViewOnly;

        if (btnContinue != null) {
            if (isViewOnly) {
                btnContinue.setVisible(false);
                btnContinue.setManaged(false);
            } else {
                btnContinue.setVisible(true);
                btnContinue.setManaged(true);
            }
        }
    }

    public void setFlightData(String flightNo, String from, String to, String date, String price, int passengers, String passengerName) {
        this.currentFlightNo = flightNo;
        this.currentFrom = from;
        this.currentTo = to;
        this.currentDate = date;
        this.currentPassengers = passengers;
        this.currentPassengerName = passengerName;

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
            lblTicketPrice.setText("--");
            lblSeatCharge.setText("--");
            lblTotal.setText("--");
        }
        fetchFlightDetailsAndBookedSeats();
    }

    public void setBookingIds(int passengerId, int staffId) {
        this.currentPassengerId = passengerId;
        this.currentStaffId = staffId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        classComboBox.getItems().addAll("Business", "Premium Economic", "Economic");
        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(viewportPane.widthProperty());
        clipRect.heightProperty().bind(viewportPane.heightProperty());
        viewportPane.setClip(clipRect);

        // 🌟 အပိုင်းတစ်ခုစီအတွက် ၆ တန်းဖန်တီးခြင်း (Class အမည်ကိုပါ ထည့်ပေးထားသည်) 🌟
        generateSeats(businessSection, 6, "Business");
        generateSeats(premiumSection, 6, "Premium Economic");
        generateSeats(economySection, 6, "Economic");

        classComboBox.setOnAction(e -> animatePlaneToSection(classComboBox.getValue()));
    }

    private void generateSeats(VBox section, int numRows, String className) {
        section.getChildren().removeIf(node -> !(node instanceof Label));
        section.setSpacing(15);

        for (int i = 1; i <= numRows; i++) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER);

            Button seatA = createSeatButton(i + "A", className);
            Button seatB = createSeatButton(i + "B", className);

            Label rowNumberLabel = new Label(String.valueOf(i));
            rowNumberLabel.setPrefWidth(30);
            rowNumberLabel.setAlignment(Pos.CENTER);
            rowNumberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            Button seatC = createSeatButton(i + "C", className);
            Button seatD = createSeatButton(i + "D", className);

            row.getChildren().addAll(seatA, seatB, rowNumberLabel, seatC, seatD);
            section.getChildren().add(row);
        }
    }

    private Button createSeatButton(String seatNumber, String className) {
        Button btn = new Button(seatNumber);
        // 🌟 နောက်ကွယ်တွင် Class နဲ့ Seat Number ကို တွဲမှတ်ထားမည် (ဥပမာ- "Business_1A") 🌟
        btn.setUserData(className + "_" + seatNumber);
        btn.setPrefSize(45, 45);
        btn.getStyleClass().add("seat-available");
        btn.setStyle("-fx-cursor: hand; -fx-background-radius: 5;");
        btn.setOnAction(this::selectSeat);
        return btn;
    }

    private void fetchFlightDetailsAndBookedSeats() {
        String flightQuery = "SELECT flight_id, TIME_FORMAT(departure_time, '%h:%i %p') as time_str FROM flights WHERE flight_number = ? AND DATE(departure_time) = ?";
        // 🌟 Seat အပြင် Class ကိုပါ ယူမည် 🌟
        String seatsQuery = "SELECT seat_number, seat_class FROM seats WHERE flight_id = ? AND is_available = false";

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
                        // 🌟 Database မှ ရလာသော "Business" နှင့် "1A" ကို ပေါင်းပြီး "Business_1A" ပုံစံ မှတ်သားမည် 🌟
                        bookedSeats.add(rs.getString("seat_class") + "_" + rs.getString("seat_number"));
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
                if (btn.getUserData() != null && bookedSeats.contains(btn.getUserData().toString())) {
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

        // 🌟 ရွေးချယ်လိုက်သော Button ၏ UserData (ဥပမာ- Business_1A) မှ ခွဲထုတ်ယူမည် 🌟
        String[] seatData = clickedButton.getUserData().toString().split("_");
        String selectedClass = seatData[0]; // Business

        lblSelectedSeat.setText(selectedClass + " - " + clickedButton.getText());

        double seatCharge = 0.0;
        if (selectedClass.equals("Business")) seatCharge = 676000.0 * currentPassengers;
        else if (selectedClass.equals("Premium Economic")) seatCharge = 418000.0 * currentPassengers;

        lblSeatCharge.setText(String.format("%,.0f MMK", seatCharge));
        currentTotalPrice = ticketPriceValue + seatCharge;
        lblTotal.setText(String.format("%,.0f MMK", currentTotalPrice));
    }

    @FXML
    void clickBack(ActionEvent event) {
        try {
            String targetFxml = isViewOnlyMode
                    ? "/com/pos/flightmanagementsystem/view/StaffFlightSchedules.fxml"
                    : "/com/pos/flightmanagementsystem/view/StaffCreateNewBooking.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
            Parent previousView = loader.load();

            Pane parentContainer = (Pane) rootStackPane.getParent();
            if (parentContainer != null) {
                if (parentContainer instanceof BorderPane) {
                    ((BorderPane) parentContainer).setCenter(previousView);
                } else {
                    int currentIndex = parentContainer.getChildren().indexOf(rootStackPane);
                    if (currentIndex != -1) {
                        parentContainer.getChildren().set(currentIndex, previousView);

                        if (parentContainer instanceof HBox) {
                            HBox.setHgrow(previousView, Priority.ALWAYS);
                        } else if (parentContainer instanceof VBox) {
                            VBox.setVgrow(previousView, Priority.ALWAYS);
                        } else if (parentContainer instanceof AnchorPane) {
                            AnchorPane.setTopAnchor(previousView, 0.0);
                            AnchorPane.setBottomAnchor(previousView, 0.0);
                            AnchorPane.setLeftAnchor(previousView, 0.0);
                            AnchorPane.setRightAnchor(previousView, 0.0);
                        }
                    }
                }
            }
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

        // 🌟 UserData မှ Class နှင့် Seat အတိအကျကို ပြန်ခွဲထုတ်ယူမည် 🌟
        String[] seatData = currentlySelectedSeat.getUserData().toString().split("_");
        String seatClass = seatData[0];      // ဥပမာ- "Business"
        String dbSeatNumber = seatData[1];   // ဥပမာ- "1A" (Database တွင် သိမ်းရန်)

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database Connection မရရှိပါ။");
            conn.setAutoCommit(false);

            try {
                int totalBookedSeats = 0;
                String countQuery = "SELECT COUNT(*) FROM seats WHERE flight_id = ? AND is_available = false";
                try (PreparedStatement countStmt = conn.prepareStatement(countQuery)) {
                    countStmt.setInt(1, currentFlightId);
                    try (ResultSet rsCount = countStmt.executeQuery()) {
                        if (rsCount.next()) totalBookedSeats = rsCount.getInt(1);
                    }
                }

                if (totalBookedSeats >= 72) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Flight Full");
                    alert.setContentText("တောင်းပန်ပါသည်။ ဤလေယာဉ်တွင် ထိုင်ခုံ ၇၂ ခုံလုံး ပြည့်သွားပါပြီ။ ထပ်မံရွေးချယ်၍ မရနိုင်ပါ။");
                    alert.showAndWait();
                    conn.rollback();
                    return;
                }

                boolean exists = false;
                boolean isAvailable = true;
                int currentSeatId = -1;

                // 🌟 Duplicate မဖြစ်စေရန် Seat Number ရော Seat Class ပါ နှစ်ခုလုံးကို တိုက်စစ်မည် 🌟
                String checkQuery = "SELECT seat_id, is_available FROM seats WHERE flight_id = ? AND seat_number = ? AND seat_class = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, currentFlightId);
                    checkStmt.setString(2, dbSeatNumber);
                    checkStmt.setString(3, seatClass);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            exists = true;
                            currentSeatId = rs.getInt("seat_id");
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
                    conn.rollback();
                    return;
                }

                if (exists) {
                    // 🌟 Update လုပ်လျှင်လည်း Class ပါ စစ်ဆေးမည် 🌟
                    String updateQuery = "UPDATE seats SET is_available = false WHERE flight_id = ? AND seat_number = ? AND seat_class = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, currentFlightId);
                        updateStmt.setString(2, dbSeatNumber);
                        updateStmt.setString(3, seatClass);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // 🌟 အသစ် Insert လုပ်လျှင် DB ၌ "1A" ဆိုသည့် အမည်ဖြင့်သာ သိမ်းဆည်းမည် 🌟
                    String insertQuery = "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) VALUES (?, ?, ?, false)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setInt(1, currentFlightId);
                        insertStmt.setString(2, dbSeatNumber);
                        insertStmt.setString(3, seatClass);
                        insertStmt.executeUpdate();
                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) currentSeatId = generatedKeys.getInt(1);
                        }
                    }
                }

                // 🌟 Bookings Table သို့ သိမ်းဆည်းခြင်း (Seat ID ကို အသုံးပြုပါသည်) 🌟
                String pnrCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                String bookingQuery = "INSERT INTO bookings (pnr_code, flight_id, passenger_id, seat_id, staff_id, total_price, payment_method, status, booking_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
                try (PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery)) {
                    bookingStmt.setString(1, pnrCode);
                    bookingStmt.setInt(2, currentFlightId);
                    bookingStmt.setInt(3, this.currentPassengerId);
                    bookingStmt.setInt(4, currentSeatId);
                    bookingStmt.setInt(5, this.currentStaffId);
                    bookingStmt.setDouble(6, currentTotalPrice);
                    bookingStmt.setString(7, "Cash");
                    bookingStmt.setString(8, "Confirmed");
                    bookingStmt.executeUpdate();
                }

                conn.commit();
                String timeString = lblTopTime.getText().replace("Time: ", "").trim();
                loadBoardingPassPage(seatClass, timeString, dbSeatNumber);

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setContentText("Booking သိမ်းဆည်းရာတွင် အမှားအယွင်းရှိနေပါသည်။\n" + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void loadBoardingPassPage(String seatClass, String timeString, String seatNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffBoardingPass.fxml"));
            Parent modalView = loader.load();
            StaffBoardingPassController controller = loader.getController();
            controller.setBoardingPassData(currentPassengerName, seatClass, currentFrom, currentTo, currentDate, timeString, currentFlightNo, seatNumber);

            modalView.setScaleX(0.5);
            modalView.setScaleY(0.5);
            modalView.setOpacity(0.0);
            rootStackPane.getChildren().add(modalView);

            GaussianBlur blur = new GaussianBlur(0);
            mainContainer.setEffect(blur);
            Timeline blurTimeline = new Timeline(new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 15)));
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