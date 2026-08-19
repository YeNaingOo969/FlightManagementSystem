package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class StaffBoardingPassController {

    @FXML private StackPane modalRoot;
    @FXML private javafx.scene.layout.Region boardingPassCard;

    @FXML private Label lblClass;
    @FXML private Label lblPassengerName;
    @FXML private Label lblFrom;
    @FXML private Label lblTo;
    @FXML private Label lblDate;
    @FXML private Label lblTime;
    @FXML private Label lblFlight;
    @FXML private Label lblSeat;
    @FXML private Label lblPaymentMethod;
    @FXML private Label lblBarcodeId;

    @FXML private HBox navBox;
    @FXML private Button btnPrevPass;
    @FXML private Button btnNextPass;
    @FXML private Label lblPassCount;

    private List<StaffPassengerDetailsController.PassengerInfo> passengersList;
    private int currentViewIndex = 0;

    private String flightNo, from, to, date, time, paymentMethod;
    private double totalPrice;

    private Runnable onSuccessCallback;

    private boolean isChangeFlightMode = false;
    private List<Integer> oldBookingIds;

    public void setOnSuccessCallback(Runnable onSuccessCallback) {
        this.onSuccessCallback = onSuccessCallback;
    }

    public void setChangeFlightMode(List<Integer> oldBookingIds) {
        this.isChangeFlightMode = true;
        this.oldBookingIds = oldBookingIds;
    }

    public void setMultiBoardingPassData(List<StaffPassengerDetailsController.PassengerInfo> passengers, String fNo, String fFrom, String fTo, String fDate, String fTime, double totalP, String payMethod) {
        this.passengersList = passengers;
        this.flightNo = fNo;
        this.from = fFrom;
        this.to = fTo;
        this.date = fDate;
        this.time = fTime;
        this.totalPrice = totalP;
        this.paymentMethod = payMethod;

        this.currentViewIndex = 0;
        updateUIForPassenger(currentViewIndex);

        if (passengersList != null && passengersList.size() > 1) {
            navBox.setVisible(true);
            navBox.setManaged(true);
            updateNavLabel();
        } else {
            navBox.setVisible(false);
            navBox.setManaged(false);
        }
    }

    private void updateUIForPassenger(int index) {
        StaffPassengerDetailsController.PassengerInfo pass = passengersList.get(index);

        lblPassengerName.setText(pass.fullName != null ? pass.fullName.toUpperCase() : "UNKNOWN");
        lblClass.setText("ECONOMY");
        lblFrom.setText(formatCityCode(from));
        lblTo.setText(formatCityCode(to));
        lblDate.setText(date);
        lblTime.setText(time);
        lblFlight.setText(flightNo);
        lblSeat.setText(pass.seatNo);
        lblPaymentMethod.setText(paymentMethod != null ? paymentMethod.toUpperCase() : "CASH");

        String randomBarcode = "BC-" + (int)(Math.random() * 900000 + 100000);
        if(lblBarcodeId != null) {
            lblBarcodeId.setText(randomBarcode);
        }
    }

    private void updateNavLabel() {
        lblPassCount.setText("Pass " + (currentViewIndex + 1) + " of " + passengersList.size());
        btnPrevPass.setDisable(currentViewIndex == 0);
        btnNextPass.setDisable(currentViewIndex == passengersList.size() - 1);
    }

    @FXML
    void clickPrevPass(ActionEvent event) {
        if (currentViewIndex > 0) {
            currentViewIndex--;
            updateUIForPassenger(currentViewIndex);
            updateNavLabel();
        }
    }

    @FXML
    void clickNextPass(ActionEvent event) {
        if (currentViewIndex < passengersList.size() - 1) {
            currentViewIndex++;
            updateUIForPassenger(currentViewIndex);
            updateNavLabel();
        }
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

    @FXML
    void clickPrint(ActionEvent event) {
        if (saveAllToDatabase()) {
            generateAllBoardingPassImages();

            if (isChangeFlightMode) {
                StaffManageBookingController.showBookingSuccessToast = true;
                StaffManageBookingController.successMessage = "Successfully changed flight for " + passengersList.size() + " passenger(s)!";
            } else {
                StaffCreateNewBookingController.showBookingSuccessToast = true;
                StaffCreateNewBookingController.successMessage = "Successfully generated " + passengersList.size() + " Boarding Pass(es)!";
            }

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            closeModal();
        }
    }

    private boolean saveAllToDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database connection failed.");
            conn.setAutoCommit(false);

            try {
                if (this.isChangeFlightMode && this.oldBookingIds != null && !this.oldBookingIds.isEmpty()) {
                    for (int oldId : oldBookingIds) {
                        String freeOldSeatQuery = "UPDATE seats SET is_available = true WHERE seat_id = (SELECT seat_id FROM bookings WHERE booking_id = ?)";
                        try (PreparedStatement freeStmt = conn.prepareStatement(freeOldSeatQuery)) {
                            freeStmt.setInt(1, oldId);
                            freeStmt.executeUpdate();
                        }
                        String updateOldBookingQuery = "UPDATE bookings SET status = 'Rescheduled' WHERE booking_id = ?";
                        try (PreparedStatement updStmt = conn.prepareStatement(updateOldBookingQuery)) {
                            updStmt.setInt(1, oldId);
                            updStmt.executeUpdate();
                        }
                    }
                }

                int flightId = -1;
                String flightQuery = "SELECT flight_id FROM flights WHERE flight_number = ? AND DATE(departure_time) = ?";
                try (PreparedStatement flightStmt = conn.prepareStatement(flightQuery)) {
                    flightStmt.setString(1, this.flightNo);
                    flightStmt.setString(2, this.date);
                    try (ResultSet rs = flightStmt.executeQuery()) {
                        if (rs.next()) flightId = rs.getInt("flight_id");
                    }
                }
                if (flightId == -1) throw new Exception("Flight ID not found.");

                int staffId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;
                double pricePerSeat = this.totalPrice / passengersList.size();

                String groupPnrCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

                for (StaffPassengerDetailsController.PassengerInfo pass : passengersList) {
                    int passengerId = -1;
                    String checkPassQuery = "SELECT passenger_id FROM passengers WHERE nrc_passport_no = ?";
                    try (PreparedStatement pStmt = conn.prepareStatement(checkPassQuery)) {
                        pStmt.setString(1, pass.nrcOrPassport);
                        try (ResultSet rs = pStmt.executeQuery()) {
                            if (rs.next()) passengerId = rs.getInt("passenger_id");
                        }
                    }

                    if (passengerId != -1) {
                        String updateQuery = "UPDATE passengers SET full_name=?, gender=?, dob=?, nationality=?, phone=?, email=? WHERE passenger_id=?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, pass.fullName); updateStmt.setString(2, pass.gender); updateStmt.setDate(3, java.sql.Date.valueOf(pass.dob));
                            updateStmt.setString(4, pass.nationality); updateStmt.setString(5, pass.phone); updateStmt.setString(6, pass.email); updateStmt.setInt(7, passengerId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        String insertQuery = "INSERT INTO passengers (full_name, gender, nrc_passport_no, dob, nationality, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                            insertStmt.setString(1, pass.fullName); insertStmt.setString(2, pass.gender); insertStmt.setString(3, pass.nrcOrPassport);
                            insertStmt.setDate(4, java.sql.Date.valueOf(pass.dob)); insertStmt.setString(5, pass.nationality); insertStmt.setString(6, pass.phone); insertStmt.setString(7, pass.email);
                            insertStmt.executeUpdate();
                            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) passengerId = generatedKeys.getInt(1);
                            }
                        }
                    }

                    int seatId = -1;
                    boolean isAvail = true;
                    String checkSeatQuery = "SELECT seat_id, is_available FROM seats WHERE flight_id = ? AND seat_number = ?";
                    try (PreparedStatement checkSeatStmt = conn.prepareStatement(checkSeatQuery)) {
                        checkSeatStmt.setInt(1, flightId); checkSeatStmt.setString(2, pass.seatNo);
                        try (ResultSet rs = checkSeatStmt.executeQuery()) {
                            if (rs.next()) {
                                seatId = rs.getInt("seat_id");
                                isAvail = rs.getBoolean("is_available");
                            }
                        }
                    }

                    if (!isAvail) throw new Exception("Seat " + pass.seatNo + " was just booked by someone else!");

                    String updateSeatQuery = "UPDATE seats SET is_available = false WHERE seat_id = ?";
                    try (PreparedStatement updateSeatStmt = conn.prepareStatement(updateSeatQuery)) {
                        updateSeatStmt.setInt(1, seatId); updateSeatStmt.executeUpdate();
                    }

                    String bookingQuery = "INSERT INTO bookings (pnr_code, flight_id, passenger_id, seat_id, staff_id, total_price, payment_method, status, booking_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
                    try (PreparedStatement bookStmt = conn.prepareStatement(bookingQuery)) {
                        bookStmt.setString(1, groupPnrCode);
                        bookStmt.setInt(2, flightId); bookStmt.setInt(3, passengerId);
                        bookStmt.setInt(4, seatId); bookStmt.setInt(5, staffId); bookStmt.setDouble(6, pricePerSeat);
                        bookStmt.setString(7, this.paymentMethod); bookStmt.setString(8, "Confirmed");
                        bookStmt.executeUpdate();
                    }
                }

                conn.commit();

                // 🌟 Boarding Pass (Booking) အောင်မြင်ပါက System Log သို့ မှတ်တမ်းတင်မည် 🌟
                String logAction = isChangeFlightMode ? "Rescheduled flight for PNR: " : "Created new booking PNR: ";
                insertLog(staffId, logAction + groupPnrCode + " (Flight: " + flightNo + ")");

                return true;

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🌟 System Log ထဲသို့ သိမ်းဆည်းမည့် Method 🌟
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

    private void generateAllBoardingPassImages() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        for (int i = 0; i < passengersList.size(); i++) {
            updateUIForPassenger(i);

            boardingPassCard.applyCss();
            boardingPassCard.layout();

            WritableImage image = captureBoardingPass();
            String safeName = passengersList.get(i).fullName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String fileName = "BoardingPass_" + safeName + "_" + timestamp + ".png";

            saveToResourcesFolder(image, fileName);
        }

        updateUIForPassenger(currentViewIndex);
    }

    private WritableImage captureBoardingPass() {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return boardingPassCard.snapshot(params, null);
    }

    private void saveToResourcesFolder(WritableImage image, String fileName) {
        try {
            String dirPath = "src/main/resources/com/pos/flightmanagementsystem/BoardingPass";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, fileName);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            System.out.println("✅ Boarding Pass saved: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clickClose(ActionEvent event) {
        closeModal();
    }

    private void closeModal() {
        StackPane parentRoot = (StackPane) modalRoot.getParent();
        if (parentRoot != null) {
            if (!parentRoot.getChildren().isEmpty()) {
                Node mainView = parentRoot.getChildren().get(0);
                if (mainView.getEffect() instanceof GaussianBlur) {
                    GaussianBlur blur = (GaussianBlur) mainView.getEffect();
                    Timeline blurOut = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 0)));
                    blurOut.setOnFinished(ev -> mainView.setEffect(null)); blurOut.play();
                }
            }

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), modalRoot); scaleDown.setToX(0.5); scaleDown.setToY(0.5);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), modalRoot); fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
            ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut); pt.setInterpolator(Interpolator.EASE_IN);
            pt.setOnFinished(e -> parentRoot.getChildren().remove(modalRoot)); pt.play();
        }
    }
}