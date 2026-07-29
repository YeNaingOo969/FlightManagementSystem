package com.pos.flightmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class AdminCancelBookingReasonController {

    @FXML private BorderPane rootPane;
    @FXML private TextArea txtReason;

    // Callback to trigger modal closing on main view
    private Runnable cancelCallback;

    public void setCancelCallback(Runnable cancelCallback) {
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
        if (reason.trim().isEmpty()) {
            System.out.println("Please provide a reason!");
            return;
        }

        System.out.println("Booking Cancelled. Reason: " + reason);

        if(cancelCallback != null) {
            cancelCallback.run();
        }
    }
}