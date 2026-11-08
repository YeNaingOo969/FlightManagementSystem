package com.pos.flightmanagementsystem;

import com.pos.flightmanagementsystem.controller.DatabaseConnection;
import com.pos.flightmanagementsystem.controller.LoginController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/pos/flightmanagementsystem/view/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("ArrLuuu Airline - Login");
        stage.setScene(scene);
        stage.setMaximized(true);

        // 🌟 ကြက်ခြေခတ် (X) ကို နှိပ်၍ App ပိတ်လိုက်သောအခါ အလုပ်လုပ်မည့် Code 🌟
        stage.setOnCloseRequest(event -> {
            if (LoginController.loggedInUser != null) {
                // Application အပြီးမပိတ်ခင်လေးမှာ လက်ရှိ User ကို Offline အဖြစ် ပြောင်းပေးမည်
                setOfflineStatus(LoginController.loggedInUser.getUsername());
            }
            Platform.exit(); // JavaFX Thread ကို အပြီးပိတ်မည်
            System.exit(0); // System တစ်ခုလုံးကို ရပ်တန့်မည်
        });

        stage.show();
    }

    // 🌟 User ကို Offline အဖြစ် ပြောင်းပေးမည့် Helper Method (Force Commit ပါဝင်သည်) 🌟
    private void setOfflineStatus(String username) {
        String sql = "UPDATE users SET is_online = 0 WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();

            // 🌟 Database AutoCommit ပိတ်နေခဲ့လျှင် အတင်းသိမ်းခိုင်းရန် 🌟
            try {
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
            } catch (Exception ignored) {}

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}