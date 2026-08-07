package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Flight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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

    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;

    @FXML private CheckBox chkMon, chkTue, chkWed, chkThu, chkFri, chkSat, chkSun;

    @FXML private ComboBox<String> cmbDepartureHour;
    @FXML private ComboBox<String> cmbDepartureMinute;

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
        // 🌟 Flight တစ်ခုချင်းစီကို Update လုပ်တဲ့အခါ 🌟
        this.currentFlightId = flight.getFlightId();

        txtFlightNumber.setText(flight.getFlightNumber());
        txtAirline.setText(flight.getAirline());

        cmbDepartureCity.setValue(getCityName(flight.getDepartureCityId()));
        cmbArrivalCity.setValue(getCityName(flight.getArrivalCityId()));

        dpStartDate.setValue(flight.getDepartureTime().toLocalDate());
        dpEndDate.setValue(flight.getDepartureTime().toLocalDate());
        dpEndDate.setDisable(true); // Update လုပ်ရာတွင် ရက်စွဲအပိုင်းအခြား ပြောင်းခွင့်မပြုပါ

        cmbDepartureHour.setValue(String.format("%02d", flight.getDepartureTime().getHour()));
        cmbDepartureMinute.setValue(String.format("%02d", flight.getDepartureTime().getMinute()));

        cmbArrivalHour.setValue(String.format("%02d", flight.getArrivalTime().getHour()));
        cmbArrivalMinute.setValue(String.format("%02d", flight.getArrivalTime().getMinute()));

        txtTotalSeats.setText(String.valueOf(flight.getTotalSeats()));
        txtTotalSeats.setDisable(true); // ခုံအရေအတွက်ကို Update လုပ်ခွင့်မပြုပါ (Seats table နှင့် ချိတ်ဆက်ထားသောကြောင့်)

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
        ObservableList<String> statusList = FXCollections.observableArrayList("Scheduled", "Delayed", "Cancelled");
        cmbStatus.setItems(statusList);
    }

    private void setupDateValidations() {
        dpStartDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });

        dpEndDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate minDate = dpStartDate.getValue() != null ? dpStartDate.getValue() : LocalDate.now();
                setDisable(empty || date.compareTo(minDate) < 0);
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

    private boolean isDaySelected(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        switch (day) {
            case MONDAY: return chkMon.isSelected();
            case TUESDAY: return chkTue.isSelected();
            case WEDNESDAY: return chkWed.isSelected();
            case THURSDAY: return chkThu.isSelected();
            case FRIDAY: return chkFri.isSelected();
            case SATURDAY: return chkSat.isSelected();
            case SUNDAY: return chkSun.isSelected();
            default: return false;
        }
    }

    @FXML
    void clickSave(ActionEvent event) {
        // Validation Checks
        if (txtFlightNumber.getText().isEmpty() || txtAirline.getText().isEmpty() ||
                cmbDepartureCity.getValue() == null || cmbArrivalCity.getValue() == null ||
                dpStartDate.getValue() == null || dpEndDate.getValue() == null ||
                cmbDepartureHour.getValue() == null || cmbDepartureMinute.getValue() == null ||
                cmbArrivalHour.getValue() == null || cmbArrivalMinute.getValue() == null ||
                txtTotalSeats.getText().isEmpty() || txtBasePrice.getText().isEmpty() || cmbStatus.getValue() == null) {

            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
            return;
        }

        boolean hasOperatingDay = chkMon.isSelected() || chkTue.isSelected() || chkWed.isSelected() ||
                chkThu.isSelected() || chkFri.isSelected() || chkSat.isSelected() || chkSun.isSelected();

        if (currentFlightId == null && !hasOperatingDay) {
            showAlert(Alert.AlertType.WARNING, "Select Days", "Please select at least one operating day.");
            return;
        }

        try {
            String flightNumber = txtFlightNumber.getText();
            String airline = txtAirline.getText();
            int departureCityId = getCityId(cmbDepartureCity.getValue());
            int arrivalCityId = getCityId(cmbArrivalCity.getValue());

            LocalTime depTime = LocalTime.of(Integer.parseInt(cmbDepartureHour.getValue()), Integer.parseInt(cmbDepartureMinute.getValue()));
            LocalTime arrTime = LocalTime.of(Integer.parseInt(cmbArrivalHour.getValue()), Integer.parseInt(cmbArrivalMinute.getValue()));

            int totalSeats = Integer.parseInt(txtTotalSeats.getText());
            double basePrice = Double.parseDouble(txtBasePrice.getText());
            String status = cmbStatus.getValue();

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Transaction စတင်မည် (အမှားပါက နောက်ပြန်ဆုတ်ရန်)

                try {
                    if (currentFlightId == null) {
                        // 🌟 MULTIPLE FLIGHTS GENERATION (The Magic Loop) 🌟
                        String insertFlightSQL = "INSERT INTO flights (flight_number, airline, departure_city_id, arrival_city_id, departure_time, arrival_time, total_seats, base_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        String insertSeatSQL = "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) VALUES (?, ?, ?, true)";

                        try (PreparedStatement pstmtFlight = conn.prepareStatement(insertFlightSQL, Statement.RETURN_GENERATED_KEYS);
                             PreparedStatement pstmtSeat = conn.prepareStatement(insertSeatSQL)) {

                            LocalDate currentDate = dpStartDate.getValue();
                            LocalDate endDate = dpEndDate.getValue();
                            int generatedCount = 0;

                            while (!currentDate.isAfter(endDate)) {
                                if (isDaySelected(currentDate)) {

                                    // ညဘက်ဖြတ်ပျံသော လေယာဉ်များအတွက် Arrival Date ကို နောက်တစ်ရက်အဖြစ် အလိုအလျောက် သတ်မှတ်ခြင်း
                                    LocalDate arrivalDate = currentDate;
                                    if (arrTime.isBefore(depTime)) {
                                        arrivalDate = currentDate.plusDays(1);
                                    }

                                    Timestamp depTimestamp = Timestamp.valueOf(currentDate.atTime(depTime));
                                    Timestamp arrTimestamp = Timestamp.valueOf(arrivalDate.atTime(arrTime));

                                    // Flight ထည့်သွင်းခြင်း
                                    pstmtFlight.setString(1, flightNumber);
                                    pstmtFlight.setString(2, airline);
                                    pstmtFlight.setInt(3, departureCityId);
                                    pstmtFlight.setInt(4, arrivalCityId);
                                    pstmtFlight.setTimestamp(5, depTimestamp);
                                    pstmtFlight.setTimestamp(6, arrTimestamp);
                                    pstmtFlight.setInt(7, totalSeats);
                                    pstmtFlight.setDouble(8, basePrice);
                                    pstmtFlight.setString(9, status);

                                    pstmtFlight.executeUpdate();
                                    generatedCount++;

                                    // 🌟 Generated 'flight_id' ကို ယူ၍ 'seats' ထဲသို့ ထည့်သွင်းခြင်း 🌟
                                    try (ResultSet generatedKeys = pstmtFlight.getGeneratedKeys()) {
                                        if (generatedKeys.next()) {
                                            int newFlightId = generatedKeys.getInt(1);
                                            generateSeatsForFlight(pstmtSeat, newFlightId, totalSeats);
                                        }
                                    }
                                }
                                currentDate = currentDate.plusDays(1);
                            }

                            if (generatedCount == 0) {
                                showAlert(Alert.AlertType.WARNING, "No Flights Generated", "The selected date range does not contain any of the selected operating days.");
                                conn.rollback();
                                return;
                            }
                        }

                        conn.commit(); // အားလုံး အောင်မြင်မှသာ Database ထဲ အတည်ပြုသိမ်းဆည်းမည်
                        if (saveCallback != null) saveCallback.accept("Successfully generated flights for the selected period.");

                    } else {
                        // 🌟 SINGLE FLIGHT UPDATE 🌟
                        LocalDate depDate = dpStartDate.getValue();
                        LocalDate arrDate = depDate;
                        if (arrTime.isBefore(depTime)) {
                            arrDate = depDate.plusDays(1);
                        }

                        String updateSQL = "UPDATE flights SET flight_number=?, airline=?, departure_city_id=?, arrival_city_id=?, departure_time=?, arrival_time=?, base_price=?, status=? WHERE flight_id=?";
                        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                            pstmt.setString(1, flightNumber);
                            pstmt.setString(2, airline);
                            pstmt.setInt(3, departureCityId);
                            pstmt.setInt(4, arrivalCityId);
                            pstmt.setTimestamp(5, Timestamp.valueOf(depDate.atTime(depTime)));
                            pstmt.setTimestamp(6, Timestamp.valueOf(arrDate.atTime(arrTime)));
                            pstmt.setDouble(7, basePrice);
                            pstmt.setString(8, status);
                            pstmt.setInt(9, currentFlightId);

                            pstmt.executeUpdate();
                        }
                        conn.commit();
                        if (saveCallback != null) saveCallback.accept("Flight updated successfully.");
                    }

                } catch (SQLException ex) {
                    conn.rollback(); // Error တက်ပါက Transaction ကို နောက်ပြန်ဆုတ်မည်
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Action failed: " + e.getMessage());
        }
    }

    // 🌟 Flight အသစ်အတွက် Seat များကို အလိုအလျောက် ပွားပေးမည့် Method 🌟
    private void generateSeatsForFlight(PreparedStatement pstmtSeat, int flightId, int totalSeats) throws SQLException {
        String[] columns = {"A", "B", "C", "D", "E", "F"}; // ခုံတန်းတစ်ခုတွင် ခုံ (၆) ခုံ ရှိသည်ဟု ယူဆပါသည်
        int seatCount = 0;
        int rowNumber = 1;

        while (seatCount < totalSeats) {
            for (String col : columns) {
                if (seatCount >= totalSeats) break;

                String seatNumber = rowNumber + col; // ဥပမာ - 1A, 1B, 1C
                String seatClass = (rowNumber <= 5) ? "Business" : "Economy"; // ရှေ့ (၅) တန်းကို Business Class အဖြစ် သတ်မှတ်ခြင်း

                pstmtSeat.setInt(1, flightId);
                pstmtSeat.setString(2, seatNumber);
                pstmtSeat.setString(3, seatClass);
                pstmtSeat.addBatch(); // Batch ထဲသို့ စုထည့်သည် (Performance ပိုမြန်စေရန်)

                seatCount++;
            }
            rowNumber++;
        }
        pstmtSeat.executeBatch(); // Batch တစ်ခုလုံးကို Database ထဲသို့ တစ်ပြိုင်နက် သွင်းသည်
    }

    @FXML
    void clickCancel(ActionEvent event) {
        if (cancelCallback != null) cancelCallback.run();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}