package com.pos.flightmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import java.util.function.Consumer;

public class AdminCancelBookingReasonController {

    @FXML private BorderPane rootPane;
    @FXML private TextArea txtReason;

    private Consumer<String> confirmCallback;
    private Runnable cancelCallback;

    // 🌟 Confirm အတွက် သီးသန့် Callback 🌟
    public void setConfirmCallback(Consumer<String> confirmCallback) {
        this.confirmCallback = confirmCallback;
    }

    // 🌟 Cancel အတွက် သီးသန့် Callback (Error တက်နေသော နေရာကို ဖြေရှင်းထားသည်) 🌟
    public void setCancelCallback(Runnable cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    // 🌟 တစ်ပေါင်းတည်း ခေါ်သုံးချင်လျှင် သုံးနိုင်ရန် (Backup အနေဖြင့်) 🌟
    public void setCallbacks(Consumer<String> confirmCallback, Runnable cancelCallback) {
        this.confirmCallback = confirmCallback;
        this.cancelCallback = cancelCallback;
    }

    @FXML
    void clickCancel(ActionEvent event) {
        if(cancelCallback != null) {
            cancelCallback.run();
        }
    }

    @FXML
    void clickConfirm(ActionEvent event) {
        String reason = txtReason.getText();

        // Reason အကွက်လွတ်နေလျှင် သတိပေးချက်ပြမည်
        if (reason == null || reason.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Reason Required");
            alert.setHeaderText(null);
            alert.setContentText("ကျေးဇူးပြု၍ Cancel လုပ်ရသည့် အကြောင်းရင်း (Reason) ကို ထည့်သွင်းပါ။");
            alert.showAndWait();
            return;
        }

        // Reason ရှိလျှင် မိခင် Form ဆီသို့ Data ပို့ပေးမည်
        if(confirmCallback != null) {
            confirmCallback.accept(reason.trim());
        }
    }
}