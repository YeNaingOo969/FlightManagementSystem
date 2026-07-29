package com.pos.flightmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

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

    // 🌟 Parent Controller (AdminCityController) မှ လှမ်းခေါ်မည့် Callbacks များ 🌟
    private Runnable saveCallback;
    private Runnable cancelCallback;

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }

    public void setCancelCallback(Runnable cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    // 🌟 Cancel ကို နှိပ်လျှင် Parent ရှိ Callback မှတစ်ဆင့် Modal ကို အလွယ်တကူပိတ်ပါမည် 🌟
    @FXML
    void clickCancel(ActionEvent event) {
        if (cancelCallback != null) {
            cancelCallback.run();
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
                        // 🌟 Save အောင်မြင်သွားပါက Default Alert ကြီး "မပြတော့ဘဲ"
                        // Parent ရှိ Callback ကိုသာ လှမ်းခေါ်ပါမည် (Background ဝါးတာတွေပြန်ရှင်းပြီး Toast ပေါ်လာပါမည်) 🌟
                        if (saveCallback != null) {
                            saveCallback.run();
                        }
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

    // Validation Error, Database Error စသည့် ပြဿနာများအတွက်သာ Alert ကို ချန်ထားပါသည်
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}