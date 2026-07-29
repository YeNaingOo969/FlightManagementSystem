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
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private StackPane profileRoot;
    @FXML private StackPane profileView;
    @FXML private StackPane modalOverlay;
    @FXML private HBox notificationToast;

    // 🌟 Profile Info Labels & Image 🌟
    @FXML private Circle profileAvatarCircle;
    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblRole;
    @FXML private Label lblEmployeeId;

    private Parent pwdChangeView;
    private String loggedInUser = "";

    // 🌟 Modal ပိတ်ရန် Callback 🌟
    private Runnable closeCallback;

    public void setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization တွင် Data မဆွဲတော့ဘဲ Parent (AdminController) မှ ထည့်ပေးသည်ကို စောင့်မည်
    }

    // ==========================================
    // 🌟 ADMIN CONTROLLER မှ USER DATA လက်ခံရန် METHOD 🌟
    // ==========================================
    public void setUserData(String username, String role, String imagePath) {
        this.loggedInUser = username;

        // UI တွင် တိုက်ရိုက်ပြရန်
        lblFullName.setText(username);
        lblRole.setText(role != null ? role.toUpperCase() : "UNKNOWN");

        // Profile ပုံကို ထည့်သွင်းရန်
        if (profileAvatarCircle != null && imagePath != null && !imagePath.isEmpty()) {
            try {
                File imgFile = new File("src/main/resources/com/pos/flightmanagementsystem/image/" + imagePath);
                if (imgFile.exists()) {
                    Image image = new Image(imgFile.toURI().toString());
                    profileAvatarCircle.setFill(new ImagePattern(image));
                } else {
                    URL url = getClass().getResource("/com/pos/flightmanagementsystem/image/" + imagePath);
                    if (url != null) {
                        Image image = new Image(url.toExternalForm());
                        profileAvatarCircle.setFill(new ImagePattern(image));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Database မှ Email နှင့် User ID ကို အမှန်တကယ် ပြန်ဆွဲထုတ်ရန် Thread သုံးခြင်း
        new Thread(() -> loadDatabaseInfo(username)).start();
    }

    private void loadDatabaseInfo(String username) {
        // 🌟 ဝင်ထားသော User ၏ အချက်အလက်ကိုသာ အတိအကျ ဆွဲထုတ်မည် 🌟
        String query = "SELECT user_id, email FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("email");
                    int userId = rs.getInt("user_id");

                    Platform.runLater(() -> {
                        lblEmail.setText(email != null && !email.isEmpty() ? email : "No Email Provided");
                        lblEmployeeId.setText(String.format("EMP-%04d", userId));
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clickClose(ActionEvent event) {
        if (closeCallback != null) {
            closeCallback.run(); // 🌟 Parent တွင် Animation နှင့် ပြန်ပိတ်မည့် Code ကို ခေါ်မည်
        }
    }

    // ==========================================
    // 🌟 PASSWORD CHANGE FLOATING (POP-UP) EFFECT 🌟
    // ==========================================
    @FXML
    void showPasswordView(ActionEvent event) {
        try {
            if (pwdChangeView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/SuperAdminPwChange.fxml"));
                pwdChangeView = loader.load();

                PwChangeController pwdController = loader.getController();
                if (pwdController != null) {
                    pwdController.setParentController(this);
                    pwdController.setCurrentUser(loggedInUser);
                }

                modalOverlay.getChildren().add(pwdChangeView);
            }

            modalOverlay.setVisible(true);

            FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), modalOverlay);
            fadeOverlay.setFromValue(0.0);
            fadeOverlay.setToValue(1.0);

            pwdChangeView.setScaleX(0.5);
            pwdChangeView.setScaleY(0.5);
            pwdChangeView.setOpacity(0.0);

            FadeTransition fadeModal = new FadeTransition(Duration.millis(300), pwdChangeView);
            fadeModal.setToValue(1.0);

            ScaleTransition scaleModal = new ScaleTransition(Duration.millis(400), pwdChangeView);
            scaleModal.setToX(1.0);
            scaleModal.setToY(1.0);
            scaleModal.setInterpolator(new Interpolator() {
                @Override
                protected double curve(double t) {
                    double tension = 2.0;
                    t -= 1.0;
                    return t * t * ((tension + 1) * t + tension) + 1.0;
                }
            });

            ParallelTransition pt = new ParallelTransition(fadeOverlay, fadeModal, scaleModal);
            pt.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hidePasswordView() {
        FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), modalOverlay);
        fadeOverlay.setToValue(0.0);

        ScaleTransition scaleModal = new ScaleTransition(Duration.millis(300), pwdChangeView);
        scaleModal.setToX(0.8);
        scaleModal.setToY(0.8);
        scaleModal.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeModal = new FadeTransition(Duration.millis(300), pwdChangeView);
        fadeModal.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(fadeOverlay, scaleModal, fadeModal);
        pt.setOnFinished(e -> modalOverlay.setVisible(false));
        pt.play();
    }

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