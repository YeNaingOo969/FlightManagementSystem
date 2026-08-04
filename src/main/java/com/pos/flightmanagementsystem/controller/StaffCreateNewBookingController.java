package com.pos.flightmanagementsystem.controller;

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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class StaffCreateNewBookingController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private ComboBox<String> fromComboBox;
    @FXML private ComboBox<String> toComboBox;
    @FXML private DatePicker departureDatePicker;
    @FXML private Spinner<Integer> passengersSpinner;
    @FXML private Button btnSearchFlights;

    @FXML private TableView<FlightDummy> flightsTable;
    @FXML private TableColumn<FlightDummy, String> colFlightNo;
    @FXML private TableColumn<FlightDummy, String> colFrom;
    @FXML private TableColumn<FlightDummy, String> colTo;
    @FXML private TableColumn<FlightDummy, String> colDate;
    @FXML private TableColumn<FlightDummy, String> colTime;
    @FXML private TableColumn<FlightDummy, String> colPrice;
    @FXML private TableColumn<FlightDummy, Void> colAction;

    private final ObservableList<String> allCitiesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        passengersSpinner.setValueFactory(valueFactory);

        departureDatePicker.setValue(LocalDate.now());
        loadCitiesFromDatabase();
        setupToComboBoxDisableLogic();
        btnSearchFlights.setOnAction(this::handleSearchAction);

        colFlightNo.setCellValueFactory(new PropertyValueFactory<>("flightNo"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("to"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        setupActionColumn();
        fetchAllFlightsFromDatabase();
    }

    private void loadCitiesFromDatabase() {
        String query = "SELECT city_name FROM cities";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database Connection မရရှိပါ။");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    allCitiesList.add(rs.getString("city_name"));
                }
            }
            fromComboBox.setItems(allCitiesList);
            toComboBox.setItems(allCitiesList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupToComboBoxDisableLogic() {
        toComboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setDisable(false);
                            setStyle("");
                        } else {
                            setText(item);
                            String selectedFrom = fromComboBox.getValue();
                            if (item.equals(selectedFrom)) {
                                setDisable(true);
                                setStyle("-fx-text-fill: #a0a0a0; -fx-font-style: italic;");
                            } else {
                                setDisable(false);
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });

        fromComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.equals(toComboBox.getValue())) {
                toComboBox.getSelectionModel().clearSelection();
            }
            ObservableList<String> items = toComboBox.getItems();
            toComboBox.setItems(null);
            toComboBox.setItems(items);
        });
    }

    private void fetchAllFlightsFromDatabase() {
        ObservableList<FlightDummy> allFlights = FXCollections.observableArrayList();
        String query = "SELECT f.flight_number, c1.city_name AS from_city, c2.city_name AS to_city, " +
                "DATE(f.departure_time) AS dep_date, TIME(f.departure_time) AS dep_time, f.base_price " +
                "FROM flights f " +
                "INNER JOIN cities c1 ON f.departure_city_id = c1.city_id " +
                "INNER JOIN cities c2 ON f.arrival_city_id = c2.city_id " +
                "ORDER BY f.departure_time ASC";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database Connection မရရှိပါ။");
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    double price = rs.getDouble("base_price");
                    String formattedPrice = String.format("%,.0f MMK", price);

                    allFlights.add(new FlightDummy(
                            rs.getString("flight_number"),
                            rs.getString("from_city"),
                            rs.getString("to_city"),
                            rs.getString("dep_date"),
                            rs.getString("dep_time"),
                            formattedPrice
                    ));
                }
            }
            flightsTable.setItems(allFlights);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSearchAction(ActionEvent event) {
        String from = fromComboBox.getValue();
        String to = toComboBox.getValue();
        LocalDate date = departureDatePicker.getValue();

        if(from == null || to == null || date == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Incomplete Data");
            alert.setHeaderText(null);
            alert.setContentText("ကျေးဇူးပြု၍ From, To နှင့် Departure Date အားလုံးကို ရွေးချယ်ပါ။");
            alert.showAndWait();
            return;
        }
        fetchFlightsFromDatabase(from, to, date);
    }

    private void fetchFlightsFromDatabase(String fromCity, String toCity, LocalDate depDate) {
        ObservableList<FlightDummy> searchResults = FXCollections.observableArrayList();
        String query = "SELECT f.flight_number, c1.city_name AS from_city, c2.city_name AS to_city, " +
                "DATE(f.departure_time) AS dep_date, TIME(f.departure_time) AS dep_time, f.base_price " +
                "FROM flights f " +
                "INNER JOIN cities c1 ON f.departure_city_id = c1.city_id " +
                "INNER JOIN cities c2 ON f.arrival_city_id = c2.city_id " +
                "WHERE c1.city_name = ? AND c2.city_name = ? AND DATE(f.departure_time) = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database Connection မရရှိပါ။");
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, fromCity);
                pstmt.setString(2, toCity);
                pstmt.setDate(3, java.sql.Date.valueOf(depDate));
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        double price = rs.getDouble("base_price");
                        String formattedPrice = String.format("%,.0f MMK", price);

                        searchResults.add(new FlightDummy(
                                rs.getString("flight_number"),
                                rs.getString("from_city"),
                                rs.getString("to_city"),
                                rs.getString("dep_date"),
                                rs.getString("dep_time"),
                                formattedPrice
                        ));
                    }
                }
            }
            flightsTable.setItems(searchResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupActionColumn() {
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
                            int passengers = passengersSpinner.getValue();

                            loadPassengerDetailsPage(selectedFlight, passengers);
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
    }

    // ==========================================
    // 🌟 Theme အား အကုန်လိုက်ပြောင်းပေးမည့် Method (Recursive Method)
    // ==========================================
    private void applyThemeRecursively(Parent node, boolean isDarkMode) {
        if (node.getStylesheets() != null && !node.getStylesheets().isEmpty()) {
            ObservableList<String> styles = node.getStylesheets();
            for (int i = 0; i < styles.size(); i++) {
                String css = styles.get(i);
                if (isDarkMode && css.contains("Light")) {
                    styles.set(i, css.replace("Light", "Dark"));
                } else if (!isDarkMode && css.contains("Dark")) {
                    styles.set(i, css.replace("Dark", "Light"));
                }
            }
        }
        // အတွင်းပိုင်း Node တွေပါ အကုန်လိုက်စစ်ရန်
        for (javafx.scene.Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                applyThemeRecursively((Parent) child, isDarkMode);
            }
        }
    }

    private void loadPassengerDetailsPage(FlightDummy selectedFlight, int passengers) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffPassengerDetails.fxml"));
            Parent nextView = loader.load();

            // -----------------------------------------------------------------
            // 🌟 Theme ကို သေချာဖမ်းပြီး Next Page ရဲ့ အစွန်အဖျားထိ လိုက်ပြောင်းပေးခြင်း
            // -----------------------------------------------------------------
            boolean isDarkMode = true; // Default ထားပါသည်
            Parent current = rootStackPane;
            while (current != null) {
                String currentStyles = current.getStylesheets().toString();
                if (currentStyles.contains("Dark")) {
                    isDarkMode = true;
                    break;
                } else if (currentStyles.contains("Light")) {
                    isDarkMode = false;
                    break;
                }
                current = current.getParent();
            }

            // Next Page တစ်ခုလုံးကို အပေါ်က Method ဖြင့် Theme ဝင်အောင် ဖိချိန်းပါမည်
            applyThemeRecursively(nextView, isDarkMode);
            // -----------------------------------------------------------------

            StaffPassengerDetailsController controller = loader.getController();
            controller.setFlightData(
                    selectedFlight.getFlightNo(),
                    selectedFlight.getFrom(),
                    selectedFlight.getTo(),
                    selectedFlight.getDate(),
                    selectedFlight.getPrice(),
                    passengers
            );

            nextView.setScaleX(0.8);
            nextView.setScaleY(0.8);
            nextView.setOpacity(0.0);

            rootStackPane.getChildren().add(nextView);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer);
            scaleOut.setToX(0.8);
            scaleOut.setToY(0.8);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn);
            pt.setInterpolator(Interpolator.EASE_BOTH);

            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer));
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
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