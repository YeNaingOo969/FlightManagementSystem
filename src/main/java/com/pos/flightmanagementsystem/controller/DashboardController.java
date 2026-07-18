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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ၁။ Sidebar Entry Animation (ဘယ်ဘက်မှ ညာဘက်သို့ ဝင်လာခြင်း - 700ms)
        if (sidebar != null) {
            sidebar.setTranslateX(-300);
            TranslateTransition sidebarAnim = new TranslateTransition(Duration.millis(700), sidebar);
            sidebarAnim.setToX(0);
            sidebarAnim.setInterpolator(Interpolator.EASE_OUT);
            sidebarAnim.play();
        }

        // ၂။ Login ဝင်ဝင်ချင်း ညာဘက်ခြမ်း ဖြတ်ခနဲမပေါ်လာစေရန် Platform.runLater ကို သုံးပါမည်
        Platform.runLater(() -> {
            loadPage("/com/pos/flightmanagementsystem/view/SuperDashboard.fxml");
        });
    }

    // ==========================================
    // PAGE TRANSITION LOGIC
    // ==========================================
    private void loadPage(String fxmlPath) {
        Node currentPage = rootPane.getCenter();

        if (currentPage != null) {
            // လက်ရှိ Page ရှိနေလျှင် (Page ကူးပြောင်းချိန်) ညာဘက်သို့ ဆွဲထုတ်မည်
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(350), currentPage);
            slideOut.setToX(1200);
            slideOut.setInterpolator(Interpolator.EASE_IN);

            // ထွက်သွားပြီးမှ Page အသစ်ကို ဝင်လာစေမည် (Speed ကို 400ms ဖြင့် သတ်မှတ်သည်)
            slideOut.setOnFinished(e -> showNewPage(fxmlPath, 400));
            slideOut.play();
        } else {
            // Login ဝင်ဝင်ချင်း ပထမဆုံးအကြိမ်ဖြစ်လျှင် Sidebar နှင့် Speed ကိုက်ညီစေရန် (700ms ဖြင့် သတ်မှတ်သည်)
            showNewPage(fxmlPath, 700);
        }
    }

    // showNewPage ကို Speed (durationMillis) အလိုအလျောက်ပြောင်းလဲနိုင်အောင် ပြင်ဆင်ထားပါသည်
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

            // Speed နေရာတွင် သတ်မှတ်ပေးလိုက်သော အချိန်ကို ယူသုံးပါမည်
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
    @FXML void clickDashboard(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SuperDashboard.fxml"); }

    @FXML void clickProfile(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/Profile.fxml"); }

    @FXML void clickACManagement(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/AccountManagement.fxml"); }

    @FXML void clickSystemLogs(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SystemLogs.fxml"); }

    @FXML void clickSystemTools(ActionEvent event) { loadPage("/com/pos/flightmanagementsystem/view/SystemTools.fxml"); }

    @FXML void clickLogout(ActionEvent event) { System.out.println("Logging out..."); }
}