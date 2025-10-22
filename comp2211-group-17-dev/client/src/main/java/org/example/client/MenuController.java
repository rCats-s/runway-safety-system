package org.example.client;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class MenuController {

    @FXML
    private AnchorPane menuBarRoot;

    @FXML
    public void initialize() {

    }

    @FXML
    private void setDarkMode(){
        Scene scene = menuBarRoot.getScene();
        if (scene != null) {scene.getStylesheets().clear(); scene.getStylesheets().add(
            getClass().getResource("/org/example/client/stylesheets/dark.css").toExternalForm());} else {
            System.out.println("scene is null");}
    }
    public AnchorPane loadMenuBar() throws IOException, IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/components/appmenubar-component.fxml"));
        loader.setController(this);
        return loader.load();
    }


}
