package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class AdminCityController {

    @FXML private StackPane rootStackPane;
    @FXML private Button btnAddCity;

    @FXML
    void clickAddCity(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminAddCity.fxml"));
            Parent modalView = loader.load();

            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

            Bounds buttonBounds = btnAddCity.localToScene(btnAddCity.getBoundsInLocal());

            double rootCenterX = rootStackPane.getWidth() / 2;
            double rootCenterY = rootStackPane.getHeight() / 2;

            double buttonCenterX = buttonBounds.getMinX() + (buttonBounds.getWidth() / 2);
            double buttonCenterY = buttonBounds.getMinY() + (buttonBounds.getHeight() / 2);

            modalView.setTranslateX(buttonCenterX - rootCenterX);
            modalView.setTranslateY(buttonCenterY - rootCenterY);

            modalView.setScaleX(0.0);
            modalView.setScaleY(0.0);

            rootStackPane.getChildren().add(overlay);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(350), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(350), modalView);
            moveCenter.setToX(0);
            moveCenter.setToY(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, moveCenter, fadeIn);
            pt.setInterpolator(Interpolator.EASE_OUT);
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load AdminAddCity.fxml");
        }
    }
}