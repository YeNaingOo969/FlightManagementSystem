package com.pos.flightmanagementsystem.controller;

import javafx.animation.*;
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
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SuperAdminController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;

    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnACManage;
    @FXML private Button btnSystemLogs;
    @FXML private Button btnSystemTools;
    @FXML private Button btnLogout;

    // +++ Icon Animation များအတွက် FXML ချိတ်ဆက်ခြင်း +++
    @FXML private SVGPath iconDashboard;
    @FXML private SVGPath iconProfile;
    @FXML private SVGPath iconACManage;
    @FXML private SVGPath iconSystemLogs;
    @FXML private SVGPath iconSystemTools;
    @FXML private SVGPath iconLogout;

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
            loadPage("/com/pos/flightmanagementsystem/view/SuperAdminDashboard.fxml");
            setActiveButton(btnDashboard);
        });

        // ၃။ Icon Animation များကို စတင်မည်
        startIconAnimations();
    }

    // ==========================================
    // 🌟 CONTINUOUS ICON ANIMATIONS
    // ==========================================
    // ==========================================
    // 🌟 CONTINUOUS ICON ANIMATIONS (ပိုမိုသိသာထင်ရှားသော အလန်းစား Effect များ)
    // ==========================================
    private void startIconAnimations() {
        // ၁. Dashboard - Pulse (နှလုံးခုန်သလို သိသိသာသာ ကြီးလိုက်သေးလိုက်)
        ScaleTransition dashAnim = new ScaleTransition(Duration.seconds(1.0), iconDashboard); // အချိန်ပိုမြန်သွားသည်
        dashAnim.setByX(0.25); // ပိုကြီးသွားမည် (အရင် 0.15)
        dashAnim.setByY(0.25);
        dashAnim.setAutoReverse(true);
        dashAnim.setCycleCount(Timeline.INDEFINITE);
        dashAnim.setInterpolator(Interpolator.EASE_BOTH);
        dashAnim.play();

        // ၂. Profile - Floating (လေပေါ် ပိုမိုမြင့်မားစွာ လွင့်နေခြင်း)
        TranslateTransition profileAnim = new TranslateTransition(Duration.seconds(1.5), iconProfile);
        profileAnim.setByY(-6); // အပေါ်ကို ပိုတက်မည် (အရင် -3)
        profileAnim.setAutoReverse(true);
        profileAnim.setCycleCount(Timeline.INDEFINITE);
        profileAnim.setInterpolator(Interpolator.EASE_BOTH);
        profileAnim.play();

        // ၃. Account Manage - Swinging (ဘယ်ညာ ပိုကျယ်ကျယ် လွှဲနေခြင်း)
        RotateTransition acManageAnim = new RotateTransition(Duration.seconds(1.5), iconACManage); // ပိုမြန်မည်
        acManageAnim.setByAngle(25); // ပိုကျယ်ကျယ်လွှဲမည် (အရင် 15)
        acManageAnim.setAutoReverse(true);
        acManageAnim.setCycleCount(Timeline.INDEFINITE);
        acManageAnim.setInterpolator(Interpolator.EASE_BOTH);
        acManageAnim.play();

        // ၄. System Logs - Bouncing (အပေါ်အောက် ခပ်မြန်မြန်နှင့် သိသိသာသာ ခုန်ခြင်း)
        TranslateTransition logsAnim = new TranslateTransition(Duration.seconds(0.8), iconSystemLogs); // ပိုမြန်မည်
        logsAnim.setByY(-4); // အပေါ်ကို ခုန်တက်မည် (အရင် +2)
        logsAnim.setAutoReverse(true);
        logsAnim.setCycleCount(Timeline.INDEFINITE);
        logsAnim.setInterpolator(Interpolator.EASE_BOTH);
        logsAnim.play();

        // ၅. System Tools - Rotating Gear (ဂီယာလေး ၃၆၀ ဒီဂရီ အပြည့်လည်ခြင်း)
        RotateTransition toolsAnim = new RotateTransition(Duration.seconds(3.0), iconSystemTools); // ပိုမြန်မည်
        toolsAnim.setByAngle(360); // တစ်ပတ်အပြည့်လည်မည် (အရင် 180)
        toolsAnim.setCycleCount(Timeline.INDEFINITE);
        toolsAnim.setInterpolator(Interpolator.LINEAR);
        toolsAnim.play();

        // ၆. Logout - Sliding Arrow (အပြင်ထွက်ရန် သိသိသာသာ ညွှန်ပြနေခြင်း)
        TranslateTransition logoutAnim = new TranslateTransition(Duration.seconds(0.7), iconLogout); // ပိုမြန်မည်
        logoutAnim.setByX(8); // ပိုပြီး ထိုးထွက်မည် (အရင် 4)
        logoutAnim.setAutoReverse(true);
        logoutAnim.setCycleCount(Timeline.INDEFINITE);
        logoutAnim.setInterpolator(Interpolator.EASE_BOTH);
        logoutAnim.play();
    }

    // ==========================================
    // DYNAMIC ACTIVE BUTTON LOGIC
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
        loadPage("/com/pos/flightmanagementsystem/view/SuperAdminDashboard.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML void clickProfile(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SuperAdminProfile.fxml");
        setActiveButton(btnProfile);
    }

    @FXML void clickACManagement(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SuperAdminAccManagement.fxml");
        setActiveButton(btnACManage);
    }

    @FXML void clickSystemLogs(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SuperAdminSystemLogs.fxml");
        setActiveButton(btnSystemLogs);
    }

    @FXML void clickSystemTools(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SuperAdminSystemTools.fxml");
        setActiveButton(btnSystemTools);
    }

    // ==========================================
    // LOGOUT ANIMATION & LOGIC
    // ==========================================
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