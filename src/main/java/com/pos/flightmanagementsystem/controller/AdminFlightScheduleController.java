package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Flight;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
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

    private Map<Integer, String> cityIdToNameMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCitiesToMap();
        setupTableColumns();
        loadFlightData();

        if (dpFilterDate != null) {
            dpFilterDate.valueProperty().addListener((observable, oldValue, newValue) -> {
                loadFlightData();
            });
        }
    }

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
                cityIdToNameMap.put(id, name + " (" + code + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                        Flight flight = new Flight(
                                rs.getInt("flight_id"),
                                rs.getString("flight_number"),
                                rs.getString("airline"),
                                rs.getInt("departure_city_id"),
                                rs.getInt("arrival_city_id"),
                                rs.getTimestamp("departure_time").toLocalDateTime(),
                                rs.getTimestamp("arrival_time").toLocalDateTime(),
                                rs.getInt("total_seats"),
                                rs.getDouble("base_price"),
                                rs.getString("status")
                        );
                        flightList.add(flight);
                    }
                    tblFlights.setItems(flightList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getCityName(int cityId) {
        return cityIdToNameMap.getOrDefault(cityId, "Unknown City");
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(15, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-edit-table");
                deleteBtn.getStyleClass().add("btn-delete-table");
                pane.setAlignment(Pos.CENTER);

                deleteBtn.setOnAction(event -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    deleteFlight(flight);
                });

                editBtn.setOnAction(event -> {
                    Flight flight = getTableView().getItems().get(getIndex());
                    openFlightModal(flight, editBtn);
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
                        showSuccessNotification("Success!", "Flight deleted successfully.");

                        if (LoginController.loggedInUser != null) {
                            insertLog(LoginController.loggedInUser.getUserId(), "Deleted Flight: " + flight.getFlightNumber());
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertLog(int userId, String action) {
        String logQuery = "INSERT INTO logs (user_id, action, timestamp) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clickAddFlight(ActionEvent event) {
        openFlightModal(null, btnAddFlight);
    }

    private void openFlightModal(Flight flightToEdit, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminAddFlight.fxml"));
            Parent modalView = loader.load();

            // =========================================================================
            // 🌟 Theme စစ်ဆေးပြီး သက်ဆိုင်ရာ Popup CSS ကို ဒိုင်းနမစ် (Dynamic) ချိတ်ဆက်ပေးခြင်း 🌟
            // =========================================================================
            if (rootStackPane != null && !rootStackPane.getStylesheets().isEmpty()) {
                String currentMainStyle = rootStackPane.getStylesheets().get(0);
                modalView.getStylesheets().clear(); // CSS အဟောင်းများကို ရှင်းလင်းခြင်း

                if (currentMainStyle.contains("Light")) {
                    modalView.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/AdminAddFlightLight.css").toExternalForm());
                } else {
                    modalView.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/AdminAddFlightDark.css").toExternalForm());
                }
            }
            // =========================================================================

            modalView.setStyle("-fx-background-color: transparent; -fx-max-width: USE_PREF_SIZE; -fx-max-height: USE_PREF_SIZE;");

            AdminAddFlightController controller = loader.getController();
            if (flightToEdit != null) {
                controller.setFlightData(flightToEdit);
            }

            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

            Node mainContent = rootStackPane.getChildren().get(0);
            BoxBlur blur = new BoxBlur(8, 8, 3);
            mainContent.setEffect(blur);

            double startX = 0;
            double startY = 0;
            if (sourceNode != null) {
                Bounds sourceBounds = sourceNode.localToScene(sourceNode.getBoundsInLocal());
                double sourceCenterX = sourceBounds.getMinX() + (sourceBounds.getWidth() / 2);
                double sourceCenterY = sourceBounds.getMinY() + (sourceBounds.getHeight() / 2);

                Bounds rootBounds = rootStackPane.localToScene(rootStackPane.getBoundsInLocal());
                double rootCenterX = rootBounds.getMinX() + (rootBounds.getWidth() / 2);
                double rootCenterY = rootBounds.getMinY() + (rootBounds.getHeight() / 2);

                startX = sourceCenterX - rootCenterX;
                startY = sourceCenterY - rootCenterY;
            }

            final double finalStartX = startX;
            final double finalStartY = startY;

            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    closeModal(overlay, mainContent, finalStartX, finalStartY);
                }
            });

            controller.setSaveCallback((message) -> {
                loadFlightData();
                closeModal(overlay, mainContent, finalStartX, finalStartY);
                showSuccessNotification("Success!", message);
            });

            controller.setCancelCallback(() -> {
                closeModal(overlay, mainContent, finalStartX, finalStartY);
            });

            StackPane.setAlignment(modalView, Pos.CENTER);

            modalView.setTranslateX(startX);
            modalView.setTranslateY(startY);
            modalView.setScaleX(0.0);
            modalView.setScaleY(0.0);

            rootStackPane.getChildren().add(overlay);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(300), modalView);
            moveCenter.setToX(0);
            // 🌟 ကတ်ကို အောက်ရောက်မသွားစေရန် အပေါ်သို့ 45 Pixels တိုးတင်ပေးထားပါသည် 🌟
            moveCenter.setToY(-45);
            moveCenter.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, moveCenter, fadeIn);
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeModal(StackPane overlay, Node mainContent, double endX, double endY) {
        Node modalView = overlay.getChildren().get(0);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), modalView);
        scaleDown.setToX(0.0);
        scaleDown.setToY(0.0);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        TranslateTransition moveBack = new TranslateTransition(Duration.millis(250), modalView);
        moveBack.setToX(endX);
        moveBack.setToY(endY);
        moveBack.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), overlay);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(scaleDown, moveBack, fadeOut);
        pt.setOnFinished(e -> {
            rootStackPane.getChildren().remove(overlay);
            mainContent.setEffect(null);
        });
        pt.play();
    }

    // =========================================================================
    // 🌟 CUSTOM TOAST NOTIFICATION
    // =========================================================================
    private void showSuccessNotification(String title, String message) {
        rootStackPane.getChildren().removeIf(node -> "custom-toast".equals(node.getId()));

        HBox toast = new HBox(15);
        toast.setId("custom-toast");
        toast.getStyleClass().add("custom-toast-container");
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setMaxWidth(Region.USE_PREF_SIZE);
        toast.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("toast-icon-box");
        SVGPath icon = new SVGPath();
        icon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        icon.getStyleClass().add("toast-icon");
        iconBox.getChildren().add(icon);

        VBox textBox = new VBox();
        textBox.getStyleClass().add("toast-text-box");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("toast-title");
        Label lblMessage = new Label(message);
        lblMessage.getStyleClass().add("toast-message");
        textBox.getChildren().addAll(lblTitle, lblMessage);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(30);

        StackPane closeBtn = new StackPane();
        closeBtn.getStyleClass().add("toast-close-btn");
        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        closeIcon.getStyleClass().add("toast-close-icon");
        closeBtn.getChildren().add(closeIcon);

        toast.getChildren().addAll(iconBox, textBox, spacer, closeBtn);

        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(toast, new Insets(0, 30, 30, 0));

        rootStackPane.getChildren().add(toast);

        toast.setTranslateY(100);
        toast.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toast);
        slideIn.setToY(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast);
        fadeIn.setToValue(1.0);

        ParallelTransition ptIn = new ParallelTransition(slideIn, fadeIn);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), toast);
        slideOut.setToY(100);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setToValue(0.0);

        ParallelTransition ptOut = new ParallelTransition(slideOut, fadeOut);
        ptOut.setOnFinished(e -> rootStackPane.getChildren().remove(toast));

        PauseTransition pause = new PauseTransition(Duration.seconds(3.5));
        pause.setOnFinished(e -> ptOut.play());

        ptIn.setOnFinished(e -> pause.play());

        closeBtn.setOnMouseClicked(e -> {
            pause.stop();
            ptOut.play();
        });

        ptIn.play();
    }
}