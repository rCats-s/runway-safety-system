package org.example.client.components;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import org.example.client.theme.ThemedAlert;
import org.example.client.theme.ThemedDialog;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class HelpBtn extends Button {

    private final String lightstyle = "-fx-background-radius: 30; -fx-max-width: 50; -fx-min-width: 50; -fx-max-height: 50; -fx-min-height: 50; -fx-font-size: 23; -fx-font-weight:bold;-fx-text-fill:-text-primary;"; // was white
    // private final String darkstyle_ = "-fx-background-radius: 30; -fx-max-width: 50; -fx-min-width: 50; -fx-max-height: 50; -fx-min-height: 50; -fx-font-size: 23; -fx-font-weight:bold;-fx-text-fill:white;-fx-font-weight:bold;";
    public HelpBtn() {
     init();
    }

    private void init() {
        setText("?");
        setStyle(lightstyle);
        setOnMouseClicked((e) -> {
            showAlert();
        });
    }

    static class HelpAttrDialog extends ThemedDialog<Void> {
        public HelpAttrDialog() {
            setTitle("Help and Attributions");
            getDialogPane().getButtonTypes().add(ButtonType.OK);

            loadContent();
        }

        @FXML
        private void openLink(ActionEvent event) {
            var source = (Hyperlink) event.getSource();
            var url = (String) source.getUserData();
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void loadContent() {
            try {
                var loader = new FXMLLoader(getClass().getResource("helpbtn-alert.fxml"));
                loader.setController(this);
                Node content = loader.load();

                getDialogPane().setContent(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void showAlert() {
        (new HelpAttrDialog()).showAndWait();
    }
}
