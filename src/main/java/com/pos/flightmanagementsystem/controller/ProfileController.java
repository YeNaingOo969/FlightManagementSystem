package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private AnchorPane profileRoot;
    @FXML private StackPane profileView;
    @FXML private StackPane modalOverlay; // 🌟 ဤနေရာတွင် Overlay အသစ်ကို ချိတ်ဆက်ထားသည်
    @FXML private HBox notificationToast;

    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblRole;
    @FXML private Label lblEmployeeId;

    private Parent pwdChangeView;
    private String loggedInUser = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(this::loadUserData).start();
    }

    private void loadUserData() {
        String query = "SELECT user_id, username, email, role FROM users LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");

                loggedInUser = username;

                Platform.runLater(() -> {
                    lblFullName.setText(username);
                    lblEmail.setText(email != null && !email.isEmpty() ? email : "No Email Provided");
                    lblRole.setText(role != null ? role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase() : "Unknown");
                    lblEmployeeId.setText(String.format("EMP-%04d", userId));
                });

            } else {
                Platform.runLater(() -> {
                    lblFullName.setText("No User Found");
                    lblEmail.setText("-");
                    lblRole.setText("-");
                    lblEmployeeId.setText("-");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                lblFullName.setText("Database Error");
                lblEmail.setText("Check Console");
                lblRole.setText("Error");
                lblEmployeeId.setText("Error");
            });
        }
    }

    // ==========================================
    // 🌟 PASSWORD CHANGE တွင် FLOATING (POP-UP) EFFECT
    // ==========================================
    @FXML
    void showPasswordView(ActionEvent event) {
        try {
            if (pwdChangeView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/SuperAdminPwChange.fxml"));
                pwdChangeView = loader.load();

                SuperAdminPwChangeController pwdController = loader.getController();
                if (pwdController != null) {
                    pwdController.setParentController(this);
                    pwdController.setCurrentUser(loggedInUser);
                }

                // Profile ထဲ တိုက်ရိုက်မထည့်ဘဲ မှောင်နေမည့် Overlay ထဲသို့ ထည့်မည်
                modalOverlay.getChildren().add(pwdChangeView);
            }

            modalOverlay.setVisible(true);

            // ၁။ နောက်ခံအမည်းရောင် Overlay ကို မှောင်လာစေခြင်း (Fade In)
            FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), modalOverlay);
            fadeOverlay.setFromValue(0.0);
            fadeOverlay.setToValue(1.0);

            // ၂။ ကတ်အသစ်လေးကို သေးရာမှ ကြီးလာစေခြင်း (Scale In)
            pwdChangeView.setScaleX(0.5);
            pwdChangeView.setScaleY(0.5);
            pwdChangeView.setOpacity(0.0);

            FadeTransition fadeModal = new FadeTransition(Duration.millis(300), pwdChangeView);
            fadeModal.setToValue(1.0);

            ScaleTransition scaleModal = new ScaleTransition(Duration.millis(400), pwdChangeView);
            scaleModal.setToX(1.0);
            scaleModal.setToY(1.0);

            // 💡 ကိုယ်ပိုင် Overshoot (ခုန်ထွက်လာသည့်) Effect လေး
            Interpolator bouncyInterpolator = new Interpolator() {
                @Override
                protected double curve(double t) {
                    double tension = 2.0;
                    t -= 1.0;
                    return t * t * ((tension + 1) * t + tension) + 1.0;
                }
            };
            scaleModal.setInterpolator(bouncyInterpolator);

            ParallelTransition pt = new ParallelTransition(fadeOverlay, fadeModal, scaleModal);
            pt.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hidePasswordView() {
        // ပိတ်လိုက်သည့်အခါ ပြန်လည်ပျောက်သွားစေခြင်း (Fade Out & Scale Down)
        FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), modalOverlay);
        fadeOverlay.setToValue(0.0);

        ScaleTransition scaleModal = new ScaleTransition(Duration.millis(300), pwdChangeView);
        scaleModal.setToX(0.8);
        scaleModal.setToY(0.8);
        scaleModal.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeModal = new FadeTransition(Duration.millis(300), pwdChangeView);
        fadeModal.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(fadeOverlay, scaleModal, fadeModal);
        pt.setOnFinished(e -> modalOverlay.setVisible(false)); // Animation ပြီးမှ ဖျောက်မည်
        pt.play();
    }


    // ==========================================
    // Notification Animation Methods များ
    // ==========================================
    public void showSuccessNotification() {
        notificationToast.setVisible(true);
        notificationToast.setTranslateY(150);

        TranslateTransition popUp = new TranslateTransition(Duration.millis(600), notificationToast);
        popUp.setToY(0);

        Interpolator overshootInterpolator = new Interpolator() {
            @Override
            protected double curve(double t) {
                double tension = 2.0;
                t -= 1.0;
                return t * t * ((tension + 1) * t + tension) + 1.0;
            }
        };

        popUp.setInterpolator(overshootInterpolator);
        popUp.play();



        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e -> closeNotificationAnimation());
        delay.play();
    }

    @FXML
    void closeNotification(ActionEvent event) {
        closeNotificationAnimation();
    }

    private void closeNotificationAnimation() {
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(400), notificationToast);
        slideDown.setToY(150);
        slideDown.setInterpolator(Interpolator.EASE_IN);
        slideDown.setOnFinished(e -> notificationToast.setVisible(false));
        slideDown.play();
    }
}