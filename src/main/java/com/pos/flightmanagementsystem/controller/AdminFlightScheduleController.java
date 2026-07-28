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
import javafx.scene.control.DateCell;
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
import java.util.HashMap;
import java.util.Map;
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

    // Database မှ City ID နှင့် City Name ကို တွဲမှတ်ထားရန် Map
    private Map<Integer, String> cityIdToNameMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCitiesToMap();
        setupTableColumns();
        loadFlightData();

        if (dpFilterDate != null) {

            // 🌟 Date Picker တွင် ယနေ့မတိုင်ခင် (အတိတ်) ရက်များကို ရွေးခွင့်မပြုရန် ကန့်သတ်ခြင်း 🌟
            dpFilterDate.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    // ယနေ့ (LocalDate.now()) ထက် ငယ်သောရက်များကို Disable လုပ်ပါမည်
                    setDisable(empty || date.compareTo(LocalDate.now()) < 0);
                }
            });

            // Date Picker မှ ရက်ရွေးလိုက်တိုင်း Table ကို Filter လုပ်ရန်
            dpFilterDate.valueProperty().addListener((observable, oldValue, newValue) -> {
                loadFlightData();
            });
        }
    }

    // ==========================================
    // Database မှ City များကို ဆွဲထုတ်ပြီး Map ထဲသိမ်းခြင်း
    // ==========================================
    private void loadCitiesToMap() {
        cityIdToNameMap.clear();
        String query = "SELECT city_id, city_name, city_code FROM cities";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("city_id");
                String name = rs.getString("city_name");
                String code = rs.getString("city_code");

                // ဥပမာ - "Yangon (YGN)"
                cityIdToNameMap.put(id, name + " (" + code + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to load cities for mapping.");
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
                        // 🌟 Data Type Error မဖြစ်စေရန် Setter များဖြင့် အစားထိုး အသုံးပြုထားပါသည် 🌟
                        Flight flight = new Flight();
                        flight.setFlightId(rs.getInt("flight_id"));
                        flight.setFlightNumber(rs.getString("flight_number"));
                        flight.setAirline(rs.getString("airline"));
                        flight.setDepartureCityId(rs.getInt("departure_city_id"));
                        flight.setArrivalCityId(rs.getInt("arrival_city_id"));

                        // Map မှတဆင့် မြို့နာမည်များကိုပါ တခါတည်း ဆွဲထည့်ပေးထားပါသည်
                        flight.setDepartureCityName(getCityName(rs.getInt("departure_city_id")));
                        flight.setArrivalCityName(getCityName(rs.getInt("arrival_city_id")));

                        flight.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
                        flight.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
                        flight.setTotalSeats(rs.getInt("total_seats"));
                        flight.setBasePrice(rs.getDouble("base_price"));
                        flight.setStatus(rs.getString("status"));

                        // Table List ထဲသို့ ထည့်ခြင်း
                        flightList.add(flight);
                    }
                    // Loop ပြီးသွားမှသာ Table ထဲသို့ Data များ ထည့်သွင်းပါမည်
                    tblFlights.setItems(flightList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ID ကို အသုံးပြု၍ Map မှ City Name ပြန်ရှာခြင်း
    private String getCityName(int cityId) {
        return cityIdToNameMap.getOrDefault(cityId, "Unknown City");
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

            AdminAddFlightController controller = loader.getController();

            if (flightToEdit != null) {
                controller.setFlightData(flightToEdit);
            }

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