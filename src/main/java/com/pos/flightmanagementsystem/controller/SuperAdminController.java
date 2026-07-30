package com.pos.flightmanagementsystem.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SuperAdminController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;
    @FXML private StackPane rightSideStack;
    @FXML private StackPane mainContentArea;
    @FXML private StackPane profileOverlayBg;
    @FXML private StackPane profilePopupContainer;

    @FXML private Button btnDashboard, btnProfile, btnACManage, btnSystemLogs, btnSystemTools, btnLogout;

    @FXML private Label topNavUsername;
    @FXML private Label topNavRole;
    @FXML private Circle topNavAvatarCircle;
    @FXML private HBox navProfileBox;

    @FXML private SVGPath iconDashboard, iconProfile, iconACManage, iconSystemLogs, iconSystemTools, iconLogout;

    @FXML private StackPane themeToggleBg, themeThumb;
    @FXML private SVGPath themeIcon;

    private boolean isDarkMode = true;
    private boolean isProfileOpen = false;

    private String loggedInUsername = "";
    private String loggedInRole = "";
    private String loggedInImagePath = "";

    private final String MOON_SVG = "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z";
    private final String SUN_SVG = "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5s5-2.24,5-5S14.76,7,12,7z M2,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0 c-0.55,0-1,0.45-1,1S1.45,13,2,13z M20,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0c-0.55,0-1,0.45-1,1S19.45,13,20,13z M11,2v2 c0,0.55,0.45,1,1,1s1-0.45,1-1V2c0-0.55-0.45-1-1-1S11,1.45,11,2z M11,20v2c0,0.55,0.45,1,1,1s1-0.45,1-1v-2c0-0.55-0.45-1-1-1 C11.45,19,11,19.45,11,20z M5.99,4.58c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41l1.06,1.06 c0.39,0.39,1.03,0.39,1.41,0s0.39-1.03,0-1.41L5.99,4.58z M18.36,16.95c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41 l1.06,1.06c0.39,0.39,1.03,0.39,1.41,0c0.39-0.39,0.39-1.03,0-1.41L18.36,16.95z M19.42,5.99c0.39-0.39,0.39-1.03,0-1.41 c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L19.42,5.99z M7.05,18.36 c0.39-0.39,0.39-1.03,0-1.41c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L7.05,18.36z";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebar != null) {
            sidebar.setTranslateX(-300);
            TranslateTransition sidebarAnim = new TranslateTransition(Duration.millis(700), sidebar);
            sidebarAnim.setToX(0); sidebarAnim.setInterpolator(Interpolator.EASE_OUT); sidebarAnim.play();
        }

        Platform.runLater(() -> {
            loadPage("/com/pos/flightmanagementsystem/view/SuperAdminDashboard.fxml");
            setActiveButton(btnDashboard);
        });

        startIconAnimations();
    }

    public void setLoggedInUser(String username, String role, String imagePath) {
        this.loggedInUsername = username;
        this.loggedInRole = role;
        this.loggedInImagePath = imagePath;

        if (topNavUsername != null) topNavUsername.setText(username);
        if (topNavRole != null) topNavRole.setText(role.toUpperCase());

        if (topNavAvatarCircle != null && imagePath != null && !imagePath.isEmpty()) {
            try {
                File imgFile = new File("src/main/resources/com/pos/flightmanagementsystem/image/" + imagePath);
                if (imgFile.exists()) {
                    topNavAvatarCircle.setFill(new ImagePattern(new Image(imgFile.toURI().toString())));
                } else {
                    URL url = getClass().getResource("/com/pos/flightmanagementsystem/image/" + imagePath);
                    if (url != null) topNavAvatarCircle.setFill(new ImagePattern(new Image(url.toExternalForm())));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ==========================================
    // 🌟 TOP NAV PROFILE POPUP 🌟
    // ==========================================
    @FXML
    void openProfilePopup(MouseEvent event) {
        if (isProfileOpen) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Profile.fxml"));
            Parent profileRoot = loader.load();

            ProfileController controller = loader.getController();
            controller.setUserData(loggedInUsername, loggedInRole, loggedInImagePath);

            updateStylesheets(profileRoot.getStylesheets());
            profileRoot.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0.2, 0, 10); -fx-background-radius: 12px;");

            profilePopupContainer.getChildren().setAll(profileRoot);
            profileOverlayBg.setVisible(true);
            isProfileOpen = true;

            Bounds sourceBounds = navProfileBox.localToScene(navProfileBox.getBoundsInLocal());
            Bounds rootBounds = rightSideStack.localToScene(rightSideStack.getBoundsInLocal());
            double startX = (sourceBounds.getMinX() + (sourceBounds.getWidth() / 2)) - (rootBounds.getMinX() + (rootBounds.getWidth() / 2));
            double startY = (sourceBounds.getMinY() + (sourceBounds.getHeight() / 2)) - (rootBounds.getMinY() + (rootBounds.getHeight() / 2));

            profilePopupContainer.setTranslateX(startX); profilePopupContainer.setTranslateY(startY);
            profilePopupContainer.setScaleX(0.05); profilePopupContainer.setScaleY(0.05); profilePopupContainer.setOpacity(0.0);

            controller.setCloseCallback(() -> closeProfileModal(startX, startY));

            Node mainContent = rightSideStack.getChildren().get(0);
            mainContent.setEffect(new BoxBlur(12, 12, 3));

            Interpolator smoothOvershoot = new Interpolator() { @Override protected double curve(double t) { double tension = 1.1; t -= 1.0; return t * t * ((tension + 1) * t + tension) + 1.0; } };

            ScaleTransition scale = new ScaleTransition(Duration.millis(400), profilePopupContainer); scale.setToX(1.0); scale.setToY(1.0); scale.setInterpolator(smoothOvershoot);
            TranslateTransition translate = new TranslateTransition(Duration.millis(400), profilePopupContainer); translate.setToX(0); translate.setToY(0); translate.setInterpolator(smoothOvershoot);
            FadeTransition fade = new FadeTransition(Duration.millis(300), profilePopupContainer); fade.setToValue(1.0);
            FadeTransition bgFade = new FadeTransition(Duration.millis(300), profileOverlayBg); bgFade.setFromValue(0.0); bgFade.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(translate, scale, fade, bgFade);
            pt.play();

        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void closeProfilePopup(MouseEvent event) {
        if (event.getTarget() == profileOverlayBg) {
            Bounds sourceBounds = navProfileBox.localToScene(navProfileBox.getBoundsInLocal());
            Bounds rootBounds = rightSideStack.localToScene(rightSideStack.getBoundsInLocal());
            double startX = (sourceBounds.getMinX() + (sourceBounds.getWidth() / 2)) - (rootBounds.getMinX() + (rootBounds.getWidth() / 2));
            double startY = (sourceBounds.getMinY() + (sourceBounds.getHeight() / 2)) - (rootBounds.getMinY() + (rootBounds.getHeight() / 2));
            closeProfileModal(startX, startY);
        }
    }

    private void closeProfileModal(double endX, double endY) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(300), profilePopupContainer); translate.setToX(endX); translate.setToY(endY); translate.setInterpolator(Interpolator.EASE_IN);
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), profilePopupContainer); scale.setToX(0.05); scale.setToY(0.05); scale.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fade = new FadeTransition(Duration.millis(300), profilePopupContainer); fade.setToValue(0.0);
        FadeTransition bgFade = new FadeTransition(Duration.millis(300), profileOverlayBg); bgFade.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(translate, scale, fade, bgFade);
        pt.setOnFinished(e -> {
            profileOverlayBg.setVisible(false); profilePopupContainer.getChildren().clear(); isProfileOpen = false;
            rightSideStack.getChildren().get(0).setEffect(null);
        });
        pt.play();
    }

    private void startIconAnimations() {
        if (iconDashboard != null) { ScaleTransition dashAnim = new ScaleTransition(Duration.seconds(1.0), iconDashboard); dashAnim.setByX(0.25); dashAnim.setByY(0.25); dashAnim.setAutoReverse(true); dashAnim.setCycleCount(Timeline.INDEFINITE); dashAnim.setInterpolator(Interpolator.EASE_BOTH); dashAnim.play(); }
        if (iconProfile != null) { TranslateTransition profileAnim = new TranslateTransition(Duration.seconds(1.5), iconProfile); profileAnim.setByY(-6); profileAnim.setAutoReverse(true); profileAnim.setCycleCount(Timeline.INDEFINITE); profileAnim.setInterpolator(Interpolator.EASE_BOTH); profileAnim.play(); }
        if (iconACManage != null) { RotateTransition acManageAnim = new RotateTransition(Duration.seconds(1.5), iconACManage); acManageAnim.setByAngle(25); acManageAnim.setAutoReverse(true); acManageAnim.setCycleCount(Timeline.INDEFINITE); acManageAnim.setInterpolator(Interpolator.EASE_BOTH); acManageAnim.play(); }
        if (iconSystemLogs != null) { TranslateTransition logsAnim = new TranslateTransition(Duration.seconds(0.8), iconSystemLogs); logsAnim.setByY(-4); logsAnim.setAutoReverse(true); logsAnim.setCycleCount(Timeline.INDEFINITE); logsAnim.setInterpolator(Interpolator.EASE_BOTH); logsAnim.play(); }
        if (iconSystemTools != null) { RotateTransition toolsAnim = new RotateTransition(Duration.seconds(3.0), iconSystemTools); toolsAnim.setByAngle(360); toolsAnim.setCycleCount(Timeline.INDEFINITE); toolsAnim.setInterpolator(Interpolator.LINEAR); toolsAnim.play(); }
        if (iconLogout != null) { TranslateTransition logoutAnim = new TranslateTransition(Duration.seconds(0.7), iconLogout); logoutAnim.setByX(8); logoutAnim.setAutoReverse(true); logoutAnim.setCycleCount(Timeline.INDEFINITE); logoutAnim.setInterpolator(Interpolator.EASE_BOTH); logoutAnim.play(); }
    }

    @FXML
    public void toggleTheme(MouseEvent event) {
        isDarkMode = !isDarkMode;
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), themeThumb); tt.setToX(isDarkMode ? 0 : 24); tt.setInterpolator(Interpolator.EASE_BOTH); tt.play();
        if (isDarkMode) { themeIcon.setContent(MOON_SVG); themeThumb.setStyle("-fx-background-color: #00D4FF; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;"); }
        else { themeIcon.setContent(SUN_SVG); themeThumb.setStyle("-fx-background-color: #F59E0B; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;"); }
        updateTheme();
    }

    private void updateTheme() {
        updateStylesheets(rootPane.getStylesheets());
        if (mainContentArea != null && !mainContentArea.getChildren().isEmpty()) { Node rightPage = mainContentArea.getChildren().get(0); if (rightPage instanceof Parent) updateStylesheets(((Parent) rightPage).getStylesheets()); }
        if (profilePopupContainer != null && !profilePopupContainer.getChildren().isEmpty()) { Node profilePage = profilePopupContainer.getChildren().get(0); if (profilePage instanceof Parent) updateStylesheets(((Parent) profilePage).getStylesheets()); }
        Scene scene = rootPane.getScene(); if (scene != null) updateStylesheets(scene.getStylesheets());
    }

    private void updateStylesheets(ObservableList<String> stylesheets) {
        for (int i = 0; i < stylesheets.size(); i++) {
            String cssPath = stylesheets.get(i);
            if (isDarkMode && cssPath.contains("Light")) stylesheets.set(i, cssPath.replace("Light", "Dark"));
            else if (!isDarkMode && cssPath.contains("Dark")) stylesheets.set(i, cssPath.replace("Dark", "Light"));
        }
    }

    private void setActiveButton(Button clickedButton) {
        Button[] allButtons = {btnDashboard, btnProfile, btnACManage, btnSystemLogs, btnSystemTools};
        for (Button btn : allButtons) { if (btn != null) { btn.getStyleClass().remove("super-nav-button-active"); if (!btn.getStyleClass().contains("super-nav-button")) btn.getStyleClass().add("super-nav-button"); } }
        if (clickedButton != null) { clickedButton.getStyleClass().remove("super-nav-button"); clickedButton.getStyleClass().add("super-nav-button-active"); }
    }

    private void loadPage(String fxmlPath) {
        Node currentPage = mainContentArea.getChildren().isEmpty() ? null : mainContentArea.getChildren().get(0);
        if (currentPage != null) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(350), currentPage); slideOut.setToX(1200); slideOut.setInterpolator(Interpolator.EASE_IN); slideOut.setOnFinished(e -> showNewPage(fxmlPath, 400)); slideOut.play();
        } else { showNewPage(fxmlPath, 700); }
    }

    private void showNewPage(String fxmlPath, double durationMillis) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath)); Parent newPage = loader.load();
            updateStylesheets(newPage.getStylesheets());
            newPage.setTranslateX(1200); mainContentArea.getChildren().clear(); mainContentArea.getChildren().add(newPage);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(durationMillis), newPage); slideIn.setToX(0); slideIn.setInterpolator(Interpolator.EASE_OUT); slideIn.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void clickDashboard(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminDashboard.fxml"); setActiveButton(btnDashboard); }

    // Friend's code for Sidebar Profile Button
    @FXML void clickProfile(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminProfile.fxml"); setActiveButton(btnProfile); }

    @FXML void clickACManagement(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminAccManagement.fxml"); setActiveButton(btnACManage); }
    @FXML void clickSystemLogs(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminSystemLogs.fxml"); setActiveButton(btnSystemLogs); }
    @FXML void clickSystemTools(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminSystemTools.fxml"); setActiveButton(btnSystemTools); }

    @FXML
    void clickLogout(ActionEvent event) {
        if (LoginController.loggedInUser != null) {
            insertLog(LoginController.loggedInUser.getUserId(), "Log out");
            LoginController.loggedInUser = null;
        }

        TranslateTransition leftAnim = new TranslateTransition(Duration.millis(700), sidebar); leftAnim.setByX(-800); leftAnim.setInterpolator(Interpolator.EASE_IN);
        Node rightContent = rootPane.getCenter();
        TranslateTransition rightAnim = new TranslateTransition(Duration.millis(700), rightContent);
        if (rightContent != null) { rightAnim.setByX(800); rightAnim.setInterpolator(Interpolator.EASE_IN); }

        ParallelTransition pt = new ParallelTransition(leftAnim, rightAnim);
        pt.setOnFinished(e -> performLogout(event)); pt.play();
    }

    private void performLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
            stage.setScene(new Scene(loader.load())); stage.setMaximized(false); stage.setMaximized(true); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(logQuery)) {
            pstmt.setInt(1, userId); pstmt.setString(2, action); pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}