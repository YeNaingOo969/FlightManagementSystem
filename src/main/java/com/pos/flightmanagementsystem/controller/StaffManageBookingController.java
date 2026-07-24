package com.pos.flightmanagementsystem.controller;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class StaffManageBookingController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;
    @FXML private StackPane waitingModal;

    // 🌟 လည်နေမည့် Icon အတွက်
    @FXML private SVGPath modalSpinnerIcon;
    private RotateTransition spinTransition;

    // 🌟 မည်သည့် Booking ကို Cancel လုပ်နေသည်ကို မှတ်ထားရန်
    private BookingData pendingBookingData = null;

    // --- Table Elements ---
    @FXML private TableView<BookingData> bookingTable;
    @FXML private TableColumn<BookingData, String> colBookingId;
    @FXML private TableColumn<BookingData, String> colPassenger;
    @FXML private TableColumn<BookingData, String> colRoute;
    @FXML private TableColumn<BookingData, String> colDate;
    @FXML private TableColumn<BookingData, String> colStatus;
    @FXML private TableColumn<BookingData, Void> colAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 🌟 1. Modal ထဲက အဝိုင်း Icon လေးကို လည်စေမည့် Animation (Rotate) သတ်မှတ်ခြင်း
        spinTransition = new RotateTransition(Duration.seconds(1.5), modalSpinnerIcon);
        spinTransition.setByAngle(360); // တစ်ပတ်အပြည့်လည်မည်
        spinTransition.setCycleCount(Animation.INDEFINITE); // မရပ်မချင်း ဆက်လည်နေမည်
        spinTransition.setInterpolator(Interpolator.LINEAR); // ညီညာစွာလည်ရန်

        // ၂။ Column များနှင့် Data Model ချိတ်ဆက်ခြင်း
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passenger"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // ၃။ Action Column တွင် Button နှင့် Loading Spinner ဖန်တီးထည့်သွင်းခြင်း
        Callback<TableColumn<BookingData, Void>, TableCell<BookingData, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<BookingData, Void> call(final TableColumn<BookingData, Void> param) {
                return new TableCell<>() {
                    // Normal Cancel Button
                    private final Button btn = new Button("Cancel");

                    // Waiting Spinner UI (Progress Indicator + Text)
                    private final HBox waitingBox = new HBox(8);
                    private final ProgressIndicator spinner = new ProgressIndicator();
                    private final Label lblWaiting = new Label("Waiting");

                    {
                        // Setup Button
                        btn.getStyleClass().add("btn-cancel-table");
                        btn.setOnAction((ActionEvent event) -> {
                            // 🌟 နှိပ်လိုက်သော Row ၏ Data ကို မှတ်သားထားမည်
                            pendingBookingData = getTableView().getItems().get(getIndex());
                            openWaitingModal();
                        });

                        // Setup Waiting UI
                        spinner.setPrefSize(16, 16);
                        spinner.setStyle("-fx-progress-color: #F59E0B;"); // လိမ္မော်ရောင်
                        lblWaiting.getStyleClass().add("waiting-text");
                        waitingBox.setAlignment(Pos.CENTER_LEFT);
                        waitingBox.getChildren().addAll(spinner, lblWaiting);

                        setAlignment(Pos.CENTER_LEFT);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BookingData data = getTableView().getItems().get(getIndex());
                            // 🌟 Data တွင် cancelPending ဖြစ်နေပါက Spinner ပြမည်၊ မဟုတ်လျှင် Button ပြမည်
                            if (data != null && data.isCancelPending()) {
                                setGraphic(waitingBox);
                            } else {
                                setGraphic(btn);
                            }
                        }
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);

        // ၄။ Table ထဲသို့ Sample Data များ ထည့်သွင်းခြင်း
        ObservableList<BookingData> dummyData = FXCollections.observableArrayList(
                new BookingData("BK-202601", "John Doe", "FL-101 (YGN-BKK)", "2026-07-24", "Confirmed"),
                new BookingData("BK-202602", "Alice Smith", "FL-102 (YGN-MDL)", "2026-07-25", "Pending"),
                new BookingData("BK-202603", "Bob Johnson", "FL-103 (MDL-HEH)", "2026-07-26", "Confirmed"),
                new BookingData("BK-202604", "Emma Davis", "FL-104 (YGN-NYT)", "2026-07-27", "Confirmed")
        );
        bookingTable.setItems(dummyData);
    }

    // ==========================================
    // 🌟 WAITING MODAL ဖွင့်မည့် လုပ်ဆောင်ချက်
    // ==========================================
    private void openWaitingModal() {
        waitingModal.setVisible(true);
        spinTransition.play(); // 🌟 အဝိုင်းစလည်မည်

        GaussianBlur blur = new GaussianBlur(0);
        mainContainer.setEffect(blur);
        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 15))
        );
        blurTimeline.play();

        waitingModal.setScaleX(0.5);
        waitingModal.setScaleY(0.5);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), waitingModal);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), waitingModal);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn);
        pt.setInterpolator(Interpolator.EASE_OUT);
        pt.play();
    }

    // ==========================================
    // 🌟 WAITING MODAL ပိတ်မည့် လုပ်ဆောင်ချက် (မစောင့်နိုင်၍ ထွက်လျှင် Table တွင် Spinner ပေါ်ရန်)
    // ==========================================
    @FXML
    void closeWaitingModal(ActionEvent event) {

        // 🌟 1. Table တွင် Pending State သို့ ပြောင်းပြီး Refresh လုပ်မည်
        if (pendingBookingData != null) {
            pendingBookingData.setCancelPending(true);
            bookingTable.refresh();
            pendingBookingData = null; // Reset လုပ်ထားမည်
        }

        // 2. Background Blur ပြန်ကြည်လင်စေမည်
        if (mainContainer.getEffect() instanceof GaussianBlur) {
            GaussianBlur blur = (GaussianBlur) mainContainer.getEffect();
            Timeline blurOut = new Timeline(
                    new KeyFrame(Duration.millis(250), new KeyValue(blur.radiusProperty(), 0))
            );
            blurOut.setOnFinished(e -> mainContainer.setEffect(null));
            blurOut.play();
        }

        // 3. Modal ပျောက်သွားမည်
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), waitingModal);
        scaleDown.setToX(0.5);
        scaleDown.setToY(0.5);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), waitingModal);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setInterpolator(Interpolator.EASE_IN);
        pt.setOnFinished(e -> {
            waitingModal.setVisible(false);
            spinTransition.stop(); // 🌟 ပျောက်သွားချိန်တွင် အဝိုင်းလည်ခြင်းကိုပါ ရပ်ထားမည်
        });
        pt.play();
    }

    // ==========================================
    // 🌟 Sample Data Model (cancelPending ထပ်တိုးထားသည်)
    // ==========================================
    public static class BookingData {
        private String bookingId, passenger, route, date, status;
        private boolean cancelPending = false; // 🌟 ဤ Property အသစ် ထပ်တိုးထားပါသည်

        public BookingData(String bookingId, String passenger, String route, String date, String status) {
            this.bookingId = bookingId;
            this.passenger = passenger;
            this.route = route;
            this.date = date;
            this.status = status;
        }

        public String getBookingId() { return bookingId; }
        public String getPassenger() { return passenger; }
        public String getRoute() { return route; }
        public String getDate() { return date; }
        public String getStatus() { return status; }

        public boolean isCancelPending() { return cancelPending; }
        public void setCancelPending(boolean cancelPending) { this.cancelPending = cancelPending; }
    }
}