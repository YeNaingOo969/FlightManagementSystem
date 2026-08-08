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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class StaffViewFlightScheduleController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupActionColumn();
        loadFlightSchedules();
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

            controller.setFlightData(flight.getFlightNumber(), flight.getDepartureCityName(), flight.getArrivalCityName(), dateStr, priceStr, 1, "Seat View Only");
            controller.setViewOnlyMode(true);

            applyThemeRecursively(seatView, checkIsDarkMode());

            // 🌟 Dashboard Panel ထဲတွင် ချောမွေ့စွာ အရှေ့သို့ Zoom In ဝင်သွားမည့် Animation 🌟
            Pane parentContainer = (Pane) rootStackPane.getParent();
            if (parentContainer != null) {
                // အသစ်တက်လာမည့် Page သည် အရှေ့ဘက်မှ ကြီးရာမှ သေး၍ဝင်လာမည်
                seatView.setScaleX(1.2);
                seatView.setScaleY(1.2);
                seatView.setOpacity(0.0);
                parentContainer.getChildren().add(seatView); // အပေါ်မှ ထပ်ထည့်သည်

                // လက်ရှိ Page သည် နောက်သို့ သေး၍ ပျောက်သွားမည်
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

    private void loadFlightSchedules() {
        flightList.clear();
        String query = "SELECT f.flight_id, f.flight_number, f.airline, c1.city_name AS dep_city_name, c2.city_name AS arr_city_name, f.departure_time, f.arrival_time, f.total_seats, f.base_price, f.status FROM flights f JOIN cities c1 ON f.departure_city_id = c1.city_id JOIN cities c2 ON f.arrival_city_id = c2.city_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}