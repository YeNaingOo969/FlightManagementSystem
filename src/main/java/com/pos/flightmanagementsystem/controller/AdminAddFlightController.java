package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Flight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AdminAddFlightController implements Initializable {

    @FXML private BorderPane rootPane;

    @FXML private TextField txtFlightNumber;
    @FXML private TextField txtAirline;

    @FXML private ComboBox<String> cmbDepartureCity;
    @FXML private ComboBox<String> cmbArrivalCity;

    @FXML private DatePicker dpDepartureDate;
    @FXML private ComboBox<String> cmbDepartureHour;
    @FXML private ComboBox<String> cmbDepartureMinute;

    @FXML private DatePicker dpArrivalDate;
    @FXML private ComboBox<String> cmbArrivalHour;
    @FXML private ComboBox<String> cmbArrivalMinute;

    @FXML private TextField txtTotalSeats;
    @FXML private TextField txtBasePrice;
    @FXML private ComboBox<String> cmbStatus;

    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private Integer currentFlightId = null;

    private Map<String, Integer> cityNameToIdMap = new HashMap<>();
    private Map<Integer, String> cityIdToNameMap = new HashMap<>();
    private ObservableList<String> allCitiesList = FXCollections.observableArrayList();

    private boolean isUpdatingCities = false;

    // 🌟 Message ပေးပို့နိုင်ရန် Consumer ကို အသုံးပြုထားသည် 🌟
    private Consumer<String> saveCallback;
    private Runnable cancelCallback;

    public void setSaveCallback(Consumer<String> saveCallback) {
        this.saveCallback = saveCallback;
    }

    public void setCancelCallback(Runnable cancelCallback) {
        this.cancelCallback = cancelCallback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTimeComboBoxes();
        setupCityComboBoxes();
        setupStatusComboBox();
        setupDateValidations();
    }

    public void setFlightData(Flight flight) {
        this.currentFlightId = flight.getFlightId();

        txtFlightNumber.setText(flight.getFlightNumber());
        txtAirline.setText(flight.getAirline());

        cmbDepartureCity.setValue(getCityName(flight.getDepartureCityId()));
        cmbArrivalCity.setValue(getCityName(flight.getArrivalCityId()));

        dpDepartureDate.setValue(flight.getDepartureTime().toLocalDate());
        cmbDepartureHour.setValue(String.format("%02d", flight.getDepartureTime().getHour()));
        cmbDepartureMinute.setValue(String.format("%02d", flight.getDepartureTime().getMinute()));

        dpArrivalDate.setValue(flight.getArrivalTime().toLocalDate());
        cmbArrivalHour.setValue(String.format("%02d", flight.getArrivalTime().getHour()));
        cmbArrivalMinute.setValue(String.format("%02d", flight.getArrivalTime().getMinute()));

        txtTotalSeats.setText(String.valueOf(flight.getTotalSeats()));
        txtBasePrice.setText(String.valueOf(flight.getBasePrice()));
        cmbStatus.setValue(flight.getStatus());

        btnSave.setText("Update Flight");
    }

    private void setupTimeComboBoxes() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i <= 23; i++) hours.add(String.format("%02d", i));
        cmbDepartureHour.setItems(hours);
        cmbArrivalHour.setItems(hours);

        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) minutes.add(String.format("%02d", i));

        cmbDepartureMinute.setItems(minutes);
        cmbArrivalMinute.setItems(minutes);
    }

    private void setupCityComboBoxes() {
        allCitiesList.clear();
        String query = "SELECT city_id, city_name, city_code FROM cities";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("city_id");
                String name = rs.getString("city_name");
                String code = rs.getString("city_code");

                String displayString = name + " (" + code + ")";

                cityNameToIdMap.put(displayString, id);
                cityIdToNameMap.put(id, displayString);

                allCitiesList.add(displayString);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cmbDepartureCity.setItems(FXCollections.observableArrayList(allCitiesList));
        cmbArrivalCity.setItems(FXCollections.observableArrayList(allCitiesList));

        cmbDepartureCity.valueProperty().addListener((observable, oldValue, newValue) -> updateCityComboBoxes());
        cmbArrivalCity.valueProperty().addListener((observable, oldValue, newValue) -> updateCityComboBoxes());
    }

    private void updateCityComboBoxes() {
        if (isUpdatingCities) return;
        isUpdatingCities = true;

        String currentDep = cmbDepartureCity.getValue();
        String currentArr = cmbArrivalCity.getValue();

        ObservableList<String> depList = FXCollections.observableArrayList(allCitiesList);
        ObservableList<String> arrList = FXCollections.observableArrayList(allCitiesList);

        if (currentArr != null) depList.remove(currentArr);
        if (currentDep != null) arrList.remove(currentDep);

        cmbDepartureCity.setItems(depList);
        cmbArrivalCity.setItems(arrList);

        cmbDepartureCity.setValue(currentDep);
        cmbArrivalCity.setValue(currentArr);

        isUpdatingCities = false;
    }

    private void setupStatusComboBox() {
        ObservableList<String> statusList = FXCollections.observableArrayList(
                "Scheduled", "Delayed", "Cancelled", "Boarding"
        );
        cmbStatus.setItems(statusList);
    }

    private void setupDateValidations() {
        dpDepartureDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });

        dpArrivalDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate minDate = dpDepartureDate.getValue() != null ? dpDepartureDate.getValue() : LocalDate.now();
                setDisable(empty || date.compareTo(minDate) < 0);
            }
        });

        dpDepartureDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && dpArrivalDate.getValue() != null) {
                if (dpArrivalDate.getValue().isBefore(newValue)) {
                    dpArrivalDate.setValue(null);
                }
            }
        });
    }

    private int getCityId(String cityName) {
        if (cityName == null || !cityNameToIdMap.containsKey(cityName)) return 0;
        return cityNameToIdMap.get(cityName);
    }

    private String getCityName(int cityId) {
        return cityIdToNameMap.getOrDefault(cityId, null);
    }

    @FXML
    void clickSave(ActionEvent event) {
        if (txtFlightNumber.getText().isEmpty() || txtAirline.getText().isEmpty() ||
                cmbDepartureCity.getValue() == null || cmbArrivalCity.getValue() == null ||
                dpDepartureDate.getValue() == null || cmbDepartureHour.getValue() == null || cmbDepartureMinute.getValue() == null ||
                dpArrivalDate.getValue() == null || cmbArrivalHour.getValue() == null || cmbArrivalMinute.getValue() == null ||
                txtTotalSeats.getText().isEmpty() || txtBasePrice.getText().isEmpty() || cmbStatus.getValue() == null) {

            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
            return;
        }

        try {
            String flightNumber = txtFlightNumber.getText();
            String airline = txtAirline.getText();
            int departureCityId = getCityId(cmbDepartureCity.getValue());
            int arrivalCityId = getCityId(cmbArrivalCity.getValue());

            String departureDateTime = dpDepartureDate.getValue() + " " + cmbDepartureHour.getValue() + ":" + cmbDepartureMinute.getValue() + ":00";
            String arrivalDateTime = dpArrivalDate.getValue() + " " + cmbArrivalHour.getValue() + ":" + cmbArrivalMinute.getValue() + ":00";

            int totalSeats = Integer.parseInt(txtTotalSeats.getText());
            double basePrice = Double.parseDouble(txtBasePrice.getText());
            String status = cmbStatus.getValue();

            String sql;
            if (currentFlightId == null) {
                sql = "INSERT INTO flights (flight_number, airline, departure_city_id, arrival_city_id, departure_time, arrival_time, total_seats, base_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE flights SET flight_number=?, airline=?, departure_city_id=?, arrival_city_id=?, departure_time=?, arrival_time=?, total_seats=?, base_price=?, status=? WHERE flight_id=?";
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    showAlert(Alert.AlertType.ERROR, "Connection Error", "Failed to connect to the database.");
                    return;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, flightNumber);
                    pstmt.setString(2, airline);
                    pstmt.setInt(3, departureCityId);
                    pstmt.setInt(4, arrivalCityId);
                    pstmt.setString(5, departureDateTime);
                    pstmt.setString(6, arrivalDateTime);
                    pstmt.setInt(7, totalSeats);
                    pstmt.setDouble(8, basePrice);
                    pstmt.setString(9, status);

                    if (currentFlightId != null) {
                        pstmt.setInt(10, currentFlightId);
                    }

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        // 🌟 Default Alert အကြီးကြီးကို ဖယ်ရှားပြီး မိခင် Form ဆီသို့ Message ပေးပို့ပါမည် 🌟
                        String msg = (currentFlightId == null) ? "Flight added successfully." : "Flight updated successfully.";
                        if (saveCallback != null) {
                            saveCallback.accept(msg);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Action failed: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Total Seats and Base Price must be numbers.");
        }
    }

    @FXML
    void clickCancel(ActionEvent event) {
        if (cancelCallback != null) {
            cancelCallback.run();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}