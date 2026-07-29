package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.City;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AdminCityController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private Button btnAddCity;

    @FXML private TextField txtSearchCity;

    @FXML private TableView<City> tableCities;
    @FXML private TableColumn<City, Integer> colCityId;
    @FXML private TableColumn<City, String> colCityCode;
    @FXML private TableColumn<City, String> colCityName;
    @FXML private TableColumn<City, String> colCountry;
    @FXML private TableColumn<City, LocalDateTime> colCreatedAt;

    private ObservableList<City> cityList;
    private FilteredList<City> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colCityId.setCellValueFactory(new PropertyValueFactory<>("cityId"));
        colCityCode.setCellValueFactory(new PropertyValueFactory<>("cityCode"));
        colCityName.setCellValueFactory(new PropertyValueFactory<>("cityName"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        cityList = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(cityList, b -> true);

        setupSearchFilter();

        SortedList<City> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableCities.comparatorProperty());
        tableCities.setItems(sortedData);

        loadCityData();
    }

    private void setupSearchFilter() {
        txtSearchCity.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(city -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                boolean matchCityName = city.getCityName() != null && city.getCityName().toLowerCase().startsWith(lowerCaseFilter);
                boolean matchCityCode = city.getCityCode() != null && city.getCityCode().toLowerCase().startsWith(lowerCaseFilter);
                return matchCityName || matchCityCode;
            });
        });
    }

    private void loadCityData() {
        cityList.clear();
        String query = "SELECT * FROM cities";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int cityId = rs.getInt("city_id");
                String cityCode = rs.getString("city_code");
                String cityName = rs.getString("city_name");
                String country = rs.getString("country");

                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = null;
                if (timestamp != null) {
                    createdAt = timestamp.toLocalDateTime();
                }

                cityList.add(new City(cityId, cityCode, cityName, country, createdAt));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Database ကနေ City Data များ ဆွဲထုတ်ရာတွင် အမှားအယွင်းဖြစ်နေပါသည်။");
        }
    }

    @FXML
    void clickAddCity(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminAddCity.fxml"));
            Parent modalView = loader.load();

            ObservableList<String> currentStyles = rootStackPane.getStylesheets();
            boolean isLightMode = currentStyles.stream().anyMatch(css -> css.contains("Light"));

            ObservableList<String> modalStyles = modalView.getStylesheets();
            for (int i = 0; i < modalStyles.size(); i++) {
                String css = modalStyles.get(i);
                if (isLightMode && css.contains("Dark")) {
                    modalStyles.set(i, css.replace("Dark", "Light"));
                } else if (!isLightMode && css.contains("Light")) {
                    modalStyles.set(i, css.replace("Light", "Dark"));
                }
            }

            modalView.setStyle("-fx-background-color: transparent;");
            if (modalView instanceof BorderPane) {
                Node centerNode = ((BorderPane) modalView).getCenter();
                if (centerNode != null) {
                    centerNode.setStyle("-fx-background-color: transparent;");
                }
            }

            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

            Node mainContent = rootStackPane.getChildren().get(0);
            mainContent.setEffect(new GaussianBlur(12));

            rootStackPane.getChildren().addListener((ListChangeListener<Node>) c -> {
                while (c.next()) {
                    if (c.wasRemoved() && c.getRemoved().contains(overlay)) {
                        mainContent.setEffect(null);
                    }
                }
            });

            overlay.getChildren().addListener((ListChangeListener<Node>) c -> {
                if (overlay.getChildren().isEmpty()) {
                    rootStackPane.getChildren().remove(overlay);
                }
            });

            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    rootStackPane.getChildren().remove(overlay);
                }
            });

            AdminAddCityController addCityController = loader.getController();

            // 🌟 Save နှိပ်လျှင် Data ပြန်ခေါ်ပြီး Custom Notification ပြမည်
            addCityController.setSaveCallback(() -> {
                loadCityData();
                rootStackPane.getChildren().remove(overlay);
                showSuccessNotification("Success!", "City added successfully.");
            });

            // 🌟 Cancel နှိပ်လျှင် Modal ပိတ်မည်
            addCityController.setCancelCallback(() -> {
                rootStackPane.getChildren().remove(overlay);
            });

            Bounds buttonBounds = btnAddCity.localToScene(btnAddCity.getBoundsInLocal());
            double rootCenterX = rootStackPane.getWidth() / 2;
            double rootCenterY = rootStackPane.getHeight() / 2;
            double buttonCenterX = buttonBounds.getMinX() + (buttonBounds.getWidth() / 2);
            double buttonCenterY = buttonBounds.getMinY() + (buttonBounds.getHeight() / 2);

            modalView.setTranslateX(buttonCenterX - rootCenterX);
            modalView.setTranslateY(buttonCenterY - rootCenterY);
            modalView.setScaleX(0.0);
            modalView.setScaleY(0.0);

            rootStackPane.getChildren().add(overlay);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(350), modalView);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            TranslateTransition moveCenter = new TranslateTransition(Duration.millis(350), modalView);
            moveCenter.setToX(0);
            moveCenter.setToY(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), overlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition pt = new ParallelTransition(scaleUp, moveCenter, fadeIn);
            pt.setInterpolator(Interpolator.EASE_OUT);
            pt.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // 🌟 CUSTOM TOAST NOTIFICATION 🌟
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