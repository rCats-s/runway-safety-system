package org.example.client;

//THis is the welcome scene controller!!!!!

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.Data;
import org.example.Protocols;
import org.example.User;
import org.example.client.components.DarkBtn;
import org.example.client.components.HelpBtn;
import org.example.client.components.RevealBtn;
import org.example.client.theme.ThemeManager;
import org.example.client.theme.ThemedAlert;

public class ClientController {
  public StackPane root;
  public ImageView sotonlogo;
  public DarkBtn darkBtn;
  public RevealBtn revealbtn;
  public TextField passwordShow;
  public HelpBtn helpbtn;

  @FXML
  private Button helloButton;
  @FXML
  private TextField username;
  @FXML
  private PasswordField password;

  private ServerListener loginListener;


  @FXML
  protected void onHelloButtonClick() {
    loginhandling();
  }

  private void loadnextscene(User user) throws IOException {
    FXMLLoader loader = switch (user.getRole()) {
        case "ATC", "Runway Staff" ->
            new FXMLLoader(getClass().getResource("/org/example/client/calcSelectScene.fxml"));
        case "Admin" ->
            new FXMLLoader(getClass().getResource("/org/example/client/adminViewScene.fxml"));
        default -> throw new RuntimeException("Role is invalid so could not load suitable view");
    };
    Parent root = loader.load(); // Properly load the FXML
    Scene newScene = new Scene(root, 400, 300);
    Stage stage = (Stage) helloButton.getScene().getWindow();

    // Set the new scene
    stage.setScene(newScene);
    stage.show();
  }

  @FXML
  public void initialize() {
    ThemeManager.applyStyleSheet(root, getClass().getResource("/org/example/client/stylesheets/style.css"));
    ThemeManager.applyTheme(root);

    Image img = new Image(getClass().getResource("University_of_Southampton_Logo.png").toExternalForm());
    sotonlogo.setImage(img);
    sotonlogo.fitHeightProperty().bind(root.heightProperty().multiply(0.2));
    sotonlogo.setPreserveRatio(true);

    passwordShow.textProperty().bindBidirectional(password.textProperty());
    revealbtn.setOnAction(e -> {
      if (!revealbtn.isShowing()){
        passwordShow.setVisible(true);
        passwordShow.setManaged(true);
        password.setVisible(false);
        password.setManaged(false);
      } else {
        passwordShow.setVisible(false);
        passwordShow.setManaged(false);
        password.setVisible(true);
        password.setManaged(true);
      }
      revealbtn.toggleShowing();
    });
    
    
    var connection = SocketManager.getInstance();

    loginListener = new ServerListener() {
      @Override
      public void onMessageReceived(Data message) {
        System.out.println("Message received");
        Platform.runLater(() -> { // must be on JavaFX thread as the socket runs in a different one
          Alert alert = new ThemedAlert(Alert.AlertType.INFORMATION);
          switch (message.getMessage()) {
            case Protocols.LOGGED_IN -> Platform.runLater(() -> {
              User user = (User) message.getValue();
              alert.setHeaderText("Login Successful");
              alert.setContentText("Welcome back, "+ username.getText()+"!");
              alert.showAndWait();
              try {
                connection.removeListener(loginListener);
                loadnextscene(user);
              } catch (IOException e) {
                throw new RuntimeException("IO error! " + e);
              }
            });
            case Protocols.FAILED_LOGIN -> Platform.runLater(() -> {
              alert.setHeaderText("Invalid credentials..");
              alert.setContentText("Username or password incorrect. Please try again.");
              alert.showAndWait();});
          }
        });
      }

      @Override
      public void onError(Throwable error) {
        System.err.println("Error: cc err " + error.getLocalizedMessage());
      }
    };

    connection.addListener(loginListener);
  }


  private void loginhandling() {
    var connection = SocketManager.getInstance();
    String username = this.username.getText();
    String password = this.password.getText();
    try {
      connection.send(new Data(Protocols.USER_LOGIN, new String[]{username, password}));
      System.out.println("USER_LOGIN sent");
    } catch (IOException e) {
      System.err.println("Failed to connect to server: " + e.getMessage());
    }
  }
}