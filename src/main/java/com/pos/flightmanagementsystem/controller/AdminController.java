package com.pos.flightmanagementsystem.controller;

import javafx.animation.Interpolator;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Button btnDashboard, btnProfile, btnFlightSchedule, btnViewCity, btnBookingManagement, btnViewSaleDetails, btnLogout;

    // 🌟 Button များကို Array ထဲထည့်ထားမည် (Active State ပြောင်းရန်လွယ်ကူစေရန်)
    private Button[] navButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Button အားလုံးကို Array ထဲ စုထည့်ပါမည်
        navButtons = new Button[]{btnDashboard, btnProfile, btnFlightSchedule, btnViewCity, btnBookingManagement, btnViewSaleDetails};

        // စစချင်း Dashboard ကို Default အရောင်တင်ထားပါမည်
        setActiveButton(btnDashboard);
        loadPage("SuperAdminDashboard.fxml");
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
    // 🌟 DYNAMIC VIEW LOADER
    // ==========================================
    private void loadPage(String fxmlFileName) {
        try {
            String path = "/com/pos/flightmanagementsystem/view/" + fxmlFileName;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent newView = loader.load();

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
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load page: " + fxmlFileName);
        }
    }

    // ==========================================
    // 🌟 SIDEBAR BUTTON ACTIONS
    // ==========================================
    @FXML
    void clickDashboard(ActionEvent event) {
        setActiveButton(btnDashboard);
        loadPage("SuperAdminDashboard.fxml");
    }

    @FXML
    void clickProfile(ActionEvent event) {
        setActiveButton(btnProfile);
        loadPage("StaffProfile.fxml");
    }

    @FXML
    void clickFlightSchedule(ActionEvent event) {
        setActiveButton(btnFlightSchedule);
        loadPage("AdminFlightSchedule.fxml");
    }

    @FXML
    void clickViewCity(ActionEvent event) {
        setActiveButton(btnViewCity);
        loadPage("AdminCity.fxml");
    }

    @FXML
    void clickBookingManagement(ActionEvent event) {
        setActiveButton(btnBookingManagement);
        loadPage("AdminBookingManagement.fxml");
    }

    @FXML
    void clickViewSaleDetails(ActionEvent event) {
        setActiveButton(btnViewSaleDetails);
        loadPage("AdminReport.fxml");
    }

    // ==========================================
    // 🌟 LOGOUT ACTION
    // ==========================================
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