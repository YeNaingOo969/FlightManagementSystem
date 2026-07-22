package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
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
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class SuperAdminAccManagementController implements Initializable {

    @FXML
    private AnchorPane acRootPane;

    @FXML
    private Button btnCreateUser;

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> comboFilterRole;

    // 🎯 Statistics Labels
    @FXML
    private Label lblTotalUsers;
    @FXML
    private Label lblActiveUsers;
    @FXML
    private Label lblStaffMembers;
    @FXML
    private Label lblAdministrators;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> colUsername;

    @FXML
    private TableColumn<User, String> colPassword;

    @FXML
    private TableColumn<User, String> colRole;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, String> colImagePath;

    @FXML
    private HBox profileCardsContainer;

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

                comboFilterRole.valueProperty().addListener((observable, oldValue, newValue) -> {
                    filterUsers();
                });
            }

            if (txtSearch != null) {
                txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterUsers();
                });
            }

            // 🎯 အစပိုင်းတွင် Data အားလုံးပြရန်နှင့် Dashboard Stats များ Update လုပ်ရန်
            loadUserData("", "All");
            updateDashboardStats();

        } catch (Exception e) {
            System.out.println("❌ Initialization Error: ");
            e.printStackTrace();
        }
    }

    // 🎯 Dashboard ပေါ်ရှိ Card (၄) ခု၏ အရေအတွက်များကို တွက်ချက်ပြသပေးမည့် Method
    private void updateDashboardStats() {
        String query = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
        int total = 0;
        int adminCount = 0;
        int staffCount = 0;

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
            System.out.println("❌ Error loading dashboard stats");
            e.printStackTrace();
        }

        // Label များပေါ်တွင် 1 ယောက်ဆိုလျှင် 'person', အများဆိုလျှင် 'persons' ဟု အလိုအလျောက်ပြပေးမည်
        if (lblTotalUsers != null) {
            lblTotalUsers.setText(total + (total == 1 ? " person" : " persons"));
        }
        if (lblActiveUsers != null) {
            lblActiveUsers.setText(total + (total == 1 ? " person" : " persons"));
        }
        if (lblStaffMembers != null) {
            lblStaffMembers.setText(staffCount + (staffCount == 1 ? " person" : " persons"));
        }
        if (lblAdministrators != null) {
            lblAdministrators.setText(adminCount + (adminCount == 1 ? " person" : " persons"));
        }
    }

    private void filterUsers() {
        String keyword = txtSearch != null ? txtSearch.getText() : "";
        String selectedRole = comboFilterRole != null ? comboFilterRole.getValue() : "All";
        loadUserData(keyword, selectedRole);
    }

    public void loadUserData(String keyword, String roleFilter) {
        userList.clear();

        StringBuilder query = new StringBuilder("SELECT user_id, username, password, email, role, image_path, created_at FROM users WHERE 1=1");

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasRole = roleFilter != null && !roleFilter.equals("All") && !roleFilter.trim().isEmpty();

        if (hasKeyword) {
            query.append(" AND LOWER(username) LIKE LOWER(?)");
        }
        if (hasRole) {
            query.append(" AND LOWER(role) = LOWER(?)");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (hasKeyword) {
                pstmt.setString(paramIndex++, keyword.trim() + "%");
            }
            if (hasRole) {
                pstmt.setString(paramIndex++, roleFilter.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    String imagePath = rs.getString("image_path");

                    java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

                    userList.add(new User(userId, username, password, email, role, imagePath, createdAt));
                }
            }

            if (usersTable != null) {
                usersTable.setItems(userList);
            }

            if (profileCardsContainer != null) {
                profileCardsContainer.getChildren().clear();
                for (User user : userList) {
                    profileCardsContainer.getChildren().add(createUserCard(user));
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error loading data from database");
            e.printStackTrace();
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox();
        card.getStyleClass().add("user-profile-card");

        card.setPrefHeight(300.0);
        card.setMinHeight(300.0);
        card.setMaxHeight(300.0);
        card.setPrefWidth(260.0);
        card.setMinWidth(260.0);
        card.setMaxWidth(260.0);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 15, 20, 15));

        StackPane iconPane = new StackPane();
        iconPane.setMinHeight(74.0);
        iconPane.setPrefHeight(74.0);
        iconPane.setPrefWidth(74.0);
        VBox.setMargin(iconPane, new Insets(0, 0, 15.0, 0));

        boolean hasImage = false;

        if (user.getImagePath() != null && !user.getImagePath().trim().isEmpty()) {
            File imgFile = new File("src/main/resources/com/pos/flightmanagementsystem/image/" + user.getImagePath());
            if (imgFile.exists()) {
                ImageView imageView = new ImageView(new Image(imgFile.toURI().toString()));
                imageView.setFitWidth(74.0);
                imageView.setFitHeight(74.0);

                Circle clip = new Circle(37, 37, 37);
                imageView.setClip(clip);

                iconPane.getChildren().add(imageView);
                hasImage = true;
            }
        }

        if (!hasImage) {
            SVGPath defaultIcon = new SVGPath();
            defaultIcon.setContent("M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z");
            defaultIcon.setScaleX(1.4);
            defaultIcon.setScaleY(1.4);
            iconPane.getChildren().add(defaultIcon);
            iconPane.getStyleClass().add("profile-section-icon");
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

            SuperAdminCreateUserController createUserController = loader.getController();

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
            overlay.getChildren().add(createUserRoot);

            acRootPane.getChildren().add(overlay);
            AnchorPane.setTopAnchor(overlay, 0.0);
            AnchorPane.setBottomAnchor(overlay, 0.0);
            AnchorPane.setLeftAnchor(overlay, 0.0);
            AnchorPane.setRightAnchor(overlay, 0.0);

            Bounds btnBounds = btnCreateUser.localToScene(btnCreateUser.getBoundsInLocal());
            Bounds rootBounds = acRootPane.localToScene(acRootPane.getBoundsInLocal());

            double rootCenterX = rootBounds.getWidth() / 2;
            double rootCenterY = rootBounds.getHeight() / 2;

            double btnCenterX = btnBounds.getMinX() + (btnBounds.getWidth() / 2) - rootBounds.getMinX();
            double btnCenterY = btnBounds.getMinY() + (btnBounds.getHeight() / 2) - rootBounds.getMinY();

            double startTransX = btnCenterX - rootCenterX;
            double startTransY = btnCenterY - rootCenterY;

            createUserController.setCancelAnimationAction(() -> {
                closePopup(overlay, createUserRoot, startTransX, startTransY);
                filterUsers();
                // 🎯 User အသစ်တစ်ခု ပြုလုပ်ပြီးသွားတိုင်း Stats ကိုပါ ချက်ချင်း Update ပြန်လုပ်ပေးမည်
                updateDashboardStats();
            });

            createUserRoot.setTranslateX(startTransX);
            createUserRoot.setTranslateY(startTransY);
            createUserRoot.setScaleX(0.0);
            createUserRoot.setScaleY(0.0);
            createUserRoot.setOpacity(0.0);
            overlay.setOpacity(0.0);

            FadeTransition bgFade = new FadeTransition(Duration.millis(300), overlay);
            bgFade.setToValue(1.0);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), createUserRoot);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(400), createUserRoot);
            moveCenter.setToX(0);
            moveCenter.setToY(0);
            moveCenter.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition formFade = new FadeTransition(Duration.millis(250), createUserRoot);
            formFade.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(bgFade, scaleUp, moveCenter, formFade);
            pt.play();

            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    closePopup(overlay, createUserRoot, startTransX, startTransY);
                    filterUsers();
                    updateDashboardStats(); // အပြင်ကိုနှိပ်ပြီး ပိတ်လျှင်လည်း Update လုပ်မည်
                }
            });

        } catch (Exception e) {
            System.out.println("❌ Error loading CreateUser.fxml");
            e.printStackTrace();
        }
    }

    private void closePopup(StackPane overlay, Parent createUserRoot, double endTransX, double endTransY) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), createUserRoot);
        scaleDown.setToX(0.0);
        scaleDown.setToY(0.0);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition moveBack = new TranslateTransition(Duration.millis(300), createUserRoot);
        moveBack.setToX(endTransX);
        moveBack.setToY(endTransY);
        moveBack.setInterpolator(Interpolator.EASE_IN);

        FadeTransition formFadeOut = new FadeTransition(Duration.millis(200), createUserRoot);
        formFadeOut.setToValue(0.0);

        FadeTransition bgFadeOut = new FadeTransition(Duration.millis(300), overlay);
        bgFadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, moveBack, formFadeOut, bgFadeOut);
        pt.setOnFinished(event -> acRootPane.getChildren().remove(overlay));
        pt.play();
    }
}