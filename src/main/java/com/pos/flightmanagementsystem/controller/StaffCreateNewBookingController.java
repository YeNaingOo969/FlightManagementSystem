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
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class StaffCreateNewBookingController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer; // လက်ရှိ Page ၏ VBox

    @FXML private TableView<FlightDummy> flightsTable;
    @FXML private TableColumn<FlightDummy, String> colFlightNo;
    @FXML private TableColumn<FlightDummy, String> colFrom;
    @FXML private TableColumn<FlightDummy, String> colTo;
    @FXML private TableColumn<FlightDummy, String> colDate;
    @FXML private TableColumn<FlightDummy, String> colTime;
    @FXML private TableColumn<FlightDummy, String> colPrice;
    @FXML private TableColumn<FlightDummy, Void> colAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        colFlightNo.setCellValueFactory(new PropertyValueFactory<>("flightNo"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("to"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Choose Button ထည့်သွင်းခြင်း
        Callback<TableColumn<FlightDummy, Void>, TableCell<FlightDummy, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<FlightDummy, Void> call(final TableColumn<FlightDummy, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Choose");

                    {
                        btn.getStyleClass().add("btn-choose");
                        setAlignment(Pos.CENTER);

                        btn.setOnAction((ActionEvent event) -> {
                            FlightDummy selectedFlight = getTableView().getItems().get(getIndex());
                            System.out.println("Flight Selected: " + selectedFlight.getFlightNo());

                            // 🌟 Page ကူးမည့် Animation ကို ခေါ်ပါမည်
                            loadPassengerDetailsPage();
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

        // Sample Data
        ObservableList<FlightDummy> dummyData = FXCollections.observableArrayList(
                new FlightDummy("FL-101", "Yangon (YGN)", "Mandalay (MDL)", "2026-07-25", "08:00 AM", "$ 120.00"),
                new FlightDummy("FL-102", "Yangon (YGN)", "Bagan (NYU)", "2026-07-25", "10:30 AM", "$ 115.00"),
                new FlightDummy("FL-103", "Mandalay (MDL)", "Heho (HEH)", "2026-07-26", "13:00 PM", "$ 95.00"),
                new FlightDummy("FL-104", "Yangon (YGN)", "Naypyidaw (NYT)", "2026-07-27", "16:45 PM", "$ 105.00")
        );
        flightsTable.setItems(dummyData);
    }

    // ==========================================
    // 🌟 ANIMATED PAGE TRANSITION (Zoom Out & Zoom In)
    // ==========================================
    private void loadPassengerDetailsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffPassengerDetails.fxml"));
            Parent nextView = loader.load();

            // Page အသစ်ကို အနောက်မှာ သေးသေးလေး အရင်ထားမည်
            nextView.setScaleX(0.8);
            nextView.setScaleY(0.8);
            nextView.setOpacity(0.0);

            rootStackPane.getChildren().add(nextView);

            // ၁။ လက်ရှိ Page ကို အနောက်သို့ ဝင်သွားစေခြင်း (Zoom Out)
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer);
            scaleOut.setToX(0.8);
            scaleOut.setToY(0.8);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0.0);

            // ၂။ Page အသစ်ကို အနောက်မှ အရှေ့သို့ ထွက်လာစေခြင်း (Zoom In)
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
            pt.setInterpolator(Interpolator.EASE_BOTH);

            // Animation ပြီးဆုံးပါက လက်ရှိ Page ကို ဖျက်ပစ်ပါမည်
            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer));
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load StaffPassengerDetails.fxml");
        }
    }

    public static class FlightDummy {
        private String flightNo, from, to, date, time, price;
        public FlightDummy(String flightNo, String from, String to, String date, String time, String price) {
            this.flightNo = flightNo; this.from = from; this.to = to; this.date = date; this.time = time; this.price = price;
        }
        public String getFlightNo() { return flightNo; }
        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getPrice() { return price; }
    }
}