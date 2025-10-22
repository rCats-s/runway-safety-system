package org.example.client.theme;

import javafx.scene.control.Alert;
import org.example.client.components.DarkBtn;

public class ThemedAlert extends Alert {
    public ThemedAlert(AlertType alertType) {
        super(alertType);

        ThemeManager.applyStyleSheet(getDialogPane(), getClass().getResource("/org/example/client/stylesheets/dialog.css"));
        ThemeManager.applyTheme(getDialogPane());
    }
}
