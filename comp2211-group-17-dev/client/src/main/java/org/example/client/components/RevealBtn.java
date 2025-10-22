package org.example.client.components;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class RevealBtn extends Button {
    public RevealBtn() {
        init();
    }

    private void init() {
        getStyleClass().addAll("reveal-image", "reveal-open");
        setStyle("-fx-background-radius: 30; -fx-max-width: 40; -fx-min-width: 40; -fx-max-height: 40; -fx-min-height: 40;");
    }

    public boolean isShowing() {
        return getStyleClass().contains("reveal-closed");
    }

    public void setShowing(boolean showing) {
        getStyleClass().removeAll("reveal-open", "reveal-closed");
        getStyleClass().add(showing ? "reveal-closed" : "reveal-open");
    }

    public void toggleShowing() {
        setShowing(!isShowing());
    }
}
