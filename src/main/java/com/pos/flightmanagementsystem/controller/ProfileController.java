package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class ProfileController implements Initializable {

    @FXML private StackPane profileRoot;
    @FXML private StackPane profileView;
    @FXML private StackPane modalOverlay;
    @FXML private HBox notificationToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastMessage;

    // 🌟 Profile Info Labels & Avatar 🌟
    @FXML private Circle profileAvatarCircle;
    @FXML private Label lblFullName;
    @FXML private Label lblUsernameCard;
    @FXML private Label lblEmail;
    @FXML private Label lblRole;
    @FXML private Label lblEmployeeId;
    @FXML private Label lblClearanceRole;
    @FXML private Label lblSectionSubtitle;

    // 🌟 Edit Mode Controls 🌟
    @FXML private Button btnEditProfile;
    @FXML private Button btnChangeAvatar;
    @FXML private HBox editActionBox;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private VBox cardUsername;
    @FXML private VBox cardEmail;

    private Parent pwdChangeView;
    private String loggedInUser = "";
    private String currentEmail = "";
    private String currentRole = "";
    private String currentImagePath = "";
    private int currentUserId = -1;

    private File selectedAvatarFile = null;
    private boolean isEditMode = false;

    private Runnable closeCallback;
    // 🌟 Top Navbar သို့ Real-Time လှမ်းပြောင်းပေးမည့် Callback 🌟
    private BiConsumer<String, String> profileUpdateCallback;

    public void setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    public void setOnProfileUpdateCallback(BiConsumer<String, String> profileUpdateCallback) {
        this.profileUpdateCallback = profileUpdateCallback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Parent controller မှ setUserData ခေါ်သည်ကို စောင့်ဆိုင်းမည်
    }

    // ==========================================
    // 🌟 ADMIN / STAFF / SUPERADMIN မှ USER DATA လက်ခံရန် 🌟
    // ==========================================
    public void setUserData(String username, String role, String imagePath) {
        // အကယ်၍ LoginController.loggedInUser တွင် အသစ်ပြောင်းထားသော နာမည်/ပုံ ရှိနေပါက ၎င်းကို ဦးစားပေးယူမည်
        String effectiveUsername = username;
        String effectiveImagePath = imagePath != null ? imagePath : "";

        if (LoginController.loggedInUser != null) {
            String globalUser = LoginController.loggedInUser.getUsername();
            String globalImg = LoginController.loggedInUser.getImagePath();
            if (globalUser != null && !globalUser.isEmpty()) {
                effectiveUsername = globalUser;
            }
            if (globalImg != null && !globalImg.isEmpty()) {
                effectiveImagePath = globalImg;
            }
        }

        this.loggedInUser = effectiveUsername;
        this.currentRole = role != null ? role.toUpperCase() : "STAFF";
        this.currentImagePath = effectiveImagePath;

        lblFullName.setText(this.loggedInUser);
        if (lblUsernameCard != null) lblUsernameCard.setText(this.loggedInUser);
        lblRole.setText(this.currentRole);
        if (lblClearanceRole != null) {
            lblClearanceRole.setText(this.currentRole + " ACCESS");
        }

        loadAvatarImage(this.currentImagePath);

        new Thread(() -> loadDatabaseInfo(this.loggedInUser)).start();
    }

    private void loadAvatarImage(String imagePath) {
        if (profileAvatarCircle == null || imagePath == null || imagePath.isEmpty()) {
            return;
        }
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

    private void loadDatabaseInfo(String username) {
        String query = "SELECT user_id, username, email, role, image_path FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String dbUsername = rs.getString("username");
                    String email = rs.getString("email");
                    String dbRole = rs.getString("role");
                    String dbImg = rs.getString("image_path");

                    this.currentUserId = userId;
                    this.currentEmail = (email != null && !email.isEmpty()) ? email : "";
                    if (dbRole != null && !dbRole.isEmpty()) {
                        this.currentRole = dbRole.toUpperCase();
                    }

                    Platform.runLater(() -> {
                        if (dbUsername != null && !dbUsername.isEmpty()) {
                            this.loggedInUser = dbUsername;
                            lblFullName.setText(dbUsername);
                            if (lblUsernameCard != null) lblUsernameCard.setText(dbUsername);
                        }
                        lblEmail.setText(!currentEmail.isEmpty() ? currentEmail : "No Email Provided");
                        lblEmployeeId.setText(String.format("EMP-%04d", userId));
                        lblRole.setText(this.currentRole);
                        if (lblClearanceRole != null) {
                            lblClearanceRole.setText(this.currentRole + " ACCESS");
                        }
                        if (dbImg != null && !dbImg.isEmpty()) {
                            currentImagePath = dbImg;
                            loadAvatarImage(currentImagePath);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 🌟 SUPERADMIN ဟုတ်/မဟုတ် စစ်ဆေးခြင်း နှင့် LOG မှတ်ခြင်း 🌟
    // ==========================================
    private boolean isSuperAdmin() {
        if (currentRole == null) return false;
        String normalizedRole = currentRole.replace(" ", "").replace("_", "").toLowerCase();
        return normalizedRole.equals("superadmin");
    }

    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {

            int finalUserId = userId;
            if (finalUserId <= 0 && LoginController.loggedInUser != null) {
                finalUserId = LoginController.loggedInUser.getUserId();
            }

            if (finalUserId > 0) {
                pstmt.setInt(1, finalUserId);
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, action);
            pstmt.executeUpdate();

            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 🌟 PROFILE EDIT MODE & IMAGE UPLOAD 🌟
    // ==========================================
    @FXML
    void toggleEditMode(ActionEvent event) {
        setEditMode(!isEditMode);
    }

    private void setEditMode(boolean enable) {
        this.isEditMode = enable;

        if (enable) {
            txtUsername.setText(loggedInUser);
            txtEmail.setText(currentEmail);

            lblUsernameCard.setVisible(false);
            lblUsernameCard.setManaged(false);
            txtUsername.setVisible(true);
            txtUsername.setManaged(true);

            lblEmail.setVisible(false);
            lblEmail.setManaged(false);
            txtEmail.setVisible(true);
            txtEmail.setManaged(true);

            editActionBox.setVisible(true);
            editActionBox.setManaged(true);
            btnEditProfile.setDisable(true);

            if (!cardUsername.getStyleClass().contains("info-mini-card-editing")) {
                cardUsername.getStyleClass().add("info-mini-card-editing");
            }
            if (!cardEmail.getStyleClass().contains("info-mini-card-editing")) {
                cardEmail.getStyleClass().add("info-mini-card-editing");
            }
            if (lblSectionSubtitle != null) {
                lblSectionSubtitle.setText("Editing mode active — update your name, email, or photo below");
            }
            txtUsername.requestFocus();
        } else {
            lblUsernameCard.setVisible(true);
            lblUsernameCard.setManaged(true);
            txtUsername.setVisible(false);
            txtUsername.setManaged(false);

            lblEmail.setVisible(true);
            lblEmail.setManaged(true);
            txtEmail.setVisible(false);
            txtEmail.setManaged(false);

            editActionBox.setVisible(false);
            editActionBox.setManaged(false);
            btnEditProfile.setDisable(false);

            cardUsername.getStyleClass().remove("info-mini-card-editing");
            cardEmail.getStyleClass().remove("info-mini-card-editing");
            if (lblSectionSubtitle != null) {
                lblSectionSubtitle.setText("View and manage your registered airline staff details");
            }
        }
    }

    @FXML
    void cancelEditMode(ActionEvent event) {
        selectedAvatarFile = null;
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            loadAvatarImage(currentImagePath);
        } else {
            profileAvatarCircle.setFill(Color.web("#31C4B4"));
        }
        setEditMode(false);
    }

    @FXML
    void chooseAvatarImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(profileRoot.getScene().getWindow());
        if (file != null) {
            selectedAvatarFile = file;
            Image previewImg = new Image(file.toURI().toString());
            profileAvatarCircle.setFill(new ImagePattern(previewImg));

            if (!isEditMode) {
                setEditMode(true);
            }
        }
    }

    @FXML
    void saveProfileChanges(ActionEvent event) {
        String newUsername = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String newEmail = txtEmail.getText() != null ? txtEmail.getText().trim() : "";

        if (newUsername.isEmpty()) {
            showCustomNotification("Validation Error", "Display Name cannot be empty.");
            return;
        }
        if (newEmail.isEmpty() || !newEmail.contains("@")) {
            showCustomNotification("Validation Error", "Please enter a valid email address.");
            return;
        }

        final String oldUsername = this.loggedInUser;
        final String oldEmail = this.currentEmail != null ? this.currentEmail : "";
        final boolean isAvatarChanged = (selectedAvatarFile != null);

        List<String> changedDetails = new ArrayList<>();
        if (!oldUsername.equals(newUsername)) {
            changedDetails.add("Username: '" + oldUsername + "' -> '" + newUsername + "'");
        }
        if (!oldEmail.equals(newEmail)) {
            changedDetails.add("Email: '" + (oldEmail.isEmpty() ? "None" : oldEmail) + "' -> '" + newEmail + "'");
        }
        if (isAvatarChanged) {
            changedDetails.add("Profile Picture");
        }

        if (changedDetails.isEmpty()) {
            setEditMode(false);
            return;
        }

        String finalImageName = currentImagePath;

        if (isAvatarChanged) {
            try {
                String ext = "";
                String origName = selectedAvatarFile.getName();
                int dotIdx = origName.lastIndexOf('.');
                if (dotIdx >= 0) {
                    ext = origName.substring(dotIdx);
                }
                finalImageName = "avatar_" + System.currentTimeMillis() + ext;

                Path srcDir = Path.of("src/main/resources/com/pos/flightmanagementsystem/image");
                if (!Files.exists(srcDir)) {
                    Files.createDirectories(srcDir);
                }
                Files.copy(selectedAvatarFile.toPath(), srcDir.resolve(finalImageName), StandardCopyOption.REPLACE_EXISTING);

                Path targetDir = Path.of("target/classes/com/pos/flightmanagementsystem/image");
                if (Files.exists(targetDir)) {
                    Files.copy(selectedAvatarFile.toPath(), targetDir.resolve(finalImageName), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final String savedImageName = finalImageName;

        new Thread(() -> {
            boolean success = updateProfileInDatabase(oldUsername, newUsername, newEmail, savedImageName);
            if (success) {
                if (!isSuperAdmin()) {
                    String logMessage = String.format("[%s] %s updated profile (%s)",
                            currentRole, newUsername, String.join(", ", changedDetails));
                    insertLog(currentUserId, logMessage);
                }
            }

            Platform.runLater(() -> {
                if (success) {
                    this.loggedInUser = newUsername;
                    this.currentEmail = newEmail;
                    this.currentImagePath = savedImageName;
                    this.selectedAvatarFile = null;

                    lblFullName.setText(newUsername);
                    lblUsernameCard.setText(newUsername);
                    lblEmail.setText(newEmail);

                    // 🌟 1. Global User Object ကို Update လုပ်ခြင်း 🌟
                    updateGlobalLoggedInUser(newUsername, newEmail, savedImageName);

                    // 🌟 2. Parent Controller (SuperAdminController) ၏ Callback ကို ခေါ်ခြင်း 🌟
                    if (profileUpdateCallback != null) {
                        profileUpdateCallback.accept(newUsername, savedImageName);
                    }

                    // 🌟 3. လက်ရှိ Window (Scene) ထဲရှိ ညာဘက်အပေါ်ထောင့် Navbar မှ နာမည်နှင့်ပုံကို တိုက်ရိုက် ပြောင်းလဲခြင်း 🌟
                    updateTopNavInScene(newUsername, savedImageName);

                    setEditMode(false);
                    showCustomNotification("Profile Updated!", "Your account information has been saved.");
                } else {
                    showCustomNotification("Update Failed", "Could not update profile. Username may already exist.");
                }
            });
        }).start();
    }

    // 🌟 SuperAdmin, Admin, Staff အားလုံး၏ Top Navbar ရှိ နာမည်နှင့်ပုံကို Logout ထွက်စရာမလိုဘဲ ချက်ချင်းပြောင်းပေးမည့် Method 🌟
    private void updateTopNavInScene(String newUsername, String newImageName) {
        try {
            if (profileRoot == null || profileRoot.getScene() == null) return;
            Scene scene = profileRoot.getScene();

            // Top Navbar Username Label ကို ရှာ၍ ချက်ချင်းပြောင်းမည်
            Node nameNode = scene.lookup("#topNavUsername");
            if (nameNode instanceof Label topNavLabel) {
                topNavLabel.setText(newUsername);
            }

            // Top Navbar Avatar Circle ကို ရှာ၍ ပုံအသစ် ချက်ချင်းပြောင်းမည်
            if (newImageName != null && !newImageName.isEmpty()) {
                Node avatarNode = scene.lookup("#topNavAvatarCircle");
                if (avatarNode instanceof Circle topNavCircle) {
                    File imgFile = new File("src/main/resources/com/pos/flightmanagementsystem/image/" + newImageName);
                    if (imgFile.exists()) {
                        Image img = new Image(imgFile.toURI().toString());
                        topNavCircle.setFill(new ImagePattern(img));
                    } else {
                        URL url = getClass().getResource("/com/pos/flightmanagementsystem/image/" + newImageName);
                        if (url != null) {
                            Image img = new Image(url.toExternalForm());
                            topNavCircle.setFill(new ImagePattern(img));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean updateProfileInDatabase(String oldUsername, String newUsername, String newEmail, String imageName) {
        String sqlById = "UPDATE users SET username = ?, email = ?, image_path = ? WHERE user_id = ?";
        String sqlByName = "UPDATE users SET username = ?, email = ?, image_path = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(currentUserId > 0 ? sqlById : sqlByName)) {
                pstmt.setString(1, newUsername);
                pstmt.setString(2, newEmail);
                pstmt.setString(3, imageName);
                if (currentUserId > 0) {
                    pstmt.setInt(4, currentUserId);
                } else {
                    pstmt.setString(4, oldUsername);
                }
                int rows = pstmt.executeUpdate();
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                return rows > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateGlobalLoggedInUser(String newUsername, String newEmail, String newImg) {
        try {
            if (LoginController.loggedInUser != null) {
                Object userObj = LoginController.loggedInUser;
                invokeSetterIfExists(userObj, "setUsername", newUsername);
                invokeSetterIfExists(userObj, "setEmail", newEmail);
                invokeSetterIfExists(userObj, "setImagePath", newImg);
            }
        } catch (Exception ignored) {}
    }

    private void invokeSetterIfExists(Object obj, String methodName, String value) {
        try {
            Method m = obj.getClass().getMethod(methodName, String.class);
            m.invoke(obj, value);
        } catch (Exception ignored) {}
    }

    @FXML
    void clickClose(ActionEvent event) {
        if (closeCallback != null) {
            closeCallback.run();
        }
    }

    // ==========================================
    // 🌟 PASSWORD CHANGE FLOATING (POP-UP) EFFECT 🌟
    // ==========================================
    @FXML
    void showPasswordView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/SuperAdminPwChange.fxml"));
            pwdChangeView = loader.load();

            PwChangeController pwdController = loader.getController();
            if (pwdController != null) {
                pwdController.setParentController(this);
                pwdController.setCurrentUser(loggedInUser);
            }

            modalOverlay.getChildren().setAll(pwdChangeView);
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
        if (!isSuperAdmin()) {
            new Thread(() -> {
                String logMessage = String.format("[%s] %s changed account password", currentRole, loggedInUser);
                insertLog(currentUserId, logMessage);
            }).start();
        }
        showCustomNotification("Success!", "Password changed successfully.");
    }

    public void showCustomNotification(String title, String message) {
        if (lblToastTitle != null) lblToastTitle.setText(title);
        if (lblToastMessage != null) lblToastMessage.setText(message);

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

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
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