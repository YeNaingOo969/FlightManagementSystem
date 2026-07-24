package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class AdminCancelBookingReasonController {

    @FXML private BorderPane rootPane;
    @FXML private TextArea txtReason;

    // 🌟 Cancel ကို နှိပ်လျှင် အနောက်သို့ ပြန်ဝင်သွားမည့် Animation
    @FXML
    void clickCancel(ActionEvent event) {
        closeModal();
    }

    // 🌟 Confirm ကို နှိပ်လျှင် Reason ကို ယူပြီး ပိတ်မည်
    @FXML
    void clickConfirm(ActionEvent event) {
        String reason = txtReason.getText();
        if (reason.trim().isEmpty()) {
            System.out.println("Please provide a reason!");
            return; // နေရာလွတ်ဖြစ်နေလျှင် မပိတ်ပါ
        }

        System.out.println("Booking Cancelled. Reason: " + reason);
        // ဤနေရာတွင် Database update ကုဒ်များ ရေးရန်

        closeModal();
    }

    // 🌟 Modal ပိတ်သည့် Animation (Zoom Out Effect)
    private void closeModal() {
        StackPane overlay = (StackPane) rootPane.getParent();

        if (overlay != null) {
            // ၁ ကနေ ၀.၅ သို့ သေးသွားမည် (အနောက်သို့ ဝင်သွားသကဲ့သို့)
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), rootPane);
            scaleDown.setToX(0.5);
            scaleDown.setToY(0.5);

            // လင်းရာမှ မှောင်သွားမည် (Fade Out)
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
            pt.setInterpolator(Interpolator.EASE_IN);

            pt.setOnFinished(e -> {
                StackPane mainRoot = (StackPane) overlay.getParent();
                if (mainRoot != null) {
                    mainRoot.getChildren().remove(overlay);
                }
            });

            pt.play();
        }
    }
}