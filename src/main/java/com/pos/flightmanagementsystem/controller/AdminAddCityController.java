package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class AdminAddCityController {

    @FXML private BorderPane rootPane;

    @FXML private TextField txtCityCode;
    @FXML private TextField txtCityName;
    @FXML private TextField txtCountry;

    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    @FXML
    void clickCancel(ActionEvent event) {
        StackPane overlay = (StackPane) rootPane.getParent();

        if (overlay != null) {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(350), rootPane);
            scaleDown.setToX(0.0);
            scaleDown.setToY(0.0);

            // X ကို 400 လောက်ထားလျှင် ညာဘက်အပေါ်ထောင့်နားရှိ Button ဆီ ပြန်လွင့်သွားပါမည်
            TranslateTransition moveBack = new TranslateTransition(Duration.millis(350), rootPane);
            moveBack.setToX(400);
            moveBack.setToY(-300);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), overlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(scaleDown, moveBack, fadeOut);
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

    @FXML
    void clickSave(ActionEvent event) {
        System.out.println("Saving New City to Database...");
        System.out.println("City Code: " + txtCityCode.getText());
        System.out.println("City Name: " + txtCityName.getText());
        System.out.println("Country: " + txtCountry.getText());

        // ဤနေရာတွင် Database သို့ Insert လုပ်သည့် ကုဒ်ရေးရန်

        clickCancel(event);
    }
}