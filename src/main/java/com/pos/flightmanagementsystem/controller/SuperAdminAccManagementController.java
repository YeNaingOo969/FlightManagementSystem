package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class SuperAdminAccManagementController implements Initializable {

    @FXML private AnchorPane acRootPane;
    @FXML private Button btnCreateUser;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboFilterRole;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblStaffMembers;
    @FXML private Label lblAdministrators;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colPassword;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colImagePath;

    @FXML private HBox profileCardsContainer;

    @FXML private HBox notificationToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastMessage;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (colUsername != null) {
                colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
                colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
                colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
                colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
                colImagePath.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
            }

            if (comboFilterRole != null) {
                comboFilterRole.getItems().addAll("All", "Admin", "Staff");
                comboFilterRole.setValue("All");
                comboFilterRole.valueProperty().addListener((observable, oldValue, newValue) -> filterUsers());
            }

            if (txtSearch != null) {
                txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterUsers());
            }

            loadUserData("", "All");
            updateDashboardStats();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDashboardStats() {
        String query = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
        int total = 0, adminCount = 0, staffCount = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");
                total += count;
                if (role != null) {
                    if (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("superadmin")) {
                        adminCount += count;
                    } else if (role.equalsIgnoreCase("staff")) {
                        staffCount += count;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lblTotalUsers != null) lblTotalUsers.setText(total + (total == 1 ? " person" : " persons"));
        if (lblActiveUsers != null) lblActiveUsers.setText(total + (total == 1 ? " person" : " persons"));
        if (lblStaffMembers != null) lblStaffMembers.setText(staffCount + (staffCount == 1 ? " person" : " persons"));
        if (lblAdministrators != null) lblAdministrators.setText(adminCount + (adminCount == 1 ? " person" : " persons"));
    }

    private void filterUsers() {
        String keyword = txtSearch != null ? txtSearch.getText() : "";
        String selectedRole = comboFilterRole != null ? comboFilterRole.getValue() : "All";
        loadUserData(keyword, selectedRole);
    }

    public void loadUserData(String keyword, String roleFilter) {
        userList.clear();
        StringBuilder query = new StringBuilder("SELECT user_id, username, email, password, role, status, image_path, created_at FROM users WHERE 1=1");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasRole = roleFilter != null && !roleFilter.equals("All") && !roleFilter.trim().isEmpty();

        if (hasKeyword) query.append(" AND LOWER(username) LIKE LOWER(?)");
        if (hasRole) query.append(" AND LOWER(role) = LOWER(?)");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (hasKeyword) pstmt.setString(paramIndex++, keyword.trim() + "%");
            if (hasRole) pstmt.setString(paramIndex++, roleFilter.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String password = rs.getString("password");
                    String role = rs.getString("role");
                    String status = rs.getString("status");
                    String imagePath = rs.getString("image_path");
                    java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

                    userList.add(new User(userId, username, email, password, role, status, imagePath, createdAt));
                }
            }

            if (usersTable != null) usersTable.setItems(userList);
            if (profileCardsContainer != null) {
                profileCardsContainer.getChildren().clear();
                for (User user : userList) {
                    profileCardsContainer.getChildren().add(createUserCard(user));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🌟 Profile ပုံနှင့် ဘောင်များကို အဝိုင်းတိတိကျကျဖြစ်အောင် ပြင်ဆင်ထားသော Method 🌟
    private VBox createUserCard(User user) {
        VBox card = new VBox();
        card.getStyleClass().add("user-profile-card");

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("user-card-avatar-container"); // CSS ဖိုင်မှ Class အသစ်ကို ချိတ်ဆက်ထားပါသည်
        VBox.setMargin(iconPane, new Insets(0, 0, 15.0, 0));

        boolean hasImage = false;
        if (user.getImagePath() != null && !user.getImagePath().trim().isEmpty()) {
            File imgFile = new File("src/main/resources/com/pos/flightmanagementsystem/image/" + user.getImagePath());
            if (imgFile.exists()) {
                ImageView imageView = new ImageView(new Image(imgFile.toURI().toString()));

                // ပုံအရွယ်အစားကို အတိအကျသတ်မှတ်ခြင်း
                imageView.setFitWidth(80.0);
                imageView.setFitHeight(80.0);

                // အဝိုင်းတိတိကျကျ ဖြတ်ထုတ်ခြင်း
                Circle clip = new Circle(40, 40, 40);
                imageView.setClip(clip);

                iconPane.getChildren().add(imageView);
                hasImage = true;
            }
        }

        if (!hasImage) {
            SVGPath defaultIcon = new SVGPath();
            defaultIcon.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
            defaultIcon.setStyle("-fx-fill: #31C4B4;");
            defaultIcon.setScaleX(1.8);
            defaultIcon.setScaleY(1.8);
            iconPane.getChildren().add(defaultIcon);
        }

        Label nameLbl = new Label(user.getUsername() != null ? user.getUsername() : "Unknown");
        nameLbl.getStyleClass().add("profile-label");
        nameLbl.setPadding(new Insets(0, 0, 12.0, 0));

        String roleStr = user.getRole() != null ? user.getRole().toUpperCase() : "STAFF";
        Label roleLbl = new Label(roleStr);
        roleLbl.getStyleClass().add("profile-label");
        roleLbl.setPadding(new Insets(0, 0, 12.0, 0));

        Label statusLbl = new Label("ACTIVE");
        statusLbl.getStyleClass().add("status-active");
        statusLbl.setPadding(new Insets(0, 0, 12.0, 0));

        card.getChildren().addAll(iconPane, nameLbl, roleLbl, statusLbl);
        return card;
    }

    @FXML
    void openCreateUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/SuperAdminCreateUser.fxml"));
            Parent createUserRoot = loader.load();

            // =========================================================================
            // 🌟 Theme စစ်ဆေးပြီး သက်ဆိုင်ရာ Popup CSS ကို ဒိုင်းနမစ် (Dynamic) ချိတ်ဆက်ပေးခြင်း 🌟
            // =========================================================================
            if (!acRootPane.getStylesheets().isEmpty()) {
                String currentMainStyle = acRootPane.getStylesheets().get(0);
                createUserRoot.getStylesheets().clear(); // FXML ထဲမှာ ကပ်ပါလာတဲ့ CSS အဟောင်းတွေကို အရင်ရှင်းထုတ်မည်

                if (currentMainStyle.contains("Light")) {
                    // Main Page က Light Mode ဖြစ်နေရင် Light CSS ကို ထည့်မည်
                    createUserRoot.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/SuperAdminCreateUserLight.css").toExternalForm());
                } else {
                    // Main Page က Dark Mode ဖြစ်နေရင် Dark CSS ကို ထည့်မည်
                    createUserRoot.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/SuperAdminCreateUserDark.css").toExternalForm());
                }
            }
            // =========================================================================

            SuperAdminCreateUserController createUserController = loader.getController();

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
            overlay.getChildren().add(createUserRoot);

            acRootPane.getChildren().add(overlay);
            AnchorPane.setTopAnchor(overlay, 0.0); AnchorPane.setBottomAnchor(overlay, 0.0);
            AnchorPane.setLeftAnchor(overlay, 0.0); AnchorPane.setRightAnchor(overlay, 0.0);

            Bounds btnBounds = btnCreateUser.localToScene(btnCreateUser.getBoundsInLocal());
            Bounds rootBounds = acRootPane.localToScene(acRootPane.getBoundsInLocal());

            double rootCenterX = rootBounds.getWidth() / 2;
            double rootCenterY = rootBounds.getHeight() / 2;
            double btnCenterX = btnBounds.getMinX() + (btnBounds.getWidth() / 2) - rootBounds.getMinX();
            double btnCenterY = btnBounds.getMinY() + (btnBounds.getHeight() / 2) - rootBounds.getMinY();

            double startTransX = btnCenterX - rootCenterX;
            double startTransY = btnCenterY - rootCenterY;

            createUserController.setSuccessAction(() -> {
                closePopup(overlay, createUserRoot, startTransX, startTransY);
                filterUsers();
                updateDashboardStats();
                showToast("Success!", "User account created successfully.");
            });

            createUserController.setCancelAnimationAction(() -> {
                closePopup(overlay, createUserRoot, startTransX, startTransY);
            });

            createUserRoot.setTranslateX(startTransX);
            createUserRoot.setTranslateY(startTransY);
            createUserRoot.setScaleX(0.0);
            createUserRoot.setScaleY(0.0);
            createUserRoot.setOpacity(0.0);
            overlay.setOpacity(0.0);

            FadeTransition bgFade = new FadeTransition(Duration.millis(300), overlay); bgFade.setToValue(1.0);
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), createUserRoot); scaleUp.setToX(1.0); scaleUp.setToY(1.0); scaleUp.setInterpolator(Interpolator.EASE_OUT);
            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(400), createUserRoot); moveCenter.setToX(0); moveCenter.setToY(0); moveCenter.setInterpolator(Interpolator.EASE_OUT);
            FadeTransition formFade = new FadeTransition(Duration.millis(250), createUserRoot); formFade.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(bgFade, scaleUp, moveCenter, formFade);
            pt.play();

            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) closePopup(overlay, createUserRoot, startTransX, startTransY);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closePopup(StackPane overlay, Parent createUserRoot, double endTransX, double endTransY) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), createUserRoot); scaleDown.setToX(0.0); scaleDown.setToY(0.0); scaleDown.setInterpolator(Interpolator.EASE_IN);
        TranslateTransition moveBack = new TranslateTransition(Duration.millis(300), createUserRoot); moveBack.setToX(endTransX); moveBack.setToY(endTransY); moveBack.setInterpolator(Interpolator.EASE_IN);
        FadeTransition formFadeOut = new FadeTransition(Duration.millis(200), createUserRoot); formFadeOut.setToValue(0.0);
        FadeTransition bgFadeOut = new FadeTransition(Duration.millis(300), overlay); bgFadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, moveBack, formFadeOut, bgFadeOut);
        pt.setOnFinished(event -> acRootPane.getChildren().remove(overlay));
        pt.play();
    }

    private void showToast(String title, String message) {
        lblToastTitle.setText(title);
        lblToastMessage.setText(message);
        notificationToast.setVisible(true);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), notificationToast);
        slideUp.setFromY(100);
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationToast);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition ptIn = new ParallelTransition(slideUp, fadeIn);
        ptIn.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> closeToast(null));
        delay.play();
    }

    @FXML
    void closeToast(ActionEvent event) {
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), notificationToast);
        slideDown.setToY(100);
        slideDown.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notificationToast);
        fadeOut.setToValue(0.0);

        ParallelTransition ptOut = new ParallelTransition(slideDown, fadeOut);
        ptOut.setOnFinished(e -> notificationToast.setVisible(false));
        ptOut.play();
    }
}