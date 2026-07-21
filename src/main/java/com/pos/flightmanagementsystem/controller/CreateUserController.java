package com.pos.flightmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CreateUserController implements Initializable {

    @FXML
    private ImageView profileImageView;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    // 👁️ Password အစစ်ပေါ်မည့် TextField (Eye ဖွင့်ထားချိန်သုံးရန်)
    @FXML
    private TextField txtPasswordVisible;

    @FXML
    private SVGPath eyeIcon;

    @FXML
    private ComboBox<String> comboRole;

    @FXML
    private Button btnChooseImage;

    @FXML
    private Button btnCancel;

    private Runnable cancelAnimationAction;
    private File selectedImageFile;

    // 👁️ Password အပိတ်/အဖွင့် Icon ပုံစံများ (SVG Paths)
    private boolean isPasswordVisible = false;
    private final String EYE_OPEN = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private final String EYE_CLOSED = "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboRole.getItems().addAll("Admin", "Staff");

        Circle clip = new Circle(50, 50, 50);
        profileImageView.setClip(clip);

        // 👁️ Password Field နှစ်ခုကို ချိတ်ဆက်ထားခြင်း (တစ်ခုပြောင်းလျှင် အခြားတစ်ခုပါ အလိုအလျောက် ပြောင်းမည်)
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
    }

    // 👁️ Eye Icon နှိပ်လျှင် အလုပ်လုပ်မည့် Function
    @FXML
    void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            // စာသားပေါ်အောင်ပြမည်
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            eyeIcon.setContent(EYE_OPEN); // မျက်လုံးအဖွင့်ပုံ
        } else {
            // ကြယ်ပွင့် (***) ပြန်ဖြစ်မည်
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            eyeIcon.setContent(EYE_CLOSED); // မျက်လုံးအပိတ်ပုံ
        }
    }

    public void setCancelAnimationAction(Runnable cancelAnimationAction) {
        this.cancelAnimationAction = cancelAnimationAction;
    }

    @FXML
    void clickCancel(ActionEvent event) {
        if (cancelAnimationAction != null) {
            cancelAnimationAction.run();
        }
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnChooseImage.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            Image image = new Image(selectedImageFile.toURI().toString());
            profileImageView.setImage(image);
        }
    }

    @FXML
    void saveUser(ActionEvent event) {
        String username = txtUsername.getText();
        String email = txtEmail.getText();

        // 👁️ Password ကို Visible ဖြစ်နေချိန်ဖြစ်စေ၊ Hide ထားချိန်ဖြစ်စေ (bind လုပ်ထား၍) txtPassword ကနေပဲ တိုက်ရိုက်ယူပါမည်
        String password = txtPassword.getText();

        String role = comboRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all required fields (Username, Password, Role)!");
            return;
        }

        String dbRole = role.toLowerCase();
        String imageFileNameForDB = null;

        try {
            if (selectedImageFile != null) {
                String destDirPath = "src/main/resources/com/pos/flightmanagementsystem/image/";
                File destDir = new File(destDirPath);

                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                String originalFileName = selectedImageFile.getName();
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = "profile_" + System.currentTimeMillis() + extension;

                Path destPath = Paths.get(destDirPath + newFileName);
                Files.copy(selectedImageFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                imageFileNameForDB = newFileName;
            }

            String sql = "INSERT INTO users (username, email, password, role, image_path) VALUES (?, ?, ?, ?, ?)";

            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, email.isEmpty() ? null : email);
                pstmt.setString(3, password);
                pstmt.setString(4, dbRole);
                pstmt.setString(5, imageFileNameForDB);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User account created successfully!");

                    if (cancelAnimationAction != null) {
                        cancelAnimationAction.run();
                    }
                }

                pstmt.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Failed to save the image file.");
            e.printStackTrace();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save user data.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}