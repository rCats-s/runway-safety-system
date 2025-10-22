package org.example.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Calculation;
import org.example.Data;
import org.example.Protocols;
import org.example.client.components.DarkBtn;
import org.example.client.components.HelpBtn;
import org.example.client.theme.ThemeManager;
import org.example.client.theme.ThemedTextInputDialog;

// rename ts in the future like wtf?
public class CalcSelectSceneController {

  // make it not just string in the future e.g. calculations and modify toString to change how they are displayed
  public ListView<Calculation> listView;
    public HelpBtn helpbtn;
  @FXML
  public VBox root;
  private ObservableList<Calculation> listViewItems;

  private ServerListener serverListener;

  @FXML
  public void initialize() throws IOException {
    ThemeManager.applyStyleSheet(root, getClass().getResource("/org/example/client/stylesheets/calcscene.css"));
    ThemeManager.applyTheme(root);

    var connection = SocketManager.getInstance();

    connection.send(new Data(Protocols.LOAD_CALCS, null));
    serverListener = new ServerListener() {
      @Override
      public void onMessageReceived(Data message) {
        if (Objects.equals(message.getMessage(), Protocols.LOADED_CALCS)) {
          System.out.println(message.getValue());
          listViewItems = FXCollections.observableArrayList((ArrayList<Calculation>) message.getValue());
          listView.setItems(listViewItems);
        }
        if (Objects.equals(message.getMessage(), Protocols.CALC_CREATED)) {
          // in the future if the created user is this user go to the main scene with the created calculation rather than just adding it to the list
          Platform.runLater(() -> listViewItems.add((Calculation) message.getValue()));
        }
      }

      @Override
      public void onError(Throwable error) {}
    };

    connection.addListener(serverListener);

    listView.setOnMouseClicked(event -> {
      Calculation selectedItem = listView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        System.out.println("Clicked on: " + selectedItem);
        try {
          showMainScene(selectedItem);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  public void onCreateNewClicked() {
    var connection = SocketManager.getInstance();
    System.out.println("Create a new");
    TextInputDialog dialog = new ThemedTextInputDialog("");
    dialog.setTitle("Create");
    dialog.setHeaderText("Enter a name:");
    dialog.setContentText("Name:");

    var result = dialog.showAndWait();
    result.ifPresent(name -> {
      var newCalc = new Calculation(name);
      try {
        connection.send(new Data(Protocols.CREATE_CALC, newCalc));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private void showMainScene(Calculation calculation) throws IOException {
    SocketManager.getInstance().removeListener(serverListener);
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/org/example/client/visualScene.fxml"));
    Parent root = loader.load(); // Properly load the FXML

    // Get the current stage
    Stage stage = (Stage) listView.getScene().getWindow();

    // Set the new scene
    stage.setScene(new Scene(root, 400, 300)); // Adjust size as needed
    stage.show();

    ((MainSceneController) loader.getController()).setServerCalculation(calculation);
  }
}
