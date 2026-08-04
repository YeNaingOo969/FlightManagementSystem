package com.pos.flightmanagementsystem.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private StackPane rightSideStack;
    @FXML private StackPane mainContentArea;
    @FXML private StackPane profileOverlayBg;
    @FXML private StackPane profilePopupContainer;

    @FXML private VBox sidebar;
    @FXML private HBox topNavBar;

    @FXML private Button btnDashboard, btnProfile, btnFlightSchedule, btnViewCity, btnBookingManagement, btnViewSaleDetails, btnLogout;

    @FXML private Label topNavUsername;
    @FXML private Label topNavRole;
    @FXML private Circle topNavAvatarCircle;
    @FXML private HBox navProfileBox;

    @FXML private StackPane themeToggleBg;
    @FXML private StackPane themeThumb;
    @FXML private SVGPath themeIcon;

    // 🌟 Notification UI Variables 🌟
    @FXML private Label notificationBadge;
    @FXML private StackPane navNotificationBtn;
    @FXML private StackPane notificationOverlayBg;
    @FXML private VBox notificationBubble;
    @FXML private VBox notificationListContainer;

    private boolean isDarkMode = true;
    private boolean isProfileOpen = false;
    private boolean isNotificationOpen = false;

    private String loggedInUsername = "";
    private String loggedInRole = "";
    private String loggedInImagePath = "";

    // 🌟 Noti တစ်ခါကြည့်ပြီးသည်နှင့် Badge ဖျောက်ရန်အတွက် နောက်ဆုံးကြည့်ခဲ့သောအချိန်ကို မှတ်ထားမည့် Variable 🌟
    private LocalDateTime lastViewedTime = null;

    private final String MOON_SVG = "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z";
    private final String SUN_SVG = "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5s5-2.24,5-5S14.76,7,12,7z M2,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0 c-0.55,0-1,0.45-1,1S1.45,13,2,13z M20,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0c-0.55,0-1,0.45-1,1S19.45,13,20,13z M11,2v2 c0,0.55,0.45,1,1,1s1-0.45,1-1V2c0-0.55-0.45-1-1-1S11,1.45,11,2z M11,20v2c0,0.55,0.45,1,1,1s1-0.45,1-1v-2c0-0.55-0.45-1-1-1 C11.45,19,11,19.45,11,20z M5.99,4.58c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41l1.06,1.06 c0.39,0.39,1.03,0.39,1.41,0s0.39-1.03,0-1.41L5.99,4.58z M18.36,16.95c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41 l1.06,1.06c0.39,0.39,1.03,0.39,1.41,0c0.39-0.39,0.39-1.03,0-1.41L18.36,16.95z M19.42,5.99c0.39-0.39,0.39-1.03,0-1.41 c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L19.42,5.99z M7.05,18.36 c0.39-0.39,0.39-1.03,0-1.41c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L7.05,18.36z";

    private Button[] navButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navButtons = new Button[]{btnDashboard, btnProfile, btnFlightSchedule, btnViewCity, btnBookingManagement, btnViewSaleDetails};
        setActiveButton(btnDashboard);
        loadPage("SuperAdminDashboard.fxml", false);

        // 🌟 App စဖွင့်ချိန် Notification များကို စစ်ဆေးပါမည်
        fetchNotifications();
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
                    topNavAvatarCircle.setFill(new ImagePattern(new Image(imgFile.toURI().toString())));
                } else {
                    URL url = getClass().getResource("/com/pos/flightmanagementsystem/image/" + imagePath);
                    if (url != null) topNavAvatarCircle.setFill(new ImagePattern(new Image(url.toExternalForm())));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ==========================================
    // 🌟 NOTIFICATION SYSTEM 🌟
    // ==========================================
    private void fetchNotifications() {
        notificationListContainer.getChildren().clear();
        int unreadCount = 0;
        int totalCount = 0;

        // 🌟 ၂၄ နာရီအတွင်းက Data များ နှင့် Staff Name ကိုပါ ဆွဲထုတ်မည် 🌟
        String query = "SELECT r.request_id, r.booking_id, r.reason, r.created_at, b.pnr_code, p.full_name, " +
                "(SELECT username FROM users WHERE user_id = r.staff_id) AS staff_name " +
                "FROM requests r " +
                "JOIN bookings b ON r.booking_id = b.booking_id " +
                "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                "WHERE r.status = 'Pending' " +
                "AND r.created_at >= NOW() - INTERVAL 1 DAY " +
                "ORDER BY r.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                totalCount++;
                int requestId = rs.getInt("request_id");
                int bookingId = rs.getInt("booking_id");
                String pnrCode = rs.getString("pnr_code");
                String passengerName = rs.getString("full_name");
                String reason = rs.getString("reason");
                String staffName = rs.getString("staff_name");
                java.sql.Timestamp createdAt = rs.getTimestamp("created_at");

                if (staffName == null) staffName = "Unknown Staff";

                // 🌟 နောက်ဆုံးကြည့်ခဲ့သည့်အချိန်ထက် နောက်ကျမှဝင်လာသော Noti များကိုသာ Badge တွင် ပြမည် 🌟
                if (lastViewedTime == null || (createdAt != null && createdAt.toLocalDateTime().isAfter(lastViewedTime))) {
                    unreadCount++;
                }

                VBox requestCard = createNotificationCard(requestId, bookingId, pnrCode, passengerName, reason, staffName);
                notificationListContainer.getChildren().add(requestCard);
            }

            // Update Badge Count
            if (unreadCount > 0) {
                notificationBadge.setText(String.valueOf(unreadCount));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }

            if (totalCount == 0) {
                Label emptyLabel = new Label("No new notifications in the last 24 hours.");
                emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-padding: 20; -fx-font-style: italic;");
                notificationListContainer.getChildren().add(emptyLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🌟 Staff Name ကိုပါ လက်ခံနိုင်ရန် parameter ထပ်ထည့်ထားပါသည် 🌟
    private VBox createNotificationCard(int requestId, int bookingId, String pnrCode, String passengerName, String reason, String staffName) {
        VBox card = new VBox(8);
        card.getStyleClass().add("noti-request-card");

        Label lblTitle = new Label("Cancellation Request: " + pnrCode);
        lblTitle.getStyleClass().add("noti-request-title");

        // 🌟 Passenger Name နှင့် ၎င်းကို Request လုပ်သော Staff Name ပါ တွဲပြမည် 🌟
        Label lblPassenger = new Label("Passenger: " + passengerName + "   •   Staff: " + staffName);
        lblPassenger.getStyleClass().add("noti-request-subtitle");

        Label lblReason = new Label("Reason: " + reason);
        lblReason.getStyleClass().add("noti-request-reason");
        lblReason.setWrapText(true);

        HBox actionBox = new HBox(10);
        Button btnApprove = new Button("Approve");
        btnApprove.getStyleClass().add("noti-btn-approve");
        btnApprove.setOnAction(e -> handleRequestAction(requestId, bookingId, true));

        Button btnReject = new Button("Cancel");
        btnReject.getStyleClass().add("noti-btn-reject");
        btnReject.setOnAction(e -> handleRequestAction(requestId, bookingId, false));

        actionBox.getChildren().addAll(btnApprove, btnReject);
        card.getChildren().addAll(lblTitle, lblPassenger, lblReason, actionBox);

        return card;
    }

    private void handleRequestAction(int requestId, int bookingId, boolean isApprove) {
        int adminId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;

        String reqStatus = isApprove ? "Approved" : "Cancelled";
        String bookingStatus = isApprove ? "Cancelled" : "Confirmed"; // ပြန်ပယ်ချလျှင် Confirmed/Completed အဖြစ် ပြန်ထားမည်

        String updateReq = "UPDATE requests SET status = ?, admin_id = ? WHERE request_id = ?";
        String updateBooking = "UPDATE bookings SET status = ? WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement reqStmt = conn.prepareStatement(updateReq);
                 PreparedStatement bookStmt = conn.prepareStatement(updateBooking)) {

                reqStmt.setString(1, reqStatus);
                reqStmt.setInt(2, adminId);
                reqStmt.setInt(3, requestId);
                reqStmt.executeUpdate();

                bookStmt.setString(1, bookingStatus);
                bookStmt.setInt(2, bookingId);
                bookStmt.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            // Database ပြင်ပြီးပါက Notifications များကို Refresh လုပ်ပါမည်
            fetchNotifications();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openNotificationPopup(MouseEvent event) {
        if (isNotificationOpen) return;

        // 🌟 Noti ကို ဖွင့်ကြည့်လိုက်ပြီ ဖြစ်သောကြောင့် လက်ရှိအချိန်ကို မှတ်ထားပြီး Badge ဖျောက်ပါမည် 🌟
        lastViewedTime = LocalDateTime.now();
        notificationBadge.setVisible(false);

        fetchNotifications(); // ဖွင့်လိုက်တိုင်း အသစ်ပြန်ဆွဲထုတ်မည်
        notificationOverlayBg.setVisible(true);
        isNotificationOpen = true;

        Platform.runLater(() -> {
            Bounds bellBounds = navNotificationBtn.localToScene(navNotificationBtn.getBoundsInLocal());
            Bounds rootBounds = rightSideStack.localToScene(rightSideStack.getBoundsInLocal());

            // Bell ရဲ့ Center မှ Screen ညာဘက်အစွန်းထိ အကွာအဝေးကို တွက်မည်
            double distanceFromRight = rootBounds.getMaxX() - (bellBounds.getMinX() + bellBounds.getWidth() / 2);

            // မြှားအလယ်မှတ်သည် Bubble ၏ ညာဘက်မှ 52px အကွာတွင်ရှိသည် (padding 40 + arrow half width 12 = 52)
            double translateX = -(distanceFromRight - 52);
            notificationBubble.setTranslateX(translateX);
        });

        notificationBubble.setScaleX(0.05);
        notificationBubble.setScaleY(0.05);
        notificationBubble.setOpacity(0.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), notificationBubble);
        scale.setToX(1.0); scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(250), notificationBubble);
        fade.setToValue(1.0);

        ParallelTransition pt = new ParallelTransition(scale, fade);
        pt.play();
    }

    @FXML
    void closeNotificationPopup(MouseEvent event) {
        if (event.getTarget() == notificationOverlayBg) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), notificationBubble);
            scale.setToX(0.05); scale.setToY(0.05);
            scale.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fade = new FadeTransition(Duration.millis(200), notificationBubble);
            fade.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(scale, fade);
            pt.setOnFinished(e -> {
                notificationOverlayBg.setVisible(false);
                isNotificationOpen = false;
            });
            pt.play();
        }
    }

    // ==========================================
    // 🌟 TOP NAV PROFILE POPUP 🌟
    // ==========================================
    @FXML
    void openProfilePopup(MouseEvent event) {
        if (isProfileOpen) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Profile.fxml"));
            Parent profileRoot = loader.load();

            ProfileController controller = loader.getController();
            controller.setUserData(loggedInUsername, loggedInRole, loggedInImagePath);

            updateStylesheets(profileRoot.getStylesheets());
            profileRoot.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0.2, 0, 10); -fx-background-radius: 12px;");

            profilePopupContainer.getChildren().setAll(profileRoot);
            profileOverlayBg.setVisible(true);
            isProfileOpen = true;

            Bounds sourceBounds = navProfileBox.localToScene(navProfileBox.getBoundsInLocal());
            Bounds rootBounds = rightSideStack.localToScene(rightSideStack.getBoundsInLocal());
            double startX = (sourceBounds.getMinX() + (sourceBounds.getWidth() / 2)) - (rootBounds.getMinX() + (rootBounds.getWidth() / 2));
            double startY = (sourceBounds.getMinY() + (sourceBounds.getHeight() / 2)) - (rootBounds.getMinY() + (rootBounds.getHeight() / 2));

            profilePopupContainer.setTranslateX(startX);
            profilePopupContainer.setTranslateY(startY);
            profilePopupContainer.setScaleX(0.05);
            profilePopupContainer.setScaleY(0.05);
            profilePopupContainer.setOpacity(0.0);

            controller.setCloseCallback(() -> closeProfileModal(startX, startY));

            Node mainContent = rightSideStack.getChildren().get(0);
            mainContent.setEffect(new BoxBlur(12, 12, 3));

            Interpolator smoothOvershoot = new Interpolator() {
                @Override
                protected double curve(double t) {
                    double tension = 1.1;
                    t -= 1.0;
                    return t * t * ((tension + 1) * t + tension) + 1.0;
                }
            };

            ScaleTransition scale = new ScaleTransition(Duration.millis(400), profilePopupContainer);
            scale.setToX(1.0); scale.setToY(1.0); scale.setInterpolator(smoothOvershoot);
            TranslateTransition translate = new TranslateTransition(Duration.millis(400), profilePopupContainer);
            translate.setToX(0); translate.setToY(0); translate.setInterpolator(smoothOvershoot);
            FadeTransition fade = new FadeTransition(Duration.millis(300), profilePopupContainer); fade.setToValue(1.0);
            FadeTransition bgFade = new FadeTransition(Duration.millis(300), profileOverlayBg); bgFade.setFromValue(0.0); bgFade.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(translate, scale, fade, bgFade);
            pt.play();

        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void closeProfilePopup(MouseEvent event) {
        if (event.getTarget() == profileOverlayBg) {
            Bounds sourceBounds = navProfileBox.localToScene(navProfileBox.getBoundsInLocal());
            Bounds rootBounds = rightSideStack.localToScene(rightSideStack.getBoundsInLocal());
            double startX = (sourceBounds.getMinX() + (sourceBounds.getWidth() / 2)) - (rootBounds.getMinX() + (rootBounds.getWidth() / 2));
            double startY = (sourceBounds.getMinY() + (sourceBounds.getHeight() / 2)) - (rootBounds.getMinY() + (rootBounds.getHeight() / 2));
            closeProfileModal(startX, startY);
        }
    }

    private void closeProfileModal(double endX, double endY) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(300), profilePopupContainer);
        translate.setToX(endX); translate.setToY(endY); translate.setInterpolator(Interpolator.EASE_IN);
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), profilePopupContainer);
        scale.setToX(0.05); scale.setToY(0.05); scale.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fade = new FadeTransition(Duration.millis(300), profilePopupContainer); fade.setToValue(0.0);
        FadeTransition bgFade = new FadeTransition(Duration.millis(300), profileOverlayBg); bgFade.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(translate, scale, fade, bgFade);
        pt.setOnFinished(e -> {
            profileOverlayBg.setVisible(false);
            profilePopupContainer.getChildren().clear();
            isProfileOpen = false;
            rightSideStack.getChildren().get(0).setEffect(null);
        });
        pt.play();
    }

    // ==========================================
    // 🌟 REMAINDER OF YOUR CODE (Theme, Nav)
    // ==========================================
    @FXML
    public void toggleTheme(MouseEvent event) {
        isDarkMode = !isDarkMode;
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), themeThumb);
        tt.setToX(isDarkMode ? 0 : 24);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();

        if (isDarkMode) {
            themeIcon.setContent(MOON_SVG);
            themeThumb.setStyle("-fx-background-color: #00D4FF; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;");
        } else {
            themeIcon.setContent(SUN_SVG);
            themeThumb.setStyle("-fx-background-color: #F59E0B; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-background-radius: 50%; -fx-alignment: center;");
        }
        updateTheme();
    }

    private void updateTheme() {
        updateStylesheets(rootPane.getStylesheets());
        if (mainContentArea != null && !mainContentArea.getChildren().isEmpty()) {
            Node rightPage = mainContentArea.getChildren().get(0);
            if (rightPage instanceof Parent) updateStylesheets(((Parent) rightPage).getStylesheets());
        }
        if (profilePopupContainer != null && !profilePopupContainer.getChildren().isEmpty()) {
            Node profilePage = profilePopupContainer.getChildren().get(0);
            if (profilePage instanceof Parent) updateStylesheets(((Parent) profilePage).getStylesheets());
        }
        Scene scene = rootPane.getScene();
        if (scene != null) updateStylesheets(scene.getStylesheets());
    }

    private void updateStylesheets(ObservableList<String> stylesheets) {
        for (int i = 0; i < stylesheets.size(); i++) {
            String cssPath = stylesheets.get(i);
            if (isDarkMode && cssPath.contains("Light")) stylesheets.set(i, cssPath.replace("Light", "Dark"));
            else if (!isDarkMode && cssPath.contains("Dark")) stylesheets.set(i, cssPath.replace("Dark", "Light"));
        }
    }

    private void setActiveButton(Button clickedButton) {
        for (Button btn : navButtons) {
            if (btn != null) btn.getStyleClass().remove("super-nav-button-active");
        }
        if (clickedButton != null) clickedButton.getStyleClass().add("super-nav-button-active");
    }

    private void loadPage(String fxmlFileName, boolean animate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/" + fxmlFileName));
            Parent newView = loader.load();
            updateStylesheets(newView.getStylesheets());

            if (animate) {
                newView.setTranslateX(800);
                Node currentView = mainContentArea.getChildren().isEmpty() ? null : mainContentArea.getChildren().get(0);
                if (currentView != null) {
                    TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), currentView);
                    slideOut.setToX(800); slideOut.setInterpolator(Interpolator.EASE_IN);
                    slideOut.setOnFinished(e -> {
                        mainContentArea.getChildren().clear();
                        mainContentArea.getChildren().add(newView);
                        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), newView);
                        slideIn.setToX(0); slideIn.setInterpolator(Interpolator.EASE_OUT); slideIn.play();
                    });
                    slideOut.play();
                } else {
                    mainContentArea.getChildren().add(newView);
                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), newView);
                    slideIn.setToX(0); slideIn.setInterpolator(Interpolator.EASE_OUT); slideIn.play();
                }
            } else {
                newView.setTranslateX(0);
                mainContentArea.getChildren().clear();
                mainContentArea.getChildren().add(newView);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void clickDashboard(ActionEvent event) { setActiveButton(btnDashboard); loadPage("SuperAdminDashboard.fxml", true); }
    @FXML void clickProfile(ActionEvent event) { setActiveButton(btnProfile); loadPage("StaffProfile.fxml", true); }
    @FXML void clickFlightSchedule(ActionEvent event) { setActiveButton(btnFlightSchedule); loadPage("AdminFlightSchedule.fxml", true); }
    @FXML void clickViewCity(ActionEvent event) { setActiveButton(btnViewCity); loadPage("AdminCity.fxml", true); }
    @FXML void clickBookingManagement(ActionEvent event) { setActiveButton(btnBookingManagement); loadPage("AdminBookingManagement.fxml", true); }
    @FXML void clickViewSaleDetails(ActionEvent event) { setActiveButton(btnViewSaleDetails); loadPage("AdminReport.fxml", true); }

    public void prepareIntroAnimation() {
        if (sidebar != null) { sidebar.setTranslateX(-300); sidebar.setOpacity(0); }
        if (topNavBar != null) { topNavBar.setTranslateY(-150); topNavBar.setOpacity(0); }
        if (mainContentArea != null) { mainContentArea.setTranslateX(800); mainContentArea.setOpacity(0); }
    }

    public void playIntroAnimation() {
        ParallelTransition dashAnimIn = new ParallelTransition();
        if (sidebar != null) {
            TranslateTransition st = new TranslateTransition(Duration.millis(700), sidebar); st.setToX(0); st.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition ft = new FadeTransition(Duration.millis(700), sidebar); ft.setToValue(1);
            dashAnimIn.getChildren().addAll(st, ft);
        }
        if (topNavBar != null) {
            TranslateTransition st = new TranslateTransition(Duration.millis(700), topNavBar); st.setToY(0); st.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition ft = new FadeTransition(Duration.millis(700), topNavBar); ft.setToValue(1);
            dashAnimIn.getChildren().addAll(st, ft);
        }
        if (mainContentArea != null) {
            TranslateTransition st = new TranslateTransition(Duration.millis(700), mainContentArea); st.setToX(0); st.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition ft = new FadeTransition(Duration.millis(700), mainContentArea); ft.setToValue(1);
            dashAnimIn.getChildren().addAll(st, ft);
        }
        dashAnimIn.play();
    }

    @FXML
    void clickLogout(ActionEvent event) {
        if (LoginController.loggedInUser != null) {
            insertLog(LoginController.loggedInUser.getUserId(), "Log out");
        }

        try {
            LoginController.isFromLogout = true;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
            Parent loginRoot = loader.load();
            LoginController loginCtrl = loader.getController();

            Scene scene = btnLogout.getScene();
            Parent currentDashboardRoot = scene.getRoot();

            StackPane transitionWrapper = new StackPane();
            transitionWrapper.setStyle("-fx-background-color: #06080D;");
            transitionWrapper.getChildren().addAll(loginRoot, currentDashboardRoot);

            scene.setRoot(transitionWrapper);

            ParallelTransition dashOut = new ParallelTransition();
            if (sidebar != null) {
                TranslateTransition sOut = new TranslateTransition(Duration.millis(700), sidebar); sOut.setToX(-300); sOut.setInterpolator(Interpolator.EASE_BOTH);
                FadeTransition fOut = new FadeTransition(Duration.millis(700), sidebar); fOut.setToValue(0);
                dashOut.getChildren().addAll(sOut, fOut);
            }
            if (topNavBar != null) {
                TranslateTransition tOut = new TranslateTransition(Duration.millis(700), topNavBar); tOut.setToY(-150); tOut.setInterpolator(Interpolator.EASE_BOTH);
                FadeTransition fOut = new FadeTransition(Duration.millis(700), topNavBar); fOut.setToValue(0);
                dashOut.getChildren().addAll(tOut, fOut);
            }
            if (mainContentArea != null) {
                TranslateTransition mOut = new TranslateTransition(Duration.millis(700), mainContentArea); mOut.setToX(800); mOut.setInterpolator(Interpolator.EASE_BOTH);
                FadeTransition fOut = new FadeTransition(Duration.millis(700), mainContentArea); fOut.setToValue(0);
                dashOut.getChildren().addAll(mOut, fOut);
            }

            dashOut.setOnFinished(e -> {
                transitionWrapper.getChildren().remove(loginRoot);
                scene.setRoot(loginRoot);
                if (loginCtrl != null) {
                    loginCtrl.playLoginInAnimation();
                }
            });
            dashOut.play();

            LoginController.loggedInUser = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}