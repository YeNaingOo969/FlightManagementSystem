package com.pos.flightmanagementsystem.controller;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StaffController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;

    // --- Buttons ---
    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnCreateBooking;
    @FXML private Button btnManageBooking;
    @FXML private Button btnViewSchedule;
    @FXML private Button btnLogout;

    // --- Icons ---
    @FXML private SVGPath iconDashboard;
    @FXML private SVGPath iconProfile;
    @FXML private SVGPath iconCreateBooking;
    @FXML private SVGPath iconManageBooking;
    @FXML private SVGPath iconViewSchedule;
    @FXML private SVGPath iconLogout;

    // 🌟 Theme Toggle Nodes & Variables
    @FXML private StackPane themeToggleBg;
    @FXML private StackPane themeThumb;
    @FXML private SVGPath themeIcon;

    private boolean isDarkMode = true; // Default Dark Mode

    private final String MOON_SVG = "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z";
    private final String SUN_SVG = "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5s5-2.24,5-5S14.76,7,12,7z M2,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0 c-0.55,0-1,0.45-1,1S1.45,13,2,13z M20,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0c-0.55,0-1,0.45-1,1S19.45,13,20,13z M11,2v2 c0,0.55,0.45,1,1,1s1-0.45,1-1V2c0-0.55-0.45-1-1-1S11,1.45,11,2z M11,20v2c0,0.55,0.45,1,1,1s1-0.45,1-1v-2c0-0.55-0.45-1-1-1 C11.45,19,11,19.45,11,20z M5.99,4.58c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41l1.06,1.06 c0.39,0.39,1.03,0.39,1.41,0s0.39-1.03,0-1.41L5.99,4.58z M18.36,16.95c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41 l1.06,1.06c0.39,0.39,1.03,0.39,1.41,0c0.39-0.39,0.39-1.03,0-1.41L18.36,16.95z M19.42,5.99c0.39-0.39,0.39-1.03,0-1.41 c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L19.42,5.99z M7.05,18.36 c0.39-0.39,0.39-1.03,0-1.41c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L7.05,18.36z";

    // 🌟 Button များကို Array ထဲထည့်ထားမည် (Active State ပြောင်းရန်)
    private Button[] navButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 🌟 Button အားလုံးကို Array ထဲ စုထည့်ပါမည် (Logout မပါပါ)
        navButtons = new Button[]{btnDashboard, btnProfile, btnCreateBooking, btnManageBooking, btnViewSchedule};

        // 🌟 1. Sidebar ဘယ်ဘက်မှ ဝင်လာမည့် Entrance Animation
        if (sidebar != null) {
            sidebar.setTranslateX(-300);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), sidebar);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);
            slideIn.play();
        }

        // 🌟 2. စစချင်း Dashboard ကို Default Active အရောင်တင်ထားပါမည်
        setActiveButton(btnDashboard);
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffDashboard.fxml");

        // 🌟 3. Icon Hover Animations များ ချိတ်ဆက်ခြင်း
        setupIconHoverAnimation(btnDashboard, iconDashboard);
        setupIconHoverAnimation(btnProfile, iconProfile);
        setupIconHoverAnimation(btnCreateBooking, iconCreateBooking);
        setupIconHoverAnimation(btnManageBooking, iconManageBooking);
        setupIconHoverAnimation(btnViewSchedule, iconViewSchedule);
        setupIconHoverAnimation(btnLogout, iconLogout);
    }

    // ==========================================
    // 🌗 THEME TOGGLE LOGIC
    // ==========================================
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
            themeThumb.setStyle("-fx-background-color: #00D4FF; " + thumbBaseStyle);
        } else {
            themeIcon.setContent(SUN_SVG);
            themeThumb.setStyle("-fx-background-color: #F59E0B; " + thumbBaseStyle);
        }

        updateTheme();
    }

    private void updateTheme() {
        updateStylesheets(rootPane.getStylesheets());
        Node rightPage = rootPane.getCenter();
        if (rightPage != null && rightPage instanceof Parent) {
            updateStylesheets(((Parent) rightPage).getStylesheets());
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

    // ==========================================
    // 🌟 ACTIVE BUTTON ပြောင်းပေးမည့် HELPER METHOD
    // ==========================================
    private void setActiveButton(Button clickedButton) {
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("super-nav-button-active");
            }
        }
        if (clickedButton != null && !clickedButton.getStyleClass().contains("super-nav-button-active")) {
            clickedButton.getStyleClass().add("super-nav-button-active");
        }
    }

    // ==========================================
    // 🌟 DYNAMIC VIEW LOADER (လက်ရှိ Page ထွက်သွားပြီးမှ အသစ်ဝင်လာမည့် Animation)
    // ==========================================
    private void loadCenterView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newView = loader.load();

            // လက်ရှိ Theme အတိုင်း CSS ကို မှန်ကန်စွာ ဝင်စေရန်
            updateStylesheets(newView.getStylesheets());

            newView.setTranslateX(800);
            Node currentView = rootPane.getCenter();

            if (currentView != null) {
                TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), currentView);
                slideOut.setToX(800);
                slideOut.setInterpolator(Interpolator.EASE_IN);

                slideOut.setOnFinished(e -> {
                    rootPane.setCenter(newView);
                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), newView);
                    slideIn.setToX(0);
                    slideIn.setInterpolator(Interpolator.EASE_OUT);
                    slideIn.play();
                });
                slideOut.play();

            } else {
                rootPane.setCenter(newView);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), newView);
                slideIn.setToX(0);
                slideIn.setInterpolator(Interpolator.EASE_OUT);
                slideIn.play();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxmlPath);
        }
    }

    // ==========================================
    // 🌟 ICON HOVER ANIMATION HELPER
    // ==========================================
    private void setupIconHoverAnimation(Button button, SVGPath icon) {
        if (button != null && icon != null) {
            button.setOnMouseEntered(e -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), icon);
                scaleUp.setToX(1.2);
                scaleUp.setToY(1.2);
                scaleUp.play();
            });

            button.setOnMouseExited(e -> {
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), icon);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });
        }
    }

    // ==========================================
    // 🌟 BUTTON ACTION HANDLERS
    // ==========================================

    @FXML
    void clickDashboard(ActionEvent event) {
        setActiveButton(btnDashboard);
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffDashboard.fxml");
    }

    @FXML
    void clickProfile(ActionEvent event) {
        setActiveButton(btnProfile);
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffProfile.fxml");
    }

    @FXML
    void clickCreateBooking(ActionEvent event) {
        setActiveButton(btnCreateBooking);
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffCreateNewBooking.fxml");
    }

    @FXML
    void clickManageBooking(ActionEvent event) {
        setActiveButton(btnManageBooking);
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffManageBooking.fxml");
    }

    @FXML
    void clickViewSchedule(ActionEvent event) {
        setActiveButton(btnViewSchedule);
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffFlightSchedules.fxml");
    }

    @FXML
    void clickLogout(ActionEvent event) {
        TranslateTransition slideOutLeft = new TranslateTransition(Duration.millis(600), sidebar);
        slideOutLeft.setToX(-300);
        slideOutLeft.setInterpolator(Interpolator.EASE_IN);

        Node centerNode = rootPane.getCenter();
        TranslateTransition slideOutRight = new TranslateTransition(Duration.millis(600), centerNode);
        if (centerNode != null) {
            slideOutRight.setToX(800);
            slideOutRight.setInterpolator(Interpolator.EASE_IN);
        }

        ParallelTransition ptOut = new ParallelTransition(slideOutLeft, slideOutRight);
        ptOut.setOnFinished(e -> {
            try {
                Scene currentScene = ((Node) event.getSource()).getScene();
                Stage stage = (Stage) currentScene.getWindow();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
                Parent root = loader.load();

                currentScene.setRoot(root);
                stage.setMaximized(true);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        ptOut.play();
    }
}