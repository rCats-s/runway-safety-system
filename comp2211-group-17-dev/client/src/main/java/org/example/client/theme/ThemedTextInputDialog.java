package org.example.client.theme;

import javafx.scene.control.TextInputDialog;

public class ThemedTextInputDialog extends TextInputDialog {
    public ThemedTextInputDialog(String s) {
        super(s);
        ThemeManager.applyStyleSheet(getDialogPane(), getClass().getResource("/org/example/client/stylesheets/dialog.css"));
        ThemeManager.applyTheme(getDialogPane());
    }
}
