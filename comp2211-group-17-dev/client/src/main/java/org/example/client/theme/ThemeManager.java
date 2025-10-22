package org.example.client.theme;

import javafx.scene.Parent;

import javax.swing.plaf.RootPaneUI;
import java.net.URL;

public class ThemeManager {
    private static String currentTheme = Theme.LIGHT_THEME;

    public static void applyStyleSheet(Parent root, URL sheet) {
        var external = sheet.toExternalForm();
        if (!root.getStylesheets().contains(external)) root.getStylesheets().add(external);
    }

    public static void applyTheme(Parent root) {
        root.getStyleClass().remove(Theme.getOpposite(currentTheme));
        root.getStyleClass().add(currentTheme);
    }

    public static void toggleTheme() {
        currentTheme = currentTheme.equals(Theme.LIGHT_THEME) ? Theme.DARK_THEME : Theme.LIGHT_THEME;
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }
}