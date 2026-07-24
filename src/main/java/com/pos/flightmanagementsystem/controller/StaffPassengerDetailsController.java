package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class StaffPassengerDetailsController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private Button btnBack;
    @FXML private Button btnContinue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Form အဖွင့်တွင် လိုအပ်ပါက Initial ကုဒ်များ ဤနေရာတွင် ရေးရန်
    }

    // ==========================================
    // 🌟 BACK BUTTON (အရင် Page သို့ ပြန်သွားမည်)
    // ==========================================
    @FXML
    void clickBack(ActionEvent event) {
        // StaffCreateNewBooking သို့ Backward Animation ဖြင့် ပြန်သွားမည်
        navigateTo("/com/pos/flightmanagementsystem/view/StaffCreateNewBooking.fxml", true);
    }

    // ==========================================
    // 🌟 CONTINUE BUTTON (နောက် Page သို့ ဆက်သွားမည်)
    // ==========================================
    @FXML
    void clickContinue(ActionEvent event) {
        // Seat Selection သို့ Forward Animation ဖြင့် ဆက်သွားမည်
        navigateTo("/com/pos/flightmanagementsystem/view/StaffSeatSelection.fxml", false);
    }

    // ==========================================
    // 🌟 ANIMATED PAGE TRANSITION (Forward & Backward 3D Depth Effect)
    // ==========================================
    private void navigateTo(String fxmlPath, boolean isBack) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent nextView = loader.load();

            // 🌟 ဝင်လာမည့် Page ၏ အနေအထား (Scale) သတ်မှတ်ခြင်း
            if (isBack) {
                // အနောက်သို့ ပြန်သွားမည်ဆိုပါက, Page အသစ်က (ကင်မရာအရှေ့ ကြီးရာမှနေ၍) ဝင်လာမည်
                nextView.setScaleX(1.2);
                nextView.setScaleY(1.2);
            } else {
                // အရှေ့သို့ သွားမည်ဆိုပါက, Page အသစ်က (အနောက်ဘက် သေးရာမှနေ၍) ဝင်လာမည်
                nextView.setScaleX(0.8);
                nextView.setScaleY(0.8);
            }
            nextView.setOpacity(0.0);

            // StackPane ထဲသို့ Page အသစ်ကို ထပ်ထည့်လိုက်ပါမည်
            rootStackPane.getChildren().add(nextView);

            // 🌟 ၁။ လက်ရှိ Page (mainContainer) ထွက်သွားမည့် Animation
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer);
            if (isBack) {
                scaleOut.setToX(0.8); // Back ဆိုလျှင် လက်ရှိ Page က အနောက်သို့ ကျုံ့ဝင်သွားမည်
                scaleOut.setToY(0.8);
            } else {
                scaleOut.setToX(1.2); // Continue ဆိုလျှင် လက်ရှိ Page က ကင်မရာအရှေ့သို့ ထွက်သွားမည်
                scaleOut.setToY(1.2);
            }
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0.0);

            // 🌟 ၂။ Page အသစ် (nextView) ဝင်လာမည့် Animation (1.0 သို့ ပြန်ရောက်မည်)
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView);
            fadeIn.setToValue(1.0);

            // 🌟 ၃။ Transition နှစ်ခုကို တပြိုင်နက် Run ပေးခြင်း
            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
            pt.setInterpolator(Interpolator.EASE_BOTH); // အဝင်အထွက် ညက်ညောစေရန်

            // Animation ပြီးဆုံးပါက လက်ရှိ Page အဟောင်းကို RAM ပေါ်မှ ဖယ်ရှားပါမည်
            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer));
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load FXML file: " + fxmlPath);
        }
    }
}