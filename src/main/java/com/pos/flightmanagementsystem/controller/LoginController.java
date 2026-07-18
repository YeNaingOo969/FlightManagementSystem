package com.pos.flightmanagementsystem.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
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

    @FXML private VBox loginSection;
    @FXML private VBox emailRequestSection;
    @FXML private VBox otpSection;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPasswordHidden;
    @FXML private TextField txtPasswordVisible;
    @FXML private SVGPath eyeIcon;
    private boolean isPasswordVisible = false;

    @FXML private Button btnLogin;
    @FXML private TextField emailField;
    @FXML private Button sendButton;

    @FXML private TextField otp1, otp2, otp3, otp4, otp5, otp6;
    private TextField[] otpFields;

    @FXML private Button verifyButton;
    @FXML private Label statusLabel;

    private String generatedOTP = "";
    private final String senderEmail = "luuarr344@gmail.com";
    private final String senderPassword = "ifwo vnqq vmok ayan";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (txtPasswordHidden != null && txtPasswordVisible != null) {
            txtPasswordVisible.textProperty().bindBidirectional(txtPasswordHidden.textProperty());
        }

        if (otp1 != null) {
            otpFields = new TextField[]{otp1, otp2, otp3, otp4, otp5, otp6};
            setupOTPInputs();
        }

        setupInputFocusEffects();

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
                if (event.getCode() == javafx.scene.input.KeyCode.BACK_SPACE && currentField.getText().isEmpty()) {
                    if (index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    private void setupInputFocusEffects() {
        TextField[] fields = {txtUsername, txtPasswordHidden, txtPasswordVisible, emailField};

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
                            if (!inputBox.getStyleClass().contains("focused-box")) {
                                inputBox.getStyleClass().add("focused-box");
                            }
                        } else {
                            inputBox.getStyleClass().remove("focused-box");
                        }
                    }
                });
            }
        }
    }

    @FXML
    void showLoginSection(ActionEvent event) {
        switchSection(loginSection);
        statusLabel.setText("");
    }

    @FXML
    void showEmailRequestSection(ActionEvent event) {
        switchSection(emailRequestSection);
        statusLabel.setText("");
    }

    private void switchSection(VBox targetSection) {
        loginSection.setVisible(false);
        loginSection.setManaged(false);
        emailRequestSection.setVisible(false);
        emailRequestSection.setManaged(false);
        otpSection.setVisible(false);
        otpSection.setManaged(false);

        targetSection.setVisible(true);
        targetSection.setManaged(true);
    }

    @FXML
    void clickLogin(ActionEvent event) {
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

                if (role.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("superadmin")) {
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    playExitAnimation(stage, "/com/pos/flightmanagementsystem/view/SuperAdmin.fxml");
                }
                else if (role.equalsIgnoreCase("Admin")) {
                    showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Admin Dashboard is not ready yet.");
                }
                else if (role.equalsIgnoreCase("Staff")) {
                    showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Staff Dashboard is not ready yet.");
                }
                else {
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
            if (stage.isMaximized()) {
                stage.setMaximized(false);
            }
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // 💡 Animation ပြီးသွားတဲ့အချိန်မှာ Error တက်ရင် Alert Box ပြဖို့ Platform.runLater သုံးထားပေးပါတယ်
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the dashboard file.");
            });
        }
    }

    @FXML
    void togglePassword(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            txtPasswordVisible.setVisible(true);
            txtPasswordHidden.setVisible(false);
            eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
            eyeIcon.setFill(Color.web("#06b6d4"));
        } else {
            txtPasswordVisible.setVisible(false);
            txtPasswordHidden.setVisible(true);
            eyeIcon.setContent("M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z");
            eyeIcon.setFill(Color.web("#9ca3af"));
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

        statusLabel.setTextFill(Color.BLUE);
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
            statusLabel.setText("OTP Verified Successfully! You can now reset your password.");
            verifyButton.setDisable(true);
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Invalid OTP. Please try again.");
        }
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}