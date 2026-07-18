package com.pos.flightmanagementsystem.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class PwdChangeController implements Initializable {

    @FXML private VBox sectionCurrentPwd, sectionEmail, sectionOTP, sectionNewPwd;
    @FXML private Label lblPwdStatus;
    @FXML private PasswordField txtCurrentPwd, txtNewPwd, txtConfirmPwd;
    @FXML private TextField txtEmailReset;
    @FXML private TextField otp1, otp2, otp3, otp4, otp5, otp6;
    @FXML private Button btnSendOTP, btnVerifyOTP;

    private TextField[] otpFields;
    private ProfileController parentController;

    private String generatedOTP = "";
    private final String senderEmail = "luuarr344@gmail.com";
    private final String senderPassword = "ifwo vnqq vmok ayan";

    // Hardcode အစား ပြောင်းလဲနိုင်သော Variable အဖြစ်သတ်မှတ်ထားခြင်း
    private String currentUser = "";

    public void setParentController(ProfileController parentController) {
        this.parentController = parentController;
    }

    // ProfileController မှ လက်ရှိ Username ကို လှမ်းထည့်ပေးမည့် Method
    public void setCurrentUser(String user) {
        this.currentUser = user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        otpFields = new TextField[]{otp1, otp2, otp3, otp4, otp5, otp6};
        setupOTPInputs();
        resetPasswordForms();
    }

    @FXML
    void hidePasswordView(ActionEvent event) {
        if (parentController != null) {
            parentController.hidePasswordView();
            resetPasswordForms();
        }
    }

    private void switchSection(VBox targetSection) {
        sectionCurrentPwd.setVisible(false); sectionCurrentPwd.setManaged(false);
        sectionEmail.setVisible(false); sectionEmail.setManaged(false);
        sectionOTP.setVisible(false); sectionOTP.setManaged(false);
        sectionNewPwd.setVisible(false); sectionNewPwd.setManaged(false);

        targetSection.setVisible(true); targetSection.setManaged(true);
    }

    private void resetPasswordForms() {
        switchSection(sectionCurrentPwd);
        lblPwdStatus.setText("");
        txtCurrentPwd.clear(); txtEmailReset.clear();
        txtNewPwd.clear(); txtConfirmPwd.clear();
        for (TextField f : otpFields) f.clear();
    }

    @FXML void showCurrentPwdSection() {
        switchSection(sectionCurrentPwd);
        lblPwdStatus.setText("");
    }

    @FXML void showEmailSection() {
        switchSection(sectionEmail);
        lblPwdStatus.setText("");
    }

    @FXML
    void verifyCurrentPassword(ActionEvent event) {
        String inputPwd = txtCurrentPwd.getText().trim();
        if (inputPwd.isEmpty()) {
            lblPwdStatus.setTextFill(Color.RED);
            lblPwdStatus.setText("Please enter your current password.");
            return;
        }

        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, currentUser);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (dbPassword.equals(inputPwd)) {
                    lblPwdStatus.setTextFill(Color.GREEN);
                    lblPwdStatus.setText("Password verified. Enter your new password.");
                    switchSection(sectionNewPwd);
                } else {
                    lblPwdStatus.setTextFill(Color.RED);
                    lblPwdStatus.setText("Incorrect current password.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void sendOTP(ActionEvent event) {
        String targetEmail = txtEmailReset.getText().trim();
        if (targetEmail.isEmpty()) {
            lblPwdStatus.setTextFill(Color.RED);
            lblPwdStatus.setText("Please enter your email.");
            return;
        }

        String query = "SELECT email FROM users WHERE username = ? AND email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, currentUser);
            pstmt.setString(2, targetEmail);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                lblPwdStatus.setTextFill(Color.BLUE);
                lblPwdStatus.setText("Sending OTP... Please wait.");
                btnSendOTP.setDisable(true);

                generatedOTP = String.format("%06d", new Random().nextInt(999999));

                new Thread(() -> {
                    boolean isSent = sendEmail(targetEmail, generatedOTP);
                    Platform.runLater(() -> {
                        btnSendOTP.setDisable(false);
                        if (isSent) {
                            lblPwdStatus.setTextFill(Color.GREEN);
                            lblPwdStatus.setText("OTP sent successfully to your email.");
                            switchSection(sectionOTP);
                        } else {
                            lblPwdStatus.setTextFill(Color.RED);
                            lblPwdStatus.setText("Failed to send OTP. Check connection.");
                        }
                    });
                }).start();
            } else {
                lblPwdStatus.setTextFill(Color.RED);
                lblPwdStatus.setText("Email does not match our records.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void verifyOTP(ActionEvent event) {
        String enteredOTP = otp1.getText() + otp2.getText() + otp3.getText() +
                otp4.getText() + otp5.getText() + otp6.getText();

        if (enteredOTP.length() < 6) {
            lblPwdStatus.setTextFill(Color.RED);
            lblPwdStatus.setText("Please enter the complete 6-digit OTP.");
            return;
        }

        if (enteredOTP.equals(generatedOTP)) {
            lblPwdStatus.setTextFill(Color.GREEN);
            lblPwdStatus.setText("OTP Verified! You can now reset your password.");
            switchSection(sectionNewPwd);
        } else {
            lblPwdStatus.setTextFill(Color.RED);
            lblPwdStatus.setText("Invalid OTP. Please try again.");
        }
    }

    @FXML
    void applyNewPassword(ActionEvent event) {
        String newPwd = txtNewPwd.getText().trim();
        String confPwd = txtConfirmPwd.getText().trim();

        if (newPwd.isEmpty() || confPwd.isEmpty()) {
            lblPwdStatus.setTextFill(Color.RED);
            lblPwdStatus.setText("Fields cannot be empty.");
            return;
        }

        if (!newPwd.equals(confPwd)) {
            lblPwdStatus.setTextFill(Color.RED);
            lblPwdStatus.setText("Passwords do not match!");
            return;
        }

        String query = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newPwd);
            pstmt.setString(2, currentUser);

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                if (parentController != null) {
                    parentController.hidePasswordView();
                    resetPasswordForms();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean sendEmail(String recipientEmail, String otpCode) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Password Reset OTP - ArrLuuu Airline");
            message.setText("Dear User,\n\nYour OTP for password reset is: " + otpCode +
                    "\n\nPlease do not share this code with anyone.\n\nBest Regards,\nArrLuuu Airline Team");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setupOTPInputs() {
        for (int i = 0; i < otpFields.length; i++) {
            TextField currentField = otpFields[i];
            final int index = i;

            currentField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    currentField.setText(newValue.replaceAll("[^\\d]", ""));
                    return;
                }
                if (newValue.length() > 1) {
                    currentField.setText(newValue.substring(0, 1));
                    return;
                }
                if (newValue.length() == 1 && index < otpFields.length - 1) {
                    otpFields[index + 1].requestFocus();
                }
            });

            currentField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.BACK_SPACE && currentField.getText().isEmpty()) {
                    if (index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }
            });
        }
    }
}