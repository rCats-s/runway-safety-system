package org.example.client.components;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.client.theme.Theme;
import org.example.client.theme.ThemeManager;

public class DarkBtn extends Button {
    private static ImageView imageView;
    private static Image darkmoon;
    private static Image lightmoon;
    public DarkBtn() {
        init();
    }

    private void init() {
        lightmoon = new Image(getClass().getResourceAsStream("/org/example/client/moon.png"));
        darkmoon = new Image(getClass().getResourceAsStream("/org/example/client/darkmoon.png"));

        imageView = new ImageView(lightmoon);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        setGraphic(imageView);
        setStyle("-fx-background-radius: 30; -fx-max-width: 50; -fx-min-width: 50; -fx-max-height: 50; -fx-min-height: 50;");
        setOnAction(event -> toggleTheme());
    }

    public static void setImage(){
        imageView.setImage(ThemeManager.getCurrentTheme() == Theme.DARK_THEME ? darkmoon : lightmoon);
    }

    public void toggleTheme() {
        var root = getScene().getWindow().getScene().getRoot();

        ThemeManager.toggleTheme();
        ThemeManager.applyTheme(root);
    }
}
