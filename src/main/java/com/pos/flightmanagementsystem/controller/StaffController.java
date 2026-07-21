package com.pos.flightmanagementsystem.controller;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
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

public class StaffController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;

    // --- Buttons ---
    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnCreateBooking;
    @FXML private Button btnManageBooking;
    @FXML private Button btnViewSchedule;
    @FXML private Button btnLogout;

    // --- Icons ---
    @FXML private SVGPath iconDashboard;
    @FXML private SVGPath iconProfile;
    @FXML private SVGPath iconCreateBooking;
    @FXML private SVGPath iconManageBooking;
    @FXML private SVGPath iconViewSchedule;
    @FXML private SVGPath iconLogout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 🌟 1. Sidebar ဘယ်ဘက်မှ ဝင်လာမည့် Entrance Animation
        if (sidebar != null) {
            sidebar.setTranslateX(-300);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), sidebar);
            slideIn.setToX(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);
            slideIn.play();
        }

        // 🌟 2. Default အနေဖြင့် StaffProfile.fxml ကို Center တွင် ပြသခြင်း
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffProfile.fxml");

        // 🌟 3. Icon Hover Animations များ ချိတ်ဆက်ခြင်း
        setupIconHoverAnimation(btnDashboard, iconDashboard);
        setupIconHoverAnimation(btnProfile, iconProfile);
        setupIconHoverAnimation(btnCreateBooking, iconCreateBooking);
        setupIconHoverAnimation(btnManageBooking, iconManageBooking);
        setupIconHoverAnimation(btnViewSchedule, iconViewSchedule);
        setupIconHoverAnimation(btnLogout, iconLogout);
    }

    // ==========================================
    // 🌟 DYNAMIC VIEW LOADER (ညာဘက်မှ ဝင်လာမည့် Animation ဖြင့်)
    // ==========================================
    private void loadCenterView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // ညာဘက်မှ Center သို့ ဝင်လာမည့် Animation
            view.setTranslateX(800);
            TranslateTransition rightIn = new TranslateTransition(Duration.millis(800), view);
            rightIn.setToX(0);
            rightIn.setInterpolator(Interpolator.EASE_OUT);
            rightIn.play();

            // BorderPane ၏ Center တွင် နေရာချခြင်း
            rootPane.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxmlPath);
        }
    }

    // ==========================================
    // 🌟 ICON HOVER ANIMATION HELPER
    // ==========================================
    private void setupIconHoverAnimation(Button button, SVGPath icon) {
        if (button != null && icon != null) {
            button.setOnMouseEntered(e -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), icon);
                scaleUp.setToX(1.2);
                scaleUp.setToY(1.2);
                scaleUp.play();
            });

            button.setOnMouseExited(e -> {
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), icon);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });
        }
    }

    // ==========================================
    // 🌟 BUTTON ACTION HANDLERS
    // ==========================================

    @FXML
    void clickDashboard(ActionEvent event) {
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffProfile.fxml");
    }

    @FXML
    void clickProfile(ActionEvent event) {
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffProfile.fxml");
    }

    @FXML
    void clickCreateBooking(ActionEvent event) {
        // 🌟 Create New Booking ကို နှိပ်လျှင် StaffCreateNewBooking.fxml သို့ Center တွင် ပြောင်းလဲပေးမည်
        loadCenterView("/com/pos/flightmanagementsystem/view/StaffCreateNewBooking.fxml");
    }

    @FXML
    void clickManageBooking(ActionEvent event) {
        System.out.println("Manage Booking Clicked - View Coming Soon");
    }

    @FXML
    void clickViewSchedule(ActionEvent event) {
        System.out.println("View Schedule Clicked - View Coming Soon");
    }

    @FXML
    void clickLogout(ActionEvent event) {
        TranslateTransition slideOutLeft = new TranslateTransition(Duration.millis(600), sidebar);
        slideOutLeft.setToX(-300);
        slideOutLeft.setInterpolator(Interpolator.EASE_IN);

        Node centerNode = rootPane.getCenter();
        TranslateTransition slideOutRight = new TranslateTransition(Duration.millis(600), centerNode);
        if (centerNode != null) {
            slideOutRight.setToX(800);
            slideOutRight.setInterpolator(Interpolator.EASE_IN);
        }

        ParallelTransition ptOut = new ParallelTransition(slideOutLeft, slideOutRight);
        ptOut.setOnFinished(e -> {
            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        ptOut.play();
    }
}