package com.pos.flightmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class StaffCreateNewBookingController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

    @FXML private ComboBox<String> fromComboBox;
    @FXML private ComboBox<String> toComboBox;
    @FXML private DatePicker departureDatePicker;

    @FXML private Spinner<Integer> spinnerAdults;
    @FXML private Spinner<Integer> spinnerChildren;
    @FXML private Spinner<Integer> spinnerInfants;

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

    public static boolean showBookingSuccessToast = false;
    public static String successMessage = "";

    // 🌟 List ပြောင်းထားသည် 🌟
    private boolean isChangeFlightMode = false;
    private List<Integer> oldBookingIds;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spinnerAdults.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1));
        spinnerChildren.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0));
        spinnerInfants.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0));

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

        Platform.runLater(() -> {
            if (showBookingSuccessToast) {
                showToastNotification("Success", successMessage, false);
                showBookingSuccessToast = false;
            }
        });
    }

    // 🌟 Group Booking IDs ကို လက်ခံမည့် Method 🌟
    public void initChangeFlightMode(List<Integer> oldBookingIds) {
        this.isChangeFlightMode = true;
        this.oldBookingIds = oldBookingIds;

        int totalPax = oldBookingIds.size();
        spinnerAdults.getValueFactory().setValue(totalPax); // Group အရေအတွက်အတိုင်း တင်ပေးမည်
        spinnerAdults.setDisable(true);
        spinnerChildren.getValueFactory().setValue(0);
        spinnerChildren.setDisable(true);
        spinnerInfants.getValueFactory().setValue(0);
        spinnerInfants.setDisable(true);

        // Group ထဲက ပထမဆုံးလူရဲ့ Route ကိုပဲ ယူပြီး Search လုပ်ပါမည်
        int firstId = oldBookingIds.get(0);
        String query = "SELECT c1.city_name AS dep, c2.city_name AS arr " +
                "FROM bookings b " +
                "JOIN flights f ON b.flight_id = f.flight_id " +
                "JOIN cities c1 ON f.departure_city_id = c1.city_id " +
                "JOIN cities c2 ON f.arrival_city_id = c2.city_id " +
                "WHERE b.booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, firstId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dep = rs.getString("dep");
                    String arr = rs.getString("arr");

                    fromComboBox.setValue(dep);
                    toComboBox.setValue(arr);

                    departureDatePicker.setValue(LocalDate.now().plusDays(1));
                    fetchFlightsFromDatabase(dep, arr, departureDatePicker.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToastNotification(String title, String message, boolean isError) {
        HBox toast = new HBox(15); toast.setAlignment(Pos.CENTER_LEFT); toast.setMaxWidth(380); toast.setMinHeight(80); toast.setMaxHeight(80);
        String borderColor = isError ? "#EF4444" : "#31C4B4"; String bgColor = "#0B0E14"; String titleColor = "#FFFFFF"; String messageColor = "#8B95A5";
        toast.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + borderColor + "; -fx-border-width: 1.5px; -fx-background-radius: 14px; -fx-border-radius: 14px; -fx-padding: 16px 18px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 8);");

        StackPane iconBox = new StackPane(); iconBox.setStyle("-fx-background-color: " + borderColor + "20; -fx-background-radius: 50%; -fx-min-width: 38px; -fx-min-height: 38px; -fx-max-width: 38px; -fx-max-height: 38px;");
        SVGPath icon = new SVGPath();
        if (isError) icon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        else icon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        icon.setFill(Color.web(borderColor)); icon.setScaleX(1.2); icon.setScaleY(1.2); iconBox.getChildren().add(icon);

        VBox textContainer = new VBox(4); textContainer.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(textContainer, Priority.ALWAYS);
        HBox titleBox = new HBox(); titleBox.setAlignment(Pos.TOP_LEFT); Label lblTitle = new Label(title); lblTitle.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnClose = new Button(); SVGPath closeIcon = new SVGPath(); closeIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        closeIcon.setFill(Color.web(messageColor)); closeIcon.setScaleX(0.8); closeIcon.setScaleY(0.8); btnClose.setGraphic(closeIcon); btnClose.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        btnClose.setOnMouseEntered(e -> closeIcon.setFill(Color.web("#FF4B72"))); btnClose.setOnMouseExited(e -> closeIcon.setFill(Color.web(messageColor)));
        titleBox.getChildren().addAll(lblTitle, spacer, btnClose);

        Label lblMessage = new Label(message); lblMessage.setStyle("-fx-text-fill: " + messageColor + "; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';"); lblMessage.setWrapText(true); lblMessage.setMaxWidth(280);
        textContainer.getChildren().addAll(titleBox, lblMessage); toast.getChildren().addAll(iconBox, textContainer);

        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT); StackPane.setMargin(toast, new Insets(0, 30, 100, 0)); rootStackPane.getChildren().add(toast); toast.setTranslateY(120); toast.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toast); slideIn.setToY(0); slideIn.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast); fadeIn.setToValue(1.0); ParallelTransition ptIn = new ParallelTransition(slideIn, fadeIn);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), toast); slideOut.setToY(120); slideOut.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast); fadeOut.setToValue(0.0); ParallelTransition ptOut = new ParallelTransition(slideOut, fadeOut);
        ptOut.setOnFinished(e -> rootStackPane.getChildren().remove(toast));

        PauseTransition delay = new PauseTransition(Duration.seconds(3.5)); delay.setOnFinished(ev -> ptOut.play()); ptIn.setOnFinished(e -> delay.play()); btnClose.setOnAction(e -> { delay.stop(); ptOut.play(); }); ptIn.play();
    }

    private void loadCitiesFromDatabase() {
        String query = "SELECT city_name FROM cities";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) allCitiesList.add(rs.getString("city_name"));
            fromComboBox.setItems(allCitiesList); toComboBox.setItems(allCitiesList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupToComboBoxDisableLogic() {
        toComboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setDisable(false); setStyle(""); }
                        else {
                            setText(item); String selectedFrom = fromComboBox.getValue();
                            if (item.equals(selectedFrom)) { setDisable(true); setStyle("-fx-text-fill: #a0a0a0; -fx-font-style: italic;"); }
                            else { setDisable(false); setStyle(""); }
                        }
                    }
                };
            }
        });
        fromComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.equals(toComboBox.getValue())) toComboBox.getSelectionModel().clearSelection();
            ObservableList<String> items = toComboBox.getItems(); toComboBox.setItems(null); toComboBox.setItems(items);
        });
    }

    private void fetchAllFlightsFromDatabase() {
        ObservableList<FlightDummy> allFlights = FXCollections.observableArrayList();
        String query = "SELECT f.flight_number, c1.city_name AS from_city, c2.city_name AS to_city, DATE(f.departure_time) AS dep_date, TIME(f.departure_time) AS dep_time, f.base_price FROM flights f INNER JOIN cities c1 ON f.departure_city_id = c1.city_id INNER JOIN cities c2 ON f.arrival_city_id = c2.city_id ORDER BY f.departure_time ASC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                double price = rs.getDouble("base_price"); String formattedPrice = String.format("%,.0f MMK", price);
                allFlights.add(new FlightDummy( rs.getString("flight_number"), rs.getString("from_city"), rs.getString("to_city"), rs.getString("dep_date"), rs.getString("dep_time"), formattedPrice ));
            }
            flightsTable.setItems(allFlights);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleSearchAction(ActionEvent event) {
        String from = fromComboBox.getValue(); String to = toComboBox.getValue(); LocalDate date = departureDatePicker.getValue();
        if (from == null || to == null) { showWarningModal("Incomplete Data", "Please select at least the 'From' and 'To' cities."); return; }
        fetchFlightsFromDatabase(from, to, date);
    }

    private void showWarningModal(String title, String message) {
        // ... (Warning Modal Logic as before) ...
        StackPane overlay = new StackPane(); overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); BoxBlur blur = new BoxBlur(8, 8, 3); mainContainer.setEffect(blur);
        VBox modalCard = new VBox(20); modalCard.setMaxSize(420, Region.USE_PREF_SIZE); modalCard.setStyle("-fx-background-color: #0B0E14; -fx-border-color: #F59E0B; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 24px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 10);");
        HBox header = new HBox(); header.setAlignment(Pos.CENTER_LEFT); Label titleLabel = new Label(title); titleLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 16px; -fx-font-weight: bold;"); Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        StackPane closeBtn = new StackPane(); SVGPath closeIcon = new SVGPath(); closeIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"); closeIcon.setStyle("-fx-fill: #8B95A5; -fx-scale-x: 0.8; -fx-scale-y: 0.8;"); closeBtn.getChildren().add(closeIcon); closeBtn.setCursor(Cursor.HAND); closeBtn.setOnMouseEntered(e -> closeIcon.setStyle("-fx-fill: #FFFFFF; -fx-scale-x: 0.8; -fx-scale-y: 0.8;")); closeBtn.setOnMouseExited(e -> closeIcon.setStyle("-fx-fill: #8B95A5; -fx-scale-x: 0.8; -fx-scale-y: 0.8;")); header.getChildren().addAll(titleLabel, spacer, closeBtn);
        HBox body = new HBox(15); body.setAlignment(Pos.CENTER_LEFT); SVGPath warningIcon = new SVGPath(); warningIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"); warningIcon.setStyle("-fx-fill: #F59E0B; -fx-scale-x: 1.5; -fx-scale-y: 1.5;"); StackPane iconPane = new StackPane(warningIcon); iconPane.setPadding(new Insets(0, 10, 0, 10)); Label messageLabel = new Label(message); messageLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;"); messageLabel.setWrapText(true); messageLabel.setMaxWidth(300); body.getChildren().addAll(iconPane, messageLabel);
        HBox footer = new HBox(); footer.setAlignment(Pos.CENTER_RIGHT); Button okBtn = new Button("OK"); okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #F59E0B; -fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;"); okBtn.setOnMouseEntered(e -> okBtn.setStyle("-fx-background-color: rgba(245, 158, 11, 0.15); -fx-border-color: #F59E0B; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;")); okBtn.setOnMouseExited(e -> okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #F59E0B; -fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-border-radius: 6px; -background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;")); footer.getChildren().add(okBtn);
        modalCard.getChildren().addAll(header, body, footer); overlay.getChildren().add(modalCard); rootStackPane.getChildren().add(overlay);
        modalCard.setScaleX(0.5); modalCard.setScaleY(0.5); modalCard.setOpacity(0); overlay.setOpacity(0);
        ScaleTransition stIn = new ScaleTransition(Duration.millis(300), modalCard); stIn.setToX(1); stIn.setToY(1); stIn.setInterpolator(Interpolator.EASE_OUT); FadeTransition ftIn = new FadeTransition(Duration.millis(300), modalCard); ftIn.setToValue(1); FadeTransition bgFtIn = new FadeTransition(Duration.millis(300), overlay); bgFtIn.setToValue(1); ParallelTransition ptIn = new ParallelTransition(stIn, ftIn, bgFtIn); ptIn.play();
        Runnable closeAction = () -> { ScaleTransition stOut = new ScaleTransition(Duration.millis(200), modalCard); stOut.setToX(0.5); stOut.setToY(0.5); stOut.setInterpolator(Interpolator.EASE_IN); FadeTransition ftOut = new FadeTransition(Duration.millis(200), modalCard); ftOut.setToValue(0); FadeTransition bgFtOut = new FadeTransition(Duration.millis(200), overlay); bgFtOut.setToValue(0); ParallelTransition ptOut = new ParallelTransition(stOut, ftOut, bgFtOut); ptOut.setOnFinished(ev -> { rootStackPane.getChildren().remove(overlay); mainContainer.setEffect(null); }); ptOut.play(); };
        closeBtn.setOnMouseClicked(e -> closeAction.run()); okBtn.setOnAction(e -> closeAction.run());
    }

    private void fetchFlightsFromDatabase(String fromCity, String toCity, LocalDate depDate) {
        ObservableList<FlightDummy> searchResults = FXCollections.observableArrayList();
        StringBuilder queryBuilder = new StringBuilder( "SELECT f.flight_number, c1.city_name AS from_city, c2.city_name AS to_city, DATE(f.departure_time) AS dep_date, TIME(f.departure_time) AS dep_time, f.base_price FROM flights f INNER JOIN cities c1 ON f.departure_city_id = c1.city_id INNER JOIN cities c2 ON f.arrival_city_id = c2.city_id WHERE c1.city_name = ? AND c2.city_name = ?" );
        if (depDate != null) queryBuilder.append(" AND DATE(f.departure_time) = ?");
        else queryBuilder.append(" AND DATE(f.departure_time) >= CURRENT_DATE");
        queryBuilder.append(" ORDER BY f.departure_time ASC");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
            pstmt.setString(1, fromCity); pstmt.setString(2, toCity);
            if (depDate != null) pstmt.setDate(3, java.sql.Date.valueOf(depDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double price = rs.getDouble("base_price"); String formattedPrice = String.format("%,.0f MMK", price);
                    searchResults.add(new FlightDummy( rs.getString("flight_number"), rs.getString("from_city"), rs.getString("to_city"), rs.getString("dep_date"), rs.getString("dep_time"), formattedPrice ));
                }
            }
            flightsTable.setItems(searchResults);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupActionColumn() {
        Callback<TableColumn<FlightDummy, Void>, TableCell<FlightDummy, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<FlightDummy, Void> call(final TableColumn<FlightDummy, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Choose");
                    {
                        btn.getStyleClass().add("btn-choose"); setAlignment(Pos.CENTER);
                        btn.setOnAction((ActionEvent event) -> {
                            int adults = spinnerAdults.getValue(); int children = spinnerChildren.getValue(); int infants = spinnerInfants.getValue();
                            if (infants > adults) { showWarningModal("Invalid Passenger Count", "Infants (under 2) cannot exceed the number of Adults."); return; }
                            int totalSeatsRequired = adults + children;
                            FlightDummy selectedFlight = getTableView().getItems().get(getIndex());
                            loadSeatSelectionPage(selectedFlight, totalSeatsRequired);
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null); else setGraphic(btn);
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);
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

    private void loadSeatSelectionPage(FlightDummy selectedFlight, int passengers) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffSeatSelection.fxml"));
            Parent nextView = loader.load();

            boolean isDarkMode = true; Parent current = rootStackPane;
            while (current != null) {
                String currentStyles = current.getStylesheets().toString();
                if (currentStyles.contains("Dark")) { isDarkMode = true; break; }
                else if (currentStyles.contains("Light")) { isDarkMode = false; break; }
                current = current.getParent();
            }
            applyThemeRecursively(nextView, isDarkMode);

            StaffSeatSelectionController controller = loader.getController();
            controller.setFlightData( selectedFlight.getFlightNo(), selectedFlight.getFrom(), selectedFlight.getTo(), selectedFlight.getDate(), selectedFlight.getPrice(), passengers );

            // 🌟 List ကို Seat Selection ထံ ပို့ပေးပါမည် 🌟
            if (isChangeFlightMode) {
                controller.initChangeFlightMode(oldBookingIds);
            }

            nextView.setScaleX(0.8); nextView.setScaleY(0.8); nextView.setOpacity(0.0);
            rootStackPane.getChildren().add(nextView);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer); scaleOut.setToX(0.8); scaleOut.setToY(0.8);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer); fadeOut.setToValue(0.0);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView); scaleIn.setToX(1.0); scaleIn.setToY(1.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView); fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn); pt.setInterpolator(Interpolator.EASE_BOTH);
            pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer)); pt.play();

        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class FlightDummy {
        private String flightNo, from, to, date, time, price;
        public FlightDummy(String flightNo, String from, String to, String date, String time, String price) { this.flightNo = flightNo; this.from = from; this.to = to; this.date = date; this.time = time; this.price = price; }
        public String getFlightNo() { return flightNo; } public String getFrom() { return from; } public String getTo() { return to; }
        public String getDate() { return date; } public String getTime() { return time; } public String getPrice() { return price; }
    }
}