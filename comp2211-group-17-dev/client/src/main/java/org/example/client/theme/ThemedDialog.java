package org.example.client.theme;

import javafx.scene.control.Dialog;

public class ThemedDialog<R> extends Dialog<R> {
    public ThemedDialog() {
        super();
        ThemeManager.applyStyleSheet(getDialogPane(), getClass().getResource("/org/example/client/stylesheets/dialog.css"));
        ThemeManager.applyTheme(getDialogPane());
    }
}
