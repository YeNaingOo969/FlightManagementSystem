package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

public class StaffBoardingPassController {

    @FXML private StackPane modalRoot;

    @FXML private Label lblClass;
    @FXML private Label lblPassengerName;
    @FXML private Label lblFrom;
    @FXML private Label lblTo;
    @FXML private Label lblDate;
    @FXML private Label lblTime;
    @FXML private Label lblFlight;
    @FXML private Label lblSeat;

    @FXML private Label lblPaymentMethod;

    private int flightId;
    private int passengerId;
    private int staffId;
    private double totalPrice;
    private String seatClass;
    private String seatNumber;
    private String paymentMethod;

    public void setBoardingPassData(String pName, String fClass, String from, String to, String date, String time, String flightNo, String seatNumber) {
        this.seatClass = fClass;
        this.seatNumber = seatNumber;

        lblPassengerName.setText(pName != null ? pName.toUpperCase() : "UNKNOWN");
        lblClass.setText(fClass != null ? fClass.toUpperCase() : "ECONOMY");
        lblFrom.setText(formatCityCode(from));
        lblTo.setText(formatCityCode(to));
        lblDate.setText(date);
        lblTime.setText(time);
        lblFlight.setText(flightNo);
        lblSeat.setText(seatNumber);
    }

    public void setBookingDetailsForDatabase(int flightId, int passengerId, int staffId, double totalPrice, String paymentMethod) {
        this.flightId = flightId;
        this.passengerId = passengerId;
        this.staffId = staffId;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;

        lblPaymentMethod.setText(paymentMethod != null ? paymentMethod.toUpperCase() : "CASH");
    }

    private String formatCityCode(String city) {
        if (city == null || city.isEmpty()) return "UNK";
        if (city.equalsIgnoreCase("Yangon")) return "YGN";
        if (city.equalsIgnoreCase("Mandalay")) return "MDL";
        if (city.equalsIgnoreCase("Bangkok")) return "BKK";
        if (city.equalsIgnoreCase("Singapore")) return "SIN";
        if (city.equalsIgnoreCase("Sittwe")) return "AKY";
        return city.substring(0, 3).toUpperCase();
    }

    // =========================================================================
    // 🌟 ဤနေရာတွင် Toast Notification အား Light Mode ပုံစံသို့ ပြောင်းလဲထားပါသည် 🌟
    // =========================================================================
    private void showToastNotification(String title, String message, boolean isError) {
        StackPane parentRoot = (StackPane) modalRoot.getParent();
        if (parentRoot == null) return;

        HBox toast = new HBox(15);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setMaxWidth(380);
        toast.setMinHeight(80);
        toast.setMaxHeight(80);

        String borderColor = isError ? "#EF4444" : "#10B981";

        // အဖြူရောင် Card အောက်ခံ
        toast.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: " + borderColor + "; -fx-border-width: 1.5px; -fx-background-radius: 14px; -fx-border-radius: 14px; -fx-padding: 16px 18px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 8);");

        // အဝိုင်းလေး နောက်ခံ (အစိမ်း သို့မဟုတ် အနီ)
        StackPane iconBox = new StackPane();
        iconBox.setStyle("-fx-background-color: " + borderColor + "; -fx-background-radius: 50%; -fx-min-width: 38px; -fx-min-height: 38px; -fx-max-width: 38px; -fx-max-height: 38px;");

        SVGPath icon = new SVGPath();
        if (isError) {
            icon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        } else {
            icon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        }
        icon.setFill(Color.WHITE); // အဝိုင်းထဲမှ အမှန်ခြစ်ကို အဖြူရောင်ပေးခြင်း
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        iconBox.getChildren().add(icon);

        VBox textContainer = new VBox(4);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        // ခေါင်းစဉ် စာလုံးအရောင် အမည်းရောင်
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #1F2937; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");

        // စာသား အရောင် ခဲရောင်
        Label lblMessage = new Label(message);
        lblMessage.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        lblMessage.setWrapText(true);
        lblMessage.setMaxWidth(300);

        textContainer.getChildren().addAll(lblTitle, lblMessage);
        toast.getChildren().addAll(iconBox, textContainer);

        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(toast, new Insets(0, 30, 30, 0));

        parentRoot.getChildren().add(toast);

        toast.setTranslateY(100);
        toast.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toast);
        slideIn.setToY(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast);
        fadeIn.setToValue(1.0);

        ParallelTransition ptIn = new ParallelTransition(slideIn, fadeIn);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), toast);
        slideOut.setToY(100);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setToValue(0.0);

        ParallelTransition ptOut = new ParallelTransition(slideOut, fadeOut);
        ptOut.setOnFinished(e -> parentRoot.getChildren().remove(toast));

        ptIn.setOnFinished(e -> {
            PauseTransition delay = new PauseTransition(Duration.seconds(3.5));
            delay.setOnFinished(ev -> ptOut.play());
            delay.play();
        });

        ptIn.play();
    }

    @FXML
    void clickClose(ActionEvent event) {
        closeModal();
    }

    @FXML
    void clickDownload(ActionEvent event) {
        System.out.println("Downloading Boarding Pass...");
        showToastNotification("Download Started", "Your boarding pass is downloading as PDF.", false);
    }

    @FXML
    void clickPrint(ActionEvent event) {
        if (saveBookingToDatabase()) {
            showToastNotification("Booking Successful", "Booking has been saved successfully. Printing Boarding Pass...", false);
            closeModal();
        }
    }

    private boolean saveBookingToDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database connection failed.");
            conn.setAutoCommit(false);

            try {
                boolean exists = false;
                boolean isAvailable = true;
                int currentSeatId = -1;

                String checkQuery = "SELECT seat_id, is_available FROM seats WHERE flight_id = ? AND seat_number = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, this.flightId);
                    checkStmt.setString(2, this.seatNumber);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            exists = true;
                            currentSeatId = rs.getInt("seat_id");
                            isAvailable = rs.getBoolean("is_available");
                        }
                    }
                }

                if (exists && !isAvailable) {
                    showToastNotification("Seat Unavailable", "Sorry, this seat was just purchased by someone else. Please close and select another seat.", true);
                    closeModal();
                    return false;
                }

                if (exists) {
                    String updateQuery = "UPDATE seats SET is_available = false, seat_class = ? WHERE flight_id = ? AND seat_number = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, this.seatClass);
                        updateStmt.setInt(2, this.flightId);
                        updateStmt.setString(3, this.seatNumber);
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) VALUES (?, ?, ?, false)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setInt(1, this.flightId);
                        insertStmt.setString(2, this.seatNumber);
                        insertStmt.setString(3, this.seatClass);
                        insertStmt.executeUpdate();

                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                currentSeatId = generatedKeys.getInt(1);
                            }
                        }
                    }
                }

                String pnrCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                String bookingQuery = "INSERT INTO bookings (pnr_code, flight_id, passenger_id, seat_id, staff_id, total_price, payment_method, status, booking_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

                try (PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery)) {
                    bookingStmt.setString(1, pnrCode);
                    bookingStmt.setInt(2, this.flightId);
                    bookingStmt.setInt(3, this.passengerId);
                    bookingStmt.setInt(4, currentSeatId);
                    bookingStmt.setInt(5, this.staffId);
                    bookingStmt.setDouble(6, this.totalPrice);
                    bookingStmt.setString(7, this.paymentMethod);
                    bookingStmt.setString(8, "Confirmed");

                    bookingStmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showToastNotification("Database Error", "An error occurred while saving the booking.\n" + e.getMessage(), true);
            });
            return false;
        }
    }

    private void closeModal() {
        StackPane parentRoot = (StackPane) modalRoot.getParent();

        if (parentRoot != null) {
            if (!parentRoot.getChildren().isEmpty()) {
                Node mainView = parentRoot.getChildren().get(0);
                if (mainView.getEffect() instanceof GaussianBlur) {
                    GaussianBlur blur = (GaussianBlur) mainView.getEffect();
                    Timeline blurOut = new Timeline(
                            new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 0))
                    );
                    blurOut.setOnFinished(ev -> mainView.setEffect(null));
                    blurOut.play();
                }
            }

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), modalRoot);
            scaleDown.setToX(0.5);
            scaleDown.setToY(0.5);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), modalRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
            pt.setInterpolator(Interpolator.EASE_IN);
            pt.setOnFinished(e -> parentRoot.getChildren().remove(modalRoot));
            pt.play();
        }
    }
}