package com.pos.flightmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CreateUserController {

    @FXML
    private Button btnCancel;

    // ပြန်ပိတ်မည့် Animation ကို Main Controller (ACManagement) ဆီမှ လက်ခံရန်
    private Runnable cancelAnimationAction;

    // ACManagementController ဘက်မှ ဒီ Method ကိုလှမ်းခေါ်ပြီး Animation တွဲပေးပါမည်
    public void setCancelAnimationAction(Runnable cancelAnimationAction) {
        this.cancelAnimationAction = cancelAnimationAction;
    }

    @FXML
    void clickCancel(ActionEvent event) {
        // Cancel ကို နှိပ်လိုက်လျှင် ပိတ်မည့် Animation အလုပ်လုပ်ပါမည်
        if (cancelAnimationAction != null) {
            cancelAnimationAction.run();
        }
    }
}