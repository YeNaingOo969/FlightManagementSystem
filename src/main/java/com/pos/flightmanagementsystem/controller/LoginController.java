package com.pos.flightmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    // FXML ထဲက fx:id တွေနဲ့ တိုက်ရိုက်ချိတ်ဆက်မယ့် Variables များ
    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Hyperlink forgotPasswordLink;

    // Login Button နှိပ်တဲ့အခါ အလုပ်လုပ်မယ့် Method
    @FXML
    void clickLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        // ၁။ Input တွေ အလွတ်ဖြစ်နေသလား စစ်ဆေးခြင်း
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields (Username, Email, and Password).");
            return;
        }

        // ၂။ Database က Data တွေနဲ့ တိုက်စစ်မယ့် အပိုင်း (ဒီနေရာမှာ Database Logic လာရပါမယ်)
        // လောလောဆယ် Sample အနေနဲ့ Admin စစ်ဆေးပုံလေး ထည့်ပေးထားပါတယ်။
        if (username.equals("admin") && password.equals("admin123")) {

            showAlert(Alert.AlertType.INFORMATION, "Login Success", "Welcome back, " + username + "!");

            // ဤနေရာတွင် Dashboard သို့ သွားမယ့် Code များကို ရေးရန်
            // ဥပမာ - loadScene("Dashboard.fxml");

        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Username, Email or Password. Please try again.");
        }
    }

    // Forgot Password Link နှိပ်တဲ့အခါ အလုပ်လုပ်မယ့် Method
    @FXML
    void handleForgotPassword(ActionEvent event) {
        // Password Reset လုပ်မယ့် Page (သို့) Dialog ကို ဒီနေရာမှာ ခေါ်ရပါမယ်
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password", "Redirecting to Password Recovery Page...");
    }

    // Alert Box တွေ အလွယ်တကူထုတ်ပြဖို့ သုံးမယ့် Helper Method
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // ခေါင်းစဉ်ခွဲ မလိုချင်ရင် null ထားပါ
        alert.setContentText(message);
        alert.showAndWait();
    }
}