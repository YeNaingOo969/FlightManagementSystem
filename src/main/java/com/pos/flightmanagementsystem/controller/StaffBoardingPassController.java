package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class StaffBoardingPassController {

    @FXML private StackPane modalRoot;

    @FXML
    void clickClose(ActionEvent event) {
        closeModal();
    }

    @FXML
    void clickDownload(ActionEvent event) {
        System.out.println("Downloading Boarding Pass...");
        closeModal();
    }

    @FXML
    void clickPrint(ActionEvent event) {
        System.out.println("Printing Boarding Pass...");
        closeModal();
    }

    private void closeModal() {
        StackPane parentRoot = (StackPane) modalRoot.getParent();

        if (parentRoot != null) {

            // 🌟 1. အနောက်က Background (mainContainer) ရဲ့ Blur ကို ပြန်ဖြုတ်မည့် Animation
            if (!parentRoot.getChildren().isEmpty()) {
                Node mainView = parentRoot.getChildren().get(0);
                if (mainView.getEffect() instanceof GaussianBlur) {
                    GaussianBlur blur = (GaussianBlur) mainView.getEffect();
                    Timeline blurOut = new Timeline(
                            new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 0)) // 15 ကနေ 0 သို့ ပြန်ကြည်သွားမည်
                    );
                    blurOut.setOnFinished(ev -> mainView.setEffect(null)); // ကြည်သွားရင် Effect ကို လုံးဝဖြုတ်ပစ်မည်
                    blurOut.play();
                }
            }

            // 2. Modal ကိုယ်တိုင် ပျောက်သွားမည့် Animation
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), modalRoot);
            scaleDown.setToX(0.5);
            scaleDown.setToY(0.5);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), modalRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
            pt.setInterpolator(Interpolator.EASE_IN);

            pt.setOnFinished(e -> parentRoot.getChildren().remove(modalRoot));
            pt.play();
        }
    }
}