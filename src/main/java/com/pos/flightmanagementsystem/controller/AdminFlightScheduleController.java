package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Flight;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminFlightScheduleController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private Button btnAddFlight;
    @FXML private DatePicker dpFilterDate;

    @FXML private TableView<Flight> tblFlights;
    @FXML private TableColumn<Flight, String> colFlightNo;
    @FXML private TableColumn<Flight, String> colAirline;
    @FXML private TableColumn<Flight, String> colFrom;
    @FXML private TableColumn<Flight, String> colTo;
    @FXML private TableColumn<Flight, String> colDeparture;
    @FXML private TableColumn<Flight, String> colArrival;
    @FXML private TableColumn<Flight, Void> colActions;

    private ObservableList<Flight> flightList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadFlightData();

        if (dpFilterDate != null) {
            dpFilterDate.valueProperty().addListener((observable, oldValue, newValue) -> {
                loadFlightData();
            });
        }
    }

    private void setupTableColumns() {
        colFlightNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFlightNumber()));
        colAirline.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAirline()));
        colFrom.setCellValueFactory(cellData -> new SimpleStringProperty(getCityName(cellData.getValue().getDepartureCityId())));
        colTo.setCellValueFactory(cellData -> new SimpleStringProperty(getCityName(cellData.getValue().getArrivalCityId())));
        colDeparture.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartureTime().format(formatter)));
        colArrival.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getArrivalTime().format(formatter)));

        setupActionColumn();
    }

    private void loadFlightData() {
        flightList.clear();
        LocalDate selectedDate = (dpFilterDate != null) ? dpFilterDate.getValue() : null;
        String sql = "SELECT * FROM flights";

        if (selectedDate != null) sql += " WHERE DATE(departure_time) = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (selectedDate != null) {
                    pstmt.setDate(1, java.sql.Date.valueOf(selectedDate));
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Flight flight = new Flight(
                                rs.getInt("flight_id"),
                                rs.getString("flight_number"),
                                rs.getString("airline"),
                                rs.getInt("departure_city_id"),
                                rs.getInt("arrival_city_id"),
                                rs.getTimestamp("departure_time").toLocalDateTime(),
                                rs.getTimestamp("arrival_time").toLocalDateTime(),
                                rs.getInt("total_seats"),
                                rs.getDouble("base_price"),
                                rs.getString("status")
                        );
                        flightList.add(flight);
                    }
                    tblFlights.setItems(flightList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getCityName(int cityId) {
        switch (cityId) {
            case 1: return "Yangon (YGN)";
            case 2: return "Mandalay (MDL)";
            case 3: return "Bagan (NYU)";
            case 4: return "Naypyidaw (NYT)";
            case 5: return "Heho (HEH)";
            default: return "Unknown";
        }
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");

                deleteBtn.setOnAction(event -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    deleteFlight(flight);
                });

                editBtn.setOnAction(event -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    openFlightModal(flight);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void deleteFlight(Flight flight) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Flight: " + flight.getFlightNumber());
        confirmAlert.setContentText("Are you sure you want to delete this flight data?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM flights WHERE flight_id = ?";
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) return;
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, flight.getFlightId());
                    if (pstmt.executeUpdate() > 0) {
                        flightList.remove(flight);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void clickAddFlight(ActionEvent event) {
        openFlightModal(null);
    }

    private void openFlightModal(Flight flightToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminAddFlight.fxml"));
            Parent modalView = loader.load();

            // 🌟 Controller ကိုယူပြီး Callback လှမ်းချိတ်ပါမည်
            AdminAddFlightController controller = loader.getController();

            if (flightToEdit != null) {
                controller.setFlightData(flightToEdit);
            }

            // 🌟 Save / Update အောင်မြင်သွားပါက Table ကို ချက်ချင်း Refresh လုပ်ပေးပါမည်
            controller.setSaveCallback(() -> {
                loadFlightData();
            });

            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

            Bounds buttonBounds = btnAddFlight.localToScene(btnAddFlight.getBoundsInLocal());
            double rootCenterX = rootStackPane.getWidth() / 2;
            double rootCenterY = rootStackPane.getHeight() / 2;
            double buttonCenterX = buttonBounds.getMinX() + (buttonBounds.getWidth() / 2);
            double buttonCenterY = buttonBounds.getMinY() + (buttonBounds.getHeight() / 2);

            modalView.setTranslateX(buttonCenterX - rootCenterX);
            modalView.setTranslateY(buttonCenterY - rootCenterY);
            modalView.setScaleX(0.0);
            modalView.setScaleY(0.0);

            rootStackPane.getChildren().add(overlay);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(350), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(350), modalView);
            moveCenter.setToX(0);
            moveCenter.setToY(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, moveCenter, fadeIn);
            pt.setInterpolator(Interpolator.EASE_OUT);
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}