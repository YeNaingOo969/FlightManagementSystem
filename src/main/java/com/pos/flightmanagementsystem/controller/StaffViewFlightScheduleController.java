package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Flight;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StaffViewFlightScheduleController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private ComboBox<String> fromComboBox;
    @FXML private ComboBox<String> toComboBox;
    @FXML private DatePicker departureDatePicker;
    @FXML private Button btnSearch;

    @FXML private TableView<Flight> flightTable;
    @FXML private TableColumn<Flight, Integer> colFlightId;
    @FXML private TableColumn<Flight, String> colFlightNumber;
    @FXML private TableColumn<Flight, String> colAirline;
    @FXML private TableColumn<Flight, String> colDepCityName;
    @FXML private TableColumn<Flight, String> colArrCityName;
    @FXML private TableColumn<Flight, LocalDateTime> colDepTime;
    @FXML private TableColumn<Flight, LocalDateTime> colArrTime;
    @FXML private TableColumn<Flight, Integer> colTotalSeats;
    @FXML private TableColumn<Flight, Double> colBasePrice;
    @FXML private TableColumn<Flight, String> colStatus;
    @FXML private TableColumn<Flight, Void> colAction;

    private ObservableList<Flight> flightList = FXCollections.observableArrayList();
    private final ObservableList<String> allCitiesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupActionColumn();

        departureDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                departureDatePicker.setValue(null);
            }
        });

        // 🌟 ယနေ့မတိုင်ခင် ရက်များအား ရွေးချယ်ခွင့်ပိတ်ခြင်း (Disable Past Dates - Dark Theme) 🌟
        departureDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                if (date != null && date.isBefore(today)) {
                    setDisable(true);
                    // Inline Style အစား CSS Class ကို လှမ်းချိတ်မည်
                    if (!getStyleClass().contains("disabled-date")) {
                        getStyleClass().add("disabled-date");
                    }
                } else {
                    // အခြားရက်များအတွက် CSS ကို ပြန်ဖြုတ်မည်
                    getStyleClass().remove("disabled-date");
                }
            }
        });

        loadCitiesFromDatabase();
        setupToComboBoxDisableLogic();

        btnSearch.setOnAction(e -> handleSearchAction());

        // အစပိုင်းတွင် Data အားလုံးကို ပြထားမည်
        loadFlightSchedules(null, null, null);
    }

    private void loadCitiesFromDatabase() {
        String query = "SELECT city_name FROM cities";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    allCitiesList.add(rs.getString("city_name"));
                }
            }
            fromComboBox.setItems(allCitiesList);
            toComboBox.setItems(allCitiesList);

            setupSearchableComboBox(fromComboBox, allCitiesList);
            setupSearchableComboBox(toComboBox, allCitiesList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSearchableComboBox(ComboBox<String> comboBox, ObservableList<String> itemsList) {
        comboBox.setEditable(true);

        comboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            String selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null && selected.equals(newValue)) {
                return;
            }

            if (newValue == null || newValue.isEmpty()) {
                comboBox.setItems(itemsList);
                comboBox.getSelectionModel().clearSelection();
                return;
            }

            ObservableList<String> filteredList = FXCollections.observableArrayList();
            for (String item : itemsList) {
                if (item.toLowerCase().contains(newValue.toLowerCase())) {
                    filteredList.add(item);
                }
            }

            comboBox.setItems(filteredList);

            if (!filteredList.isEmpty() && comboBox.isFocused()) {
                comboBox.show();
            } else {
                comboBox.hide();
            }

            Platform.runLater(() -> {
                comboBox.getEditor().setText(newValue);
                comboBox.getEditor().positionCaret(newValue.length());
            });
        });

        comboBox.getEditor().setOnMouseClicked(event -> {
            if (comboBox.getEditor().getText() == null || comboBox.getEditor().getText().isEmpty()) {
                comboBox.setItems(itemsList);
            }
            comboBox.show();
        });
    }

    private void setupToComboBoxDisableLogic() {
        toComboBox.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                    setStyle("");
                } else {
                    setText(item);
                    String selectedFrom = fromComboBox.getEditor().getText();
                    if (item.equals(selectedFrom) && !selectedFrom.isEmpty()) {
                        setDisable(true);
                        setStyle("-fx-text-fill: #a0a0a0; -fx-font-style: italic;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            }
        });

        fromComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.equals(toComboBox.getEditor().getText()) && !newVal.isEmpty()) {
                toComboBox.getSelectionModel().clearSelection();
                toComboBox.getEditor().clear();
            }
        });
    }

    private void handleSearchAction() {
        String from = fromComboBox.getEditor().getText().trim();
        String to = toComboBox.getEditor().getText().trim();
        LocalDate date = departureDatePicker.getValue();

        if (from.equalsIgnoreCase(to) && !from.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Data");
            alert.setHeaderText(null);
            alert.setContentText("ထွက်ခွာမည့်မြို့နှင့် ဆိုက်ရောက်မည့်မြို့ တူညီနေပါသည်။");
            alert.showAndWait();
            return;
        }

        loadFlightSchedules(from.isEmpty() ? null : from, to.isEmpty() ? null : to, date);
    }

    private void setupTableColumns() {
        colFlightId.setCellValueFactory(new PropertyValueFactory<>("flightId"));
        colFlightNumber.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colAirline.setCellValueFactory(new PropertyValueFactory<>("airline"));
        colDepCityName.setCellValueFactory(new PropertyValueFactory<>("departureCityName"));
        colArrCityName.setCellValueFactory(new PropertyValueFactory<>("arrivalCityName"));
        colDepTime.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        colArrTime.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        colTotalSeats.setCellValueFactory(new PropertyValueFactory<>("totalSeats"));
        colBasePrice.setCellValueFactory(new PropertyValueFactory<>("basePrice"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    if (item.equalsIgnoreCase("Scheduled")) {
                        lbl.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;"); // Green
                    } else if (item.equalsIgnoreCase("Delayed")) {
                        lbl.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;"); // Orange
                    } else if (item.equalsIgnoreCase("Cancelled")) {
                        lbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;"); // Red
                    } else {
                        lbl.setStyle("-fx-text-fill: #8B95A5; -fx-font-weight: bold;"); // Gray
                    }
                    setGraphic(lbl);
                }
            }
        });

        colAction.setText("View Seat");
    }

    private void setupActionColumn() {
        Callback<TableColumn<Flight, Void>, TableCell<Flight, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Flight, Void> call(final TableColumn<Flight, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("View Seat");
                    {
                        btn.getStyleClass().add("btn-view-seat");
                        btn.setOnAction((ActionEvent event) -> {
                            Flight data = getTableView().getItems().get(getIndex());
                            openSeatSelection(data);
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null);
                        else setGraphic(btn);
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    private void openSeatSelection(Flight flight) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffSeatSelection.fxml"));
            Parent seatView = loader.load();
            StaffSeatSelectionController controller = loader.getController();

            String dateStr = flight.getDepartureTime() != null ? flight.getDepartureTime().toLocalDate().toString() : "Unknown Date";
            String priceStr = String.valueOf(flight.getBasePrice());

            controller.setFlightData(flight.getFlightNumber(), flight.getDepartureCityName(), flight.getArrivalCityName(), dateStr, priceStr, 1);
            controller.setViewOnlyMode(true);

            applyThemeRecursively(seatView, checkIsDarkMode());

            Pane parentContainer = (Pane) rootStackPane.getParent();
            if (parentContainer != null) {
                seatView.setScaleX(1.2);
                seatView.setScaleY(1.2);
                seatView.setOpacity(0.0);
                parentContainer.getChildren().add(seatView);

                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), rootStackPane);
                scaleOut.setToX(0.8);
                scaleOut.setToY(0.8);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), rootStackPane);
                fadeOut.setToValue(0.0);

                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), seatView);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), seatView);
                fadeIn.setToValue(1.0);

                ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
                pt.setInterpolator(Interpolator.EASE_BOTH);
                pt.setOnFinished(e -> parentContainer.getChildren().remove(rootStackPane));
                pt.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyThemeRecursively(Parent node, boolean isDarkMode) {
        if (node.getStylesheets() != null && !node.getStylesheets().isEmpty()) {
            ObservableList<String> styles = node.getStylesheets();
            for (int i = 0; i < styles.size(); i++) {
                String css = styles.get(i);
                if (isDarkMode && css.contains("Light")) styles.set(i, css.replace("Light", "Dark"));
                else if (!isDarkMode && css.contains("Dark")) styles.set(i, css.replace("Dark", "Light"));
            }
        }
        for (javafx.scene.Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Parent) applyThemeRecursively((Parent) child, isDarkMode);
        }
    }

    private boolean checkIsDarkMode() {
        boolean isDarkMode = true;
        Parent current = rootStackPane;
        while (current != null) {
            String currentStyles = current.getStylesheets().toString();
            if (currentStyles.contains("Dark")) return true;
            else if (currentStyles.contains("Light")) return false;
            current = current.getParent();
        }
        return isDarkMode;
    }

    private void loadFlightSchedules(String fromCity, String toCity, LocalDate depDate) {
        flightList.clear();
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT f.flight_id, f.flight_number, f.airline, c1.city_name AS dep_city_name, " +
                        "c2.city_name AS arr_city_name, f.departure_time, f.arrival_time, f.total_seats, " +
                        "f.base_price, f.status " +
                        "FROM flights f " +
                        "JOIN cities c1 ON f.departure_city_id = c1.city_id " +
                        "JOIN cities c2 ON f.arrival_city_id = c2.city_id WHERE f.departure_time >= NOW()"
        );

        List<Object> parameters = new ArrayList<>();

        if (fromCity != null) {
            queryBuilder.append(" AND c1.city_name = ?");
            parameters.add(fromCity);
        }
        if (toCity != null) {
            queryBuilder.append(" AND c2.city_name = ?");
            parameters.add(toCity);
        }
        if (depDate != null) {
            queryBuilder.append(" AND DATE(f.departure_time) = ?");
            parameters.add(java.sql.Date.valueOf(depDate));
        }

        queryBuilder.append(" ORDER BY f.departure_time ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp depTimestamp = rs.getTimestamp("departure_time");
                    LocalDateTime depTime = (depTimestamp != null) ? depTimestamp.toLocalDateTime() : null;
                    Timestamp arrTimestamp = rs.getTimestamp("arrival_time");
                    LocalDateTime arrTime = (arrTimestamp != null) ? arrTimestamp.toLocalDateTime() : null;

                    Flight flight = new Flight();
                    flight.setFlightId(rs.getInt("flight_id"));
                    flight.setFlightNumber(rs.getString("flight_number"));
                    flight.setAirline(rs.getString("airline"));
                    flight.setDepartureCityName(rs.getString("dep_city_name"));
                    flight.setArrivalCityName(rs.getString("arr_city_name"));
                    flight.setDepartureTime(depTime);
                    flight.setArrivalTime(arrTime);
                    flight.setTotalSeats(rs.getInt("total_seats"));
                    flight.setBasePrice(rs.getDouble("base_price"));
                    flight.setStatus(rs.getString("status"));

                    flightList.add(flight);
                }
                flightTable.setItems(flightList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}