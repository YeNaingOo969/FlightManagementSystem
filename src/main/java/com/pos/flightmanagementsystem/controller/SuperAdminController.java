package com.pos.flightmanagementsystem.controller;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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
    // 🌟 DYNAMIC ACTIVE BUTTON LOGIC (အသစ်ထည့်ထားသောအပိုင်း)
    // ==========================================
    private void setActiveButton(Button clickedButton) {
        // ခလုတ်အားလုံးကို Array ထဲ စုထည့်ပါမည်
        Button[] allButtons = {btnDashboard, btnProfile, btnACManage, btnSystemLogs, btnSystemTools};

        for (Button btn : allButtons) {
            if (btn != null) {
                // ခလုတ်အားလုံးဆီမှ active style ကို ဖြုတ်ပစ်မည်
                btn.getStyleClass().remove("super-nav-button-active");
                // ရိုးရိုး style မရှိသေးရင် ပြန်ထည့်ပေးမည် (ပုံမှန်အရောင် ပြန်ဖြစ်သွားရန်)
                if (!btn.getStyleClass().contains("super-nav-button")) {
                    btn.getStyleClass().add("super-nav-button");
                }
            }
        }

        // နှိပ်လိုက်သော (ရွေးချယ်လိုက်သော) ခလုတ်ကိုသာ active style ထည့်မည်
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
        setActiveButton(btnDashboard); // Dashboard ခလုတ်ကို Active ပြောင်းမည်
    }

    @FXML void clickProfile(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/Profile.fxml");
        setActiveButton(btnProfile); // Profile ခလုတ်ကို Active ပြောင်းမည်
    }

    @FXML void clickACManagement(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/ACManagement.fxml");
        setActiveButton(btnACManage); // AC Manage ခလုတ်ကို Active ပြောင်းမည်
    }

    @FXML void clickSystemLogs(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SystemLogs.fxml");
        setActiveButton(btnSystemLogs); // System Logs ခလုတ်ကို Active ပြောင်းမည်
    }

    @FXML void clickSystemTools(ActionEvent event) {
        loadPage("/com/pos/flightmanagementsystem/view/SystemTools.fxml");
        setActiveButton(btnSystemTools); // System Tools ခလုတ်ကို Active ပြောင်းမည်
    }

    @FXML void clickLogout(ActionEvent event) { System.out.println("Logging out..."); }
}