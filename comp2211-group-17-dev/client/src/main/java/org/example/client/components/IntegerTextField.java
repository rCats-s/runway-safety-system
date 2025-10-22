package org.example.client.components;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

public class IntegerTextField extends TextField {
    public IntegerTextField() {
        var formatter = new TextFormatter<>(new IntegerStringConverter(), 0, change ->
                change.getControlNewText().matches("-?\\d*") ? change : null);

        setTextFormatter(formatter);
    }
}
