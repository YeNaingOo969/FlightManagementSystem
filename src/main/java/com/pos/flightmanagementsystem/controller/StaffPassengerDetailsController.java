package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class StaffPassengerDetailsController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtFullName;
    @FXML private TextField txtNrcPassport;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cmbNationality;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;

    @FXML private Label lblFlightNo;
    @FXML private Label lblFrom;
    @FXML private Label lblTo;
    @FXML private Label lblDeparture;
    @FXML private Label lblTotalPrice;

    @FXML private Button btnBack;
    @FXML private Button btnContinue;

    private String currentFlightNo;
    private String currentFrom;
    private String currentTo;
    private String currentDate;
    private String currentPrice;
    private int currentPassengers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> genderOptions = FXCollections.observableArrayList("Male", "Female");
        cmbGender.setItems(genderOptions);
        cmbGender.getSelectionModel().selectFirst();

        ObservableList<String> nationalityOptions = FXCollections.observableArrayList("Myanmar", "Foreigner");
        cmbNationality.setItems(nationalityOptions);
        cmbNationality.getSelectionModel().selectFirst();
    }

    // ==========================================
    // 🌟 MMK ဖြင့် Total Price ကို တွက်ချက်ပြသခြင်း
    // ==========================================
    public void setFlightData(String flightNo, String from, String to, String date, String price, int passengers) {
        this.currentFlightNo = flightNo;
        this.currentFrom = from;
        this.currentTo = to;
        this.currentDate = date;
        this.currentPrice = price;
        this.currentPassengers = passengers;

        lblFlightNo.setText(flightNo);
        lblFrom.setText(from);
        lblTo.setText(to);
        lblDeparture.setText(date);

        try {
            // 🌟 ဥပမာ - "980,000 MMK" မှ ဂဏန်းများကိုသာ ဖြတ်ထုတ်ခြင်း
            double singlePrice = Double.parseDouble(price.replaceAll("[^\\d.]", ""));
            double totalPrice = singlePrice * passengers;

            // 🌟 တွက်ချက်ပြီးသား Total ကို MMK ဖြင့် ပြန်လည်ဖော်ပြခြင်း
            lblTotalPrice.setText(String.format("%,.0f MMK", totalPrice));
        } catch (Exception e) {
            // Error တက်ပါက မူလပါလာသော စာသားကိုသာ ပြမည်
            lblTotalPrice.setText(price);
        }
    }

    @FXML
    void clickBack(ActionEvent event) {
        navigateTo("/com/pos/flightmanagementsystem/view/StaffCreateNewBooking.fxml", true);
    }

    @FXML
    void clickContinue(ActionEvent event) {
        String gender = cmbGender.getValue();
        String fullName = txtFullName.getText();
        String nrcPassport = txtNrcPassport.getText();
        LocalDate dob = dpDateOfBirth.getValue();
        String nationality = cmbNationality.getValue();
        String phone = txtPhone.getText();
        String email = txtEmail.getText();

        if (fullName == null || fullName.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                dob == null || gender == null || nationality == null ||
                nrcPassport == null || nrcPassport.trim().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Data");
            alert.setHeaderText(null);
            alert.setContentText("ကျေးဇူးပြု၍ ခရီးသည်၏ အချက်အလက်များကို ပြည့်စုံစွာ ဖြည့်စွက်ပါ။");
            alert.showAndWait();
            return;
        }

        String query = "INSERT INTO passengers (full_name, gender, nrc_passport_no, dob, nationality, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database Connection မရရှိပါ။");

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, fullName);
                pstmt.setString(2, gender);
                pstmt.setString(3, nrcPassport);
                pstmt.setDate(4, java.sql.Date.valueOf(dob));
                pstmt.setString(5, nationality);
                pstmt.setString(6, phone);
                pstmt.setString(7, email);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Passenger Data ကို အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ။");
                    loadSeatSelectionPage();
                } else {
                    throw new Exception("Passenger Data သိမ်းဆည်းခြင်း မအောင်မြင်ပါ။");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Passenger အချက်အလက် သိမ်းဆည်းရာတွင် အမှားအယွင်းရှိနေပါသည်။");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void loadSeatSelectionPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffSeatSelection.fxml"));
            Parent nextView = loader.load();

            StaffSeatSelectionController controller = loader.getController();
            controller.setFlightData(
                    currentFlightNo,
                    currentFrom,
                    currentTo,
                    currentDate,
                    currentPrice,
                    currentPassengers,
                    txtFullName.getText()
            );

            nextView.setScaleX(0.8);
            nextView.setScaleY(0.8);
            nextView.setOpacity(0.0);

            rootStackPane.getChildren().add(nextView);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer);
            scaleOut.setToX(1.2);
            scaleOut.setToY(1.2);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
            pt.setInterpolator(Interpolator.EASE_BOTH);

            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer));
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load StaffSeatSelection.fxml");
        }
    }

    private void navigateTo(String fxmlPath, boolean isBack) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent nextView = loader.load();

            if (isBack) {
                nextView.setScaleX(1.2);
                nextView.setScaleY(1.2);
            }
            nextView.setOpacity(0.0);
            rootStackPane.getChildren().add(nextView);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer);
            if (isBack) {
                scaleOut.setToX(0.8);
                scaleOut.setToY(0.8);
            }
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
            pt.setInterpolator(Interpolator.EASE_BOTH);

            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer));
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}