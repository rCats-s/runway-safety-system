package org.example.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.*;
import org.example.client.components.IntegerTextField;
import org.example.Data;
import org.example.Protocols;
import org.example.User;
import org.example.client.components.DarkBtn;
import org.example.client.components.HelpBtn;
import org.example.client.theme.ThemeManager;
import org.example.client.theme.ThemedAlert;
import org.example.client.theme.ThemedDialog;

public class AdminViewController {

    public ListView<User> listView;
    public ListView<Airport> listViewAirport;

    public VBox root;

    public HelpBtn helpbtn;
    public HelpBtn helpbtn2;

    private ObservableList<User> listViewItems;
    public ObservableList<Airport> listViewAirportItems;

    private ServerListener serverListener;

    @FXML
    public void initialize() throws IOException {
        ThemeManager.applyStyleSheet(root, getClass().getResource("/org/example/client/stylesheets/calcscene.css"));
        ThemeManager.applyTheme(root);

        var connection = SocketManager.getInstance();

        connection.send(new Data(Protocols.LOAD_USERS, null));
        connection.send(new Data(Protocols.GET_AIRPORTS, null));

        serverListener = new ServerListener() {
            @Override
            public void onMessageReceived(Data message) {
                if (Objects.equals(message.getMessage(), Protocols.LOADED_USERS)) {
                    listViewItems = FXCollections.observableArrayList((ArrayList<User>) message.getValue());
                    listView.setItems(listViewItems);
                }
                if (Objects.equals(message.getMessage(), Protocols.USER_CREATED)) {
                    Platform.runLater(() -> listViewItems.add((User) message.getValue()));
                }
                if (message.getMessage().equals(Protocols.USER_DELETED)) {
                }
                if (message.getMessage().equals(Protocols.USER_UPDATED)) {
                    Platform.runLater(() -> listViewItems.add((User) message.getValue()));
                }
                if (message.getMessage().equals(Protocols.USER_UPDATE_FAILED)) {
                    showErrorMsg("User update failed!");
                }
                if (message.getMessage().equals(Protocols.USER_CREATION_FAILED)) {
                    showErrorMsg("User creation failed!");
                }
                if (message.getMessage().equals(Protocols.USER_DELETION_FAILED)) {
                    showErrorMsg("Failed to delete user!");
                }
                if (Objects.equals(message.getMessage(), Protocols.AIRPORTS_RETRIEVED)) {
                    listViewAirportItems = FXCollections.observableArrayList((ArrayList<Airport>) message.getValue());
                    listViewAirport.setItems(listViewAirportItems);
                }
                if (Objects.equals(message.getMessage(), Protocols.AIRPORT_CREATED)) {
                    Platform.runLater(() -> listViewAirportItems.add((Airport) message.getValue()));
                }
                if (message.getMessage().equals(Protocols.AIRPORT_DELETED)) {
                }
                if (message.getMessage().equals(Protocols.AIRPORT_UPDATED)) {
                    Platform.runLater(() -> listViewAirportItems.add((Airport) message.getValue()));
                }
                if (message.getMessage().equals(Protocols.AIRPORT_CREATION_FAILED)) {
                    showErrorMsg("Airport creation failed!");
                }
                if (message.getMessage().equals(Protocols.AIRPORT_DELETION_FAILED)) {
                    showErrorMsg("Airport deletion failed!");
                }
                if (message.getMessage().equals(Protocols.AIRPORTS_RETRIEVAL_FAILED)) {
                    showErrorMsg("Airport retrieval failed!");
                }
            }

            @Override
            public void onError(Throwable error) {
            }
        };

        connection.addListener(serverListener);

        listView.setOnMouseClicked(event -> {
            User selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                System.out.println("Clicked on: " + selectedItem);
                try {
                    showUserModify(selectedItem);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        listViewAirport.setOnMouseClicked(event -> {
            Airport selectedItem = listViewAirport.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                System.out.println("Clicked on: " + selectedItem);
                try {
                    showAirportModify(selectedItem);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void showAirportModify(Airport airport) throws IOException {
        var connection = SocketManager.getInstance();

        Dialog<ButtonType> dialog = new ThemedDialog<>();
        dialog.setTitle("Modify Airport");
        dialog.setHeaderText("Enter the new airport details");

        ButtonType modifyButtonType = new ButtonType("Modify", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, modifyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Name");
        name.setText(airport.getName());
        TextField code = new TextField();
        code.setDisable(true);
        code.setPromptText("Code");
        code.setText(airport.getCode());
        TextField location = new TextField();
        location.setPromptText("Location");
        location.setText(airport.getLocation());

        ListView<Runway> runwayListView = new ListView<>();
        runwayListView.setOnMouseClicked(event -> {
            Runway selectedItem = runwayListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                System.out.println("Clicked on: " + selectedItem);
                showRunwayModify(selectedItem, runwayListView);
            }
        });


        var createNewRunwayBtn = new Button("Create New Runway");
        createNewRunwayBtn.onActionProperty().set((e) -> onCreateNewRunwayClicked(e, airport));
        var runwayListener = new ServerListener() {
            @Override
            public void onMessageReceived(Data message) {
                if (message.getMessage().equals(Protocols.RUNWAYS_RETRIEVED)) {
                    Platform.runLater(() -> {
                        var runwayListViewItems = FXCollections.observableArrayList((ArrayList<Runway>) message.getValue());
                        runwayListView.setItems(runwayListViewItems);
                    });
                }
                if (Objects.equals(message.getMessage(), Protocols.RUNWAY_CREATED)) {
                    Platform.runLater(() -> runwayListView.getItems().add((Runway) message.getValue()));
                }
                if (message.getMessage().equals(Protocols.RUNWAY_DELETED)) {
                }
                if (message.getMessage().equals(Protocols.RUNWAY_UPDATED)) {
                    Platform.runLater(() -> runwayListView.getItems().add((Runway) message.getValue()));
                }
                if (message.getMessage().equals(Protocols.RUNWAY_CREATION_FAILED)) {
                    showErrorMsg("Runway creation failed!");
                }
                if (message.getMessage().equals(Protocols.RUNWAY_DELETION_FAILED)) {
                    showErrorMsg("Runway deletion failed!");
                }
                if (message.getMessage().equals(Protocols.RUNWAYS_RETRIEVAL_FAILED)) {
                    showErrorMsg("Runway retrieval failed!");
                }
            }

            @Override
            public void onError(Throwable error) {
            }
        };
        connection.addListener(runwayListener);

        connection.send(new Data(Protocols.GET_RUNWAYS, airport.getId()));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(code, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(location, 1, 2);
        grid.add(new Label("Runways:"), 0, 3);
        grid.add(runwayListView, 0, 4);
        GridPane.setColumnSpan(runwayListView,2);
        grid.add(createNewRunwayBtn, 0, 5);

        dialog.getDialogPane().setContent(grid);

        var result = dialog.showAndWait();
        result.ifPresent(btn -> {
            if (btn.getText().equals("Modify")) {
                if (name.getText().isEmpty() || code.getText().isEmpty() || location.getText().isEmpty()) {
                    showErrorMsg("Invalid input, some of the fields are empty.");
                } else {
                    airport.setCode(code.getText());
                    airport.setLocation(location.getText());
                    airport.setName(name.getText());
                    try {
                        connection.send(new Data(Protocols.UPDATE_AIRPORT, airport));
                        listViewAirportItems.remove(airport);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (btn.getText().equals("Delete")) {
                try {
                    connection.send(new Data(Protocols.DELETE_AIRPORT, airport));
                    listViewAirportItems.remove(airport);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        connection.removeListener(runwayListener);
    }

    public void showRunwayModify(Runway runway, ListView<Runway> runwayListView) {
        var connection = SocketManager.getInstance();

        Dialog<ButtonType> dialog = new ThemedDialog<>();
        dialog.setTitle("Modify Runway");
        dialog.setHeaderText("Enter the new runway details");

        ButtonType modifyButtonType = new ButtonType("Modify", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, modifyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        IntegerTextField clearway = new IntegerTextField();
        clearway.setPromptText("Clearway");
        clearway.setText(String.valueOf(runway.getClearway()));
        TextField designation = new TextField();
        designation.setPromptText("Designation");
        designation.setDisable(true);
        designation.setText(runway.getDesignation());
        IntegerTextField threshold = new IntegerTextField();
        threshold.setPromptText("Displaced Threshold");
        threshold.setText(String.valueOf(runway.getDisplacedThreshold()));
        IntegerTextField ASDA = new IntegerTextField();
        ASDA.setPromptText("Original ASDA");
        ASDA.setText(String.valueOf(runway.getOriginalASDA()));
        IntegerTextField LDA = new IntegerTextField();
        LDA.setPromptText("Original LDA");
        LDA.setText(String.valueOf(runway.getOriginalLDA()));
        IntegerTextField TODA = new IntegerTextField();
        TODA.setPromptText("Original TODA");
        TODA.setText(String.valueOf(runway.getOriginalTODA()));
        IntegerTextField TORA = new IntegerTextField();
        TORA.setPromptText("Original TORA");
        TORA.setText(String.valueOf(runway.getOriginalTORA()));

        grid.add(new Label("Clearway:"), 0, 0);
        grid.add(clearway, 1, 0);
        grid.add(new Label("Designation:"), 0, 1);
        grid.add(designation, 1, 1);
        grid.add(new Label("Displaced Threshold:"), 0, 2);
        grid.add(threshold, 1, 2);
        grid.add(new Label("Original ASDA:"), 0, 3);
        grid.add(ASDA, 1, 3);
        grid.add(new Label("Original LDA:"), 0, 4);
        grid.add(LDA, 1, 4);
        grid.add(new Label("Original TODA:"), 0, 5);
        grid.add(TODA, 1, 5);
        grid.add(new Label("Original TORA:"), 0, 6);
        grid.add(TORA, 1, 6);

        dialog.getDialogPane().setContent(grid);

        var result = dialog.showAndWait();
        result.ifPresent(btn -> {
            if (btn.getText().equals("Modify")) {
                if (clearway.getText().isEmpty() || designation.getText().isEmpty() || threshold.getText().isEmpty() || ASDA.getText().isEmpty() || LDA.getText().isEmpty() || TODA.getText().isEmpty() || TORA.getText().isEmpty()) {
                    showErrorMsg("Invalid input, some of the fields are empty.");
                } else {
                    runway.setClearway(((TextFormatter<Integer>) clearway.getTextFormatter()).getValue());
                    runway.setDesignation(designation.getText());
                    runway.setDisplacedThreshold(((TextFormatter<Integer>) threshold.getTextFormatter()).getValue());
                    runway.setOriginalASDA(((TextFormatter<Integer>) ASDA.getTextFormatter()).getValue());
                    runway.setOriginalLDA(((TextFormatter<Integer>) LDA.getTextFormatter()).getValue());
                    runway.setOriginalTODA(((TextFormatter<Integer>) TODA.getTextFormatter()).getValue());
                    runway.setOriginalTORA(((TextFormatter<Integer>) TORA.getTextFormatter()).getValue());
                    try {
                        connection.send(new Data(Protocols.UPDATE_RUNWAY, runway));
                        runwayListView.getItems().remove(runway);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (btn.getText().equals("Delete")) {
                try {
                    connection.send(new Data(Protocols.DELETE_RUNWAY, runway));
                    runwayListView.getItems().remove(runway);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void onCreateNewClicked() {
        var connection = SocketManager.getInstance();
        Dialog<ButtonType> dialog = new ThemedDialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Enter the user details");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        ComboBox<String> roles = new ComboBox<>(
                FXCollections.observableArrayList("ATC", "Runway Staff", "Admin")
        );
        roles.setPromptText("Select Role for User");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roles, 1, 2);

        dialog.getDialogPane().setContent(grid);

        var result = dialog.showAndWait();
        result.ifPresent(btn -> {
            if (btn.getText().equals("Create")) {
                if (username.getText().isEmpty() || password.getText().isEmpty() || roles.getValue() == null) {
                    showErrorMsg("Invalid input, some of the fields are empty.");
                } else {
                    var newUser = new User(username.getText(), password.getText(), roles.getValue());
                    try {
                        connection.send(new Data(Protocols.CREATE_USER, newUser));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void showUserModify(User user) throws IOException {
        var connection = SocketManager.getInstance();
        Dialog<ButtonType> dialog = new ThemedDialog<>();
        dialog.setTitle("Modify User");
        dialog.setHeaderText("Enter the new user details");

        ButtonType modifyButtonType = new ButtonType("Modify", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, modifyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setDisable(true);
        username.setText(user.getUsername());
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setText(user.getPassword());
        ComboBox<String> roles = new ComboBox<>(
                FXCollections.observableArrayList("ATC", "Runway Staff", "Admin")
        );
        roles.setPromptText("Select Role for User");
        roles.setValue(user.getRole());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roles, 1, 2);

        dialog.getDialogPane().setContent(grid);

        var result = dialog.showAndWait();
        result.ifPresent(btn -> {
            if (btn.getText().equals("Modify")) {
                if (password.getText().isEmpty() || roles.getValue() == null) {
                    showErrorMsg("Invalid input, some of the fields are empty.");
                } else {
                    user.setPassword(password.getText());
                    user.setRole(roles.getValue());
                    try {
                        connection.send(new Data(Protocols.UPDATE_USER, user));
                        listViewItems.remove(user);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (btn.getText().equals("Delete")) {
                try {
                    connection.send(new Data(Protocols.DELETE_USER, user));
                    listViewItems.remove(user);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    static public void showErrorMsg(String msg) {
        Platform.runLater(() -> {
            Alert alert = new ThemedAlert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("There was an error doing that request");
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public void onCreateNewRunwayClicked(ActionEvent actionEvent, Airport airport) {
        var connection = SocketManager.getInstance();

        Dialog<ButtonType> dialog = new ThemedDialog<>();
        dialog.setTitle("Create Runway");
        dialog.setHeaderText("Enter the new runway details");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        IntegerTextField clearway = new IntegerTextField();
        clearway.setPromptText("Clearway");
        TextField designation = new TextField();
        designation.setPromptText("Designation");
        IntegerTextField threshold = new IntegerTextField();
        threshold.setPromptText("Displaced Threshold");
        IntegerTextField ASDA = new IntegerTextField();
        ASDA.setPromptText("Original ASDA");
        IntegerTextField LDA = new IntegerTextField();
        LDA.setPromptText("Original LDA");
        IntegerTextField TODA = new IntegerTextField();
        TODA.setPromptText("Original TODA");
        IntegerTextField TORA = new IntegerTextField();
        TORA.setPromptText("Original TORA");

        grid.add(new Label("Clearway:"), 0, 0);
        grid.add(clearway, 1, 0);
        grid.add(new Label("Designation:"), 0, 1);
        grid.add(designation, 1, 1);
        grid.add(new Label("Displaced Threshold:"), 0, 2);
        grid.add(threshold, 1, 2);
        grid.add(new Label("Original ASDA:"), 0, 3);
        grid.add(ASDA, 1, 3);
        grid.add(new Label("Original LDA:"), 0, 4);
        grid.add(LDA, 1, 4);
        grid.add(new Label("Original TODA:"), 0, 5);
        grid.add(TODA, 1, 5);
        grid.add(new Label("Original TORA:"), 0, 6);
        grid.add(TORA, 1, 6);

        dialog.getDialogPane().setContent(grid);

        var result = dialog.showAndWait();
        result.ifPresent(btn -> {
            if (btn.getText().equals("Create")) {
                if (clearway.getText().isEmpty() || designation.getText().isEmpty() || threshold.getText().isEmpty() || ASDA.getText().isEmpty() || LDA.getText().isEmpty() || TODA.getText().isEmpty() || TORA.getText().isEmpty()) {
                    showErrorMsg("Invalid input, some of the fields are empty.");
                } else {
                    var runway = new Runway();
                    runway.setAirportId(airport.getId());
                    runway.setClearway(((TextFormatter<Integer>) clearway.getTextFormatter()).getValue());
                    runway.setDesignation(designation.getText());
                    runway.setDisplacedThreshold(((TextFormatter<Integer>) threshold.getTextFormatter()).getValue());
                    runway.setOriginalASDA(((TextFormatter<Integer>) ASDA.getTextFormatter()).getValue());
                    runway.setOriginalLDA(((TextFormatter<Integer>) LDA.getTextFormatter()).getValue());
                    runway.setOriginalTODA(((TextFormatter<Integer>) TODA.getTextFormatter()).getValue());
                    runway.setOriginalTORA(((TextFormatter<Integer>) TORA.getTextFormatter()).getValue());
                    try {
                        connection.send(new Data(Protocols.CREATE_RUNWAY, runway));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public void onCreateNewAirportClicked(ActionEvent actionEvent) {
        var connection = SocketManager.getInstance();

        Dialog<ButtonType> dialog = new ThemedDialog<>();
        dialog.setTitle("Create Airport");
        dialog.setHeaderText("Enter the new airport details");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Name");
        TextField code = new TextField();
        code.setPromptText("Code");
        TextField location = new TextField();
        location.setPromptText("Location");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(code, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(location, 1, 2);

        dialog.getDialogPane().setContent(grid);

        var result = dialog.showAndWait();
        result.ifPresent(btn -> {
            if (btn.getText().equals("Create")) {
                if (name.getText().isEmpty() || code.getText().isEmpty() || location.getText().isEmpty()) {
                    showErrorMsg("Invalid input, some of the fields are empty.");
                } else {
                    var airport = new Airport();
                    airport.setCode(code.getText());
                    airport.setLocation(location.getText());
                    airport.setName(name.getText());
                    try {
                        connection.send(new Data(Protocols.CREATE_AIRPORT, airport));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
