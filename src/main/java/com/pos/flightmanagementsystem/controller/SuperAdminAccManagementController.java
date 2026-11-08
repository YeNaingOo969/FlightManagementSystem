package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.TableCell;
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
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class SuperAdminAccManagementController implements Initializable {

    @FXML private AnchorPane acRootPane;
    @FXML private Button btnCreateUser;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboFilterRole;
    @FXML private ComboBox<String> comboFilterStatus;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblStaffMembers;
    @FXML private Label lblAdministrators;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User,String> colUsername;
    @FXML private TableColumn<User,String> colPassword;
    @FXML private TableColumn<User,String> colRole;
    @FXML private TableColumn<User,String> colEmail;
    @FXML private TableColumn<User,String> colStatus;

    @FXML private HBox profileCardsContainer;
    @FXML private HBox notificationToast;
    @FXML private Label lblToastTitle;
    @FXML private Label lblToastMessage;

    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (colUsername != null) {
                colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
                colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
                colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
                colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
                colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
                setupStatusColumn();
            }

            if (comboFilterRole != null) {
                comboFilterRole.getItems().addAll("All","Admin","Staff");
                comboFilterRole.setValue("All");
                comboFilterRole.valueProperty().addListener((observable,oldValue,newValue) -> filterUsers());
            }

            if (comboFilterStatus != null) {
                comboFilterStatus.getItems().addAll("Active","Inactive");
                comboFilterStatus.setValue("Active");
                comboFilterStatus.valueProperty().addListener((observable,oldValue,newValue) -> filterUsers());
            }

            if (txtSearch != null) {
                txtSearch.textProperty().addListener((observable,oldValue,newValue) -> filterUsers());
            }

            loadUserData("","All","Active");
            updateDashboardStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupStatusColumn() {
        colStatus.setCellFactory(column -> new TableCell<User,String>() {
            private final Button statusBtn = new Button();
            private final Button editBtn = new Button("Edit");
            private final HBox container = new HBox(8,editBtn,statusBtn);

            {
                container.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("secondary-button");


                statusBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleUserStatus(user);
                });

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    openEditUser(user);
                });
            }

            @Override
            protected void updateItem(String status,boolean empty) {
                super.updateItem(status,empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    statusBtn.setText(status);
                    if (status.equalsIgnoreCase("Active")) {
                        statusBtn.getStyleClass().setAll("status-btn-active");
                    } else {
                        statusBtn.getStyleClass().setAll("status-btn-inactive");
                    }
                    setGraphic(container);
                }
            }
        });
    }

    private void toggleUserStatus(User user) {
        String newStatus = user.getStatus().equalsIgnoreCase("Active") ? "Inactive" : "Active";
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1,newStatus);
            pstmt.setInt(2,user.getUserId());

            if (pstmt.executeUpdate() > 0) {
                int adminId = LoginController.loggedInUser != null ? LoginController.loggedInUser.getUserId() : 1;
                insertLog(adminId,"Changed status of user '" + user.getUsername() + "' to " + newStatus);
                user.setStatus(newStatus);
                usersTable.refresh();
                updateProfileCards();
                updateDashboardStats();
                showToast("Status Updated",user.getUsername() + "'s account is now " + newStatus + ".");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertLog(int userId,String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {

            if (userId > 0) {
                pstmt.setInt(1,userId);
            } else {
                pstmt.setNull(1,java.sql.Types.INTEGER);
            }

            pstmt.setString(2,action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDashboardStats() {
        String query = "SELECT role, status, COUNT(*) as count FROM users GROUP BY role, status";
        int total = 0,adminCount = 0,staffCount = 0,activeCount = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String role = rs.getString("role");
                String status = rs.getString("status");
                int count = rs.getInt("count");

                total += count;

                if (status != null && status.equalsIgnoreCase("Active")) {
                    activeCount += count;
                }

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
        if (lblActiveUsers != null) lblActiveUsers.setText(activeCount + (activeCount == 1 ? " person" : " persons"));
        if (lblStaffMembers != null) lblStaffMembers.setText(staffCount + (staffCount == 1 ? " person" : " persons"));
        if (lblAdministrators != null) lblAdministrators.setText(adminCount + (adminCount == 1 ? " person" : " persons"));
    }

    private void filterUsers() {
        String keyword = txtSearch != null ? txtSearch.getText() : "";
        String selectedRole = comboFilterRole != null ? comboFilterRole.getValue() : "All";
        String selectedStatus = comboFilterStatus != null ? comboFilterStatus.getValue() : "Active";
        loadUserData(keyword,selectedRole,selectedStatus);
    }

    public void loadUserData(String keyword,String roleFilter,String statusFilter) {
        userList.clear();

        StringBuilder query = new StringBuilder(
                "SELECT user_id, username, email, password, role, status, image_path, created_at FROM users WHERE 1=1"
        );

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasRole = roleFilter != null && !roleFilter.equals("All") && !roleFilter.trim().isEmpty();
        boolean hasStatus = statusFilter != null && !statusFilter.trim().isEmpty();

        if (hasKeyword) query.append(" AND LOWER(username) LIKE LOWER(?)");
        if (hasRole) query.append(" AND LOWER(role) = LOWER(?)");
        if (hasStatus) query.append(" AND LOWER(status) = LOWER(?)");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;

            if (hasKeyword) pstmt.setString(paramIndex++,keyword.trim() + "%");
            if (hasRole) pstmt.setString(paramIndex++,roleFilter.trim());
            if (hasStatus) pstmt.setString(paramIndex++,statusFilter.trim());

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
                    LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

                    userList.add(new User(userId,username,email,password,role,status,imagePath,createdAt));
                }
            }

            if (usersTable != null) usersTable.setItems(userList);
            updateProfileCards();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProfileCards() {
        if (profileCardsContainer != null) {
            profileCardsContainer.getChildren().clear();
            for (User user : userList) {
                profileCardsContainer.getChildren().add(createUserCard(user));
            }
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox();
        card.getStyleClass().add("user-profile-card");

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("user-card-avatar-container");
        VBox.setMargin(iconPane,new Insets(0,0,15.0,0));

        boolean hasImage = false;

        if (user.getImagePath() != null && !user.getImagePath().trim().isEmpty()) {
            File imgFile = new File("src/main/resources/com/pos/flightmanagementsystem/image/" + user.getImagePath());

            if (imgFile.exists()) {
                ImageView imageView = new ImageView(new Image(imgFile.toURI().toString()));
                imageView.setFitWidth(80.0);
                imageView.setFitHeight(80.0);
                Circle clip = new Circle(40,40,40);
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
        nameLbl.setPadding(new Insets(0,0,12.0,0));

        String roleStr = user.getRole() != null ? user.getRole().toUpperCase() : "STAFF";
        Label roleLbl = new Label(roleStr);
        roleLbl.getStyleClass().add("profile-label");
        roleLbl.setPadding(new Insets(0,0,12.0,0));

        String currentStatus = user.getStatus() != null ? user.getStatus() : "Active";
        Label statusLbl = new Label(currentStatus.toUpperCase());

        if (currentStatus.equalsIgnoreCase("Active")) {
            statusLbl.getStyleClass().add("status-active");
        } else {
            statusLbl.getStyleClass().add("status-inactive");
        }

        statusLbl.setPadding(new Insets(0,0,12.0,0));
        card.getChildren().addAll(iconPane,nameLbl,roleLbl,statusLbl);
        return card;
    }

    @FXML
    void openCreateUser(ActionEvent event) {
        showUserDialog(null);
    }

    private void openEditUser(User user) {
        showUserDialog(user);
    }

    private void showUserDialog(User userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/pos/flightmanagementsystem/view/SuperAdminCreateUser.fxml")
            );

            Parent dialogRoot = loader.load();

            if (!acRootPane.getStylesheets().isEmpty()) {
                String currentMainStyle = acRootPane.getStylesheets().get(0);
                dialogRoot.getStylesheets().clear();

                if (currentMainStyle.contains("Light")) {
                    dialogRoot.getStylesheets().add(
                            getClass().getResource(
                                    "/com/pos/flightmanagementsystem/Style/SuperAdminCreateUserLight.css"
                            ).toExternalForm()
                    );
                } else {
                    dialogRoot.getStylesheets().add(
                            getClass().getResource(
                                    "/com/pos/flightmanagementsystem/Style/SuperAdminCreateUserDark.css"
                            ).toExternalForm()
                    );
                }
            }

            SuperAdminCreateUserController controller = loader.getController();

            if (userToEdit != null) {
                controller.setEditMode(userToEdit);
            }

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
            overlay.getChildren().add(dialogRoot);

            acRootPane.getChildren().add(overlay);

            AnchorPane.setTopAnchor(overlay,0.0);
            AnchorPane.setBottomAnchor(overlay,0.0);
            AnchorPane.setLeftAnchor(overlay,0.0);
            AnchorPane.setRightAnchor(overlay,0.0);

            Bounds btnBounds = btnCreateUser.localToScene(btnCreateUser.getBoundsInLocal());
            Bounds rootBounds = acRootPane.localToScene(acRootPane.getBoundsInLocal());

            double rootCenterX = rootBounds.getWidth() / 2;
            double rootCenterY = rootBounds.getHeight() / 2;

            double btnCenterX = btnBounds.getMinX() + (btnBounds.getWidth() / 2) - rootBounds.getMinX();
            double btnCenterY = btnBounds.getMinY() + (btnBounds.getHeight() / 2) - rootBounds.getMinY();

            double startTransX = btnCenterX - rootCenterX;
            double startTransY = btnCenterY - rootCenterY;

            controller.setSuccessAction(() -> {
                closePopup(overlay,dialogRoot,startTransX,startTransY);
                filterUsers();
                updateDashboardStats();
                String message = userToEdit != null
                        ? "User account updated successfully."
                        : "User account created successfully.";
                showToast("Success!",message);
            });

            controller.setCancelAnimationAction(() -> {
                closePopup(overlay,dialogRoot,startTransX,startTransY);
            });

            dialogRoot.setTranslateX(startTransX);
            dialogRoot.setTranslateY(startTransY);
            dialogRoot.setScaleX(0.0);
            dialogRoot.setScaleY(0.0);
            dialogRoot.setOpacity(0.0);
            overlay.setOpacity(0.0);

            FadeTransition bgFade = new FadeTransition(Duration.millis(300),overlay);
            bgFade.setToValue(1.0);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400),dialogRoot);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(400),dialogRoot);
            moveCenter.setToX(0);
            moveCenter.setToY(0);
            moveCenter.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition formFade = new FadeTransition(Duration.millis(250),dialogRoot);
            formFade.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(bgFade,scaleUp,moveCenter,formFade);
            pt.play();

            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    closePopup(overlay,dialogRoot,startTransX,startTransY);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closePopup(StackPane overlay,Parent dialogRoot,double endTransX,double endTransY) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300),dialogRoot);
        scaleDown.setToX(0.0);
        scaleDown.setToY(0.0);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition moveBack = new TranslateTransition(Duration.millis(300),dialogRoot);
        moveBack.setToX(endTransX);
        moveBack.setToY(endTransY);
        moveBack.setInterpolator(Interpolator.EASE_IN);

        FadeTransition formFadeOut = new FadeTransition(Duration.millis(200),dialogRoot);
        formFadeOut.setToValue(0.0);

        FadeTransition bgFadeOut = new FadeTransition(Duration.millis(300),overlay);
        bgFadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown,moveBack,formFadeOut,bgFadeOut);
        pt.setOnFinished(event -> acRootPane.getChildren().remove(overlay));
        pt.play();
    }

    private void showToast(String title,String message) {
        lblToastTitle.setText(title);
        lblToastMessage.setText(message);
        notificationToast.setVisible(true);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(300),notificationToast);
        slideUp.setFromY(100);
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300),notificationToast);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition ptIn = new ParallelTransition(slideUp,fadeIn);
        ptIn.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> closeToast(null));
        delay.play();
    }

    @FXML
    void closeToast(ActionEvent event) {
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(300),notificationToast);
        slideDown.setToY(100);
        slideDown.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300),notificationToast);
        fadeOut.setToValue(0.0);

        ParallelTransition ptOut = new ParallelTransition(slideDown,fadeOut);
        ptOut.setOnFinished(e -> notificationToast.setVisible(false));
        ptOut.play();
    }
}