package com.pos.flightmanagementsystem.controller;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StaffManageBookingController implements Initializable {

    @FXML private StackPane rootStackPane;
    @FXML private VBox mainContainer;
    @FXML private StackPane waitingModal;

    @FXML private TextField searchField;

    @FXML private Label lblTabAll;
    @FXML private Label lblTabCompleted;
    @FXML private Label lblTabCancelled;

    @FXML private SVGPath modalSpinnerIcon;
    private RotateTransition spinTransition;

    @FXML private TableView<BookingData> bookingTable;
    @FXML private TableColumn<BookingData, String> colBookingId;
    @FXML private TableColumn<BookingData, String> colPassenger;
    @FXML private TableColumn<BookingData, String> colRoute;
    @FXML private TableColumn<BookingData, String> colDate;
    @FXML private TableColumn<BookingData, String> colStatus;
    @FXML private TableColumn<BookingData, Void> colAction;

    private ObservableList<BookingData> bookingList = FXCollections.observableArrayList();

    public static boolean showBookingSuccessToast = false;
    public static String successMessage = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spinTransition = new RotateTransition(Duration.seconds(1.5), modalSpinnerIcon);
        spinTransition.setByAngle(360);
        spinTransition.setCycleCount(Animation.INDEFINITE);
        spinTransition.setInterpolator(Interpolator.LINEAR);

        colBookingId.setCellValueFactory(new PropertyValueFactory<>("pnrCode"));
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passenger"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lblStatus = new Label(status);
                    String baseStyle = "-fx-font-weight: bold; -fx-padding: 4px 12px; -fx-background-radius: 12px; -fx-border-radius: 12px; -fx-border-width: 1px; -fx-font-size: 12px;";

                    switch (status.toLowerCase()) {
                        case "confirmed":
                        case "completed":
                            lblStatus.setStyle(baseStyle + "-fx-text-fill: #31C4B4; -fx-background-color: rgba(49, 196, 180, 0.15); -fx-border-color: #31C4B4;");
                            break;
                        case "cancelled":
                            lblStatus.setStyle(baseStyle + "-fx-text-fill: #FF4B72; -fx-background-color: rgba(255, 75, 114, 0.15); -fx-border-color: #FF4B72;");
                            break;
                        case "cancel pending":
                            lblStatus.setStyle(baseStyle + "-fx-text-fill: #F59E0B; -fx-background-color: rgba(245, 158, 11, 0.15); -fx-border-color: #F59E0B;");
                            break;
                        case "rescheduled":
                            lblStatus.setStyle(baseStyle + "-fx-text-fill: #3B82F6; -fx-background-color: rgba(59, 130, 246, 0.15); -fx-border-color: #3B82F6;");
                            break;
                        default:
                            lblStatus.setStyle(baseStyle + "-fx-text-fill: #8B95A5; -fx-background-color: rgba(139, 149, 165, 0.15); -fx-border-color: #8B95A5;");
                            break;
                    }

                    setGraphic(lblStatus);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        Callback<TableColumn<BookingData, Void>, TableCell<BookingData, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<BookingData, Void> call(final TableColumn<BookingData, Void> param) {
                return new TableCell<>() {
                    private final Button btnChange = new Button("Change Flight");
                    private final Button btnCancel = new Button("Cancel");
                    private final HBox actionBox = new HBox(8);

                    private final HBox waitingBox = new HBox(8);
                    private final ProgressIndicator spinner = new ProgressIndicator();
                    private final Label lblWaiting = new Label("Waiting");

                    {
                        btnChange.setStyle("-fx-background-color: transparent; -fx-border-color: #31C4B4; -fx-text-fill: #31C4B4; -fx-font-weight: bold; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-font-size: 12px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 5px 12px;");
                        btnChange.setOnMouseEntered(e -> {
                            if (!btnChange.isDisabled()) btnChange.setStyle("-fx-background-color: rgba(49, 196, 180, 0.15); -fx-border-color: #31C4B4; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-font-size: 12px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 5px 12px;");
                        });
                        btnChange.setOnMouseExited(e -> {
                            if (!btnChange.isDisabled()) btnChange.setStyle("-fx-background-color: transparent; -fx-border-color: #31C4B4; -fx-text-fill: #31C4B4; -fx-font-weight: bold; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-font-size: 12px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 5px 12px;");
                        });

                        btnChange.setOnAction((ActionEvent event) -> {
                            BookingData data = getTableView().getItems().get(getIndex());
                            checkAndGoToChangeFlightMode(data);
                        });

                        btnCancel.getStyleClass().add("btn-cancel-table");
                        btnCancel.setOnAction((ActionEvent event) -> {
                            BookingData data = getTableView().getItems().get(getIndex());
                            checkAndGoToCancelMode(data);
                        });

                        actionBox.setAlignment(Pos.CENTER);
                        actionBox.getChildren().addAll(btnChange, btnCancel);

                        spinner.setPrefSize(16, 16);
                        spinner.setStyle("-fx-progress-color: #F59E0B;");
                        lblWaiting.getStyleClass().add("waiting-text");

                        waitingBox.setAlignment(Pos.CENTER);
                        waitingBox.getChildren().addAll(spinner, lblWaiting);
                        setAlignment(Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BookingData data = getTableView().getItems().get(getIndex());
                            if (data != null) {
                                if (data.getStatus().equalsIgnoreCase("Cancelled")) {
                                    Label lblCancelled = new Label("Cancelled");
                                    lblCancelled.setStyle("-fx-text-fill: #FF4B72; -fx-font-weight: bold; -fx-font-size: 12px;");
                                    setGraphic(lblCancelled);
                                }
                                else if (data.isCancelPending() || data.getStatus().equalsIgnoreCase("Cancel Pending")) {
                                    setGraphic(waitingBox);
                                }
                                else if (data.getStatus().equalsIgnoreCase("Rescheduled")) {
                                    Label lblRescheduled = new Label("-");
                                    lblRescheduled.setStyle("-fx-text-fill: #8B95A5; -fx-font-weight: bold;");
                                    setGraphic(lblRescheduled);
                                }
                                else {
                                    // 🌟 Time ကို အတိအကျ စစ်ဆေးတဲ့ Logic ကို ခေါ်သုံးထားပါတယ် 🌟
                                    if (data.isLessThan24Hours()) {
                                        btnChange.setDisable(true);
                                        btnChange.setStyle("-fx-background-color: transparent; -fx-border-color: #8B95A5; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-border-width: 1.5px; -fx-cursor: default; -fx-font-size: 12px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 5px 12px; -fx-opacity: 0.5;");
                                    } else {
                                        btnChange.setDisable(false);
                                        btnChange.setStyle("-fx-background-color: transparent; -fx-border-color: #31C4B4; -fx-text-fill: #31C4B4; -fx-font-weight: bold; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-font-size: 12px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 5px 12px;");
                                    }
                                    setGraphic(actionBox);
                                }
                            }
                        }
                    }
                };
            }
        };
        colAction.setPrefWidth(220.0);
        colAction.setCellFactory(cellFactory);

        setupSearchFilter();
        loadBookingsFromDatabase("All");

        Platform.runLater(() -> {
            if (showBookingSuccessToast) {
                showToastNotification("Success", successMessage, false);
                showBookingSuccessToast = false;
            }
        });
    }

    // =========================================================================
    // 🌟 CHANGE FLIGHT LOGIC 🌟
    // =========================================================================
    private void checkAndGoToChangeFlightMode(BookingData data) {
        List<Integer> groupIds = getGroupIdsForPNR(data.getPnrCode());
        if (groupIds.size() > 1) {
            showGroupRescheduleModal(data, groupIds);
        } else {
            List<Integer> singleId = new ArrayList<>(); singleId.add(data.getDbId());
            loadCreateNewBooking(singleId);
        }
    }

    private void showGroupRescheduleModal(BookingData data, List<Integer> groupIds) {
        StackPane overlay = new StackPane(); overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); BoxBlur blur = new BoxBlur(8, 8, 3); mainContainer.setEffect(blur);
        VBox modalCard = new VBox(20); modalCard.setMaxSize(480, Region.USE_PREF_SIZE); modalCard.setStyle("-fx-background-color: #0B0E14; -fx-border-color: #31C4B4; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 24px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 10);");

        HBox header = new HBox(); header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Group Booking Detected"); titleLabel.setStyle("-fx-text-fill: #31C4B4; -fx-font-size: 18px; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); header.getChildren().addAll(titleLabel, spacer);

        Label messageLabel = new Label("This booking (PNR: " + data.getPnrCode() + ") contains " + groupIds.size() + " passengers.\n\nDo you want to reschedule the flight for the ENTIRE GROUP, or ONLY for " + data.getPassenger() + "?");
        messageLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;"); messageLabel.setWrapText(true);

        HBox footer = new HBox(15); footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnOnlyOne = new Button("Only " + data.getPassenger());
        btnOnlyOne.setStyle("-fx-background-color: transparent; -fx-border-color: #8B95A5; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;");
        btnOnlyOne.setOnMouseEntered(e -> btnOnlyOne.setStyle("-fx-background-color: rgba(139, 149, 165, 0.15); -fx-border-color: #FFFFFF; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;"));
        btnOnlyOne.setOnMouseExited(e -> btnOnlyOne.setStyle("-fx-background-color: transparent; -fx-border-color: #8B95A5; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;"));

        Button btnGroup = new Button("Entire Group (" + groupIds.size() + " Pax)");
        btnGroup.setStyle("-fx-background-color: transparent; -fx-border-color: #31C4B4; -fx-text-fill: #31C4B4; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;");
        btnGroup.setOnMouseEntered(e -> btnGroup.setStyle("-fx-background-color: rgba(49, 196, 180, 0.15); -fx-border-color: #31C4B4; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;"));
        btnGroup.setOnMouseExited(e -> btnGroup.setStyle("-fx-background-color: transparent; -fx-border-color: #31C4B4; -fx-text-fill: #31C4B4; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;"));

        footer.getChildren().addAll(btnOnlyOne, btnGroup); modalCard.getChildren().addAll(header, messageLabel, footer); overlay.getChildren().add(modalCard); rootStackPane.getChildren().add(overlay);

        modalCard.setScaleX(0.5); modalCard.setScaleY(0.5); modalCard.setOpacity(0);
        ScaleTransition stIn = new ScaleTransition(Duration.millis(300), modalCard); stIn.setToX(1); stIn.setToY(1); stIn.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ftIn = new FadeTransition(Duration.millis(300), modalCard); ftIn.setToValue(1); ParallelTransition ptIn = new ParallelTransition(stIn, ftIn); ptIn.play();

        Runnable closeAction = () -> {
            ScaleTransition stOut = new ScaleTransition(Duration.millis(200), modalCard); stOut.setToX(0.5); stOut.setToY(0.5); stOut.setInterpolator(Interpolator.EASE_IN);
            FadeTransition ftOut = new FadeTransition(Duration.millis(200), modalCard); ftOut.setToValue(0); ParallelTransition ptOut = new ParallelTransition(stOut, ftOut);
            ptOut.setOnFinished(ev -> { rootStackPane.getChildren().remove(overlay); mainContainer.setEffect(null); }); ptOut.play();
        };

        Button btnCloseX = new Button("X"); btnCloseX.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-cursor: hand;"); btnCloseX.setOnAction(e -> closeAction.run()); header.getChildren().add(btnCloseX);
        btnOnlyOne.setOnAction(e -> { closeAction.run(); List<Integer> singleId = new ArrayList<>(); singleId.add(data.getDbId()); loadCreateNewBooking(singleId); });
        btnGroup.setOnAction(e -> { closeAction.run(); loadCreateNewBooking(groupIds); });
    }

    private void loadCreateNewBooking(List<Integer> oldBookingIds) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/StaffCreateNewBooking.fxml"));
            Parent nextView = loader.load();
            StaffCreateNewBookingController controller = loader.getController(); controller.initChangeFlightMode(oldBookingIds);
            if (nextView.getStylesheets() != null && !nextView.getStylesheets().isEmpty()) {
                ObservableList<String> styles = nextView.getStylesheets();
                for (int i = 0; i < styles.size(); i++) { String css = styles.get(i); if (checkIsDarkMode() && css.contains("Light")) styles.set(i, css.replace("Light", "Dark")); else if (!checkIsDarkMode() && css.contains("Dark")) styles.set(i, css.replace("Dark", "Light")); }
            }
            nextView.setScaleX(0.8); nextView.setScaleY(0.8); nextView.setOpacity(0.0); rootStackPane.getChildren().add(nextView);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainContainer); scaleOut.setToX(1.2); scaleOut.setToY(1.2); FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainContainer); fadeOut.setToValue(0.0);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), nextView); scaleIn.setToX(1.0); scaleIn.setToY(1.0); FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextView); fadeIn.setToValue(1.0);
            ParallelTransition pt = new ParallelTransition(scaleOut, fadeOut, scaleIn, fadeIn); pt.setInterpolator(Interpolator.EASE_BOTH); pt.setOnFinished(e -> rootStackPane.getChildren().remove(mainContainer)); pt.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =========================================================================
    // 🌟 CANCEL FLIGHT LOGIC 🌟
    // =========================================================================
    private void checkAndGoToCancelMode(BookingData data) {
        List<Integer> groupIds = getGroupIdsForPNR(data.getPnrCode());
        if (groupIds.size() > 1) {
            showGroupCancelModal(data, groupIds);
        } else {
            List<Integer> singleId = new ArrayList<>(); singleId.add(data.getDbId());
            openReasonModal(singleId);
        }
    }

    private void showGroupCancelModal(BookingData data, List<Integer> groupIds) {
        StackPane overlay = new StackPane(); overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); BoxBlur blur = new BoxBlur(8, 8, 3); mainContainer.setEffect(blur);

        VBox modalCard = new VBox(20); modalCard.setMaxSize(480, Region.USE_PREF_SIZE); modalCard.setStyle("-fx-background-color: #0B0E14; -fx-border-color: #FF4B72; -fx-border-width: 1.5px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 24px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 10);");

        HBox header = new HBox(); header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Group Cancellation Detected"); titleLabel.setStyle("-fx-text-fill: #FF4B72; -fx-font-size: 18px; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS); header.getChildren().addAll(titleLabel, spacer);

        Label messageLabel = new Label("This booking (PNR: " + data.getPnrCode() + ") contains " + groupIds.size() + " passengers.\n\nDo you want to CANCEL the booking for the ENTIRE GROUP, or ONLY for " + data.getPassenger() + "?");
        messageLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;"); messageLabel.setWrapText(true);

        HBox footer = new HBox(15); footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnOnlyOne = new Button("Only " + data.getPassenger());
        btnOnlyOne.setStyle("-fx-background-color: transparent; -fx-border-color: #8B95A5; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;");
        btnOnlyOne.setOnMouseEntered(e -> btnOnlyOne.setStyle("-fx-background-color: rgba(139, 149, 165, 0.15); -fx-border-color: #FFFFFF; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;"));
        btnOnlyOne.setOnMouseExited(e -> btnOnlyOne.setStyle("-fx-background-color: transparent; -fx-border-color: #8B95A5; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;"));

        Button btnGroup = new Button("Cancel Entire Group (" + groupIds.size() + " Pax)");
        btnGroup.setStyle("-fx-background-color: transparent; -fx-border-color: #FF4B72; -fx-text-fill: #FF4B72; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;");
        btnGroup.setOnMouseEntered(e -> btnGroup.setStyle("-fx-background-color: rgba(255, 75, 114, 0.15); -fx-border-color: #FF4B72; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;"));
        btnGroup.setOnMouseExited(e -> btnGroup.setStyle("-fx-background-color: transparent; -fx-border-color: #FF4B72; -fx-text-fill: #FF4B72; -fx-font-weight: bold; -fx-border-radius: 6px; -fx-padding: 8px 20px; -fx-cursor: hand;"));

        footer.getChildren().addAll(btnOnlyOne, btnGroup); modalCard.getChildren().addAll(header, messageLabel, footer); overlay.getChildren().add(modalCard); rootStackPane.getChildren().add(overlay);

        modalCard.setScaleX(0.5); modalCard.setScaleY(0.5); modalCard.setOpacity(0);
        ScaleTransition stIn = new ScaleTransition(Duration.millis(300), modalCard); stIn.setToX(1); stIn.setToY(1); stIn.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ftIn = new FadeTransition(Duration.millis(300), modalCard); ftIn.setToValue(1); ParallelTransition ptIn = new ParallelTransition(stIn, ftIn); ptIn.play();

        Runnable closeAction = () -> {
            ScaleTransition stOut = new ScaleTransition(Duration.millis(200), modalCard); stOut.setToX(0.5); stOut.setToY(0.5); stOut.setInterpolator(Interpolator.EASE_IN);
            FadeTransition ftOut = new FadeTransition(Duration.millis(200), modalCard); ftOut.setToValue(0); ParallelTransition ptOut = new ParallelTransition(stOut, ftOut);
            ptOut.setOnFinished(ev -> { rootStackPane.getChildren().remove(overlay); mainContainer.setEffect(null); }); ptOut.play();
        };

        Button btnCloseX = new Button("X"); btnCloseX.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B95A5; -fx-font-weight: bold; -fx-cursor: hand;"); btnCloseX.setOnAction(e -> closeAction.run()); header.getChildren().add(btnCloseX);
        btnOnlyOne.setOnAction(e -> { closeAction.run(); List<Integer> singleId = new ArrayList<>(); singleId.add(data.getDbId()); openReasonModal(singleId); });
        btnGroup.setOnAction(e -> { closeAction.run(); openReasonModal(groupIds); });
    }

    private void openReasonModal(List<Integer> bookingIdsToCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/flightmanagementsystem/view/AdminCancelBookingReason.fxml"));
            Parent modalView = loader.load();
            if (rootStackPane.getStylesheets() != null && !rootStackPane.getStylesheets().isEmpty()) {
                String currentMainStyle = rootStackPane.getStylesheets().get(0);
                modalView.getStylesheets().clear();
                if (currentMainStyle.contains("Light")) modalView.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/AdminBookingLight.css").toExternalForm());
                else modalView.getStylesheets().add(getClass().getResource("/com/pos/flightmanagementsystem/Style/AdminBookingDark.css").toExternalForm());
            }

            AdminCancelBookingReasonController controller = loader.getController();
            StackPane overlay = new StackPane(modalView); overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
            GaussianBlur blur = new GaussianBlur(8); mainContainer.setEffect(blur);

            controller.setCallbacks( (reason) -> {
                processCancellationRequest(bookingIdsToCancel, reason);
                closeModal(overlay);
                openWaitingModal();
            }, () -> closeModal(overlay) );

            modalView.setScaleX(0.0); modalView.setScaleY(0.0); rootStackPane.getChildren().add(overlay);
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), modalView); scaleUp.setToX(1.0); scaleUp.setToY(1.0); scaleUp.setInterpolator(Interpolator.EASE_OUT);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay); fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);
            ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn); pt.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void processCancellationRequest(List<Integer> bookingIds, String reason) {
        int staffId = (LoginController.loggedInUser != null) ? LoginController.loggedInUser.getUserId() : 1;
        String insertRequestQuery = "INSERT INTO requests (booking_id, staff_id, reason, status) VALUES (?, ?, ?, 'Pending')";
        String updateBookingQuery = "UPDATE bookings SET status = 'Cancel Pending' WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement insertStmt = conn.prepareStatement(insertRequestQuery);
                 PreparedStatement updateStmt = conn.prepareStatement(updateBookingQuery)) {

                for (int bookingId : bookingIds) {
                    insertStmt.setInt(1, bookingId);
                    insertStmt.setInt(2, staffId);
                    insertStmt.setString(3, reason);
                    insertStmt.executeUpdate();

                    updateStmt.setInt(1, bookingId);
                    updateStmt.executeUpdate();
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            String currentTab = "All";
            if (lblTabCompleted.getStyleClass().contains("active-tab")) currentTab = "Completed";
            else if (lblTabCancelled.getStyleClass().contains("active-tab")) currentTab = "Cancelled";
            loadBookingsFromDatabase(currentTab);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // =========================================================================
    // 🌟 UTILITY METHODS 🌟
    // =========================================================================
    private List<Integer> getGroupIdsForPNR(String pnrCode) {
        List<Integer> groupIds = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT b.booking_id FROM bookings b WHERE b.pnr_code = ? AND b.status IN ('Confirmed', 'Completed')")) {
            stmt.setString(1, pnrCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) { groupIds.add(rs.getInt("booking_id")); }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return groupIds;
    }

    private void showToastNotification(String title, String message, boolean isError) {
        HBox toast = new HBox(15);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setMaxWidth(380); toast.setMinHeight(80); toast.setMaxHeight(80);
        String borderColor = isError ? "#EF4444" : "#31C4B4"; String bgColor = "#0B0E14"; String titleColor = "#FFFFFF"; String messageColor = "#8B95A5";
        toast.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + borderColor + "; -fx-border-width: 1.5px; -fx-background-radius: 14px; -fx-border-radius: 14px; -fx-padding: 16px 18px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 8);");

        StackPane iconBox = new StackPane();
        iconBox.setStyle("-fx-background-color: " + borderColor + "20; -fx-background-radius: 50%; -fx-min-width: 38px; -fx-min-height: 38px; -fx-max-width: 38px; -fx-max-height: 38px;");
        SVGPath icon = new SVGPath();
        if (isError) icon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        else icon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        icon.setFill(Color.web(borderColor)); icon.setScaleX(1.2); icon.setScaleY(1.2); iconBox.getChildren().add(icon);

        VBox textContainer = new VBox(4); textContainer.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(textContainer, Priority.ALWAYS);
        HBox titleBox = new HBox(); titleBox.setAlignment(Pos.TOP_LEFT);
        Label lblTitle = new Label(title); lblTitle.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Segoe UI';");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button(); SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        closeIcon.setFill(Color.web(messageColor)); closeIcon.setScaleX(0.8); closeIcon.setScaleY(0.8); btnClose.setGraphic(closeIcon);
        btnClose.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        btnClose.setOnMouseEntered(e -> closeIcon.setFill(Color.web("#FF4B72"))); btnClose.setOnMouseExited(e -> closeIcon.setFill(Color.web(messageColor)));
        titleBox.getChildren().addAll(lblTitle, spacer, btnClose);

        Label lblMessage = new Label(message); lblMessage.setStyle("-fx-text-fill: " + messageColor + "; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        lblMessage.setWrapText(true); lblMessage.setMaxWidth(280);
        textContainer.getChildren().addAll(titleBox, lblMessage); toast.getChildren().addAll(iconBox, textContainer);

        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT); StackPane.setMargin(toast, new Insets(0, 30, 100, 0));
        rootStackPane.getChildren().add(toast); toast.setTranslateY(120); toast.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toast); slideIn.setToY(0); slideIn.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast); fadeIn.setToValue(1.0);
        ParallelTransition ptIn = new ParallelTransition(slideIn, fadeIn);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), toast); slideOut.setToY(120); slideOut.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast); fadeOut.setToValue(0.0);
        ParallelTransition ptOut = new ParallelTransition(slideOut, fadeOut); ptOut.setOnFinished(e -> rootStackPane.getChildren().remove(toast));

        PauseTransition delay = new PauseTransition(Duration.seconds(3.5)); delay.setOnFinished(ev -> ptOut.play());
        ptIn.setOnFinished(e -> delay.play()); btnClose.setOnAction(e -> { delay.stop(); ptOut.play(); }); ptIn.play();
    }

    private boolean checkIsDarkMode() {
        if (rootStackPane.getStylesheets() == null || rootStackPane.getStylesheets().isEmpty()) return true;
        return rootStackPane.getStylesheets().get(0).contains("Dark");
    }

    private void setupSearchFilter() {
        FilteredList<BookingData> filteredData = new FilteredList<>(bookingList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(booking -> {
                if (newValue == null || newValue.isEmpty() || newValue.isBlank()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (booking.getPnrCode().toLowerCase().startsWith(lowerCaseFilter)) return true;
                else if (booking.getPassenger().toLowerCase().startsWith(lowerCaseFilter)) return true;
                return false;
            });
        });
        SortedList<BookingData> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(bookingTable.comparatorProperty());
        bookingTable.setItems(sortedData);
    }

    @FXML void filterAll(MouseEvent event) { setActiveTab(lblTabAll); loadBookingsFromDatabase("All"); }
    @FXML void filterCompleted(MouseEvent event) { setActiveTab(lblTabCompleted); loadBookingsFromDatabase("Completed"); }
    @FXML void filterCancelled(MouseEvent event) { setActiveTab(lblTabCancelled); loadBookingsFromDatabase("Cancelled"); }

    private void setActiveTab(Label activeLabel) {
        lblTabAll.getStyleClass().remove("active-tab"); lblTabCompleted.getStyleClass().remove("active-tab"); lblTabCancelled.getStyleClass().remove("active-tab");
        activeLabel.getStyleClass().add("active-tab");
    }

    private void loadBookingsFromDatabase(String filterType) {
        bookingList.clear();
        String query = "SELECT b.booking_id, b.pnr_code, p.full_name AS passenger_name, f.flight_number, dep_city.city_code AS dep_code, arr_city.city_code AS arr_code, DATE(f.departure_time) AS dep_date, f.departure_time, b.status FROM bookings b JOIN passengers p ON b.passenger_id = p.passenger_id JOIN flights f ON b.flight_id = f.flight_id JOIN cities dep_city ON f.departure_city_id = dep_city.city_id JOIN cities arr_city ON f.arrival_city_id = arr_city.city_id ";
        if (filterType.equals("Completed")) query += "WHERE b.status IN ('Confirmed', 'Completed') ";
        else if (filterType.equals("Cancelled")) query += "WHERE b.status IN ('Cancelled', 'Cancel Pending') ";
        query += "ORDER BY b.booking_date DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Timestamp depTimestamp = rs.getTimestamp("departure_time");
                LocalDateTime departureTime = (depTimestamp != null) ? depTimestamp.toLocalDateTime() : null;

                BookingData data = new BookingData(
                        rs.getInt("booking_id"),
                        rs.getString("pnr_code"),
                        rs.getString("passenger_name"),
                        rs.getString("flight_number") + " (" + rs.getString("dep_code") + "-" + rs.getString("arr_code") + ")",
                        rs.getString("dep_date"),
                        rs.getString("status"),
                        departureTime
                );

                if (data.getStatus().equalsIgnoreCase("Cancel Pending")) data.setCancelPending(true);
                bookingList.add(data);
            }
        } catch (Exception e) { e.printStackTrace(); }
        bookingTable.refresh();
    }

    private void closeModal(StackPane overlay) {
        Parent modalView = (Parent) overlay.getChildren().get(0);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), modalView); scaleDown.setToX(0.0); scaleDown.setToY(0.0); scaleDown.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), overlay); fadeOut.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut);
        pt.setOnFinished(e -> { rootStackPane.getChildren().remove(overlay); mainContainer.setEffect(null); }); pt.play();
    }

    private void openWaitingModal() {
        waitingModal.setVisible(true); spinTransition.play(); GaussianBlur blur = new GaussianBlur(15); mainContainer.setEffect(blur);
        waitingModal.setScaleX(0.5); waitingModal.setScaleY(0.5);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), waitingModal); scaleUp.setToX(1.0); scaleUp.setToY(1.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), waitingModal); fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);
        ParallelTransition pt = new ParallelTransition(scaleUp, fadeIn); pt.setInterpolator(Interpolator.EASE_OUT); pt.play();
    }

    @FXML void closeWaitingModal(ActionEvent event) {
        if (mainContainer.getEffect() instanceof GaussianBlur) {
            GaussianBlur blur = (GaussianBlur) mainContainer.getEffect();
            Timeline blurOut = new Timeline(new KeyFrame(Duration.millis(250), new KeyValue(blur.radiusProperty(), 0))); blurOut.setOnFinished(e -> mainContainer.setEffect(null)); blurOut.play();
        }
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(250), waitingModal); scaleDown.setToX(0.5); scaleDown.setToY(0.5);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), waitingModal); fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(scaleDown, fadeOut); pt.setInterpolator(Interpolator.EASE_IN);
        pt.setOnFinished(e -> { waitingModal.setVisible(false); spinTransition.stop(); }); pt.play();
    }

    public static class BookingData {
        private int dbId;
        private String pnrCode, passenger, route, date, status;
        private boolean cancelPending = false;
        private LocalDateTime departureTime;

        public BookingData(int dbId, String pnrCode, String passenger, String route, String date, String status, LocalDateTime departureTime) {
            this.dbId = dbId; this.pnrCode = pnrCode; this.passenger = passenger; this.route = route; this.date = date; this.status = status;
            this.departureTime = departureTime;
        }

        public int getDbId() { return dbId; }
        public String getPnrCode() { return pnrCode; }
        public String getPassenger() { return passenger; }
        public String getRoute() { return route; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isCancelPending() { return cancelPending; }
        public void setCancelPending(boolean cancelPending) { this.cancelPending = cancelPending; }

        // 🌟 ပြင်ဆင်ထားသော စက္ကန့်တိကျသည့် ၂၄ နာရီ Logic 🌟
        public boolean isLessThan24Hours() {
            if (departureTime == null) return false;
            LocalDateTime now = LocalDateTime.now();

            // လက်ရှိအချိန်ကနေ ရှေ့ကို ၂၄ နာရီ အတိအကျ ပေါင်းထည့်လိုက်ပါတယ်
            LocalDateTime exactly24HoursLater = now.plusHours(24);

            // Database ထဲက Flight ထွက်မယ့်အချိန် (departureTime) ဟာ exactly24HoursLater ထက် မနောက်ကျဘူးဆိုရင် (စောနေတယ် သို့ အတူတူပဲဆိုရင်)
            // ရည်ရွယ်ချက် - ၂၄ နာရီ အတိအကျ သို့မဟုတ် အောက်ရောက်သွားပြီမို့ True (Disable) ပြန်ပေးမည်
            return !departureTime.isAfter(exactly24HoursLater);
        }
    }
}