package com.pos.flightmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // FXML ဖိုင်လမ်းကြောင်းကို သေချာစစ်ပါ
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/pos/flightmanagementsystem/view/Staff.fxml"));

        // Window အရွယ်အစား သတ်မှတ်ချက် (320, 240) ကို ဖျက်လိုက်ပါ
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("ArrLuuu Airline - Login");
        stage.setScene(scene);

        // Full Screen ဖြစ်အောင် လုပ်ခြင်း
        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}