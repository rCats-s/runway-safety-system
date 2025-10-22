package org.example.client.components;

import org.example.client.Calculation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

// ok simple wrapper just to be able to draw pdf table, if it breaks something then....
public class SimpleTableView extends VBox {

  private final TableView<Calculation> tableView = new TableView<>();
  private final TableColumn<Calculation, Number> toraColumn = new TableColumn<>("TORA");
  private final TableColumn<Calculation, Number> todaColumn = new TableColumn<>("TODA");
  private final TableColumn<Calculation, Number> asdaColumn = new TableColumn<>("ASDA");
  private final TableColumn<Calculation, Number> ldaColumn = new TableColumn<>("LDA");
  private final TableColumn<Calculation, Number> timeColumn = new TableColumn<>("Time Stamp");

  // Inner class for table data structure
  public static class TableData {
    private int originalTORA, calculatedTORA, diffTORA;
    private int originalTODA, calculatedTODA, diffTODA;
    private int originalASDA, calculatedASDA, diffASDA;
    private int originalLDA, calculatedLDA, diffLDA;
    private String explanationTORA, explanationTODA, explanationASDA, explanationLDA;
    
    // Getters
    public int getOriginalTORA() { return originalTORA; }
    public int getCalculatedTORA() { return calculatedTORA; }
    public int getDiffTORA() { return diffTORA; }
    public int getOriginalTODA() { return originalTODA; }
    public int getCalculatedTODA() { return calculatedTODA; }
    public int getDiffTODA() { return diffTODA; }
    public int getOriginalASDA() { return originalASDA; }
    public int getCalculatedASDA() { return calculatedASDA; }
    public int getDiffASDA() { return diffASDA; }
    public int getOriginalLDA() { return originalLDA; }
    public int getCalculatedLDA() { return calculatedLDA; }
    public int getDiffLDA() { return diffLDA; }
    public String getExplanationTORA() { return explanationTORA; }
    public String getExplanationTODA() { return explanationTODA; }
    public String getExplanationASDA() { return explanationASDA; }
    public String getExplanationLDA() { return explanationLDA; }
  }

  // Inner class for table row representation
  private static class CustomTableRow {
    private final String parameter;
    private final int original;
    private final int calculated;
    private final int diff;
    private final String explanation;
    
    public CustomTableRow(String parameter, int original, int calculated, int diff, String explanation) {
      this.parameter = parameter;
      this.original = original;
      this.calculated = calculated;
      this.diff = diff;
      this.explanation = explanation;
    }
    
    public String getParameter() { return parameter; }
    public int getOriginal() { return original; }
    public int getCalculated() { return calculated; }
    public int getDiff() { return diff; }
    public String getExplanation() { return explanation; }
  }

  public SimpleTableView() {
    setupColumns();
    tableView.getColumns().addAll(toraColumn, todaColumn, asdaColumn, ldaColumn, timeColumn);
    getChildren().add(tableView);
  }

  private void setupColumns() {
    toraColumn.setCellValueFactory(cellData -> cellData.getValue().getUiTora());
    todaColumn.setCellValueFactory(cellData -> cellData.getValue().getUiToda());
    asdaColumn.setCellValueFactory(cellData -> cellData.getValue().getUiAsda());
    ldaColumn.setCellValueFactory(cellData -> cellData.getValue().getUiLda());
    timeColumn.setCellValueFactory(cellData -> cellData.getValue().getTime());
  }

  public void setCalculation(Calculation calculation) {
    updateItems(calculation);
  }

  private void updateItems(Calculation calculation) {
    if (calculation != null) {
      ObservableList<Calculation> items = FXCollections.observableArrayList(calculation);
      tableView.setItems(items);
    } else {
      tableView.getItems().clear();
    }
  }

  //items for direct access
  public ObservableList<Calculation> getItems() {
    return tableView.getItems();
  }

  public TableData getData() {
    TableData data = new TableData();

    if (tableView != null && tableView.getItems() != null && !tableView.getItems().isEmpty()) {
      Calculation calc = tableView.getItems().get(0);
      
      // TORA data
      data.originalTORA = calc.getInitetora(); // Changed from getOriginalTora
      data.calculatedTORA = calc.getUiTora().getValue().intValue();
      data.diffTORA = data.calculatedTORA - data.originalTORA;
      data.explanationTORA = getExplanationFor("TORA");
      
      // TODA data
      data.originalTODA = calc.getInitetoda(); // Changed from getOriginalToda
      data.calculatedTODA = calc.getUiToda().getValue().intValue();
      data.diffTODA = data.calculatedTODA - data.originalTODA;
      data.explanationTODA = getExplanationFor("TODA");
      
      // ASDA data
      data.originalASDA = calc.getIniteasda(); // Changed from getOriginalAsda
      data.calculatedASDA = calc.getUiAsda().getValue().intValue();
      data.diffASDA = data.calculatedASDA - data.originalASDA;
      data.explanationASDA = getExplanationFor("ASDA");
      
      // LDA data
      data.originalLDA = calc.getInitelda(); // Changed from getOriginalLda
      data.calculatedLDA = calc.getUiLda().getValue().intValue();
      data.diffLDA = data.calculatedLDA - data.originalLDA;
      data.explanationLDA = getExplanationFor("LDA");
    }
    
    return data;
  }
  
  //placeholder for now
  private String getExplanationFor(String parameter) {
    return parameter + " calculation based on obstacle position and aircraft type";
  }


  private CustomTableRow getRowByParameter(String parameter) {
    if (tableView.getItems().isEmpty()) return null;
    
    Calculation calc = tableView.getItems().get(0);
    switch (parameter) {
      case "TORA":
        return new CustomTableRow(
            "TORA", 
            calc.getInitetora(),
            calc.getUiTora().getValue().intValue(),
            calc.getUiTora().getValue().intValue() - calc.getInitetora(),
            getExplanationFor("TORA"));
      case "TODA":
        return new CustomTableRow(
            "TODA", 
            calc.getInitetoda(),
            calc.getUiToda().getValue().intValue(),
            calc.getUiToda().getValue().intValue() - calc.getInitetoda(), 
            getExplanationFor("TODA"));
      case "ASDA":
        return new CustomTableRow(
            "ASDA", 
            calc.getIniteasda(), 
            calc.getUiAsda().getValue().intValue(),
            calc.getUiAsda().getValue().intValue() - calc.getIniteasda(), 
            getExplanationFor("ASDA"));
      case "LDA":
        return new CustomTableRow(
            "LDA", 
            calc.getInitelda(),
            calc.getUiLda().getValue().intValue(),
            calc.getUiLda().getValue().intValue() - calc.getInitelda(),
            getExplanationFor("LDA"));
      default:
        return null;
    }
  }
}
