package org.example.client.components;

import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.example.client.ClientApplication;

public class AppMenuBar extends MenuBar {
  @FXML private MenuItem backMenuItem;

  private final ObjectProperty<EventHandler<ActionEvent>> onBackAction =
          new SimpleObjectProperty<>();

  public AppMenuBar() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("appmenubar-component.fxml"));
    loader.setRoot(this);
    loader.setController(this);
    loader.load();
  }

  @FXML
  private void initialize() {
    initComponent();
    setupListeners();
  }

  private void initComponent() {
  }

  private void setupListeners() {
    backMenuItem.disableProperty().bind(onBackAction.isNull());
  }

  @FXML
  private void handleLogoutAction(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/hello-view.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) getScene().getWindow();

    stage.setScene(new Scene(root, 400, 300)); // Adjust size as needed
    stage.show();
  }

  @FXML
  private void handleQuitAction(ActionEvent event) {
    Platform.exit();
    System.exit(0);
  }

  @FXML
  private void handleBackAction(ActionEvent event) throws IOException {
    if (onBackAction.get() != null) onBackAction.get().handle(event);
  }

  public EventHandler<ActionEvent> getOnBackAction() {
    return onBackAction.get();
  }

  public void setOnBackAction(EventHandler<ActionEvent> value) {
    onBackAction.set(value);
  }

  public ObjectProperty<EventHandler<ActionEvent>> onBackActionProperty() {
    return onBackAction;
  }
}
