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
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class StaffBoardingPassController {

    @FXML private StackPane modalRoot;

    // 🌟 Boarding Pass တွင် Data များထည့်ရန် Label များ
    @FXML private Label lblClass;
    @FXML private Label lblPassengerName;
    @FXML private Label lblFrom;
    @FXML private Label lblTo;
    @FXML private Label lblDate;
    @FXML private Label lblTime;
    @FXML private Label lblFlight;
    @FXML private Label lblSeat;
    @FXML private Label lblGate;

    // ==========================================
    // 🌟 Seat Selection မှ Data များကို Boarding Pass သို့ ထည့်သွင်းခြင်း
    // ==========================================
    public void setBoardingPassData(String pName, String fClass, String from, String to, String date, String time, String flightNo, String seatNumber) {
        // Data များ အစားထိုးခြင်း
        lblPassengerName.setText(pName != null ? pName.toUpperCase() : "UNKNOWN");
        lblClass.setText(fClass != null ? fClass.toUpperCase() : "ECONOMY");

        lblFrom.setText(formatCityCode(from));
        lblTo.setText(formatCityCode(to));

        lblDate.setText(date);
        lblTime.setText(time);
        lblFlight.setText(flightNo);
        lblSeat.setText(seatNumber);

        // Gate ကို လောလောဆယ် Random ဖန်တီးထားပါသည်
        int randomGate = (int) (Math.random() * 20) + 1;
        lblGate.setText("G" + randomGate);
    }

    // 🌟 မြို့အမည်များ (Yangon, Bangkok စသည်) ကို လေယာဉ်လက်မှတ်သုံး (၃) လုံးပြ အတိုကောက်ပြောင်းခြင်း
    private String formatCityCode(String city) {
        if (city == null || city.isEmpty()) return "UNK";
        if (city.equalsIgnoreCase("Yangon")) return "YGN";
        if (city.equalsIgnoreCase("Mandalay")) return "MDL";
        if (city.equalsIgnoreCase("Bangkok")) return "BKK";
        if (city.equalsIgnoreCase("Singapore")) return "SIN";
        if (city.equalsIgnoreCase("Sittwe")) return "AKY";
        return city.substring(0, 3).toUpperCase();
    }

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
            if (!parentRoot.getChildren().isEmpty()) {
                Node mainView = parentRoot.getChildren().get(0);
                if (mainView.getEffect() instanceof GaussianBlur) {
                    GaussianBlur blur = (GaussianBlur) mainView.getEffect();
                    Timeline blurOut = new Timeline(
                            new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 0))
                    );
                    blurOut.setOnFinished(ev -> mainView.setEffect(null));
                    blurOut.play();
                }
            }

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