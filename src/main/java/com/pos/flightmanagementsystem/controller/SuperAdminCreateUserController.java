package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SuperAdminCreateUserController implements Initializable {

    @FXML private ImageView profileImageView;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private SVGPath eyeIcon;
    @FXML private ComboBox<String> comboRole;
    @FXML private Button btnChooseImage;
    @FXML private Button btnCancel;

    private Runnable cancelAnimationAction;
    private Runnable successAction;
    private File selectedImageFile;

    private boolean isPasswordVisible = false;
    private final String EYE_OPEN = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private final String EYE_CLOSED = "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboRole.getItems().addAll("Admin", "Staff");

        Circle clip = new Circle(50, 50, 50);
        profileImageView.setClip(clip);

        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
    }

    // 🌟 Password Hashing Method (SHA-256) 🌟
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

    @FXML
    void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            eyeIcon.setContent(EYE_OPEN);
        } else {
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            eyeIcon.setContent(EYE_CLOSED);
        }
    }

    public void setCancelAnimationAction(Runnable cancelAnimationAction) {
        this.cancelAnimationAction = cancelAnimationAction;
    }

    public void setSuccessAction(Runnable successAction) {
        this.successAction = successAction;
    }

    @FXML
    void clickCancel(ActionEvent event) {
        if (cancelAnimationAction != null) {
            cancelAnimationAction.run();
        }
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnChooseImage.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            Image image = new Image(selectedImageFile.toURI().toString());
            profileImageView.setImage(image);
        }
    }

    @FXML
    void saveUser(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String role = comboRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showWarningModal("Validation Error", "Please fill all required fields (Username, Password, Role)!", false);
            return;
        }

        String dbRole = role.toLowerCase();
        String imageFileNameForDB = null;

        // 🌟 Encrypt Password 🌟
        String hashedPassword = hashPassword(password);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showWarningModal("Database Error", "Failed to connect to the database.", true);
                return;
            }

            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showWarningModal("Duplicate Username", "The username '" + username + "' is already taken. Please choose another.", false);
                        return;
                    }
                }
            }

            if (selectedImageFile != null) {
                String destDirPath = "src/main/resources/com/pos/flightmanagementsystem/image/";
                File destDir = new File(destDirPath);

                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                String originalFileName = selectedImageFile.getName();
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = "profile_" + System.currentTimeMillis() + extension;

                Path destPath = Paths.get(destDirPath + newFileName);
                Files.copy(selectedImageFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                imageFileNameForDB = newFileName;
            }

            String sql = "INSERT INTO users (username, email, password, role, image_path, failed_attempts) VALUES (?, ?, ?, ?, ?, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, email.isEmpty() ? null : email);
                pstmt.setString(3, hashedPassword); // 🌟 Hashed Password သိမ်းမည် 🌟
                pstmt.setString(4, dbRole);
                pstmt.setString(5, imageFileNameForDB);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    // 🌟 Admin မှ User အသစ် Create လုပ်ကြောင်း Log မှတ်ခြင်း 🌟
                    int adminId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;
                    insertLog(adminId, "Created new user account: " + username + " (" + dbRole + ")");

                    if (successAction != null) {
                        successAction.run();
                    } else if (cancelAnimationAction != null) {
                        cancelAnimationAction.run();
                    }
                }
            }

        } catch (IOException e) {
            showWarningModal("File Error", "Failed to save the image file.", true);
            e.printStackTrace();
        } catch (SQLException e) {
            showWarningModal("Database Error", "Failed to save user data.\n" + e.getMessage(), true);
            e.printStackTrace();
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
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showWarningModal(String title, String message, boolean isError) {
        StackPane windowRoot = null;

        if (btnCancel.getScene() != null && btnCancel.getScene().getRoot() instanceof StackPane) {
            windowRoot = (StackPane) btnCancel.getScene().getRoot();
        } else {
            Parent parent = btnCancel.getParent();
            while (parent != null) {
                if (parent instanceof StackPane) {
                    windowRoot = (StackPane) parent;
                }
                parent = parent.getParent();
            }
        }

        if (windowRoot == null) return;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        Node mainContent = windowRoot.getChildren().get(0);
        BoxBlur blur = new BoxBlur(8, 8, 3);
        mainContent.setEffect(blur);

        String themeColor = isError ? "#EF4444" : "#F59E0B";
        String hoverBgColor = isError ? "rgba(239, 68, 68, 0.15)" : "rgba(245, 158, 11, 0.15)";

        String svgIconPath = isError
                ? "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"
                : "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z";

        VBox modalCard = new VBox(20);
        modalCard.setMaxSize(420, Region.USE_PREF_SIZE);
        modalCard.setStyle("-fx-background-color: #0B0E14; -fx-border-color: " + themeColor + "; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 24px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 10);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + themeColor + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane closeBtn = new StackPane();
        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        closeIcon.setStyle("-fx-fill: #8B95A5; -fx-scale-x: 0.8; -fx-scale-y: 0.8;");
        closeBtn.getChildren().add(closeIcon);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnMouseEntered(e -> closeIcon.setStyle("-fx-fill: #FFFFFF; -fx-scale-x: 0.8; -fx-scale-y: 0.8;"));
        closeBtn.setOnMouseExited(e -> closeIcon.setStyle("-fx-fill: #8B95A5; -fx-scale-x: 0.8; -fx-scale-y: 0.8;"));

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        HBox body = new HBox(15);
        body.setAlignment(Pos.CENTER_LEFT);
        SVGPath warningIcon = new SVGPath();
        warningIcon.setContent(svgIconPath);
        warningIcon.setStyle("-fx-fill: " + themeColor + "; -fx-scale-x: 1.5; -fx-scale-y: 1.5;");
        StackPane iconPane = new StackPane(warningIcon);
        iconPane.setPadding(new Insets(0, 10, 0, 10));

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        body.getChildren().addAll(iconPane, messageLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button okBtn = new Button("OK");
        okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + themeColor + "; -fx-text-fill: " + themeColor + "; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;");
        okBtn.setOnMouseEntered(e -> okBtn.setStyle("-fx-background-color: " + hoverBgColor + "; -fx-border-color: " + themeColor + "; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;"));
        okBtn.setOnMouseExited(e -> okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + themeColor + "; -fx-text-fill: " + themeColor + "; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;"));

        footer.getChildren().add(okBtn);

        modalCard.getChildren().addAll(header, body, footer);
        overlay.getChildren().add(modalCard);
        windowRoot.getChildren().add(overlay);

        modalCard.setScaleX(0.5);
        modalCard.setScaleY(0.5);
        modalCard.setOpacity(0);
        overlay.setOpacity(0);

        ScaleTransition stIn = new ScaleTransition(Duration.millis(300), modalCard);
        stIn.setToX(1); stIn.setToY(1); stIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ftIn = new FadeTransition(Duration.millis(300), modalCard);
        ftIn.setToValue(1);

        FadeTransition bgFtIn = new FadeTransition(Duration.millis(300), overlay);
        bgFtIn.setToValue(1);

        ParallelTransition ptIn = new ParallelTransition(stIn, ftIn, bgFtIn);
        ptIn.play();

        StackPane finalWindowRoot = windowRoot;
        Runnable closeAction = () -> {
            ScaleTransition stOut = new ScaleTransition(Duration.millis(200), modalCard);
            stOut.setToX(0.5); stOut.setToY(0.5); stOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition ftOut = new FadeTransition(Duration.millis(200), modalCard);
            ftOut.setToValue(0);

            FadeTransition bgFtOut = new FadeTransition(Duration.millis(200), overlay);
            bgFtOut.setToValue(0);

            ParallelTransition ptOut = new ParallelTransition(stOut, ftOut, bgFtOut);
            ptOut.setOnFinished(ev -> {
                finalWindowRoot.getChildren().remove(overlay);
                mainContent.setEffect(null);
            });
            ptOut.play();
        };

        closeBtn.setOnMouseClicked(e -> closeAction.run());
        okBtn.setOnAction(e -> closeAction.run());
    }
}