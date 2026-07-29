package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private StackPane rightSideStack;
    @FXML private StackPane mainContentArea;

    @FXML private Button btnDashboard, btnFlightSchedule, btnViewCity, btnBookingManagement, btnViewSaleDetails, btnLogout;

    @FXML private Label topNavUsername;
    @FXML private Label topNavRole;
    @FXML private Circle topNavAvatarCircle;
    @FXML private HBox navProfileBox;

    @FXML private StackPane themeToggleBg;
    @FXML private StackPane themeThumb;
    @FXML private SVGPath themeIcon;

    private boolean isDarkMode = true;

    private String loggedInUsername = "";
    private String loggedInRole = "";
    private String loggedInImagePath = "";

    private final String MOON_SVG = "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z";
    private final String SUN_SVG = "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5s5-2.24,5-5S14.76,7,12,7z M2,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0 c-0.55,0-1,0.45-1,1S1.45,13,2,13z M20,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0c-0.55,0-1,0.45-1,1S19.45,13,20,13z M11,2v2 c0,0.55,0.45,1,1,1s1-0.45,1-1V2c0-0.55-0.45-1-1-1S11,1.45,11,2z M11,20v2c0,0.55,0.45,1,1,1s1-0.45,1-1v-2c0-0.55-0.45-1-1-1 C11.45,19,11,19.45,11,20z M5.99,4.58c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41l1.06,1.06 c0.39,0.39,1.03,0.39,1.41,0s0.39-1.03,0-1.41L5.99,4.58z M18.36,16.95c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41 l1.06,1.06c0.39,0.39,1.03,0.39,1.41,0c0.39-0.39,0.39-1.03,0-1.41L18.36,16.95z M19.42,5.99c0.39-0.39,0.39-1.03,0-1.41 c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L19.42,5.99z M7.05,18.36 c0.39-0.39,0.39-1.03,0-1.41c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L7.05,18.36z";

    private Button[] navButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navButtons = new Button[]{btnDashboard, btnFlightSchedule, btnViewCity, btnBookingManagement, btnViewSaleDetails};
        setActiveButton(btnDashboard);
        loadPage("SuperAdminDashboard.fxml");
    }

    public void setLoggedInUser(String username, String role, String imagePath) {
        this.loggedInUsername = username;
        this.loggedInRole = role;
        this.loggedInImagePath = imagePath;

        if (topNavUsername != null) topNavUsername.setText(username);
        if (topNavRole != null) topNavRole.setText(role.toUpperCase());

        if (topNavAvatarCircle != null && imagePath != null && !imagePath.isEmpty()) {
            try {
                File imgFile = new File("src/main/resources/com/pos/flightmanagementsystem/image/" + imagePath);
                if (imgFile.exists()) {
                    Image image = new Image(imgFile.toURI().toString());
                    topNavAvatarCircle.setFill(new ImagePattern(image));
                } else {
                    URL url = getClass().getResource("/com/pos/flightmanagementsystem/image/" + imagePath);
                    if (url != null) {
                        Image image = new Image(url.toExternalForm());
                        topNavAvatarCircle.setFill(new ImagePattern(image));
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load profile picture: " + imagePath);
            }
        }
    }

    // ==========================================
    // 🌟 PROFILE MODAL FLOATING ANIMATION 🌟
    // ==========================================
    @FXML
    void clickProfile(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Profile.fxml"));
            Parent modalView = loader.load();

            ProfileController controller = loader.getController();
            controller.setUserData(loggedInUsername, loggedInRole, loggedInImagePath);

            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

            Node mainContent = rightSideStack.getChildren().get(0);
            BoxBlur blur = new BoxBlur(12, 12, 3);
            mainContent.setEffect(blur);

            Bounds sourceBounds = navProfileBox.localToScene(navProfileBox.getBoundsInLocal());
            double sourceCenterX = sourceBounds.getMinX() + (sourceBounds.getWidth() / 2);
            double sourceCenterY = sourceBounds.getMinY() + (sourceBounds.getHeight() / 2);

            Bounds rootBounds = rightSideStack.localToScene(rightSideStack.getBoundsInLocal());
            double rootCenterX = rootBounds.getMinX() + (rootBounds.getWidth() / 2);
            double rootCenterY = rootBounds.getMinY() + (rootBounds.getHeight() / 2);

            double startX = sourceCenterX - rootCenterX;
            double startY = sourceCenterY - rootCenterY;

            controller.setCloseCallback(() -> closeProfileModal(overlay, mainContent, startX, startY));
            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) closeProfileModal(overlay, mainContent, startX, startY);
            });

            StackPane.setAlignment(modalView, Pos.CENTER);

            // 🌟 Animation စတင်မည့် နေရာနှင့် အရွယ်အစား 🌟
            modalView.setTranslateX(startX);
            modalView.setTranslateY(startY);
            modalView.setScaleX(0.05); // 0 အစား 0.05 မှစ၍ rendering ပို smooth ဖြစ်စေရန်
            modalView.setScaleY(0.05);
            modalView.setOpacity(0.0); // ရုတ်တရက်ပေါ်မလာစေရန် Opacity 0 မှစပါမည်

            rightSideStack.getChildren().add(overlay);

            // 🌟 ကြီးလာခြင်းနှင့် ရွေ့လျားခြင်း နှစ်ခုလုံးအတွက် Interpolator တစ်ခုတည်းကိုသာ တွဲသုံးပါမည် 🌟
            Interpolator smoothOvershoot = new Interpolator() {
                @Override
                protected double curve(double t) {
                    double tension = 1.1; // သိပ်မခုန်လွန်းဘဲ Smooth ဖြစ်အောင် Tension လျှော့ထားပါသည်
                    t -= 1.0;
                    return t * t * ((tension + 1) * t + tension) + 1.0;
                }
            };

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.setInterpolator(smoothOvershoot);

            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(400), modalView);
            moveCenter.setToX(0);
            moveCenter.setToY(0);
            moveCenter.setInterpolator(smoothOvershoot);

            // Card လေးကို ဖြည်းဖြည်းချင်း လင်းလာစေမည့် Fade Effect ထပ်ပေါင်းထည့်ထားပါသည်
            FadeTransition fadeModal = new FadeTransition(Duration.millis(300), modalView);
            fadeModal.setToValue(1.0);

            FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), overlay);
            fadeOverlay.setFromValue(0.0);
            fadeOverlay.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, moveCenter, fadeModal, fadeOverlay);
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeProfileModal(StackPane overlay, Node mainContent, double endX, double endY) {
        Node modalView = overlay.getChildren().get(0);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), modalView);
        scaleDown.setToX(0.05);
        scaleDown.setToY(0.05);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition moveBack = new TranslateTransition(Duration.millis(250), modalView);
        moveBack.setToX(endX);
        moveBack.setToY(endY);
        moveBack.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOutModal = new FadeTransition(Duration.millis(200), modalView);
        fadeOutModal.setToValue(0.0);

        FadeTransition fadeOutOverlay = new FadeTransition(Duration.millis(250), overlay);
        fadeOutOverlay.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, moveBack, fadeOutModal, fadeOutOverlay);
        pt.setOnFinished(e -> {
            rightSideStack.getChildren().remove(overlay);
            mainContent.setEffect(null);
        });
        pt.play();
    }

    // ==========================================
    // THEME TOGGLE, NAV BUTTONS & LOGOUT
    // ==========================================

    @FXML
    public void toggleTheme(MouseEvent event) {
        isDarkMode = !isDarkMode;

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), themeThumb);
        tt.setToX(isDarkMode ? 0 : 24);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();

        String thumbBaseStyle = "-fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;";

        if (isDarkMode) {
            themeIcon.setContent(MOON_SVG);
            themeThumb.setStyle("-fx-background-color: #00D4FF; " + thumbBaseStyle);
        } else {
            themeIcon.setContent(SUN_SVG);
            themeThumb.setStyle("-fx-background-color: #F59E0B; " + thumbBaseStyle);
        }

        updateTheme();
    }

    private void updateTheme() {
        updateStylesheets(rootPane.getStylesheets());

        if (mainContentArea != null && !mainContentArea.getChildren().isEmpty()) {
            Node activePage = mainContentArea.getChildren().get(0);
            if (activePage instanceof Parent) {
                updateStylesheets(((Parent) activePage).getStylesheets());
            }
        }
        Scene scene = rootPane.getScene();
        if (scene != null) {
            updateStylesheets(scene.getStylesheets());
        }
    }

    private void updateStylesheets(ObservableList<String> stylesheets) {
        for (int i = 0; i < stylesheets.size(); i++) {
            String cssPath = stylesheets.get(i);
            if (isDarkMode && cssPath.contains("Light")) {
                stylesheets.set(i, cssPath.replace("Light", "Dark"));
            } else if (!isDarkMode && cssPath.contains("Dark")) {
                stylesheets.set(i, cssPath.replace("Dark", "Light"));
            }
        }
    }

    private void setActiveButton(Button clickedButton) {
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("super-nav-button-active");
            }
        }
        if (clickedButton != null && !clickedButton.getStyleClass().contains("super-nav-button-active")) {
            clickedButton.getStyleClass().add("super-nav-button-active");
        }
    }

    private void loadPage(String fxmlFileName) {
        try {
            String path = "/com/pos/flightmanagementsystem/view/" + fxmlFileName;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent newView = loader.load();

            updateStylesheets(newView.getStylesheets());
            newView.setTranslateX(800);

            if (mainContentArea != null && !mainContentArea.getChildren().isEmpty()) {
                Node currentView = mainContentArea.getChildren().get(0);

                TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), currentView);
                slideOut.setToX(800);
                slideOut.setInterpolator(Interpolator.EASE_IN);

                slideOut.setOnFinished(e -> {
                    mainContentArea.getChildren().clear();
                    mainContentArea.getChildren().add(newView);

                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), newView);
                    slideIn.setToX(0);
                    slideIn.setInterpolator(Interpolator.EASE_OUT);
                    slideIn.play();
                });
                slideOut.play();
            } else {
                if (mainContentArea != null) {
                    mainContentArea.getChildren().add(newView);
                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), newView);
                    slideIn.setToX(0);
                    slideIn.setInterpolator(Interpolator.EASE_OUT);
                    slideIn.play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void clickDashboard(ActionEvent event) { setActiveButton(btnDashboard); loadPage("SuperAdminDashboard.fxml"); }
    @FXML void clickFlightSchedule(ActionEvent event) { setActiveButton(btnFlightSchedule); loadPage("AdminFlightSchedule.fxml"); }
    @FXML void clickViewCity(ActionEvent event) { setActiveButton(btnViewCity); loadPage("AdminCity.fxml"); }
    @FXML void clickBookingManagement(ActionEvent event) { setActiveButton(btnBookingManagement); loadPage("AdminBookingManagement.fxml"); }
    @FXML void clickViewSaleDetails(ActionEvent event) { setActiveButton(btnViewSaleDetails); loadPage("AdminReport.fxml"); }

    @FXML
    void clickLogout(ActionEvent event) {
        if (mainContentArea != null && !mainContentArea.getChildren().isEmpty()) {
            Node centerNode = mainContentArea.getChildren().get(0);
            TranslateTransition slideOutRight = new TranslateTransition(Duration.millis(600), centerNode);
            slideOutRight.setToX(800);
            slideOutRight.setInterpolator(Interpolator.EASE_IN);

            slideOutRight.setOnFinished(e -> performLogout(event));
            slideOutRight.play();
        } else {
            performLogout(event);
        }
    }

    private void performLogout(ActionEvent event) {
        try {
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
            Parent root = loader.load();

            currentScene.setRoot(root);
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}