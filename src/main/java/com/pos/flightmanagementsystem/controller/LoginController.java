package com.pos.flightmanagementsystem.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private StackPane animationContainer;
    @FXML private Circle orbitPath;
    @FXML private Group smallPlane;

    @FXML private StackPane leftPanel;
    @FXML private StackPane rightPanel;

    // --- Sections ---
    @FXML private VBox loginSection;
    @FXML private VBox emailRequestSection;
    @FXML private VBox otpSection;
    @FXML private VBox resetPasswordSection;

    // --- Success Custom Dialog ---
    @FXML private StackPane successOverlay;
    @FXML private VBox successBox;

    // --- 🌟 New Custom Alert Dialog ---
    @FXML private StackPane customAlertOverlay;
    @FXML private VBox customAlertBox;
    @FXML private SVGPath customAlertIcon;
    @FXML private Label customAlertTitle;
    @FXML private Label customAlertMessage;

    // --- Fields ---
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPasswordHidden;
    @FXML private TextField txtPasswordVisible;
    @FXML private SVGPath eyeIcon;
    private boolean isPasswordVisible = false;

    @FXML private TextField emailField;
    @FXML private Button sendButton;
    @FXML private TextField otp1, otp2, otp3, otp4, otp5, otp6;
    private TextField[] otpFields;
    @FXML private Button verifyButton;

    @FXML private PasswordField txtNewPasswordHidden;
    @FXML private TextField txtNewPasswordVisible;
    @FXML private SVGPath newEyeIcon;
    private boolean isNewPasswordVisible = false;

    @FXML private PasswordField txtConfirmPasswordHidden;
    @FXML private TextField txtConfirmPasswordVisible;
    @FXML private SVGPath confirmEyeIcon;
    private boolean isConfirmPasswordVisible = false;

    @FXML private Label statusLabel;

    private String generatedOTP = "";
    private final String senderEmail = "soethihapaing123@gmail.com";
    private final String senderPassword = "dlkv vtlo zrhm hnmj";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // ==========================================
        // LOGIN PAGE ENTRANCE ANIMATION
        // ==========================================
        if (leftPanel != null && rightPanel != null) {
            leftPanel.setTranslateX(-800);
            rightPanel.setTranslateX(800);

            TranslateTransition leftIn = new TranslateTransition(Duration.millis(800), leftPanel);
            leftIn.setToX(0);
            leftIn.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition rightIn = new TranslateTransition(Duration.millis(800), rightPanel);
            rightIn.setToX(0);
            rightIn.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition ptIn = new ParallelTransition(leftIn, rightIn);
            ptIn.play();
        }

        // ==========================================
        // INITIALIZATIONS
        // ==========================================
        if (txtPasswordHidden != null && txtPasswordVisible != null) txtPasswordVisible.textProperty().bindBidirectional(txtPasswordHidden.textProperty());
        if (txtNewPasswordHidden != null && txtNewPasswordVisible != null) txtNewPasswordVisible.textProperty().bindBidirectional(txtNewPasswordHidden.textProperty());
        if (txtConfirmPasswordHidden != null && txtConfirmPasswordVisible != null) txtConfirmPasswordVisible.textProperty().bindBidirectional(txtConfirmPasswordHidden.textProperty());

        if (otp1 != null) {
            otpFields = new TextField[]{otp1, otp2, otp3, otp4, otp5, otp6};
            setupOTPInputs();
        }
        setupInputFocusEffects();

        // ==========================================
        // ENTER KEY LOGIN TRIGGER
        // ==========================================
        if (txtUsername != null) txtUsername.setOnKeyPressed(this::handleEnterKey);
        if (txtPasswordHidden != null) txtPasswordHidden.setOnKeyPressed(this::handleEnterKey);
        if (txtPasswordVisible != null) txtPasswordVisible.setOnKeyPressed(this::handleEnterKey);

        if (orbitPath != null && smallPlane != null) {
            PathTransition planeTransition = new PathTransition();
            planeTransition.setDuration(Duration.seconds(5));
            planeTransition.setPath(orbitPath);
            planeTransition.setNode(smallPlane);
            planeTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
            planeTransition.setInterpolator(Interpolator.LINEAR);
            planeTransition.setCycleCount(Timeline.INDEFINITE);
            planeTransition.play();

            Timeline smokeEmitter = new Timeline(new KeyFrame(Duration.millis(80), e -> spawnSmokeParticle()));
            smokeEmitter.setCycleCount(Timeline.INDEFINITE);
            smokeEmitter.play();

            createTwinklingStars();
            createRadarPulse();
        }
    }

    private void spawnSmokeParticle() {
        Circle smoke = new Circle(3.5, Color.web("#06b6d4"));
        smoke.setEffect(new GaussianBlur(5));
        smoke.setTranslateX(smallPlane.getTranslateX());
        smoke.setTranslateY(smallPlane.getTranslateY());
        double randomOffsetX = (Math.random() - 0.5) * 8;
        double randomOffsetY = (Math.random() - 0.5) * 8;
        smoke.setTranslateX(smoke.getTranslateX() + randomOffsetX);
        smoke.setTranslateY(smoke.getTranslateY() + randomOffsetY);
        int planeIndex = animationContainer.getChildren().indexOf(smallPlane);
        animationContainer.getChildren().add(planeIndex, smoke);

        FadeTransition fade = new FadeTransition(Duration.millis(800), smoke);
        fade.setFromValue(0.7);
        fade.setToValue(0.0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(800), smoke);
        scale.setFromX(1); scale.setFromY(1);
        scale.setToX(4.5); scale.setToY(4.5);
        ParallelTransition particleAnimation = new ParallelTransition(smoke, fade, scale);
        particleAnimation.setOnFinished(ev -> animationContainer.getChildren().remove(smoke));
        particleAnimation.play();
    }

    private void createRadarPulse() {
        Circle radarPulse = new Circle(89.0, Color.TRANSPARENT);
        radarPulse.setStroke(Color.web("#06b6d4"));
        radarPulse.setStrokeWidth(1.5);
        animationContainer.getChildren().add(0, radarPulse);
        ScaleTransition scalePulse = new ScaleTransition(Duration.seconds(2.5), radarPulse);
        scalePulse.setFromX(1.0); scalePulse.setFromY(1.0);
        scalePulse.setToX(1.8); scalePulse.setToY(1.8);
        FadeTransition fadePulse = new FadeTransition(Duration.seconds(2.5), radarPulse);
        fadePulse.setFromValue(0.5);
        fadePulse.setToValue(0.0);
        ParallelTransition pulseAnimation = new ParallelTransition(scalePulse, fadePulse);
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.play();
    }

    private void createTwinklingStars() {
        Group starGroup = new Group();
        starGroup.setManaged(false);
        for (int i = 0; i < 80; i++) {
            Circle star = new Circle(Math.random() * 1.5 + 0.8, Color.WHITE);
            star.setTranslateX((Math.random() - 0.5) * 1200);
            star.setTranslateY(-350 + Math.random() * 530);
            FadeTransition blink = new FadeTransition(Duration.seconds(0.5 + Math.random() * 1.0), star);
            blink.setFromValue(0.1);
            blink.setToValue(1.0);
            blink.setAutoReverse(true);
            blink.setCycleCount(Timeline.INDEFINITE);
            blink.setDelay(Duration.seconds(Math.random()));
            blink.play();
            starGroup.getChildren().add(star);
        }
        animationContainer.getChildren().add(0, starGroup);
    }

    private void setupOTPInputs() {
        for (int i = 0; i < otpFields.length; i++) {
            TextField currentField = otpFields[i];
            final int index = i;
            currentField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    currentField.setText(newValue.replaceAll("[^\\d]", ""));
                    return;
                }
                if (newValue.length() > 1) {
                    currentField.setText(newValue.substring(0, 1));
                    return;
                }
                if (newValue.length() == 1 && index < otpFields.length - 1) {
                    otpFields[index + 1].requestFocus();
                }
            });
            currentField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && currentField.getText().isEmpty()) {
                    if (index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    private void setupInputFocusEffects() {
        TextField[] fields = {txtUsername, txtPasswordHidden, txtPasswordVisible, emailField,
                txtNewPasswordHidden, txtNewPasswordVisible,
                txtConfirmPasswordHidden, txtConfirmPasswordVisible};

        for (TextField field : fields) {
            if (field != null) {
                field.focusedProperty().addListener((obs, oldVal, isFocused) -> {
                    javafx.scene.Parent parent = field.getParent();
                    javafx.scene.layout.HBox inputBox = null;
                    if (parent instanceof javafx.scene.layout.HBox) {
                        inputBox = (javafx.scene.layout.HBox) parent;
                    } else if (parent.getParent() instanceof javafx.scene.layout.HBox) {
                        inputBox = (javafx.scene.layout.HBox) parent.getParent();
                    }
                    if (inputBox != null) {
                        if (isFocused) {
                            if (!inputBox.getStyleClass().contains("focused-box")) inputBox.getStyleClass().add("focused-box");
                        } else {
                            inputBox.getStyleClass().remove("focused-box");
                        }
                    }
                });
            }
        }
    }

    @FXML void showLoginSection(ActionEvent event) { switchSection(loginSection); statusLabel.setText(""); }
    @FXML void showEmailRequestSection(ActionEvent event) { switchSection(emailRequestSection); statusLabel.setText(""); }

    private void switchSection(VBox targetSection) {
        if(loginSection != null) { loginSection.setVisible(false); loginSection.setManaged(false); }
        if(emailRequestSection != null) { emailRequestSection.setVisible(false); emailRequestSection.setManaged(false); }
        if(otpSection != null) { otpSection.setVisible(false); otpSection.setManaged(false); }
        if(resetPasswordSection != null) { resetPasswordSection.setVisible(false); resetPasswordSection.setManaged(false); }

        if(targetSection != null) {
            targetSection.setVisible(true);
            targetSection.setManaged(true);
        }
    }

    // ==========================================
    // ENTER KEY LOGIN LOGIC
    // ==========================================
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            attemptLogin();
        }
    }

    @FXML
    void clickLogin(ActionEvent event) {
        attemptLogin();
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPasswordHidden.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        String query = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                Stage stage = (Stage) txtUsername.getScene().getWindow();

                if (role.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("superadmin")) {
                    playExitAnimation(stage, "/com/pos/flightmanagementsystem/view/SuperAdmin.fxml");
                } else if (role.equalsIgnoreCase("Admin")) {
                    playExitAnimation(stage, "/com/pos/flightmanagementsystem/view/Admin.fxml");
                } else if (role.equalsIgnoreCase("Staff")) {
                    playExitAnimation(stage, "/com/pos/flightmanagementsystem/view/Staff.fxml");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Access Denied", "Unknown role assigned to your account.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Username or Password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
        }
    }

    private void playExitAnimation(Stage stage, String fxmlPath) {
        TranslateTransition leftAnim = new TranslateTransition(Duration.millis(700), leftPanel);
        leftAnim.setByX(-800);
        leftAnim.setInterpolator(Interpolator.EASE_IN);
        TranslateTransition rightAnim = new TranslateTransition(Duration.millis(700), rightPanel);
        rightAnim.setByX(800);
        rightAnim.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition pt = new ParallelTransition(leftAnim, rightAnim);
        pt.setOnFinished(e -> loadDashboard(stage, fxmlPath));
        pt.play();
    }

    private void loadDashboard(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            if (stage.isMaximized()) stage.setMaximized(false);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the dashboard file."));
        }
    }

    @FXML void togglePassword(ActionEvent event) { isPasswordVisible = !isPasswordVisible; updateEyeIcon(isPasswordVisible, txtPasswordHidden, txtPasswordVisible, eyeIcon); }
    @FXML void toggleNewPassword(ActionEvent event) { isNewPasswordVisible = !isNewPasswordVisible; updateEyeIcon(isNewPasswordVisible, txtNewPasswordHidden, txtNewPasswordVisible, newEyeIcon); }
    @FXML void toggleConfirmPassword(ActionEvent event) { isConfirmPasswordVisible = !isConfirmPasswordVisible; updateEyeIcon(isConfirmPasswordVisible, txtConfirmPasswordHidden, txtConfirmPasswordVisible, confirmEyeIcon); }

    private void updateEyeIcon(boolean isVisible, PasswordField hiddenField, TextField visibleField, SVGPath icon) {
        if (isVisible) {
            visibleField.setVisible(true); hiddenField.setVisible(false);
            icon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
            icon.setFill(Color.web("#06b6d4"));
        } else {
            visibleField.setVisible(false); hiddenField.setVisible(true);
            icon.setContent("M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z");
            icon.setFill(Color.web("#9ca3af"));
        }
    }

    @FXML
    void handleSendEmail(ActionEvent event) {
        String targetEmail = emailField.getText().trim();
        if (targetEmail.isEmpty() || !targetEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Please enter a valid email address.");
            return;
        }
        statusLabel.setTextFill(Color.web("#06b6d4"));
        statusLabel.setText("Sending OTP... Please wait.");
        sendButton.setDisable(true);
        generatedOTP = String.format("%06d", new Random().nextInt(999999));

        new Thread(() -> {
            boolean isSent = sendEmail(targetEmail, generatedOTP);
            Platform.runLater(() -> {
                sendButton.setDisable(false);
                if (isSent) {
                    statusLabel.setTextFill(Color.GREEN);
                    statusLabel.setText("OTP sent successfully to your email.");
                    switchSection(otpSection);
                } else {
                    statusLabel.setTextFill(Color.RED);
                    statusLabel.setText("Failed to send OTP. Check your connection.");
                }
            });
        }).start();
    }

    @FXML
    void handleVerifyOTP(ActionEvent event) {
        String enteredOTP = otp1.getText() + otp2.getText() + otp3.getText() +
                otp4.getText() + otp5.getText() + otp6.getText();

        if (enteredOTP.length() < 6) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Please enter the complete 6-digit OTP.");
            return;
        }

        if (enteredOTP.equals(generatedOTP)) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("OTP Verified Successfully!");
            verifyButton.setDisable(true);
            switchSection(resetPasswordSection);
            statusLabel.setText("");
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Invalid OTP. Please try again.");
        }
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String newPassword = txtNewPasswordHidden.getText().trim();
        String confirmPassword = txtConfirmPasswordHidden.getText().trim();
        String targetEmail = emailField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in both password fields.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "The new passwords do not match.");
            return;
        }

        String updateQuery = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, targetEmail);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                txtNewPasswordHidden.clear();
                txtNewPasswordVisible.clear();
                txtConfirmPasswordHidden.clear();
                txtConfirmPasswordVisible.clear();
                emailField.clear();
                for (TextField tf : otpFields) tf.clear();
                verifyButton.setDisable(false);
                statusLabel.setText("");

                playSuccessAnimation();

            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "No account found associated with this email address.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update your password.");
        }
    }

    private void playSuccessAnimation() {
        successOverlay.setVisible(true);
        successOverlay.setOpacity(0.0);
        successBox.setScaleX(0.5);
        successBox.setScaleY(0.5);

        FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), successOverlay);
        fadeOverlay.setToValue(1.0);

        ScaleTransition scaleBox = new ScaleTransition(Duration.millis(500), successBox);
        scaleBox.setToX(1.0);
        scaleBox.setToY(1.0);
        scaleBox.setInterpolator(new Interpolator() {
            @Override
            protected double curve(double t) {
                double tension = 2.0;
                t -= 1.0;
                return t * t * ((tension + 1) * t + tension) + 1.0;
            }
        });

        FadeTransition fadeBox = new FadeTransition(Duration.millis(300), successBox);
        fadeBox.setFromValue(0.0);
        fadeBox.setToValue(1.0);

        ParallelTransition pt = new ParallelTransition(fadeOverlay, scaleBox, fadeBox);
        pt.play();
    }

    @FXML
    void closeSuccessDialog(ActionEvent event) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), successBox);
        scaleDown.setToX(0.8);
        scaleDown.setToY(0.8);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), successOverlay);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setOnFinished(e -> {
            successOverlay.setVisible(false);
            switchSection(loginSection);
        });
        pt.play();
    }

    // ==========================================
    // 🌟 CUSTOM ANIMATED ALERT DIALOG
    // ==========================================

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        // UI ကို Main Thread မှ အမြဲ အပ်ဒိတ်လုပ်နိုင်ရန် သေချာစေခြင်း
        Platform.runLater(() -> {
            customAlertTitle.setText(title);
            customAlertMessage.setText(message);

            // ယခင် Style Class များကို ရှင်းလင်းခြင်း
            customAlertBox.getStyleClass().removeAll("alert-error", "alert-warning", "alert-info");

            // Alert အမျိုးအစားအလိုက် Theme နှင့် Icon ပြောင်းလဲခြင်း
            if (alertType == Alert.AlertType.ERROR) {
                // ကြက်ခြေခတ် Icon
                customAlertIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z");
                customAlertIcon.setFill(Color.web("#ef4444")); // Red
                customAlertBox.getStyleClass().add("alert-error");
            } else if (alertType == Alert.AlertType.WARNING) {
                // သတိပေးတြိဂံ Icon
                customAlertIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
                customAlertIcon.setFill(Color.web("#f59e0b")); // Amber/Yellow
                customAlertBox.getStyleClass().add("alert-warning");
            } else {
                // Info (i) Icon
                customAlertIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
                customAlertIcon.setFill(Color.web("#06b6d4")); // Cyan
                customAlertBox.getStyleClass().add("alert-info");
            }

            customAlertOverlay.setVisible(true);
            customAlertOverlay.setOpacity(0.0);
            customAlertBox.setScaleX(0.5);
            customAlertBox.setScaleY(0.5);

            FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), customAlertOverlay);
            fadeOverlay.setToValue(1.0);

            // Bouncy Pop-up Effect
            ScaleTransition scaleBox = new ScaleTransition(Duration.millis(400), customAlertBox);
            scaleBox.setToX(1.0);
            scaleBox.setToY(1.0);
            scaleBox.setInterpolator(new Interpolator() {
                @Override
                protected double curve(double t) {
                    double tension = 2.0;
                    t -= 1.0;
                    return t * t * ((tension + 1) * t + tension) + 1.0;
                }
            });

            FadeTransition fadeBox = new FadeTransition(Duration.millis(300), customAlertBox);
            fadeBox.setFromValue(0.0);
            fadeBox.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(fadeOverlay, scaleBox, fadeBox);
            pt.play();
        });
    }

    @FXML
    void closeCustomAlert(ActionEvent event) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), customAlertBox);
        scaleDown.setToX(0.8);
        scaleDown.setToY(0.8);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), customAlertOverlay);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setOnFinished(e -> customAlertOverlay.setVisible(false));
        pt.play();
    }

    private boolean sendEmail(String recipientEmail, String otpCode) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Password Reset OTP - ArrLuuu Airline");
            message.setText("Dear User,\n\nYour OTP for password reset is: " + otpCode +
                    "\n\nPlease do not share this code with anyone.\n\nBest Regards,\nArrLuuu Airline Team");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}