package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class AdminAddCityController {

    @FXML private BorderPane rootPane;

    @FXML private TextField txtCityCode;
    @FXML private TextField txtCityName;
    @FXML private TextField txtCountry;

    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // 🌟 အပြင် Table ကို ချက်ချင်း Refresh လုပ်ရန် အသုံးပြုမည့် Callback 🌟
    private Runnable saveCallback;

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }

    @FXML
    void clickCancel(ActionEvent event) {
        StackPane overlay = (StackPane) rootPane.getParent();

        if (overlay != null) {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(350), rootPane);
            scaleDown.setToX(0.0);
            scaleDown.setToY(0.0);

            TranslateTransition moveBack = new TranslateTransition(Duration.millis(350), rootPane);
            moveBack.setToX(400);
            moveBack.setToY(-300);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), overlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(scaleDown, moveBack, fadeOut);
            pt.setInterpolator(Interpolator.EASE_IN);

            pt.setOnFinished(e -> {
                StackPane mainRoot = (StackPane) overlay.getParent();
                if (mainRoot != null) {
                    mainRoot.getChildren().remove(overlay);
                }
            });

            pt.play();
        }
    }

    @FXML
    void clickSave(ActionEvent event) {
        String cityCode = txtCityCode.getText().trim();
        String cityName = txtCityName.getText().trim();
        String country = txtCountry.getText().trim();

        if (cityCode.isEmpty() || cityName.isEmpty() || country.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "City Code, City Name နှင့် Country အားလုံးကို ဖြည့်စွက်ပေးပါ။");
            return;
        }

        String insertQuery = "INSERT INTO cities (city_code, city_name, country) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, cityCode);
                    pstmt.setString(2, cityName);
                    pstmt.setString(3, country);

                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "City အသစ်ကို အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ။");

                        // 🌟 သိမ်းဆည်းတာ အောင်မြင်သွားရင် အပြင်က Table ကို Refresh လုပ်ခိုင်းပါမည် 🌟
                        if (saveCallback != null) {
                            saveCallback.run();
                        }

                        clickCancel(event);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Failed", "City သိမ်းဆည်းခြင်း မအောင်မြင်ပါ။ ပြန်လည်ကြိုးစားကြည့်ပါ။");
                    }
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Connection Error", "Database သို့ ချိတ်ဆက်၍ မရပါ။");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(Alert.AlertType.WARNING, "Duplicate Data", "ဤ City Code (" + cityCode + ") သည် စာရင်းထဲတွင် ရှိပြီးသားဖြစ်နေပါသည်။ အခြား Code တစ်ခု ပြောင်းထည့်ပါ။");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "အမှားအယွင်းဖြစ်ပေါ်နေပါသည်:\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}