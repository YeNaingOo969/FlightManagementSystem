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

        // 🌟 Departure Date ကို ဖျက်လို့ရအောင် (Clear Date) လုပ်ပေးထားခြင်း
        departureDatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                departureDatePicker.setValue(null);
            }
        });

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

            // 🌟 From နဲ့ To ComboBox တွေကို ရိုက်ပြီးရှာလို့ရအောင် လုပ်ဆောင်ပေးခြင်း
            setupSearchableComboBox(fromComboBox, allCitiesList);
            setupSearchableComboBox(toComboBox, allCitiesList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🌟 ComboBox တွင် စာရိုက်ထည့်ပြီး ရှာဖွေနိုင်ရန် (Searchable / Autocomplete) ထည့်သွင်းထားသော Method
    private void setupSearchableComboBox(ComboBox<String> comboBox, ObservableList<String> itemsList) {
        comboBox.setEditable(true); // ရိုက်ထည့်ခွင့်ပြုရန်

        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final TextField editor = comboBox.getEditor();
            final String selected = comboBox.getSelectionModel().getSelectedItem();

            if (selected == null || !selected.equals(editor.getText())) {
                ObservableList<String> filteredList = FXCollections.observableArrayList();
                for (String item : itemsList) {
                    if (item.toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                comboBox.setItems(filteredList);
                // User ရိုက်နေချိန်မှသာ Dropdown ကျလာစေရန်
                if (!filteredList.isEmpty() && comboBox.isFocused()) {
                    comboBox.show();
                }
            }
        });

        // နှိပ်လိုက်ချိန်တွင် Data အကုန်ပြန်ပေါ်လာစေရန်
        comboBox.getEditor().setOnMouseClicked(event -> {
            comboBox.setItems(itemsList);
            comboBox.show();
        });
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
                            // ရိုက်ထည့်ထားတဲ့ စာကိုပါ ယူနိုင်ဖို့ editor.getText() ကိုသုံးထားပါတယ်
                            String selectedFrom = fromComboBox.getEditor().getText();
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
            if (newVal != null && newVal.equals(toComboBox.getEditor().getText())) {
                toComboBox.getSelectionModel().clearSelection();
                toComboBox.getEditor().clear();
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
        // Editable ဖြစ်သွားတဲ့အတွက် getEditor().getText() ဖြင့်စာကို ဖမ်းယူပါသည်
        String from = fromComboBox.getEditor().getText().trim();
        String to = toComboBox.getEditor().getText().trim();
        LocalDate date = departureDatePicker.getValue();

        // Date ကို မစစ်တော့ဘဲ From နဲ့ To ပါမပါကိုသာ စစ်ဆေးပါမည်
        if(from.isEmpty() || to.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Incomplete Data");
            alert.setHeaderText(null);
            alert.setContentText("ကျေးဇူးပြု၍ ထွက်ခွာမည့်မြို့ (From) နှင့် ဆိုက်ရောက်မည့်မြို့ (To) ကို ရွေးချယ်ပါ။");
            alert.showAndWait();
            return;
        }

        // From နဲ့ To မြို့ချင်းတူနေပါက တားဆီးရန်
        if(from.equalsIgnoreCase(to)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Data");
            alert.setHeaderText(null);
            alert.setContentText("ထွက်ခွာမည့်မြို့နှင့် ဆိုက်ရောက်မည့်မြို့ တူညီနေပါသည်။ ကျေးဇူးပြု၍ ပြန်လည်ရွေးချယ်ပါ။");
            alert.showAndWait();
            return;
        }

        fetchFlightsFromDatabase(from, to, date);
    }

    private void fetchFlightsFromDatabase(String fromCity, String toCity, LocalDate depDate) {
        ObservableList<FlightDummy> searchResults = FXCollections.observableArrayList();

        // 🌟 Date မပါဘဲ ရှာလို့ရရန် Query ကို Dynamic တည်ဆောက်ထားပါသည်
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT f.flight_number, c1.city_name AS from_city, c2.city_name AS to_city, " +
                        "DATE(f.departure_time) AS dep_date, TIME(f.departure_time) AS dep_time, f.base_price " +
                        "FROM flights f " +
                        "INNER JOIN cities c1 ON f.departure_city_id = c1.city_id " +
                        "INNER JOIN cities c2 ON f.arrival_city_id = c2.city_id " +
                        "WHERE c1.city_name = ? AND c2.city_name = ?"
        );

        if (depDate != null) {
            queryBuilder.append(" AND DATE(f.departure_time) = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) throw new Exception("Database Connection မရရှိပါ။");

            try (PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
                pstmt.setString(1, fromCity);
                pstmt.setString(2, toCity);

                // Date ပါလာမှသာ Parameter အဖြစ် ထည့်သွင်းပေးပါမည်
                if (depDate != null) {
                    pstmt.setDate(3, java.sql.Date.valueOf(depDate));
                }

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