package com.pos.flightmanagementsystem.controller;

import com.pos.flightmanagementsystem.model.City;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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

                boolean matchCityName = city.getCityName() != null &&
                        city.getCityName().toLowerCase().startsWith(lowerCaseFilter);

                boolean matchCityCode = city.getCityCode() != null &&
                        city.getCityCode().toLowerCase().startsWith(lowerCaseFilter);

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

            // ==========================================
            // 🌟 1. THEME SYNC LOGIC (Light/Dark Auto ပြောင်းရန်)
            // ==========================================
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

            // ==========================================
            // 🌟 2. BACKGROUND FIX (Solid Background ဖျောက်ရန်)
            // ==========================================
            modalView.setStyle("-fx-background-color: transparent;");
            if (modalView instanceof BorderPane) {
                Node centerNode = ((BorderPane) modalView).getCenter();
                if (centerNode != null) {
                    centerNode.setStyle("-fx-background-color: transparent;");
                }
            }

            // ==========================================
            // 🌟 3. BLUR & OVERLAY EFFECT (နောက်ခံကို ဝါးရန်)
            // ==========================================
            StackPane overlay = new StackPane(modalView);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);"); // နောက်ခံကို ခပ်မှိုင်းမှိုင်းလေးဖြစ်စေရန်

            Node mainContent = rootStackPane.getChildren().get(0);
            mainContent.setEffect(new GaussianBlur(12)); // 🌟 Table ကြီးကို Blur ဝါးပေးမည်

            // Modal ပိတ်သွားသည့်အခါ Blur ပြန်ဖြုတ်ပေးရန် Listener
            rootStackPane.getChildren().addListener((ListChangeListener<Node>) c -> {
                while (c.next()) {
                    if (c.wasRemoved() && c.getRemoved().contains(overlay)) {
                        mainContent.setEffect(null); // Blur ကို ဖြုတ်မည်
                    }
                }
            });

            // Cancel နှိပ်၍သော်လည်းကောင်း, အထဲက Modal ပြုတ်သွားလျှင် Overlay ပါ အလိုအလျောက်ပိတ်ရန်
            overlay.getChildren().addListener((ListChangeListener<Node>) c -> {
                if (overlay.getChildren().isEmpty()) {
                    rootStackPane.getChildren().remove(overlay);
                }
            });

            // Card ရဲ့ အပြင်ဘက်ကို နှိပ်မိလျှင် Modal ပိတ်သွားစေရန်
            overlay.setOnMouseClicked(e -> {
                if (e.getTarget() == overlay) {
                    rootStackPane.getChildren().remove(overlay);
                }
            });

            // ==========================================
            // 🌟 4. CONTROLLER & ANIMATION
            // ==========================================
            AdminAddCityController addCityController = loader.getController();
            addCityController.setSaveCallback(() -> {
                loadCityData();
                rootStackPane.getChildren().remove(overlay); // Save နှိပ်လျှင် Modal ပါပိတ်မည်
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
            System.err.println("Could not load AdminAddCity.fxml");
        }
    }
}