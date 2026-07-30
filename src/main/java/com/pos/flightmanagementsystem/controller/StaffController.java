package com.pos.flightmanagementsystem.controller;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
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
    // 🌟 ACTIVE BUTTON ပြောင်းပေးမည့် HELPER METHOD
    // ==========================================
    private void setActiveButton(Button clickedButton) {
        // ၁။ Button အားလုံးဆီမှ active class ကို ဖြုတ်ပါမည်
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("super-nav-button-active");
            }
        }
        // ၂။ နှိပ်လိုက်သော Button ကိုသာ active class ပြန်ထည့်ပါမည်
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

            // အသစ်ဝင်လာမည့် page ကို ညာဘက်အစွန် (X = 800) တွင် ကြိုထားပါမည်
            newView.setTranslateX(800);

            // လက်ရှိ Center တွင် ပြနေသော View ကို ယူပါမည်
            Node currentView = rootPane.getCenter();

            if (currentView != null) {
                // 🌟 ၁။ လက်ရှိ Page ကို ထွက်သွားစေမည့် Animation (Slide Out)
                TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), currentView);

                slideOut.setToX(800);
                slideOut.setInterpolator(Interpolator.EASE_IN);

                // 🌟 ၂။ လက်ရှိ Page ထွက်သွားတာ ပြီးဆုံးမှ Page အသစ်ကို Center သို့ထည့်ပြီး ဝင်လာစေမည် (Slide In)
                slideOut.setOnFinished(e -> {
                    rootPane.setCenter(newView);

                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), newView);
                    slideIn.setToX(0);
                    slideIn.setInterpolator(Interpolator.EASE_OUT);
                    slideIn.play();
                });

                slideOut.play();

            } else {
                // ပထမဆုံးအကြိမ် Load လုပ်ချိန် (Current View မရှိသေးချိန်) တွင် တန်းပြီး ဝင်လာစေမည်
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
        // 🌟 ၁။ ဝင်ထားတဲ့ User ရှိ/မရှိ စစ်ဆေးပြီး ရှိရင် Log Out မှတ်တမ်းတင်မယ်
        if (com.pos.flightmanagementsystem.controller.LoginController.loggedInUser != null) {
            int currentUserId = com.pos.flightmanagementsystem.controller.LoginController.loggedInUser.getUserId();

            // Database logs table ထဲမှာ "Log out" ဆိုပြီး သွားမှတ်မယ်
            insertLog(currentUserId, "Log out");

            // မှတ်တမ်းတင်ပြီးရင် ဝင်ထားတဲ့ User Data ကို ရှင်းထုတ်ပါမယ်
            com.pos.flightmanagementsystem.controller.LoginController.loggedInUser = null;
        }

        // 🌟 ၂။ မူလ Animation နှင့် Page အကူး Code များ
        Node centerNode = rootPane.getCenter();

        if (centerNode != null) {
            TranslateTransition slideOutRight = new TranslateTransition(Duration.millis(600), centerNode);
            slideOutRight.setToX(800);
            slideOutRight.setInterpolator(Interpolator.EASE_IN);

            slideOutRight.setOnFinished(e -> performLogout(event));
            slideOutRight.play();
        } else {
            performLogout(event);
        }
    }

    private void performLogout(ActionEvent event) {
        try {
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
            Parent root = loader.load();

            currentScene.setRoot(root);
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🌟 Logout အတွက် Database ထဲ Data လှမ်းထည့်မည့် Method (ဒီ Controller ထဲမှာပါ တစ်ခါတည်း ထည့်ထားပေးပါ)
    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";

        try (java.sql.Connection conn = com.pos.flightmanagementsystem.controller.DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(logQuery)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.executeUpdate();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}