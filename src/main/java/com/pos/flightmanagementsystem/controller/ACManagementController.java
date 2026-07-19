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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ACManagementController {

    @FXML
    private AnchorPane acRootPane; // FXML ရဲ့ အပေါ်ဆုံး Root Pane

    @FXML
    private Button btnCreateUser; // Create User Button လေး

    @FXML
    void openCreateUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/CreateUser.fxml"));
            Parent createUserRoot = loader.load();

            // (၁) CreateUser ရဲ့ Controller ကို လှမ်းယူမည်
            CreateUserController createUserController = loader.getController();

            // နောက်ခံမှောင်သွားစေရန် Overlay ဖန်တီးခြင်း
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
            overlay.getChildren().add(createUserRoot);

            acRootPane.getChildren().add(overlay);
            AnchorPane.setTopAnchor(overlay, 0.0);
            AnchorPane.setBottomAnchor(overlay, 0.0);
            AnchorPane.setLeftAnchor(overlay, 0.0);
            AnchorPane.setRightAnchor(overlay, 0.0);

            // ==============================================================
            // ခလုတ်နေရာကနေ ထွက်လာဖို့ တည်နေရာ တွက်ချက်ခြင်း (Location Math)
            // ==============================================================

            // Screen ပေါ်က Button နဲ့ Root နေရာ အတိအကျကို ယူခြင်း
            Bounds btnBounds = btnCreateUser.localToScene(btnCreateUser.getBoundsInLocal());
            Bounds rootBounds = acRootPane.localToScene(acRootPane.getBoundsInLocal());

            // Root ရဲ့ အလယ်မှတ်
            double rootCenterX = rootBounds.getWidth() / 2;
            double rootCenterY = rootBounds.getHeight() / 2;

            // Button ရဲ့ အလယ်မှတ်
            double btnCenterX = btnBounds.getMinX() + (btnBounds.getWidth() / 2) - rootBounds.getMinX();
            double btnCenterY = btnBounds.getMinY() + (btnBounds.getHeight() / 2) - rootBounds.getMinY();

            // Button ဆီကနေ အလယ်ကို လာဖို့ အကွာအဝေး
            double startTransX = btnCenterX - rootCenterX;
            double startTransY = btnCenterY - rootCenterY;

            // (၂) Cancel နှိပ်လျှင် ပြန်ပိတ်သွားမည့် Animation (closePopup) ကို ချိတ်ပေးခြင်း
            createUserController.setCancelAnimationAction(() -> {
                closePopup(overlay, createUserRoot, startTransX, startTransY);
            });

            // Form ကို ခလုတ်နေရာမှာ သေးသေးလေးဖြစ်အောင် အရင်ထားလိုက်ခြင်း
            createUserRoot.setTranslateX(startTransX);
            createUserRoot.setTranslateY(startTransY);
            createUserRoot.setScaleX(0.0);
            createUserRoot.setScaleY(0.0);
            createUserRoot.setOpacity(0.0);
            overlay.setOpacity(0.0);

            // ==============================================================
            // Animation များ စတင်ခြင်း
            // ==============================================================

            // ၁။ အနောက်ခံ မှောင်လာခြင်း
            FadeTransition bgFade = new FadeTransition(Duration.millis(300), overlay);
            bgFade.setToValue(1.0);

            // ၂။ Form ကြီးလာခြင်း
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), createUserRoot);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.setInterpolator(Interpolator.EASE_OUT); // မြန်ရာမှ နှေးသွားစေရန်

            // ၃။ Form က ခလုတ်ဆီကနေ အလယ်ကို ပြေးလာခြင်း
            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(400), createUserRoot);
            moveCenter.setToX(0); // 0 ဆိုတာ StackPane ရဲ့ အလယ်တည့်တည့်ပါ
            moveCenter.setToY(0);
            moveCenter.setInterpolator(Interpolator.EASE_OUT);

            // ၄။ Form ပေါ်လာခြင်း
            FadeTransition formFade = new FadeTransition(Duration.millis(250), createUserRoot);
            formFade.setToValue(1.0);

            // Animation အကုန်လုံးကို ပြိုင်တူဖွင့်ခြင်း
            ParallelTransition pt = new ParallelTransition(bgFade, scaleUp, moveCenter, formFade);
            pt.play();

            // အပြင်ဘက်အမည်းရောင်နေရာကို နှိပ်လိုက်လျှင်လည်း ပြန်ပိတ်သွားအောင်
            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    closePopup(overlay, createUserRoot, startTransX, startTransY);
                }
            });

        } catch (Exception e) {
            System.out.println("❌ Error loading CreateUser.fxml");
            e.printStackTrace();
        }
    }

    // Popup ကို ခလုတ်ဆီ ပြန်ကျုံ့ဝင်သွားစေသည့် Animation
    private void closePopup(StackPane overlay, Parent createUserRoot, double endTransX, double endTransY) {

        // သေးသွားခြင်း
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), createUserRoot);
        scaleDown.setToX(0.0);
        scaleDown.setToY(0.0);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        // ခလုတ်ဆီကို ပြန်ပြေးသွားခြင်း
        TranslateTransition moveBack = new TranslateTransition(Duration.millis(300), createUserRoot);
        moveBack.setToX(endTransX);
        moveBack.setToY(endTransY);
        moveBack.setInterpolator(Interpolator.EASE_IN);

        FadeTransition formFadeOut = new FadeTransition(Duration.millis(200), createUserRoot);
        formFadeOut.setToValue(0.0);

        FadeTransition bgFadeOut = new FadeTransition(Duration.millis(300), overlay);
        bgFadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, moveBack, formFadeOut, bgFadeOut);
        pt.setOnFinished(event -> acRootPane.getChildren().remove(overlay)); // Animation ပြီးမှ ဖယ်ရှားမည်
        pt.play();
    }
}