package org.example.client.theme;

public class Theme {
    public static final String LIGHT_THEME = "light-theme";
    public static final String DARK_THEME = "dark-theme";

    public static String getOpposite(String theme) {
        return theme == LIGHT_THEME ? DARK_THEME : LIGHT_THEME;
    }
}
