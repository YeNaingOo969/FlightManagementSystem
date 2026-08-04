package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class StaffPassengerDetailsController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtFullName;
    @FXML private TextField txtNrcPassport;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cmbNationality;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;

    @FXML private Label lblFlightNo;
    @FXML private Label lblFrom;
    @FXML private Label lblTo;
    @FXML private Label lblDeparture;
    @FXML private Label lblTotalPrice;

    @FXML private Button btnBack;
    @FXML private Button btnContinue;

    private String currentFlightNo;
    private String currentFrom;
    private String currentTo;
    private String currentDate;
    private String currentPrice;
    private int currentPassengers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> genderOptions = FXCollections.observableArrayList("Male", "Female");
        cmbGender.setItems(genderOptions);
        cmbGender.getSelectionModel().selectFirst();

        ObservableList<String> nationalityOptions = FXCollections.observableArrayList("Myanmar", "Foreigner");
        cmbNationality.setItems(nationalityOptions);
        cmbNationality.getSelectionModel().selectFirst();
    }

    public void setFlightData(String flightNo, String from, String to, String date, String price, int passengers) {
        this.currentFlightNo = flightNo;
        this.currentFrom = from;
        this.currentTo = to;
        this.currentDate = date;
        this.currentPrice = price;
        this.currentPassengers = passengers;

        if (lblFlightNo != null) lblFlightNo.setText(flightNo);
        if (lblFrom != null) lblFrom.setText(from);
        if (lblTo != null) lblTo.setText(to);
        if (lblDeparture != null) lblDeparture.setText(date);

        try {
            double basePrice = 980000.0;
            double total = basePrice * passengers;
            if (lblTotalPrice != null) lblTotalPrice.setText(String.format("%,.0f MMK", total));
        } catch (Exception e) {
            if (lblTotalPrice != null) lblTotalPrice.setText("--");
        }
    }

    private void applyThemeRecursively(Parent node, boolean isDarkMode) {
        if (node.getStylesheets() != null && !node.getStylesheets().isEmpty()) {
            ObservableList<String> styles = node.getStylesheets();
            for (int i = 0; i < styles.size(); i++) {
                String css = styles.get(i);
                if (isDarkMode && css.contains("Light")) {
                    styles.set(i, css.replace("Light", "Dark"));
                } else if (!isDarkMode && css.contains("Dark")) {
                    styles.set(i, css.replace("Dark", "Light"));
                }
            }
        }
        for (javafx.scene.Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                applyThemeRecursively((Parent) child, isDarkMode);
            }
        }
    }

    private boolean checkIsDarkMode() {
        boolean isDarkMode = true;
        Parent current = rootStackPane;
        while (current != null) {
            String currentStyles = current.getStylesheets().toString();
            if (currentStyles.contains("Dark")) {
                return true;
            } else if (currentStyles.contains("Light")) {
                return false;
            }
            current = current.getParent();
        }
        return isDarkMode;
    }

    private void showCustomAlert(boolean isError, String title, String message) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setAlignment(Pos.CENTER);

        VBox dialogBox = new VBox(20);
        dialogBox.setMaxWidth(400);
        dialogBox.setMinHeight(180);
        dialogBox.setMaxHeight(Region.USE_PREF_SIZE);

        String themeColor = isError ? "#FF4B72" : "#F59E0B";

        dialogBox.setStyle("-fx-background-color: #0B0E14; -fx-border-color: " + themeColor + "; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 25px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 5);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: " + themeColor + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button btnCloseX = new Button();
        SVGPath xIcon = new SVGPath();
        xIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        xIcon.setFill(Color.web("#8B95A5"));
        btnCloseX.setGraphic(xIcon);
        btnCloseX.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        btnCloseX.setOnMouseEntered(e -> xIcon.setFill(Color.web("#FF4B72")));
        btnCloseX.setOnMouseExited(e -> xIcon.setFill(Color.web("#8B95A5")));

        header.getChildren().addAll(lblTitle, spacer, btnCloseX);

        HBox body = new HBox(15);
        body.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = new SVGPath();
        if (isError) {
            icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z");
            icon.setFill(Color.web(themeColor));
        } else {
            icon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
            icon.setFill(Color.web(themeColor));
        }
        icon.setScaleX(2.0);
        icon.setScaleY(2.0);

        StackPane iconPane = new StackPane(icon);
        iconPane.setPrefWidth(50);

        Label lblMessage = new Label(message);
        lblMessage.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        lblMessage.setWrapText(true);
        lblMessage.setMaxWidth(300);

        body.getChildren().addAll(iconPane, lblMessage);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnOk = new Button("OK");
        String btnStyle = "-fx-background-color: transparent; -fx-border-color: " + themeColor + "; -fx-text-fill: " + themeColor + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 1.5px; -fx-padding: 8px 25px; -fx-cursor: hand;";
        String btnHoverStyle = "-fx-background-color: " + themeColor + "33; -fx-border-color: " + themeColor + "; -fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 1.5px; -fx-padding: 8px 25px; -fx-cursor: hand;";

        btnOk.setStyle(btnStyle);
        btnOk.setOnMouseEntered(e -> btnOk.setStyle(btnHoverStyle));
        btnOk.setOnMouseExited(e -> btnOk.setStyle(btnStyle));

        footer.getChildren().add(btnOk);

        dialogBox.getChildren().addAll(header, body, footer);
        overlay.getChildren().add(dialogBox);
        rootStackPane.getChildren().add(overlay);

        GaussianBlur blur = new GaussianBlur(0);
        mainContainer.setEffect(blur);
        Timeline blurTimeline = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 15)));
        blurTimeline.play();

        dialogBox.setScaleX(0.7);
        dialogBox.setScaleY(0.7);
        dialogBox.setOpacity(0);

        ScaleTransition stIn = new ScaleTransition(Duration.millis(300), dialogBox);
        stIn.setToX(1.0);
        stIn.setToY(1.0);

        FadeTransition ftIn = new FadeTransition(Duration.millis(300), dialogBox);
        ftIn.setToValue(1.0);

        ParallelTransition ptIn = new ParallelTransition(stIn, ftIn);
        ptIn.setInterpolator(Interpolator.EASE_OUT);
        ptIn.play();

        Runnable closeAction = () -> {
            Timeline blurOut = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 0)));
            blurOut.setOnFinished(e -> mainContainer.setEffect(null));
            blurOut.play();

            ScaleTransition stOut = new ScaleTransition(Duration.millis(300), dialogBox);
            stOut.setToX(0.7);
            stOut.setToY(0.7);

            FadeTransition ftOut = new FadeTransition(Duration.millis(300), dialogBox);
            ftOut.setToValue(0.0);

            ParallelTransition ptOut = new ParallelTransition(stOut, ftOut);
            ptOut.setInterpolator(Interpolator.EASE_IN);
            ptOut.setOnFinished(e -> rootStackPane.getChildren().remove(overlay));
            ptOut.play();
        };

        btnCloseX.setOnAction(e -> closeAction.run());
        btnOk.setOnAction(e -> closeAction.run());
    }

    @FXML
    void clickBack(ActionEvent event) {
        navigateTo("/com/pos/flightmanagementsystem/view/StaffCreateNewBooking.fxml", true);
    }

    @FXML
    void clickContinue(ActionEvent event) {
        String gender = cmbGender.getValue();
        String fullName = txtFullName.getText();
        String nrcPassport = txtNrcPassport.getText();
        LocalDate dob = dpDateOfBirth.getValue();
        String nationality = cmbNationality.getValue();
        String phone = txtPhone.getText();
        String email = txtEmail.getText();

        if (fullName == null || fullName.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                dob == null || gender == null || nationality == null ||
                nrcPassport == null || nrcPassport.trim().isEmpty()) {
            showCustomAlert(false, "Incomplete Data", "Please fill in all the passenger's details completely.");
            return;
        }

        String query = "INSERT INTO passengers (full_name, gender, nrc_passport_no, dob, nationality, phone, email) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database Connection not available.");

            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, fullName);
                pstmt.setString(2, gender);
                pstmt.setString(3, nrcPassport);
                pstmt.setDate(4, java.sql.Date.valueOf(dob));
                pstmt.setString(5, nationality);
                pstmt.setString(6, phone);
                pstmt.setString(7, email);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    int newPassengerId = -1;
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newPassengerId = generatedKeys.getInt(1);
                        }
                    }
                    loadSeatSelectionPage(newPassengerId);
                } else {
                    throw new Exception("Failed to save Passenger Data.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showCustomAlert(true, "Database Error", "An error occurred while saving passenger data.\nError: " + e.getMessage());
            });
        }
    }

    private void loadSeatSelectionPage(int newPassengerId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffSeatSelection.fxml"));
            Parent nextView = loader.load();

            applyThemeRecursively(nextView, checkIsDarkMode());

            StaffSeatSelectionController controller = loader.getController();

            // 🌟 1. Flight Data ၇ ခုကို လှမ်းပို့မည် (Error ကင်းသွားပါပြီ) 🌟
            controller.setFlightData(
                    currentFlightNo,
                    currentFrom,
                    currentTo,
                    currentDate,
                    currentPrice,
                    currentPassengers,
                    txtFullName.getText()
            );

            // 🌟 2. Database တွင် သိမ်းရန် ID များကို သီးသန့် လှမ်းပို့မည် 🌟
            int staffId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;
            controller.setBookingIds(newPassengerId, staffId);

            nextView.setScaleX(0.8);
            nextView.setScaleY(0.8);
            nextView.setOpacity(0.0);

            rootStackPane.getChildren().add(nextView);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer);
            scaleOut.setToX(1.2);
            scaleOut.setToY(1.2);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
            pt.setInterpolator(Interpolator.EASE_BOTH);

            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer));
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, boolean isBack) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent nextView = loader.load();

            applyThemeRecursively(nextView, checkIsDarkMode());

            if (isBack) {
                nextView.setScaleX(1.2);
                nextView.setScaleY(1.2);
            }
            nextView.setOpacity(0.0);
            rootStackPane.getChildren().add(nextView);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer);
            if (isBack) {
                scaleOut.setToX(0.8);
                scaleOut.setToY(0.8);
            }
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
            pt.setInterpolator(Interpolator.EASE_BOTH);

            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer));
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}