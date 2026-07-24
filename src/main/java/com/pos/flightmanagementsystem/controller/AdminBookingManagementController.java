package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminBookingManagementController implements Initializable {

    @FXML private StackPane rootStackPane;

    // --- Table Elements ---
    @FXML private TableView<BookingDummy> bookingTable;
    @FXML private TableColumn<BookingDummy, String> colBookingId;
    @FXML private TableColumn<BookingDummy, String> colCustomer;
    @FXML private TableColumn<BookingDummy, String> colFlight;
    @FXML private TableColumn<BookingDummy, String> colDate;
    @FXML private TableColumn<BookingDummy, String> colStatus;
    @FXML private TableColumn<BookingDummy, Void> colAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ၁။ Column များနှင့် Data Model ချိတ်ဆက်ခြင်း
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colFlight.setCellValueFactory(new PropertyValueFactory<>("flight"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // ၂။ Action Column တွင် "Cancel" Button လေးများ ဖန်တီးထည့်သွင်းခြင်း
        Callback<TableColumn<BookingDummy, Void>, TableCell<BookingDummy, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<BookingDummy, Void> call(final TableColumn<BookingDummy, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Cancel");

                    {
                        // CSS မှ btn-cancel-table class ကို ခေါ်သုံးထားသည်
                        btn.getStyleClass().add("btn-cancel-table");

                        // 🌟 Button ကို Cell ၏ အလယ်တည့်တည့်တွင် ပေါ်စေရန် Alignment ချိန်ခြင်း
                        setAlignment(Pos.CENTER);

                        btn.setOnAction((ActionEvent event) -> {
                            BookingDummy data = getTableView().getItems().get(getIndex());
                            System.out.println("Attempting to cancel Booking: " + data.getBookingId());

                            // Cancel Button ကို နှိပ်လျှင် Modal လှမ်းခေါ်ပါမည်
                            openCancelModal();
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);

        // ၃။ Table ထဲသို့ Sample Data များ ထည့်သွင်းခြင်း
        ObservableList<BookingDummy> dummyData = FXCollections.observableArrayList(
                new BookingDummy("BK-202601", "John Doe", "FL-101 (YGN-MDL)", "2026-07-24", "Confirmed"),
                new BookingDummy("BK-202602", "Alice Smith", "FL-102 (YGN-NYU)", "2026-07-25", "Pending"),
                new BookingDummy("BK-202603", "Bob Johnson", "FL-103 (MDL-HEH)", "2026-07-26", "Confirmed"),
                new BookingDummy("BK-202604", "Emma Davis", "FL-104 (YGN-NYT)", "2026-07-27", "Confirmed")
        );
        bookingTable.setItems(dummyData);
    }

    // ==========================================
    // 🌟 CANCEL MODAL ဖွင့်မည့် HELPER METHOD (Animation ပါဝင်သည်)
    // ==========================================
    private void openCancelModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminCancelBookingReason.fxml"));
            Parent modalView = loader.load();

            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

            // အနောက်ကနေ အရှေ့ကို Pop-up ထွက်လာရန် စစချင်း သေးထားမည် (Scale 0.5)
            modalView.setScaleX(0.5);
            modalView.setScaleY(0.5);

            rootStackPane.getChildren().add(overlay);

            // Zoom In ဖြစ်လာမည့် Animation (0.5 ကနေ 1.0 သို့)
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            // မှေးမှိန်ရာမှ လင်းလာမည့် Animation (Fade In)
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn);
            pt.setInterpolator(Interpolator.EASE_OUT); // အထွက်မြန်ပြီး အဆုံးသတ်ငြိမ့်သွားရန်
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load AdminCancelBookingReason.fxml");
        }
    }

    // ==========================================
    // 🌟 ယာယီအသုံးပြုရန် Sample Data Model Class
    // ==========================================
    public static class BookingDummy {
        private String bookingId, customer, flight, date, status;

        public BookingDummy(String bookingId, String customer, String flight, String date, String status) {
            this.bookingId = bookingId;
            this.customer = customer;
            this.flight = flight;
            this.date = date;
            this.status = status;
        }

        public String getBookingId() { return bookingId; }
        public String getCustomer() { return customer; }
        public String getFlight() { return flight; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
    }
}