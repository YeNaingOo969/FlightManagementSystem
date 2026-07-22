package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class StaffSeatSelectionController implements Initializable {

    @FXML private ComboBox<String> classComboBox;
    @FXML private Pane viewportPane;
    @FXML private VBox sideViewPlane;

    // 🌟 StackPane ကိုပါ လက်ခံနိုင်ရန် Pane ဟု ပြောင်းထားပါသည်
    @FXML private Pane topViewPlane;

    @FXML private VBox businessSection;
    @FXML private VBox premiumSection;
    @FXML private VBox economySection;

    @FXML private Label lblSelectedSeat;
    @FXML private Label lblSeatCharge;
    @FXML private Label lblTotal;

    private boolean isTopViewVisible = false;
    private Button currentlySelectedSeat = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        classComboBox.getItems().addAll("Business", "Premium Economic", "Economic");

        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(viewportPane.widthProperty());
        clipRect.heightProperty().bind(viewportPane.heightProperty());
        viewportPane.setClip(clipRect);

        classComboBox.setOnAction(e -> animatePlaneToSection(classComboBox.getValue()));
    }

    private void animatePlaneToSection(String selectedClass) {
        double targetY = 0;

        if (selectedClass.equals("Business")) {
            targetY = -15;
        } else if (selectedClass.equals("Premium Economic")) {
            targetY = -(premiumSection.getLayoutY() - 30);
        } else if (selectedClass.equals("Economic")) {
            targetY = -(economySection.getLayoutY() - 30);
        }

        ParallelTransition parallelTransition = new ParallelTransition();

        if (!isTopViewVisible) {
            FadeTransition fadeOutSide = new FadeTransition(Duration.millis(500), sideViewPlane);
            fadeOutSide.setToValue(0);

            FadeTransition fadeInTop = new FadeTransition(Duration.millis(800), topViewPlane);
            fadeInTop.setToValue(1);

            parallelTransition.getChildren().addAll(fadeOutSide, fadeInTop);
            isTopViewVisible = true;
        }

        TranslateTransition slidePlane = new TranslateTransition(Duration.millis(800), topViewPlane);
        slidePlane.setToY(targetY);

        parallelTransition.getChildren().add(slidePlane);
        parallelTransition.play();
    }

    @FXML
    void selectSeat(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if (clickedButton.getStyleClass().contains("seat-booked")) {
            return;
        }

        if (currentlySelectedSeat != null) {
            currentlySelectedSeat.getStyleClass().remove("seat-selected");
            currentlySelectedSeat.getStyleClass().add("seat-available");
        }

        clickedButton.getStyleClass().remove("seat-available");
        clickedButton.getStyleClass().add("seat-selected");
        currentlySelectedSeat = clickedButton;

        String selectedClass = classComboBox.getValue() != null ? classComboBox.getValue() : "";
        lblSelectedSeat.setText(selectedClass + " - " + clickedButton.getText());
        lblSeatCharge.setText("\\$10.00");
        lblTotal.setText("\\$130.00");
    }
}