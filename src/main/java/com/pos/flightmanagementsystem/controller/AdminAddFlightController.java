package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Flight;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

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

    @FXML private ComboBox<String> cmbTotalSeats;
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

        cmbTotalSeats.getItems().addAll("70", "120", "160", "180");
        cmbTotalSeats.setValue("70");
    }

    public void setFlightData(Flight flight) {
        this.currentFlightId = flight.getFlightId();

        txtFlightNumber.setText(flight.getFlightNumber());
        txtAirline.setText(flight.getAirline());

        cmbDepartureCity.setValue(getCityName(flight.getDepartureCityId()));
        cmbArrivalCity.setValue(getCityName(flight.getArrivalCityId()));

        dpStartDate.setValue(flight.getDepartureTime().toLocalDate());
        dpEndDate.setValue(flight.getDepartureTime().toLocalDate());
        dpEndDate.setDisable(true);

        cmbDepartureHour.setValue(String.format("%02d", flight.getDepartureTime().getHour()));
        cmbDepartureMinute.setValue(String.format("%02d", flight.getDepartureTime().getMinute()));

        cmbArrivalHour.setValue(String.format("%02d", flight.getArrivalTime().getHour()));
        cmbArrivalMinute.setValue(String.format("%02d", flight.getArrivalTime().getMinute()));

        cmbTotalSeats.setValue(String.valueOf(flight.getTotalSeats()));
        cmbTotalSeats.setDisable(true);

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
        if (txtFlightNumber.getText().isEmpty() || txtAirline.getText().isEmpty() ||
                cmbDepartureCity.getValue() == null || cmbArrivalCity.getValue() == null ||
                dpStartDate.getValue() == null || dpEndDate.getValue() == null ||
                cmbDepartureHour.getValue() == null || cmbDepartureMinute.getValue() == null ||
                cmbArrivalHour.getValue() == null || cmbArrivalMinute.getValue() == null ||
                cmbTotalSeats.getValue() == null || txtBasePrice.getText().isEmpty() || cmbStatus.getValue() == null) {

            showWarningModal("Incomplete Data", "Please fill in all the flight details completely before saving.", false);
            return;
        }

        boolean hasOperatingDay = chkMon.isSelected() || chkTue.isSelected() || chkWed.isSelected() ||
                chkThu.isSelected() || chkFri.isSelected() || chkSat.isSelected() || chkSun.isSelected();

        if (currentFlightId == null && !hasOperatingDay) {
            showWarningModal("Select Operating Days", "Please select at least one operating day for the flight schedule.", false);
            return;
        }

        try {
            String flightNumber = txtFlightNumber.getText();
            String airline = txtAirline.getText();
            int departureCityId = getCityId(cmbDepartureCity.getValue());
            int arrivalCityId = getCityId(cmbArrivalCity.getValue());

            LocalTime depTime = LocalTime.of(Integer.parseInt(cmbDepartureHour.getValue()), Integer.parseInt(cmbDepartureMinute.getValue()));
            LocalTime arrTime = LocalTime.of(Integer.parseInt(cmbArrivalHour.getValue()), Integer.parseInt(cmbArrivalMinute.getValue()));

            int totalSeats = Integer.parseInt(cmbTotalSeats.getValue());
            double basePrice = Double.parseDouble(txtBasePrice.getText());
            String status = cmbStatus.getValue();

            int adminId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    if (currentFlightId == null) {
                        String insertFlightSQL = "INSERT INTO flights (flight_number, airline, departure_city_id, arrival_city_id, departure_time, arrival_time, total_seats, base_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        String insertSeatSQL = "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) VALUES (?, ?, ?, true)";

                        try (PreparedStatement pstmtFlight = conn.prepareStatement(insertFlightSQL, Statement.RETURN_GENERATED_KEYS);
                             PreparedStatement pstmtSeat = conn.prepareStatement(insertSeatSQL)) {

                            LocalDate currentDate = dpStartDate.getValue();
                            LocalDate endDate = dpEndDate.getValue();
                            int generatedCount = 0;

                            while (!currentDate.isAfter(endDate)) {
                                if (isDaySelected(currentDate)) {

                                    LocalDate arrivalDate = currentDate;
                                    if (arrTime.isBefore(depTime)) {
                                        arrivalDate = currentDate.plusDays(1);
                                    }

                                    Timestamp depTimestamp = Timestamp.valueOf(currentDate.atTime(depTime));
                                    Timestamp arrTimestamp = Timestamp.valueOf(arrivalDate.atTime(arrTime));

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
                                showWarningModal("No Flights Generated", "The selected date range does not contain any of the selected operating days.", false);
                                conn.rollback();
                                return;
                            }

                            // 🌟 Flight အသစ်များ ထည့်သွင်းကြောင်း Log မှတ်ခြင်း 🌟
                            insertLog(adminId, "Added new flight schedule: " + flightNumber + " (" + generatedCount + " flights generated)");
                        }

                        conn.commit();
                        if (saveCallback != null) saveCallback.accept("Successfully generated flights for the selected period.");

                    } else {
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

                        // 🌟 Flight Data Update လုပ်ကြောင်း Log မှတ်ခြင်း 🌟
                        insertLog(adminId, "Updated flight ID: " + currentFlightId + " (Flight Number: " + flightNumber + ")");

                        conn.commit();
                        if (saveCallback != null) saveCallback.accept("Flight updated successfully.");
                    }

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showWarningModal("Database Error", "Action failed: " + e.getMessage(), true);
        }
    }

    // 🌟 System Log ထဲသို့ သိမ်းဆည်းမည့် Method 🌟
    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {
            if (userId > 0) pstmt.setInt(1, userId);
            else pstmt.setNull(1, java.sql.Types.INTEGER);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =========================================================================
    // 🌟 SEAT GENERATION LOGIC 🌟
    // =========================================================================
    private void generateSeatsForFlight(PreparedStatement pstmtSeat, int flightId, int totalSeats) throws SQLException {
        String[] columns = {"A", "B", "C", "D"};
        int seatCount = 0;
        int rowNumber = 1;

        while (seatCount < totalSeats) {
            for (String col : columns) {
                if (seatCount >= totalSeats) break;

                String seatNumber = rowNumber + col;

                String seatClass = "Economic";
                if (rowNumber <= 3) {
                    seatClass = "Business";
                } else if (rowNumber <= 8) {
                    seatClass = "Premium Economic";
                }

                pstmtSeat.setInt(1, flightId);
                pstmtSeat.setString(2, seatNumber);
                pstmtSeat.setString(3, seatClass);
                pstmtSeat.addBatch();

                seatCount++;
            }
            rowNumber++;
        }
        pstmtSeat.executeBatch();
    }

    @FXML
    void clickCancel(ActionEvent event) {
        if (cancelCallback != null) cancelCallback.run();
    }

    private void showWarningModal(String title, String message, boolean isError) {
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