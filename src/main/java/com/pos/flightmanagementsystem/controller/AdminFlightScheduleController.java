package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.Flight;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
import java.util.ResourceBundle;

public class AdminFlightScheduleController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private Button btnAddFlight;
    @FXML private DatePicker dpFilterDate;

    @FXML private TableView<Flight> tblFlights;
    @FXML private TableColumn<Flight, Integer> colFlightId;
    @FXML private TableColumn<Flight, String> colFlightNo;
    @FXML private TableColumn<Flight, String> colAirline;
    @FXML private TableColumn<Flight, String> colFrom;
    @FXML private TableColumn<Flight, String> colTo;
    @FXML private TableColumn<Flight, String> colDeparture;
    @FXML private TableColumn<Flight, String> colArrival;
    @FXML private TableColumn<Flight, Integer> colTotalSeats;
    @FXML private TableColumn<Flight, Double> colBasePrice;
    @FXML private TableColumn<Flight, String> colStatus;
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
        colFlightId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightId()));
        colFlightNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFlightNumber()));
        colAirline.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAirline()));
        colFrom.setCellValueFactory(cellData -> new SimpleStringProperty(getCityName(cellData.getValue().getDepartureCityId())));
        colTo.setCellValueFactory(cellData -> new SimpleStringProperty(getCityName(cellData.getValue().getArrivalCityId())));
        colDeparture.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartureTime().format(formatter)));
        colArrival.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getArrivalTime().format(formatter)));
        colTotalSeats.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalSeats()));
        colBasePrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBasePrice()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        colStatus.setCellFactory(column -> new TableCell<Flight, String>() {
            private final Label statusLabel = new Label();
            {
                statusLabel.getStyleClass().add("table-status-pill");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    statusLabel.setText(item);
                    statusLabel.getStyleClass().removeAll("pill-scheduled", "pill-departed", "pill-delayed", "pill-cancelled");

                    String lowerItem = item.toLowerCase();
                    if (lowerItem.contains("schedule")) {
                        statusLabel.getStyleClass().add("pill-scheduled");
                    } else if (lowerItem.contains("depart")) {
                        statusLabel.getStyleClass().add("pill-departed");
                    } else if (lowerItem.contains("delay")) {
                        statusLabel.getStyleClass().add("pill-delayed");
                    } else {
                        statusLabel.getStyleClass().add("pill-cancelled");
                    }

                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        setupActionColumn();
    }

    private void loadFlightData() {
        flightList.clear();
        LocalDate selectedDate = (dpFilterDate != null) ? dpFilterDate.getValue() : null;
        // ပြီးခဲ့တဲ့ အချိန်တွေကို မပြတော့ဘဲ NOW() ကနေစပြီး ရှေ့လာမည့်ဟာတွေကိုပဲ ပြမည်
        String sql = "SELECT * FROM flights WHERE departure_time >= NOW()";

        // Date ရွေးပြီး ရှာခဲ့ရင် WHERE အစား AND နဲ့ ဆက်ပေးရပါမည်
        if (selectedDate != null) sql += " AND DATE(departure_time) = ?";

        // အချိန်အစဉ်လိုက်လေး သပ်သပ်ရပ်ရပ်ဖြစ်သွားအောင် ORDER BY ပါ တစ်ခါတည်း ထည့်ပေးလိုက်ပါတယ်
        sql += " ORDER BY departure_time ASC";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (selectedDate != null) {
                    pstmt.setDate(1, java.sql.Date.valueOf(selectedDate));
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Flight flight = new Flight();
                        flight.setFlightId(rs.getInt("flight_id"));
                        flight.setFlightNumber(rs.getString("flight_number"));
                        flight.setAirline(rs.getString("airline"));
                        flight.setDepartureCityId(rs.getInt("departure_city_id"));
                        flight.setArrivalCityId(rs.getInt("arrival_city_id"));
                        flight.setDepartureCityName(getCityName(rs.getInt("departure_city_id")));
                        flight.setArrivalCityName(getCityName(rs.getInt("arrival_city_id")));
                        flight.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
                        flight.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
                        flight.setTotalSeats(rs.getInt("total_seats"));
                        flight.setBasePrice(rs.getDouble("base_price"));
                        flight.setStatus(rs.getString("status"));

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
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

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

    // ==========================================
    // 🌟 DELETE FLIGHT & VALIDATION LOGIC 🌟
    // ==========================================
    private void deleteFlight(Flight flight) {
        String checkBookingsQuery = "SELECT COUNT(*) FROM bookings WHERE flight_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkBookingsQuery)) {

            checkStmt.setInt(1, flight.getFlightId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // 🌟 မြန်မာစာအစား English လို Professional ကျကျ ပြောင်းထားပါသည် 🌟
                    showWarningModal(
                            "Action Denied",
                            "This flight cannot be deleted because there are active bookings associated with it.\n\nIf the flight will no longer operate, please use the 'Edit' button to change its status to 'Cancelled'.",
                            false
                    );
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // 🌟 လက်မှတ် ရောင်းချထားခြင်း မရှိပါက Custom Confirmation Modal ပြမည် 🌟
        showConfirmationModal("Confirm Deletion", "Are you sure you want to delete flight " + flight.getFlightNumber() + "?", () -> {
            String sql = "DELETE FROM flights WHERE flight_id = ?";
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) return;
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, flight.getFlightId());
                    if (pstmt.executeUpdate() > 0) {
                        flightList.remove(flight);
                        showSuccessNotification("Success!", "Flight deleted successfully.");

                        int adminId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;
                        insertLog(adminId, "Deleted flight schedule: " + flight.getFlightNumber() + " (Flight ID: " + flight.getFlightId() + ")");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
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

            if (rootStackPane != null && !rootStackPane.getStylesheets().isEmpty()) {
                String currentMainStyle = rootStackPane.getStylesheets().get(0);
                modalView.getStylesheets().clear();

                if (currentMainStyle.contains("Light")) {
                    modalView.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/AdminAddFlightLight.css").toExternalForm());
                } else {
                    modalView.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/AdminAddFlightDark.css").toExternalForm());
                }
            }

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
            moveCenter.setToY(-110);
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
        closeBtn.setOnMouseClicked(e -> { pause.stop(); ptOut.play(); });
        ptIn.play();
    }

    // =========================================================================
    // 🌟 CUSTOM WARNING MODAL (ANIMATED & BLURRED) 🌟
    // =========================================================================
    private void showWarningModal(String title, String message, boolean isError) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        Node mainContent = rootStackPane.getChildren().get(0);
        BoxBlur blur = new BoxBlur(8, 8, 3);
        mainContent.setEffect(blur);

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
        closeBtn.setOnMouseEntered(e -> closeIcon.setStyle("-fx-fill: #FFFFFF;"));
        closeBtn.setOnMouseExited(e -> closeIcon.setStyle("-fx-fill: #8B95A5;"));

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
        okBtn.setOnMouseEntered(e -> okBtn.setStyle("-fx-background-color: " + hoverBgColor + "; -fx-border-color: " + themeColor + "; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px;"));
        okBtn.setOnMouseExited(e -> okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + themeColor + "; -fx-text-fill: " + themeColor + "; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px;"));

        footer.getChildren().add(okBtn);

        modalCard.getChildren().addAll(header, body, footer);
        overlay.getChildren().add(modalCard);
        rootStackPane.getChildren().add(overlay);

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

        Runnable closeAction = () -> {
            ScaleTransition stOut = new ScaleTransition(Duration.millis(200), modalCard);
            stOut.setToX(0.5); stOut.setToY(0.5); stOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition ftOut = new FadeTransition(Duration.millis(200), modalCard);
            ftOut.setToValue(0);

            FadeTransition bgFtOut = new FadeTransition(Duration.millis(200), overlay);
            bgFtOut.setToValue(0);

            ParallelTransition ptOut = new ParallelTransition(stOut, ftOut, bgFtOut);
            ptOut.setOnFinished(ev -> {
                rootStackPane.getChildren().remove(overlay);
                mainContent.setEffect(null);
            });
            ptOut.play();
        };

        closeBtn.setOnMouseClicked(e -> closeAction.run());
        okBtn.setOnAction(e -> closeAction.run());
    }

    // =========================================================================
    // 🌟 CUSTOM CONFIRMATION MODAL (ANIMATED & BLURRED) 🌟
    // =========================================================================
    private void showConfirmationModal(String title, String message, Runnable onConfirm) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        Node mainContent = rootStackPane.getChildren().get(0);
        BoxBlur blur = new BoxBlur(8, 8, 3);
        mainContent.setEffect(blur);

        String themeColor = "#EF4444"; // Red for Deletion Warning
        String hoverBgColor = "rgba(239, 68, 68, 0.15)";

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
        closeBtn.setOnMouseEntered(e -> closeIcon.setStyle("-fx-fill: #FFFFFF;"));
        closeBtn.setOnMouseExited(e -> closeIcon.setStyle("-fx-fill: #8B95A5;"));

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        HBox body = new HBox(15);
        body.setAlignment(Pos.CENTER_LEFT);
        SVGPath warningIcon = new SVGPath();
        warningIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"); // Warning Triangle
        warningIcon.setStyle("-fx-fill: " + themeColor + "; -fx-scale-x: 1.5; -fx-scale-y: 1.5;");
        StackPane iconPane = new StackPane(warningIcon);
        iconPane.setPadding(new Insets(0, 10, 0, 10));

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        body.getChildren().addAll(iconPane, messageLabel);

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-cursor: hand;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-cursor: hand;"));

        Button confirmBtn = new Button("Delete");
        confirmBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + themeColor + "; -fx-text-fill: " + themeColor + "; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px; -fx-cursor: hand;");
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle("-fx-background-color: " + hoverBgColor + "; -fx-border-color: " + themeColor + "; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px;"));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + themeColor + "; -fx-text-fill: " + themeColor + "; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6px 24px;"));

        footer.getChildren().addAll(cancelBtn, confirmBtn);

        modalCard.getChildren().addAll(header, body, footer);
        overlay.getChildren().add(modalCard);
        rootStackPane.getChildren().add(overlay);

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

        Runnable closeAction = () -> {
            ScaleTransition stOut = new ScaleTransition(Duration.millis(200), modalCard);
            stOut.setToX(0.5); stOut.setToY(0.5); stOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition ftOut = new FadeTransition(Duration.millis(200), modalCard);
            ftOut.setToValue(0);

            FadeTransition bgFtOut = new FadeTransition(Duration.millis(200), overlay);
            bgFtOut.setToValue(0);

            ParallelTransition ptOut = new ParallelTransition(stOut, ftOut, bgFtOut);
            ptOut.setOnFinished(ev -> {
                rootStackPane.getChildren().remove(overlay);
                mainContent.setEffect(null);
            });
            ptOut.play();
        };

        closeBtn.setOnMouseClicked(e -> closeAction.run());
        cancelBtn.setOnAction(e -> closeAction.run());

        confirmBtn.setOnAction(e -> {
            closeAction.run();
            onConfirm.run(); // 🌟 Confirm နှိပ်ပါက Database တွင် သွားဖျက်မည် 🌟
        });
    }
}