package com.pos.flightmanagementsystem.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    public static com.pos.flightmanagementsystem.model.User loggedInUser;
    public static boolean isFromLogout = false;

    public static boolean isLightMode = false;

    private final int MAX_ATTEMPTS = 3;

    // 🌟 OTP Expiration နှင့် Rate Limiting အတွက် 🌟
    private LocalDateTime otpGeneratedTime = null;
    private LocalDateTime lastOtpRequestTime = null;

    @FXML private StackPane animationContainer;
    @FXML private Circle orbitPath;
    @FXML private Group smallPlane;

    @FXML private StackPane leftPanel;
    @FXML private StackPane rightPanel;

    @FXML private VBox loginSection;
    @FXML private VBox emailRequestSection;
    @FXML private VBox otpSection;
    @FXML private VBox resetPasswordSection;

    @FXML private StackPane successOverlay;
    @FXML private VBox successBox;
    @FXML private StackPane customAlertOverlay;
    @FXML private VBox customAlertBox;
    @FXML private SVGPath customAlertIcon;
    @FXML private Label customAlertTitle;
    @FXML private Label customAlertMessage;

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

        Platform.runLater(() -> {
            Parent root = leftPanel.getParent();
            if (root != null) {
                root.getStylesheets().clear();
                String cssFile = isLightMode ? "LoginLight.css" : "LoginDark.css";
                URL cssUrl = getClass().getResource("/com/pos/flightmanagementsystem/Style/" + cssFile);
                if (cssUrl != null) {
                    root.getStylesheets().add(cssUrl.toExternalForm());
                }
            }

            if (smallPlane != null && !smallPlane.getChildren().isEmpty()) {
                Node node = smallPlane.getChildren().get(0);
                if (node instanceof SVGPath) {
                    ((SVGPath) node).setFill(Color.web(isLightMode ? "#00838f" : "#00D4FF"));
                }
            }

            for (Node node : animationContainer.getChildren()) {
                if (node instanceof ImageView) {
                    if (isLightMode) {
                        Lighting tint = new Lighting(new Light.Distant(45, 45, Color.web("#00838f")));
                        tint.setSurfaceScale(0.0);
                        node.setEffect(tint);
                    } else {
                        node.setEffect(null);
                    }
                }
            }
        });

        if (leftPanel != null && rightPanel != null) {
            if (!isFromLogout) {
                leftPanel.setTranslateX(-800);
                rightPanel.setTranslateX(800);
                playLoginInAnimation();
            } else {
                leftPanel.setTranslateX(-800);
                rightPanel.setTranslateX(800);
            }
        }

        if (txtPasswordHidden != null && txtPasswordVisible != null) txtPasswordVisible.textProperty().bindBidirectional(txtPasswordHidden.textProperty());
        if (txtNewPasswordHidden != null && txtNewPasswordVisible != null) txtNewPasswordVisible.textProperty().bindBidirectional(txtNewPasswordHidden.textProperty());
        if (txtConfirmPasswordHidden != null && txtConfirmPasswordVisible != null) txtConfirmPasswordVisible.textProperty().bindBidirectional(txtConfirmPasswordHidden.textProperty());

        if (otp1 != null) {
            otpFields = new TextField[]{otp1, otp2, otp3, otp4, otp5, otp6};
            setupOTPInputs();
        }
        setupInputFocusEffects();

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

            if (isLightMode) {
                createAestheticSun();
            } else {
                createTwinklingStars();
                createRadarPulse();
            }
        }
    }

    // 🌟 Password Hashing Method 🌟
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to hash password", ex);
        }
    }

    public void playLoginInAnimation() {
        if (leftPanel == null || rightPanel == null) return;
        TranslateTransition leftIn = new TranslateTransition(Duration.millis(700), leftPanel);
        leftIn.setToX(0); leftIn.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition rightIn = new TranslateTransition(Duration.millis(700), rightPanel);
        rightIn.setToX(0); rightIn.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition loginIn = new ParallelTransition(leftIn, rightIn);
        loginIn.setOnFinished(e -> isFromLogout = false);
        loginIn.play();
    }

    private void spawnSmokeParticle() {
        Circle smoke = new Circle(3.5, Color.web(isLightMode ? "#00bcd4" : "#31C4B4", isLightMode ? 0.4 : 1.0));
        smoke.setEffect(new GaussianBlur(isLightMode ? 3 : 5));
        smoke.setTranslateX(smallPlane.getTranslateX());
        smoke.setTranslateY(smallPlane.getTranslateY());
        smoke.setTranslateX(smoke.getTranslateX() + (Math.random() - 0.5) * 8);
        smoke.setTranslateY(smoke.getTranslateY() + (Math.random() - 0.5) * 8);
        animationContainer.getChildren().add(animationContainer.getChildren().indexOf(smallPlane), smoke);

        FadeTransition fade = new FadeTransition(Duration.millis(800), smoke); fade.setFromValue(isLightMode ? 0.4 : 0.7); fade.setToValue(0.0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(800), smoke); scale.setFromX(1); scale.setFromY(1); scale.setToX(4.5); scale.setToY(4.5);
        ParallelTransition pt = new ParallelTransition(smoke, fade, scale);
        pt.setOnFinished(ev -> animationContainer.getChildren().remove(smoke));
        pt.play();
    }

    private void createRadarPulse() {
        Circle radarPulse = new Circle(89.0, Color.TRANSPARENT);
        radarPulse.setStroke(Color.web("#31C4B4")); radarPulse.setStrokeWidth(1.5);
        animationContainer.getChildren().add(0, radarPulse);
        ScaleTransition scalePulse = new ScaleTransition(Duration.seconds(2.5), radarPulse); scalePulse.setFromX(1.0); scalePulse.setFromY(1.0); scalePulse.setToX(1.8); scalePulse.setToY(1.8);
        FadeTransition fadePulse = new FadeTransition(Duration.seconds(2.5), radarPulse); fadePulse.setFromValue(0.5); fadePulse.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(scalePulse, fadePulse);
        pt.setCycleCount(Timeline.INDEFINITE); pt.play();
    }

    private void createTwinklingStars() {
        Group starGroup = new Group();
        starGroup.setManaged(false);
        for (int i = 0; i < 80; i++) {
            Circle star = new Circle(Math.random() * 1.5 + 0.8, Color.WHITE);
            star.setTranslateX((Math.random() - 0.5) * 1200); star.setTranslateY(-350 + Math.random() * 530);
            FadeTransition blink = new FadeTransition(Duration.seconds(0.5 + Math.random() * 1.0), star); blink.setFromValue(0.1); blink.setToValue(1.0); blink.setAutoReverse(true); blink.setCycleCount(Timeline.INDEFINITE); blink.setDelay(Duration.seconds(Math.random()));
            blink.play();
            starGroup.getChildren().add(star);
        }
        animationContainer.getChildren().add(0, starGroup);
    }

    private void createAestheticSun() {
        Circle sunOuter = new Circle(140, Color.web("#FFE082", 0.35));
        sunOuter.setTranslateX(-160);
        sunOuter.setTranslateY(-200);
        sunOuter.setEffect(new GaussianBlur(70));

        Circle sunInner = new Circle(60, Color.web("#FFF59D", 0.6));
        sunInner.setTranslateX(-160);
        sunInner.setTranslateY(-200);
        sunInner.setEffect(new GaussianBlur(35));

        animationContainer.getChildren().add(0, sunOuter);
        animationContainer.getChildren().add(1, sunInner);

        ScaleTransition stOuter = new ScaleTransition(Duration.seconds(5), sunOuter);
        stOuter.setFromX(1.0); stOuter.setFromY(1.0);
        stOuter.setToX(1.15); stOuter.setToY(1.15);
        stOuter.setAutoReverse(true); stOuter.setCycleCount(Timeline.INDEFINITE);

        ScaleTransition stInner = new ScaleTransition(Duration.seconds(5), sunInner);
        stInner.setFromX(1.0); stInner.setFromY(1.0);
        stInner.setToX(1.1); stInner.setToY(1.1);
        stInner.setAutoReverse(true); stInner.setCycleCount(Timeline.INDEFINITE);

        stOuter.play();
        stInner.play();
    }

    private void setupOTPInputs() {
        for (int i = 0; i < otpFields.length; i++) {
            TextField currentField = otpFields[i];
            final int index = i;
            currentField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) { currentField.setText(newValue.replaceAll("[^\\d]", "")); return; }
                if (newValue.length() > 1) { currentField.setText(newValue.substring(0, 1)); return; }
                if (newValue.length() == 1 && index < otpFields.length - 1) otpFields[index + 1].requestFocus();
            });
            currentField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && currentField.getText().isEmpty() && index > 0) otpFields[index - 1].requestFocus();
            });
        }
    }

    private void setupInputFocusEffects() {
        TextField[] fields = {txtUsername, txtPasswordHidden, txtPasswordVisible, emailField, txtNewPasswordHidden, txtNewPasswordVisible, txtConfirmPasswordHidden, txtConfirmPasswordVisible};
        for (TextField field : fields) {
            if (field != null) {
                field.focusedProperty().addListener((obs, oldVal, isFocused) -> {
                    Parent parent = field.getParent();
                    HBox inputBox = null;
                    if (parent instanceof HBox) inputBox = (HBox) parent;
                    else if (parent.getParent() instanceof HBox) inputBox = (HBox) parent.getParent();
                    if (inputBox != null) {
                        if (isFocused) { if (!inputBox.getStyleClass().contains("focused-box")) inputBox.getStyleClass().add("focused-box"); }
                        else { inputBox.getStyleClass().remove("focused-box"); }
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
        if(targetSection != null) { targetSection.setVisible(true); targetSection.setManaged(true); }
    }

    private void handleEnterKey(KeyEvent event) { if (event.getCode() == KeyCode.ENTER) attemptLogin(); }

    @FXML void clickLogin(ActionEvent event) { attemptLogin(); }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPasswordHidden.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        // 🌟 Username ကိုသာ အရင်ရှာပြီး Database Persistent Lock ကို စစ်ဆေးပါမည် 🌟
        String query = "SELECT user_id, role, image_path, status, password, failed_attempts FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");
                String imagePath = rs.getString("image_path");
                String status = rs.getString("status");
                String dbPassword = rs.getString("password");
                int dbFailedAttempts = rs.getInt("failed_attempts");

                // Account Inactive စစ်ဆေးခြင်း
                if (status == null || !status.equalsIgnoreCase("Active")) {
                    insertLog(userId, "Failed Login: Account Inactive");
                    showAlert(Alert.AlertType.ERROR, "Account Suspended", "Your account is currently inactive.\nPlease contact the administrator.");
                    return;
                }

                // 🌟 Persistent Database Lock Check 🌟
                if (dbFailedAttempts >= MAX_ATTEMPTS) {
                    showAlert(Alert.AlertType.ERROR, "Account Locked", "You have exceeded the maximum login attempts. Please click 'Forgot Password' to reset your password.");
                    switchSection(emailRequestSection);
                    return;
                }

                // 🌟 Hashed Password တိုက်စစ်ခြင်း 🌟
                String hashedInputPassword = hashPassword(password);

                if (hashedInputPassword.equals(dbPassword)) {
                    // Password မှန်သွားလျှင် Failed Attempts ကို Database တွင် Reset လုပ်မည်
                    updateFailedAttempts(conn, username, 0);

                    loggedInUser = new com.pos.flightmanagementsystem.model.User();
                    loggedInUser.setUserId(userId);
                    loggedInUser.setRole(role);
                    insertLog(userId, "Login Successful");

                    Stage stage = (Stage) txtUsername.getScene().getWindow();

                    if (role.equalsIgnoreCase("Super Admin") || role.equalsIgnoreCase("superadmin")) {
                        loadDashboardWithSmoothAnimation(stage, "/com/pos/flightmanagementsystem/view/SuperAdmin.fxml", username, role, imagePath);
                    } else if (role.equalsIgnoreCase("Admin")) {
                        loadDashboardWithSmoothAnimation(stage, "/com/pos/flightmanagementsystem/view/Admin.fxml", username, role, imagePath);
                    } else if (role.equalsIgnoreCase("Staff")) {
                        loadDashboardWithSmoothAnimation(stage, "/com/pos/flightmanagementsystem/view/Staff.fxml", username, role, imagePath);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Access Denied", "Unknown role assigned to your account.");
                    }
                } else {
                    // Password မှားသွားလျှင် Database ထဲ၌ Failed Attempts တိုးမည်
                    int newAttempts = dbFailedAttempts + 1;
                    updateFailedAttempts(conn, username, newAttempts);

                    if (newAttempts >= MAX_ATTEMPTS) {
                        insertLog(userId, "Failed to login 3 times. Account locked. SSPR required.");
                        showAlert(Alert.AlertType.ERROR, "Account Locked", "You have entered the wrong password 3 times. For security reasons, please reset your password via email.");
                        switchSection(emailRequestSection);
                    } else {
                        int attemptsLeft = MAX_ATTEMPTS - newAttempts;
                        insertLog(userId, "Login Failed (" + newAttempts + "/3 attempts)");
                        showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Username or Password.\nYou have " + attemptsLeft + " attempt(s) left.");
                    }
                }
            } else {
                // Username မရှိလျှင် (Generic Message ပြမည်)
                insertLog(0, "Login Failed: Unknown username attempt");
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Username or Password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
        }
    }

    private void updateFailedAttempts(Connection conn, String username, int attempts) throws SQLException {
        String updateQuery = "UPDATE users SET failed_attempts = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setInt(1, attempts);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {
            if (userId > 0) pstmt.setInt(1, userId);
            else pstmt.setNull(1, java.sql.Types.INTEGER);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void applyThemeRecursively(Parent node, boolean lightMode) {
        if (node == null) return;
        if (node.getStylesheets() != null && !node.getStylesheets().isEmpty()) {
            ObservableList<String> styles = node.getStylesheets();
            for (int i = 0; i < styles.size(); i++) {
                String cssPath = styles.get(i);
                String newCssPath = null;
                if (!lightMode && cssPath.contains("Light")) {
                    newCssPath = cssPath.replace("Light", "Dark");
                } else if (lightMode && cssPath.contains("Dark")) {
                    newCssPath = cssPath.replace("Dark", "Light");
                }

                if (newCssPath != null) {
                    try {
                        String resourceName = newCssPath.substring(newCssPath.indexOf("/com/pos/"));
                        if (getClass().getResource(resourceName) != null) {
                            styles.set(i, newCssPath);
                        }
                    } catch (Exception e) {}
                }
            }
        }
        for (Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                applyThemeRecursively((Parent) child, lightMode);
            }
        }
    }

    private void loadDashboardWithSmoothAnimation(Stage stage, String fxmlPath, String username, String role, String imagePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent dashboardRoot = loader.load();

            applyThemeRecursively(dashboardRoot, isLightMode);

            Object controller = loader.getController();
            if (controller instanceof AdminController) {
                ((AdminController) controller).setLoggedInUser(username, role, imagePath);
            } else if (controller instanceof SuperAdminController) {
                ((SuperAdminController) controller).setLoggedInUser(username, role, imagePath);
            } else if (controller instanceof StaffController) {
                ((StaffController) controller).setLoggedInUser(username, role, imagePath);
            }

            Scene scene = txtUsername.getScene();
            Parent loginRoot = scene.getRoot();
            loginRoot.setStyle("-fx-background-color: transparent;");

            StackPane transitionContainer = new StackPane();
            transitionContainer.setStyle("-fx-background-color: " + (isLightMode ? "#f4f9f9" : "#06080D") + ";");
            transitionContainer.getChildren().addAll(dashboardRoot, loginRoot);

            scene.setRoot(transitionContainer);
            if (!stage.isMaximized()) stage.setMaximized(true);

            try {
                controller.getClass().getMethod("prepareIntroAnimation").invoke(controller);
            } catch (Exception ignored) {}

            TranslateTransition leftOut = new TranslateTransition(Duration.millis(700), leftPanel);
            leftOut.setToX(-800); leftOut.setInterpolator(Interpolator.EASE_BOTH);

            TranslateTransition rightOut = new TranslateTransition(Duration.millis(700), rightPanel);
            rightOut.setToX(800); rightOut.setInterpolator(Interpolator.EASE_BOTH);

            ParallelTransition loginAnimOut = new ParallelTransition(leftOut, rightOut);

            loginAnimOut.setOnFinished(e -> {
                transitionContainer.getChildren().remove(dashboardRoot);
                scene.setRoot(dashboardRoot);
                try {
                    controller.getClass().getMethod("playIntroAnimation").invoke(controller);
                } catch (Exception ex) { ex.printStackTrace(); }
            });

            loginAnimOut.play();

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
            icon.setFill(Color.web(isLightMode ? "#00bcd4" : "#31C4B4"));
        } else {
            visibleField.setVisible(false); hiddenField.setVisible(true);
            icon.setContent("M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z");
            icon.setFill(Color.web("#8B95A5"));
        }
    }

    @FXML
    void handleSendEmail(ActionEvent event) {
        String targetEmail = emailField.getText().trim();
        if (targetEmail.isEmpty() || !targetEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            statusLabel.setTextFill(Color.web("#FF4B72")); statusLabel.setText("Please enter a valid email address."); return;
        }

        // 🌟 OTP Rate Limiting Check (၁ မိနစ်အတွင်း တစ်ကြိမ်သာ) 🌟
        if (lastOtpRequestTime != null && java.time.Duration.between(lastOtpRequestTime, LocalDateTime.now()).getSeconds() < 60) {
            statusLabel.setTextFill(Color.web("#FF4B72"));
            statusLabel.setText("Please wait 60 seconds before requesting another OTP.");
            return;
        }

        statusLabel.setTextFill(Color.web(isLightMode ? "#0097a7" : "#31C4B4")); statusLabel.setText("Sending OTP... Please wait.");
        sendButton.setDisable(true);

        generatedOTP = String.format("%06d", new Random().nextInt(999999));
        lastOtpRequestTime = LocalDateTime.now();
        otpGeneratedTime = LocalDateTime.now();

        new Thread(() -> {
            boolean isSent = sendEmail(targetEmail, generatedOTP);
            Platform.runLater(() -> {
                sendButton.setDisable(false);
                if (isSent) {
                    statusLabel.setTextFill(Color.web("#10B981")); statusLabel.setText("OTP sent successfully. Valid for 5 minutes."); switchSection(otpSection);
                } else {
                    // Failed to send, reset timer to allow immediate retry
                    lastOtpRequestTime = null;
                    statusLabel.setTextFill(Color.web("#FF4B72")); statusLabel.setText("Failed to send OTP. Check your connection.");
                }
            });
        }).start();
    }

    @FXML
    void handleVerifyOTP(ActionEvent event) {
        String enteredOTP = otp1.getText() + otp2.getText() + otp3.getText() + otp4.getText() + otp5.getText() + otp6.getText();
        if (enteredOTP.length() < 6) { statusLabel.setTextFill(Color.web("#FF4B72")); statusLabel.setText("Please enter the complete 6-digit OTP."); return; }

        // 🌟 OTP Expiration Check (၅ မိနစ် ကျော်လွန်သွားပါက Invalid ဖြစ်မည်) 🌟
        if (otpGeneratedTime == null || java.time.Duration.between(otpGeneratedTime, LocalDateTime.now()).toMinutes() > 5) {
            statusLabel.setTextFill(Color.web("#FF4B72"));
            statusLabel.setText("Your OTP has expired. Please request a new one.");
            return;
        }

        if (enteredOTP.equals(generatedOTP)) {
            statusLabel.setTextFill(Color.web("#10B981")); statusLabel.setText("OTP Verified Successfully!");
            verifyButton.setDisable(true); switchSection(resetPasswordSection); statusLabel.setText("");
        } else {
            statusLabel.setTextFill(Color.web("#FF4B72")); statusLabel.setText("Invalid OTP. Please try again.");
        }
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String newPassword = txtNewPasswordHidden.getText().trim();
        String confirmPassword = txtConfirmPasswordHidden.getText().trim();
        String targetEmail = emailField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in both password fields."); return; }
        if (!newPassword.equals(confirmPassword)) { showAlert(Alert.AlertType.ERROR, "Password Mismatch", "The new passwords do not match."); return; }

        // 🌟 New Password ကို Encrypt ပြုလုပ်ပြီးမှ သိမ်းမည်၊ ထို့ပြင် Database Persistent Lock ကို ပြန်ဖွင့်ပေးမည် 🌟
        String hashedNewPassword = hashPassword(newPassword);
        String updateQuery = "UPDATE users SET password = ?, failed_attempts = 0, locked_until = NULL WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, hashedNewPassword);
            pstmt.setString(2, targetEmail);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                txtNewPasswordHidden.clear(); txtNewPasswordVisible.clear(); txtConfirmPasswordHidden.clear(); txtConfirmPasswordVisible.clear(); emailField.clear();
                for (TextField tf : otpFields) tf.clear();
                verifyButton.setDisable(false); statusLabel.setText("");
                playSuccessAnimation();
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "No account found associated with this email address.");
            }
        } catch (SQLException e) {
            e.printStackTrace(); showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update your password.");
        }
    }

    private void playSuccessAnimation() {
        successOverlay.setVisible(true); successOverlay.setOpacity(0.0); successBox.setScaleX(0.5); successBox.setScaleY(0.5);
        FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), successOverlay); fadeOverlay.setToValue(1.0);
        ScaleTransition scaleBox = new ScaleTransition(Duration.millis(500), successBox); scaleBox.setToX(1.0); scaleBox.setToY(1.0);
        scaleBox.setInterpolator(new Interpolator() { @Override protected double curve(double t) { double tension = 2.0; t -= 1.0; return t * t * ((tension + 1) * t + tension) + 1.0; } });
        FadeTransition fadeBox = new FadeTransition(Duration.millis(300), successBox); fadeBox.setFromValue(0.0); fadeBox.setToValue(1.0);
        ParallelTransition pt = new ParallelTransition(fadeOverlay, scaleBox, fadeBox); pt.play();
    }

    @FXML
    void closeSuccessDialog(ActionEvent event) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), successBox); scaleDown.setToX(0.8); scaleDown.setToY(0.8); scaleDown.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), successOverlay); fadeOut.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setOnFinished(e -> { successOverlay.setVisible(false); switchSection(loginSection); }); pt.play();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            customAlertTitle.setText(title); customAlertMessage.setText(message);
            customAlertBox.getStyleClass().removeAll("alert-error", "alert-warning", "alert-info");

            if (alertType == Alert.AlertType.ERROR) {
                customAlertIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z");
                customAlertIcon.setFill(Color.web("#FF4B72")); customAlertBox.getStyleClass().add("alert-error");
            } else if (alertType == Alert.AlertType.WARNING) {
                customAlertIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
                customAlertIcon.setFill(Color.web("#F59E0B")); customAlertBox.getStyleClass().add("alert-warning");
            } else {
                customAlertIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
                customAlertIcon.setFill(Color.web(isLightMode ? "#00bcd4" : "#31C4B4")); customAlertBox.getStyleClass().add("alert-info");
            }

            customAlertOverlay.setVisible(true); customAlertOverlay.setOpacity(0.0); customAlertBox.setScaleX(0.5); customAlertBox.setScaleY(0.5);
            FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), customAlertOverlay); fadeOverlay.setToValue(1.0);
            ScaleTransition scaleBox = new ScaleTransition(Duration.millis(400), customAlertBox); scaleBox.setToX(1.0); scaleBox.setToY(1.0);
            scaleBox.setInterpolator(new Interpolator() { @Override protected double curve(double t) { double tension = 2.0; t -= 1.0; return t * t * ((tension + 1) * t + tension) + 1.0; } });
            FadeTransition fadeBox = new FadeTransition(Duration.millis(300), customAlertBox); fadeBox.setFromValue(0.0); fadeBox.setToValue(1.0);
            ParallelTransition pt = new ParallelTransition(fadeOverlay, scaleBox, fadeBox); pt.play();
        });
    }

    @FXML
    void closeCustomAlert(ActionEvent event) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), customAlertBox); scaleDown.setToX(0.8); scaleDown.setToY(0.8); scaleDown.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), customAlertOverlay); fadeOut.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setOnFinished(e -> customAlertOverlay.setVisible(false)); pt.play();
    }

    private boolean sendEmail(String recipientEmail, String otpCode) {
        Properties properties = new Properties(); properties.put("mail.smtp.auth", "true"); properties.put("mail.smtp.starttls.enable", "true"); properties.put("mail.smtp.host", "smtp.gmail.com"); properties.put("mail.smtp.port", "587");
        Session session = Session.getInstance(properties, new Authenticator() { @Override protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(senderEmail, senderPassword); } });
        try {
            Message message = new MimeMessage(session); message.setFrom(new InternetAddress(senderEmail)); message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Password Reset OTP - ArrLuuu Airline"); message.setText("Dear User,\n\nYour OTP for password reset is: " + otpCode + "\n\nPlease do not share this code with anyone.\n\nBest Regards,\nArrLuuu Airline Team");
            Transport.send(message); return true;
        } catch (MessagingException e) { e.printStackTrace(); return false; }
    }
}