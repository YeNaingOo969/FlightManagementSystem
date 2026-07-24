package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminAddFlightController implements Initializable {

    @FXML private BorderPane rootPane;

    // --- Form Fields ---
    @FXML private TextField txtFlightNumber;

    // City ComboBoxes
    @FXML private ComboBox<String> cmbDepartureCity;
    @FXML private ComboBox<String> cmbArrivalCity;

    // Departure Date & Time
    @FXML private DatePicker dpDepartureDate;
    @FXML private ComboBox<String> cmbDepartureHour;
    @FXML private ComboBox<String> cmbDepartureMinute;

    // Arrival Date & Time
    @FXML private DatePicker dpArrivalDate;
    @FXML private ComboBox<String> cmbArrivalHour;
    @FXML private ComboBox<String> cmbArrivalMinute;

    @FXML private TextField txtTotalSeats;
    @FXML private TextField txtBasePrice;
    @FXML private ComboBox<String> cmbStatus;

    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 🌟 ၁။ နာရီ (00 မှ 23) Data ထည့်သွင်းခြင်း
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i <= 23; i++) {
            hours.add(String.format("%02d", i)); // 01, 02 ပုံစံဖြစ်အောင် %02d သုံးထားသည်
        }
        cmbDepartureHour.setItems(hours);
        cmbArrivalHour.setItems(hours);

        // 🌟 ၂။ မိနစ် (00 မှ 59 - ၅ မိနစ်ခြား) Data ထည့်သွင်းခြင်း
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) {
            minutes.add(String.format("%02d", i));
        }
        cmbDepartureMinute.setItems(minutes);
        cmbArrivalMinute.setItems(minutes);

        // 🌟 ၃။ City Database မှ Data ဆွဲထုတ်ရန် (ယခု ဥပမာအဖြစ် Dummy Data ထည့်ထားပါသည်)
        // တကယ့်အပြင်မှာ Database Query ရေးပြီး ဒီ List ထဲကို ထည့်ပေးရပါမည်
        ObservableList<String> cityList = FXCollections.observableArrayList(
                "Yangon (YGN)", "Mandalay (MDL)", "Bagan (NYU)", "Naypyidaw (NYT)", "Heho (HEH)"
        );
        cmbDepartureCity.setItems(cityList);
        cmbArrivalCity.setItems(cityList);
    }

    // ==========================================
    // 🌟 CANCEL BUTTON ACTION (Animation ဖြင့် ပြန်ပိတ်ရန်)
    // ==========================================
    @FXML
    void clickCancel(ActionEvent event) {
        StackPane overlay = (StackPane) rootPane.getParent();

        if (overlay != null) {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(350), rootPane);
            scaleDown.setToX(0.0);
            scaleDown.setToY(0.0);

            TranslateTransition moveBack = new TranslateTransition(Duration.millis(350), rootPane);
            moveBack.setToX(400); // ညာဘက်သို့
            moveBack.setToY(-300); // အပေါ်ဘက်သို့

            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), overlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(scaleDown, moveBack, fadeOut);
            pt.setInterpolator(Interpolator.EASE_IN);

            pt.setOnFinished(e -> {
                StackPane mainRoot = (StackPane) overlay.getParent();
                if (mainRoot != null) {
                    mainRoot.getChildren().remove(overlay);
                }
            });

            pt.play();
        }
    }

    // ==========================================
    // 🌟 SAVE BUTTON ACTION (Database သို့ သိမ်းရန်)
    // ==========================================
    @FXML
    void clickSave(ActionEvent event) {
        System.out.println("Saving Flight Data to Database...");
        System.out.println("Flight Number: " + txtFlightNumber.getText());
        System.out.println("From: " + cmbDepartureCity.getValue());
        System.out.println("To: " + cmbArrivalCity.getValue());

        // Date နဲ့ Time ကို String အဖြစ် ပြန်ပေါင်းယူနည်း
        String departureDateTime = dpDepartureDate.getValue() + " " + cmbDepartureHour.getValue() + ":" + cmbDepartureMinute.getValue() + ":00";
        System.out.println("Departure: " + departureDateTime);

        // Save ပြီးပါက Modal ကို ပြန်ပိတ်ပါမည်
        clickCancel(event);
    }
}