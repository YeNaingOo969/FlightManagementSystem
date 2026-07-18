package com.pos.flightmanagementsystem.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private AnchorPane profileRoot;
    @FXML private StackPane profileView;

    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblRole;
    @FXML private Label lblEmployeeId;

    private Parent pwdChangeView;

    // လက်ရှိ Login ဝင်ထားသူ၏ နာမည်ကို သိမ်းထားရန်
    private String loggedInUser = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(this::loadUserData).start();
    }

    private void loadUserData() {
        String query = "SELECT user_id, username, email, role FROM users LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");

                // Database မှ ရလာသော Username အား Variable ထဲသို့ ထည့်သိမ်းခြင်း
                loggedInUser = username;

                Platform.runLater(() -> {
                    lblFullName.setText(username);
                    lblEmail.setText(email != null && !email.isEmpty() ? email : "No Email Provided");
                    lblRole.setText(role != null ? role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase() : "Unknown");
                    lblEmployeeId.setText(String.format("EMP-%04d", userId));
                });

            } else {
                Platform.runLater(() -> {
                    lblFullName.setText("No User Found");
                    lblEmail.setText("-");
                    lblRole.setText("-");
                    lblEmployeeId.setText("-");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                lblFullName.setText("Database Error");
                lblEmail.setText("Check Console");
                lblRole.setText("Error");
                lblEmployeeId.setText("Error");
            });
        }
    }

    @FXML
    void showPasswordView(ActionEvent event) {
        try {
            if (pwdChangeView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/PwdChange.fxml"));
                pwdChangeView = loader.load();

                PwdChangeController pwdController = loader.getController();
                if (pwdController != null) {
                    pwdController.setParentController(this);

                    // Username အစစ်ကို PwdChangeController ဆီသို့ လှမ်းပို့ပေးခြင်း
                    pwdController.setCurrentUser(loggedInUser);
                }

                profileRoot.getChildren().add(pwdChangeView);
                AnchorPane.setTopAnchor(pwdChangeView, 0.0);
                AnchorPane.setBottomAnchor(pwdChangeView, 0.0);
                AnchorPane.setLeftAnchor(pwdChangeView, 0.0);
                AnchorPane.setRightAnchor(pwdChangeView, 0.0);
            }

            double width = profileRoot.getWidth();
            if (width == 0) width = 1000;

            pwdChangeView.setTranslateX(width);
            pwdChangeView.setVisible(true);
            pwdChangeView.toFront();

            TranslateTransition profileOut = new TranslateTransition(Duration.millis(400), profileView);
            profileOut.setToX(width);
            profileOut.play();

            TranslateTransition pwdIn = new TranslateTransition(Duration.millis(400), pwdChangeView);
            pwdIn.setToX(0);
            pwdIn.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hidePasswordView() {
        double width = profileRoot.getWidth();

        TranslateTransition pwdOut = new TranslateTransition(Duration.millis(400), pwdChangeView);
        pwdOut.setToX(width);
        pwdOut.setOnFinished(e -> pwdChangeView.setVisible(false));
        pwdOut.play();

        TranslateTransition profileIn = new TranslateTransition(Duration.millis(400), profileView);
        profileIn.setToX(0);
        profileIn.play();
    }
}