package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class AdminAddCityController {

    @FXML private BorderPane rootPane;

    @FXML private TextField txtCityCode;
    @FXML private TextField txtCityName;
    @FXML private TextField txtCountry;

    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // Callbacks to communicate with the Parent Controller (AdminCityController)
    private Runnable saveCallback;
    private Runnable cancelCallback;

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }

    public void setCancelCallback(Runnable cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    // Trigger parent callback to close the modal easily
    @FXML
    void clickCancel(ActionEvent event) {
        if (cancelCallback != null) {
            cancelCallback.run();
        }
    }

    @FXML
    void clickSave(ActionEvent event) {
        String cityCode = txtCityCode.getText().trim();
        String cityName = txtCityName.getText().trim();
        String country = txtCountry.getText().trim();

        if (cityCode.isEmpty() || cityName.isEmpty() || country.isEmpty()) {
            showWarningModal("Validation Error", "Please fill in all fields (City Code, City Name, and Country).", false);
            return;
        }

        String insertQuery = "INSERT INTO cities (city_code, city_name, country) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, cityCode);
                    pstmt.setString(2, cityName);
                    pstmt.setString(3, country);

                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        // If save is successful, trigger parent callback directly.
                        // Parent will handle closing the modal and showing the success toast.
                        if (saveCallback != null) {
                            saveCallback.run();
                        }
                    } else {
                        showWarningModal("Failed", "Failed to save the city. Please try again.", true);
                    }
                }
            } else {
                showWarningModal("Connection Error", "Could not connect to the database.", true);
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            showWarningModal("Duplicate Data", "This City Code (" + cityCode + ") already exists in the system. Please enter a different code.", false);
        } catch (SQLException e) {
            e.printStackTrace();
            showWarningModal("Database Error", "An error occurred:\n" + e.getMessage(), true);
        }
    }

    // =========================================================================
    // 🌟 CUSTOM WARNING/ERROR MODAL (ANIMATED & BLURRED BACKGROUND) 🌟
    // =========================================================================
    private void showWarningModal(String title, String message, boolean isError) {
        // Find the parent StackPane (Scene Root or Parent)
        StackPane windowRoot = null;
        if (rootPane.getScene() != null && rootPane.getScene().getRoot() instanceof StackPane) {
            windowRoot = (StackPane) rootPane.getScene().getRoot();
        } else if (rootPane.getParent() instanceof StackPane) {
            windowRoot = (StackPane) rootPane.getParent();
        }

        if (windowRoot == null) return;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        BoxBlur blur = new BoxBlur(8, 8, 3);
        rootPane.setEffect(blur);

        String themeColor = isError ? "#EF4444" : "#F59E0B";
        String hoverBgColor = isError ? "rgba(239, 68, 68, 0.15)" : "rgba(245, 158, 11, 0.15)";

        String svgIconPath = isError
                ? "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"
                : "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z";

        VBox modalCard = new VBox(20);
        modalCard.setMaxSize(420, Region.USE_PREF_SIZE);
        modalCard.setStyle("-fx-background-color: #0B0E14; -fx-border-color: " + themeColor + "; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 24px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 10);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + themeColor + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane closeBtn = new StackPane();
        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        closeIcon.setStyle("-fx-fill: #8B95A5; -fx-scale-x: 0.8; -fx-scale-y: 0.8;");
        closeBtn.getChildren().add(closeIcon);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnMouseEntered(e -> closeIcon.setStyle("-fx-fill: #FFFFFF; -fx-scale-x: 0.8; -fx-scale-y: 0.8;"));
        closeBtn.setOnMouseExited(e -> closeIcon.setStyle("-fx-fill: #8B95A5; -fx-scale-x: 0.8; -fx-scale-y: 0.8;"));

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        HBox body = new HBox(15);
        body.setAlignment(Pos.CENTER_LEFT);
        SVGPath warningIcon = new SVGPath();
        warningIcon.setContent(svgIconPath);
        warningIcon.setStyle("-fx-fill: " + themeColor + "; -fx-scale-x: 1.5; -fx-scale-y: 1.5;");
        StackPane iconPane = new StackPane(warningIcon);
        iconPane.setPadding(new Insets(0, 10, 0, 10));

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        body.getChildren().addAll(iconPane, messageLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button okBtn = new Button("OK");
        okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + themeColor + "; -fx-text-fill: " + themeColor + "; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;");
        okBtn.setOnMouseEntered(e -> okBtn.setStyle("-fx-background-color: " + hoverBgColor + "; -fx-border-color: " + themeColor + "; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;"));
        okBtn.setOnMouseExited(e -> okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + themeColor + "; -fx-text-fill: " + themeColor + "; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;"));

        footer.getChildren().add(okBtn);

        modalCard.getChildren().addAll(header, body, footer);
        overlay.getChildren().add(modalCard);
        windowRoot.getChildren().add(overlay);

        modalCard.setScaleX(0.5);
        modalCard.setScaleY(0.5);
        modalCard.setOpacity(0);
        overlay.setOpacity(0);

        ScaleTransition stIn = new ScaleTransition(Duration.millis(300), modalCard);
        stIn.setToX(1); stIn.setToY(1); stIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ftIn = new FadeTransition(Duration.millis(300), modalCard);
        ftIn.setToValue(1);

        FadeTransition bgFtIn = new FadeTransition(Duration.millis(300), overlay);
        bgFtIn.setToValue(1);

        ParallelTransition ptIn = new ParallelTransition(stIn, ftIn, bgFtIn);
        ptIn.play();

        StackPane finalWindowRoot = windowRoot;
        Runnable closeAction = () -> {
            ScaleTransition stOut = new ScaleTransition(Duration.millis(200), modalCard);
            stOut.setToX(0.5); stOut.setToY(0.5); stOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition ftOut = new FadeTransition(Duration.millis(200), modalCard);
            ftOut.setToValue(0);

            FadeTransition bgFtOut = new FadeTransition(Duration.millis(200), overlay);
            bgFtOut.setToValue(0);

            ParallelTransition ptOut = new ParallelTransition(stOut, ftOut, bgFtOut);
            ptOut.setOnFinished(ev -> {
                finalWindowRoot.getChildren().remove(overlay);
                rootPane.setEffect(null);
            });
            ptOut.play();
        };

        closeBtn.setOnMouseClicked(e -> closeAction.run());
        okBtn.setOnAction(e -> closeAction.run());
    }
}