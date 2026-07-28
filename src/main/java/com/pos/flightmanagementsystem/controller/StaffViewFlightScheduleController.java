package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Flight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class StaffViewFlightScheduleController implements Initializable {

    @FXML private VBox rootLayout; // ညာဘက်ခြမ်းကို ကိုယ်စားပြုပါသည်

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

        // 🌟 Column Name ကို Action မှ View Seat သို့ ပြောင်းလဲခြင်း 🌟
        colAction.setText("View Seat");
    }

    private void setupActionColumn() {
        Callback<TableColumn<Flight, Void>, TableCell<Flight, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Flight, Void> call(final TableColumn<Flight, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("View Seat");
                    {
                        btn.setStyle("-fx-background-color: #0b2559; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                        btn.setOnAction((ActionEvent event) -> {
                            Flight data = getTableView().getItems().get(getIndex());
                            openSeatSelection(data);
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

    private void openSeatSelection(Flight flight) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffSeatSelection.fxml"));
            Parent seatView = loader.load();
            StaffSeatSelectionController controller = loader.getController();

            String dateStr = flight.getDepartureTime() != null ? flight.getDepartureTime().toLocalDate().toString() : "Unknown Date";
            String priceStr = String.valueOf(flight.getBasePrice());

            // ထိုင်ခုံကြည့်ရုံဖြစ်သည့်အတွက် ခရီးသည်နာမည်ကို "Seat View Only" ဟု သတ်မှတ်ပေးပါသည်
            controller.setFlightData(flight.getFlightNumber(), flight.getDepartureCityName(), flight.getArrivalCityName(), dateStr, priceStr, 1, "Seat View Only");

            // View Seat မှ ခေါ်ခြင်းဖြစ်သောကြောင့် Continue Button ကို ဖျောက်ထားပါမည်
            controller.setViewOnlyMode(true);

            // 🌟 နေရာအတိအကျယူစေရန် Parent Layout အမျိုးအစားအလိုက် ထည့်သွင်းခြင်း 🌟
            Pane parentContainer = (Pane) rootLayout.getParent();
            if (parentContainer != null) {
                if (parentContainer instanceof BorderPane) {
                    ((BorderPane) parentContainer).setCenter(seatView);
                } else {
                    int currentIndex = parentContainer.getChildren().indexOf(rootLayout);
                    if (currentIndex != -1) {
                        parentContainer.getChildren().set(currentIndex, seatView);

                        // အပေါ်အောက် ဘေးဘယ်ညာ အပြည့်နေရာယူစေရန် Layout Constraints သတ်မှတ်ခြင်း
                        if (parentContainer instanceof HBox) {
                            HBox.setHgrow(seatView, Priority.ALWAYS);
                        } else if (parentContainer instanceof VBox) {
                            VBox.setVgrow(seatView, Priority.ALWAYS);
                        } else if (parentContainer instanceof AnchorPane) {
                            AnchorPane.setTopAnchor(seatView, 0.0);
                            AnchorPane.setBottomAnchor(seatView, 0.0);
                            AnchorPane.setLeftAnchor(seatView, 0.0);
                            AnchorPane.setRightAnchor(seatView, 0.0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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