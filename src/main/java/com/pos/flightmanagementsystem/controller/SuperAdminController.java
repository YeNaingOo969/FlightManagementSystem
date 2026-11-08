package com.pos.flightmanagementsystem.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
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
    @FXML private HBox topNavBar;
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

    private Button[] navButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navButtons = new Button[]{btnDashboard, btnProfile, btnACManage, btnSystemLogs, btnSystemTools};
        setActiveButton(btnDashboard);

        this.isDarkMode = !LoginController.isLightMode;

        Platform.runLater(() -> {
            if (isDarkMode) {
                themeThumb.setTranslateX(0);
                themeIcon.setContent(MOON_SVG);
                themeThumb.setStyle("-fx-background-color: #00D4FF; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;");
            } else {
                themeThumb.setTranslateX(24);
                themeIcon.setContent(SUN_SVG);
                themeThumb.setStyle("-fx-background-color: #F59E0B; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;");
            }
            updateTheme();
        });

        Platform.runLater(() -> loadPage("/com/pos/flightmanagementsystem/view/SuperAdminDashboard.fxml", false));

        startIconAnimations();
    }

    public void setLoggedInUser(String username, String role, String imagePath) {
        this.loggedInUsername = username;
        this.loggedInRole = role;
        this.loggedInImagePath = imagePath;

        if (topNavUsername != null) topNavUsername.setText(username);
        if (topNavRole != null && role != null) topNavRole.setText(role.toUpperCase());

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

    @FXML
    void openProfilePopup(MouseEvent event) {
        if (isProfileOpen) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Profile.fxml"));
            Parent profileRoot = loader.load();

            ProfileController controller = loader.getController();
            controller.setUserData(loggedInUsername, loggedInRole, loggedInImagePath);

            // 🌟 Profile တွင် နာမည် သို့မဟုတ် ပုံ ပြောင်းလိုက်သည်နှင့် ညာဘက်အပေါ်ထောင့် Navbar ကို ချက်ချင်း Update လုပ်မည် 🌟
            controller.setOnProfileUpdateCallback((newUsername, newImagePath) -> {
                setLoggedInUser(newUsername, loggedInRole, newImagePath);
            });

            applyThemeRecursively(profileRoot);

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

        LoginController.isLightMode = !isDarkMode;

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), themeThumb); tt.setToX(isDarkMode ? 0 : 24); tt.setInterpolator(Interpolator.EASE_BOTH); tt.play();
        if (isDarkMode) { themeIcon.setContent(MOON_SVG); themeThumb.setStyle("-fx-background-color: #00D4FF; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;"); }
        else { themeIcon.setContent(SUN_SVG); themeThumb.setStyle("-fx-background-color: #F59E0B; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;"); }

        updateTheme();
    }

    private void applyThemeRecursively(Parent node) {
        if (node == null) return;
        if (node.getStylesheets() != null && !node.getStylesheets().isEmpty()) {
            ObservableList<String> styles = node.getStylesheets();
            for (int i = 0; i < styles.size(); i++) {
                String cssPath = styles.get(i);
                String newCssPath = null;
                if (isDarkMode && cssPath.contains("Light")) {
                    newCssPath = cssPath.replace("Light", "Dark");
                } else if (!isDarkMode && cssPath.contains("Dark")) {
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
                applyThemeRecursively((Parent) child);
            }
        }
    }

    private void updateTheme() {
        applyThemeRecursively(rootPane);
    }

    private void setActiveButton(Button clickedButton) {
        for (Button btn : navButtons) { if (btn != null) { btn.getStyleClass().remove("super-nav-button-active"); if (!btn.getStyleClass().contains("super-nav-button")) btn.getStyleClass().add("super-nav-button"); } }
        if (clickedButton != null) { clickedButton.getStyleClass().remove("super-nav-button"); clickedButton.getStyleClass().add("super-nav-button-active"); }
    }

    private void loadPage(String fxmlPath, boolean animate) {
        Node currentPage = mainContentArea.getChildren().isEmpty() ? null : mainContentArea.getChildren().get(0);
        if (currentPage != null && animate) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(350), currentPage); slideOut.setToX(1200); slideOut.setInterpolator(Interpolator.EASE_IN); slideOut.setOnFinished(e -> showNewPage(fxmlPath, 400, true)); slideOut.play();
        } else { showNewPage(fxmlPath, 700, animate); }
    }

    private void showNewPage(String fxmlPath, double durationMillis, boolean animate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPage = loader.load();

            applyThemeRecursively(newPage);

            mainContentArea.getChildren().clear(); mainContentArea.getChildren().add(newPage);
            if (animate) {
                newPage.setTranslateX(1200);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(durationMillis), newPage); slideIn.setToX(0); slideIn.setInterpolator(Interpolator.EASE_OUT); slideIn.play();
            } else {
                newPage.setTranslateX(0);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void clickDashboard(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminDashboard.fxml", true); setActiveButton(btnDashboard); }
    @FXML void clickACManagement(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminAccManagement.fxml", true); setActiveButton(btnACManage); }
    @FXML void clickSystemLogs(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminSystemLogs.fxml", true); setActiveButton(btnSystemLogs); }
    @FXML void clickSystemTools(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminSystemTools.fxml", true); setActiveButton(btnSystemTools); }

    public void prepareIntroAnimation() {
        if (sidebar != null) { sidebar.setTranslateX(-300); sidebar.setOpacity(0); }
        if (topNavBar != null) { topNavBar.setTranslateY(-150); topNavBar.setOpacity(0); }
        if (mainContentArea != null) { mainContentArea.setTranslateX(800); mainContentArea.setOpacity(0); }
    }

    public void playIntroAnimation() {
        ParallelTransition dashAnimIn = new ParallelTransition();
        if (sidebar != null) {
            TranslateTransition st = new TranslateTransition(Duration.millis(700), sidebar); st.setToX(0); st.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition ft = new FadeTransition(Duration.millis(700), sidebar); ft.setToValue(1);
            dashAnimIn.getChildren().addAll(st, ft);
        }
        if (topNavBar != null) {
            TranslateTransition st = new TranslateTransition(Duration.millis(700), topNavBar); st.setToY(0); st.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition ft = new FadeTransition(Duration.millis(700), topNavBar); ft.setToValue(1);
            dashAnimIn.getChildren().addAll(st, ft);
        }
        if (mainContentArea != null) {
            TranslateTransition st = new TranslateTransition(Duration.millis(700), mainContentArea); st.setToX(0); st.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition Fade = new FadeTransition(Duration.millis(700), mainContentArea); Fade.setToValue(1);
            dashAnimIn.getChildren().addAll(st, Fade);
        }
        dashAnimIn.play();
    }

    @FXML
    void clickLogout(ActionEvent event) {
        if (LoginController.loggedInUser != null) {
            insertLog(LoginController.loggedInUser.getUserId(), "Log out");
        }

        try {
            LoginController.isFromLogout = true;
            LoginController.isLightMode = !this.isDarkMode;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
            Parent loginRoot = loader.load();
            LoginController loginCtrl = loader.getController();

            Scene scene = btnLogout.getScene();
            Parent currentDashboardRoot = scene.getRoot();

            StackPane transitionWrapper = new StackPane();

            transitionWrapper.setStyle("-fx-background-color: " + (!LoginController.isLightMode ? "#06080D" : "#f4f9f9") + ";");
            transitionWrapper.getChildren().addAll(loginRoot, currentDashboardRoot);

            scene.setRoot(transitionWrapper);
            transitionWrapper.applyCss(); transitionWrapper.layout();
            loginRoot.applyCss(); loginRoot.layout();

            ParallelTransition dashOut = new ParallelTransition();
            if (sidebar != null) {
                TranslateTransition sOut = new TranslateTransition(Duration.millis(700), sidebar); sOut.setToX(-300); sOut.setInterpolator(Interpolator.EASE_BOTH);
                FadeTransition fOut = new FadeTransition(Duration.millis(700), sidebar); fOut.setToValue(0);
                dashOut.getChildren().addAll(sOut, fOut);
            }
            if (topNavBar != null) {
                TranslateTransition tOut = new TranslateTransition(Duration.millis(700), topNavBar); tOut.setToY(-150); tOut.setInterpolator(Interpolator.EASE_BOTH);
                FadeTransition fOut = new FadeTransition(Duration.millis(700), topNavBar); fOut.setToValue(0);
                dashOut.getChildren().addAll(tOut, fOut);
            }
            if (mainContentArea != null) {
                TranslateTransition mOut = new TranslateTransition(Duration.millis(700), mainContentArea); mOut.setToX(800); mOut.setInterpolator(Interpolator.EASE_BOTH);
                FadeTransition fOut = new FadeTransition(Duration.millis(700), mainContentArea); fOut.setToValue(0);
                dashOut.getChildren().addAll(mOut, fOut);
            }

            dashOut.setOnFinished(e -> {
                transitionWrapper.getChildren().remove(loginRoot);
                scene.setRoot(loginRoot);
                if (loginCtrl != null) {
                    loginCtrl.playLoginInAnimation();
                }
            });
            dashOut.play();

            LoginController.loggedInUser = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}