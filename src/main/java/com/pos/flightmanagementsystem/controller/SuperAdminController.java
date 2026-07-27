package com.pos.flightmanagementsystem.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// import com.pos.flightmanagementsystem.utils.DatabaseConnection; // လိုအပ်ပါက Import လမ်းကြောင်း ပြင်ပါ

public class SuperAdminController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;
    @FXML private StackPane mainContentArea;

    // 🌟 Profile Floating Animation အတွက် Component များ 🌟
    @FXML private StackPane profileOverlayBg;
    @FXML private StackPane profilePopupContainer;
    private boolean isProfileOpen = false;

    @FXML private Label topNavUsername;
    @FXML private Label topNavRole;

    @FXML private Button btnDashboard;
    @FXML private Button btnACManage;
    @FXML private Button btnSystemLogs;
    @FXML private Button btnSystemTools;
    @FXML private Button btnLogout;

    @FXML private SVGPath iconDashboard;
    @FXML private SVGPath iconACManage;
    @FXML private SVGPath iconSystemLogs;
    @FXML private SVGPath iconSystemTools;
    @FXML private SVGPath iconLogout;

    @FXML private StackPane themeToggleBg;
    @FXML private StackPane themeThumb;
    @FXML private SVGPath themeIcon;

    private boolean isDarkMode = true;

    private final String MOON_SVG = "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z";
    private final String SUN_SVG = "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5s5-2.24,5-5S14.76,7,12,7z M2,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0 c-0.55,0-1,0.45-1,1S1.45,13,2,13z M20,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0c-0.55,0-1,0.45-1,1S19.45,13,20,13z M11,2v2 c0,0.55,0.45,1,1,1s1-0.45,1-1V2c0-0.55-0.45-1-1-1S11,1.45,11,2z M11,20v2c0,0.55,0.45,1,1,1s1-0.45,1-1v-2c0-0.55-0.45-1-1-1 C11.45,19,11,19.45,11,20z M5.99,4.58c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41l1.06,1.06 c0.39,0.39,1.03,0.39,1.41,0s0.39-1.03,0-1.41L5.99,4.58z M18.36,16.95c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41 l1.06,1.06c0.39,0.39,1.03,0.39,1.41,0c0.39-0.39,0.39-1.03,0-1.41L18.36,16.95z M19.42,5.99c0.39-0.39,0.39-1.03,0-1.41 c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L19.42,5.99z M7.05,18.36 c0.39-0.39,0.39-1.03,0-1.41c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L7.05,18.36z";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebar != null) {
            sidebar.setTranslateX(-300);
            TranslateTransition sidebarAnim = new TranslateTransition(Duration.millis(700), sidebar);
            sidebarAnim.setToX(0);
            sidebarAnim.setInterpolator(Interpolator.EASE_OUT);
            sidebarAnim.play();
        }

        Platform.runLater(() -> {
            loadPage("/com/pos/flightmanagementsystem/view/SuperAdminDashboard.fxml");
            setActiveButton(btnDashboard);
            loadUserProfile("SuperAdmin");
        });

        startIconAnimations();
    }

    private void loadUserProfile(String loggedInUsername) {
        String query = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedInUsername);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                Platform.runLater(() -> {
                    if(topNavUsername != null) topNavUsername.setText(loggedInUsername);
                    if(topNavRole != null) {
                        topNavRole.setText(role != null ? role.toUpperCase() : "UNKNOWN ROLE");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 🌟 PROFILE FLOATING ANIMATION LOGIC 🌟
    // ==========================================
    @FXML
    void openProfilePopup(MouseEvent event) {
        if (isProfileOpen) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/SuperAdminProfile.fxml"));
            Parent profileRoot = loader.load();
            updateStylesheets(profileRoot.getStylesheets());

            // လေပေါ်တွင် ပေါလောပေါ်နေစေရန် Shadow ထည့်သွင်းခြင်း
            profileRoot.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0.2, 0, 10); -fx-background-radius: 12px;");

            profilePopupContainer.getChildren().setAll(profileRoot);
            profileOverlayBg.setVisible(true);
            isProfileOpen = true;

            // ထောင့်ညာဘက်အပေါ် (PFP နေရာ) မှနေ၍ အလယ်သို့ ကြီးထွားလာသော Animation
            profilePopupContainer.setTranslateX(300);
            profilePopupContainer.setTranslateY(-300);
            profilePopupContainer.setScaleX(0.1);
            profilePopupContainer.setScaleY(0.1);
            profilePopupContainer.setOpacity(0.0);

            ParallelTransition pt = new ParallelTransition();

            TranslateTransition translate = new TranslateTransition(Duration.millis(400), profilePopupContainer);
            translate.setToX(0);
            translate.setToY(0);
            translate.setInterpolator(Interpolator.EASE_OUT);

            ScaleTransition scale = new ScaleTransition(Duration.millis(400), profilePopupContainer);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(Duration.millis(300), profilePopupContainer);
            fade.setToValue(1.0);

            FadeTransition bgFade = new FadeTransition(Duration.millis(300), profileOverlayBg);
            bgFade.setFromValue(0.0);
            bgFade.setToValue(1.0);

            pt.getChildren().addAll(translate, scale, fade, bgFade);
            pt.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void closeProfilePopup(MouseEvent event) {
        // Overlay Background အမည်းရောင်ကို နှိပ်မှသာ ပိတ်မည်
        if (event.getTarget() != profileOverlayBg) return;

        ParallelTransition pt = new ParallelTransition();

        TranslateTransition translate = new TranslateTransition(Duration.millis(300), profilePopupContainer);
        translate.setToX(300);
        translate.setToY(-300);
        translate.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), profilePopupContainer);
        scale.setToX(0.1);
        scale.setToY(0.1);
        scale.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fade = new FadeTransition(Duration.millis(300), profilePopupContainer);
        fade.setToValue(0.0);

        FadeTransition bgFade = new FadeTransition(Duration.millis(300), profileOverlayBg);
        bgFade.setToValue(0.0);

        pt.getChildren().addAll(translate, scale, fade, bgFade);
        pt.setOnFinished(e -> {
            profileOverlayBg.setVisible(false);
            profilePopupContainer.getChildren().clear();
            isProfileOpen = false;
        });
        pt.play();
    }

    private void startIconAnimations() {
        ScaleTransition dashAnim = new ScaleTransition(Duration.seconds(1.0), iconDashboard);
        dashAnim.setByX(0.25); dashAnim.setByY(0.25);
        dashAnim.setAutoReverse(true); dashAnim.setCycleCount(Timeline.INDEFINITE);
        dashAnim.setInterpolator(Interpolator.EASE_BOTH); dashAnim.play();

        RotateTransition acManageAnim = new RotateTransition(Duration.seconds(1.5), iconACManage);
        acManageAnim.setByAngle(25); acManageAnim.setAutoReverse(true);
        acManageAnim.setCycleCount(Timeline.INDEFINITE); acManageAnim.setInterpolator(Interpolator.EASE_BOTH); acManageAnim.play();

        TranslateTransition logsAnim = new TranslateTransition(Duration.seconds(0.8), iconSystemLogs);
        logsAnim.setByY(-4); logsAnim.setAutoReverse(true);
        logsAnim.setCycleCount(Timeline.INDEFINITE); logsAnim.setInterpolator(Interpolator.EASE_BOTH); logsAnim.play();

        RotateTransition toolsAnim = new RotateTransition(Duration.seconds(3.0), iconSystemTools);
        toolsAnim.setByAngle(360); toolsAnim.setCycleCount(Timeline.INDEFINITE);
        toolsAnim.setInterpolator(Interpolator.LINEAR); toolsAnim.play();

        TranslateTransition logoutAnim = new TranslateTransition(Duration.seconds(0.7), iconLogout);
        logoutAnim.setByX(8); logoutAnim.setAutoReverse(true);
        logoutAnim.setCycleCount(Timeline.INDEFINITE); logoutAnim.setInterpolator(Interpolator.EASE_BOTH); logoutAnim.play();
    }

    @FXML
    public void toggleTheme(MouseEvent event) {
        isDarkMode = !isDarkMode;

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), themeThumb);
        tt.setToX(isDarkMode ? 0 : 24);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();

        String thumbBaseStyle = "-fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;";

        if (isDarkMode) {
            themeIcon.setContent(MOON_SVG);
            themeIcon.setStyle("-fx-fill: #06080D;");
            themeThumb.setStyle("-fx-background-color: #31C4B4; " + thumbBaseStyle);
        } else {
            themeIcon.setContent(SUN_SVG);
            themeIcon.setStyle("-fx-fill: #06080D;");
            themeThumb.setStyle("-fx-background-color: #F59E0B; " + thumbBaseStyle);
        }

        updateTheme();
    }

    private void updateTheme() {
        updateStylesheets(rootPane.getStylesheets());

        if (mainContentArea != null && !mainContentArea.getChildren().isEmpty()) {
            Node rightPage = mainContentArea.getChildren().get(0);
            if (rightPage instanceof Parent) {
                updateStylesheets(((Parent) rightPage).getStylesheets());
            }
        }

        // Profile Overlay ပွင့်နေပါက ၎င်း၏ Theme ကိုပါ Change ရန်
        if (profilePopupContainer != null && !profilePopupContainer.getChildren().isEmpty()) {
            Node profilePage = profilePopupContainer.getChildren().get(0);
            if (profilePage instanceof Parent) {
                updateStylesheets(((Parent) profilePage).getStylesheets());
            }
        }

        Scene scene = rootPane.getScene();
        if (scene != null) {
            updateStylesheets(scene.getStylesheets());
        }
    }

    private void updateStylesheets(ObservableList<String> stylesheets) {
        for (int i = 0; i < stylesheets.size(); i++) {
            String cssPath = stylesheets.get(i);
            if (isDarkMode && cssPath.contains("Light")) {
                stylesheets.set(i, cssPath.replace("Light", "Dark"));
            } else if (!isDarkMode && cssPath.contains("Dark")) {
                stylesheets.set(i, cssPath.replace("Dark", "Light"));
            }
        }
    }

    private void setActiveButton(Button clickedButton) {
        Button[] allButtons = {btnDashboard, btnACManage, btnSystemLogs, btnSystemTools};
        for (Button btn : allButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("super-nav-button-active");
                if (!btn.getStyleClass().contains("super-nav-button")) {
                    btn.getStyleClass().add("super-nav-button");
                }
            }
        }
        if (clickedButton != null) {
            clickedButton.getStyleClass().remove("super-nav-button");
            clickedButton.getStyleClass().add("super-nav-button-active");
        }
    }

    private void loadPage(String fxmlPath) {
        Node currentPage = mainContentArea.getChildren().isEmpty() ? null : mainContentArea.getChildren().get(0);

        if (currentPage != null) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(350), currentPage);
            slideOut.setToX(1200);
            slideOut.setInterpolator(Interpolator.EASE_IN);
            slideOut.setOnFinished(e -> showNewPage(fxmlPath, 400));
            slideOut.play();
        } else {
            showNewPage(fxmlPath, 700);
        }
    }

    private void showNewPage(String fxmlPath, double durationMillis) {
        try {
            java.net.URL fileUrl = getClass().getResource(fxmlPath);
            if (fileUrl == null) return;

            FXMLLoader loader = new FXMLLoader(fileUrl);
            Parent newPage = loader.load();

            updateStylesheets(newPage.getStylesheets());

            newPage.setTranslateX(1200);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(newPage);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(durationMillis), newPage);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);
            slideIn.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void clickDashboard(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminDashboard.fxml"); setActiveButton(btnDashboard); }
    @FXML void clickACManagement(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminAccManagement.fxml"); setActiveButton(btnACManage); }
    @FXML void clickSystemLogs(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminSystemLogs.fxml"); setActiveButton(btnSystemLogs); }
    @FXML void clickSystemTools(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperAdminSystemTools.fxml"); setActiveButton(btnSystemTools); }

    @FXML void clickLogout(ActionEvent event) {
        TranslateTransition leftAnim = new TranslateTransition(Duration.millis(700), sidebar);
        leftAnim.setByX(-800);
        leftAnim.setInterpolator(Interpolator.EASE_IN);

        Node rightContent = rootPane.getCenter();
        TranslateTransition rightAnim = new TranslateTransition(Duration.millis(700), rightContent);
        if (rightContent != null) {
            rightAnim.setByX(800);
            rightAnim.setInterpolator(Interpolator.EASE_IN);
        }

        ParallelTransition pt = new ParallelTransition(leftAnim, rightAnim);
        pt.setOnFinished(e -> loadLoginPage(event));
        pt.play();
    }

    private void loadLoginPage(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));

            if (stage.isMaximized()) stage.setMaximized(false);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}