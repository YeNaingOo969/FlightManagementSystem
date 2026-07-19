package com.pos.flightmanagementsystem.controller;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SuperAdminController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;

    // +++ Sidebar Button များကို FXML နှင့် ချိတ်ဆက်ရန် +++
    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnACManage;
    @FXML private Button btnSystemLogs;
    @FXML private Button btnSystemTools;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ၁။ Sidebar Entry Animation
        if (sidebar != null) {
            sidebar.setTranslateX(-300);
            TranslateTransition sidebarAnim = new TranslateTransition(Duration.millis(700), sidebar);
            sidebarAnim.setToX(0);
            sidebarAnim.setInterpolator(Interpolator.EASE_OUT);
            sidebarAnim.play();
        }

        // ၂။ Login ဝင်ဝင်ချင်း Dashboard ကို ပြမည်
        Platform.runLater(() -> {
            loadPage("/com/pos/flightmanagementsystem/view/SuperDashboard.fxml");
            setActiveButton(btnDashboard); // အစပိုင်းတွင် Dashboard Button ကို Active ဖြစ်နေစေရန်
        });
    }

    // ==========================================
    // 🌟 DYNAMIC ACTIVE BUTTON LOGIC
    // ==========================================
    private void setActiveButton(Button clickedButton) {
        Button[] allButtons = {btnDashboard, btnProfile, btnACManage, btnSystemLogs, btnSystemTools};

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

    // ==========================================
    // PAGE TRANSITION LOGIC
    // ==========================================
    private void loadPage(String fxmlPath) {
        Node currentPage = rootPane.getCenter();

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
            if (fileUrl == null) {
                System.out.println("❌ ERROR: ညာဘက်ခြမ်း Page ဖိုင်ကို ရှာမတွေ့ပါ။ -> " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fileUrl);
            Parent newPage = loader.load();
            newPage.setTranslateX(1200);
            rootPane.setCenter(newPage);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(durationMillis), newPage);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);
            slideIn.play();

        } catch (Exception e) {
            System.out.println("❌ ERROR: Page ထဲတွင် မှားယွင်းမှုတစ်ခုခုရှိနေပါသည်။ ဖိုင် -> " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ==========================================
    // SIDEBAR MENU CLICKS
    // ==========================================
    @FXML void clickDashboard(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SuperDashboard.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML void clickProfile(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/Profile.fxml");
        setActiveButton(btnProfile);
    }

    @FXML void clickACManagement(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/ACManagement.fxml");
        setActiveButton(btnACManage);
    }

    @FXML void clickSystemLogs(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SystemLogs.fxml");
        setActiveButton(btnSystemLogs);
    }

    @FXML void clickSystemTools(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SystemTools.fxml");
        setActiveButton(btnSystemTools);
    }

    // ==========================================
    // 🌟 LOGOUT ANIMATION & LOGIC
    // ==========================================
    @FXML void clickLogout(ActionEvent event) {
        System.out.println("Logging out with animation...");

        // ဘယ်ဘက် Sidebar ကို ဘယ်ဘက်သို့ -800 ရွှေ့ကာ ထွက်သွားစေခြင်း
        TranslateTransition leftAnim = new TranslateTransition(Duration.millis(700), sidebar);
        leftAnim.setByX(-800);
        leftAnim.setInterpolator(Interpolator.EASE_IN);

        // ညာဘက် Content (Center Node) ကို ညာဘက်သို့ +800 ရွှေ့ကာ ထွက်သွားစေခြင်း
        Node rightContent = rootPane.getCenter();
        TranslateTransition rightAnim = new TranslateTransition(Duration.millis(700), rightContent);
        if (rightContent != null) {
            rightAnim.setByX(800);
            rightAnim.setInterpolator(Interpolator.EASE_IN);
        }

        // ParallelTransition ဖြင့် ပြိုင်တူ Animation ပြုလုပ်ပြီး ပြီးမှ Login Page ခေါ်မည်
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

            // 🌟 ဒီနှစ်ကြောင်းကို ထပ်ထည့်ပေးပါ 🌟
            if (stage.isMaximized()) stage.setMaximized(false); // Layout တွေ နေရာတကျပြန်ဖြစ်အောင် Reset အရင်ချသည်
            stage.setMaximized(true); // Full Screen (Maximized) အပြည့် ပြန်လုပ်သည်

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}