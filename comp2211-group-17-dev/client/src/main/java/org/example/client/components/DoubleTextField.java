package org.example.client.components;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.math.BigDecimal;

public class DoubleTextField extends TextField {
  public DoubleTextField() {
    var formatter = new TextFormatter<>(new BigDecimalStringConverter(), BigDecimal.ZERO, change ->
        change.getControlNewText().matches("-?\\d*(\\.\\d*)?") ? change : null);

    setTextFormatter(formatter);
  }

  // using DoubleStringConverter breaks when too many zeros are used leading to scientific notation being used
  static class BigDecimalStringConverter extends StringConverter<BigDecimal> {
    public BigDecimalStringConverter() {
    }

    @Override
    public String toString(BigDecimal var) {
      return (var != null) ? var.toPlainString() : "";
    }

    @Override
    public BigDecimal fromString(String var) {
      var text = var != null ? var.trim() : null;
      if (text == null || text.isEmpty()) return null;
      try {
        return new BigDecimal(text);
      } catch (NumberFormatException e) {
        return BigDecimal.ZERO;
      }
    }
  }
}
