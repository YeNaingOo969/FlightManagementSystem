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
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminBookingManagementController implements Initializable {

    @FXML private StackPane rootStackPane;

    @FXML private TableView<BookingDummy> bookingTable;
    @FXML private TableColumn<BookingDummy, String> colBookingId;
    @FXML private TableColumn<BookingDummy, String> colCustomer;
    @FXML private TableColumn<BookingDummy, String> colFlight;
    @FXML private TableColumn<BookingDummy, String> colDate;
    @FXML private TableColumn<BookingDummy, String> colStatus;
    @FXML private TableColumn<BookingDummy, Void> colAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colFlight.setCellValueFactory(new PropertyValueFactory<>("flight"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        Callback<TableColumn<BookingDummy, Void>, TableCell<BookingDummy, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<BookingDummy, Void> call(final TableColumn<BookingDummy, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Cancel");

                    {
                        btn.getStyleClass().add("btn-cancel-table");
                        setAlignment(Pos.CENTER);

                        btn.setOnAction((ActionEvent event) -> {
                            BookingDummy data = getTableView().getItems().get(getIndex());
                            System.out.println("Attempting to cancel Booking: " + data.getBookingId());
                            openCancelModal(btn);
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

        ObservableList<BookingDummy> dummyData = FXCollections.observableArrayList(
                new BookingDummy("BK-202601", "John Doe", "FL-101 (YGN-MDL)", "2026-07-24", "Confirmed"),
                new BookingDummy("BK-202602", "Alice Smith", "FL-102 (YGN-NYU)", "2026-07-25", "Pending"),
                new BookingDummy("BK-202603", "Bob Johnson", "FL-103 (MDL-HEH)", "2026-07-26", "Confirmed"),
                new BookingDummy("BK-202604", "Emma Davis", "FL-104 (YGN-NYT)", "2026-07-27", "Confirmed")
        );
        bookingTable.setItems(dummyData);
    }

    private void openCancelModal(Button sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminCancelBookingReason.fxml"));
            Parent modalView = loader.load();

            // Remove the default dark background and center it
            modalView.setStyle("-fx-background-color: transparent; -fx-max-width: USE_PREF_SIZE; -fx-max-height: USE_PREF_SIZE;");

            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

            // Apply blur to the main background
            Node mainContent = rootStackPane.getChildren().get(0);
            BoxBlur blur = new BoxBlur(8, 8, 3);
            mainContent.setEffect(blur);

            // Connect callback to hide overlay properly
            AdminCancelBookingReasonController controller = loader.getController();
            controller.setCancelCallback(() -> closeModal(overlay, mainContent));

            // Optional: Dismiss on clicking the dark background
            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    closeModal(overlay, mainContent);
                }
            });

            rootStackPane.getChildren().add(overlay);

            // Same default animation logic
            modalView.setScaleX(0.5);
            modalView.setScaleY(0.5);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn);
            pt.setInterpolator(Interpolator.EASE_OUT);
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load AdminCancelBookingReason.fxml");
        }
    }

    private void closeModal(StackPane overlay, Node mainContent) {
        Node modalView = overlay.getChildren().get(0);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), modalView);
        scaleDown.setToX(0.5);
        scaleDown.setToY(0.5);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), overlay);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setOnFinished(e -> {
            rootStackPane.getChildren().remove(overlay);
            mainContent.setEffect(null); // Remove the blur
        });
        pt.play();
    }

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