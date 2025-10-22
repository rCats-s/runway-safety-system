package org.example.client;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.Data;


public class ClientApplication extends Application {

  private SocketManager connection;

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("hello-view.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 320, 240);
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    stage.setTitle("Runway Re-declaration");
    stage.setScene(scene);
    stage.setWidth(bounds.getWidth() * 1);  // 70% of screen width
    stage.setHeight(bounds.getHeight() * 1); // 70% of screen height
    stage.setScene(scene);
    stage.show();


    connectToServer();
  }

  private void connectToServer() {
    connection = SocketManager.getInstance();

    ServerListener listener = new ServerListener() {
      @Override
      public void onMessageReceived(Data message) {
        Platform.runLater(() -> {
          System.out.println("Server: " + message.toString());
        });
      }

      @Override
      public void onError(Throwable error) {
        if (error == null) {
          System.err.println("Error: No error provided.");
        } else {
          // Print full stack trace for more details
          error.printStackTrace();
          // Or simply print the message if available
          System.err.println("Error: " + (error.getMessage() != null ? error.getMessage() : "No message available"));
        }
      }
    };

    try {
      connection.connect();
      System.out.println("Connected to server.");

      connection.addListener(listener);

      connection.send(new Data("Hello from the client!", 0));
    } catch (IOException e) {
      System.err.println("Failed to connect to server: " + e.getMessage());
    }
  }
}