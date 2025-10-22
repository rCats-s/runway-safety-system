package org.example.client;

import jakarta.xml.bind.JAXBException;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

import org.example.Airport;
import org.example.Data;
import org.example.Protocols;
import org.example.client.components.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.SnapshotParameters;
import javafx.embed.swing.SwingFXUtils;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.example.client.theme.ThemeManager;
import org.example.client.theme.ThemedAlert;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class MainSceneController {

  private final SimpleDoubleProperty obstaclePosition = new SimpleDoubleProperty();
  @FXML
  public VBox root;
  private SimpleStringProperty thresholdno = new SimpleStringProperty("T");

  public DoubleTextField obstacleHeight;
  public HelpBtn helpbtn;
  @FXML
  public Label status;
  public ComboBox<org.example.Airport> prefillAirportSelector;
  public ComboBox<org.example.Runway> prefillRunwaySelector;
  @FXML
  private Label path;
  private SimpleStringProperty xmlfilename = new SimpleStringProperty("");

  private SimpleStringProperty thresholdno2 = new SimpleStringProperty("X");

  private String flightStatus;
  @FXML
  private ComboBox<String> runwaySelector;
  @FXML
  private IntegerTextField thresholdVal;
  private File xmlfile;
  @FXML
  public Runway runwayComponent;
  @FXML
  private DoubleTextField obstacleLTDist;
  @FXML
  private DoubleTextField obstacleUTDist;
  @FXML
  private DoubleTextField obstacleCLDist;
  @FXML
  private ObstacleSelector obstacleSelector;
  @FXML
  private PlanesSelector planesSelector;
  @FXML
  private ComboBox LRSelector;
  @FXML
  private SimpleTableView tableView;
  @FXML
  private Label threshold = new Label("");
  @FXML
  private ComboBox aircraftStatus;
  private String selectedRunway = "09L";
  private Calculation calc;
  private String runwayPair;
  private String LRrunway;

  private Alert alert = new ThemedAlert(Alert.AlertType.INFORMATION);

  @FXML
  private ComboBox<String> topsideComboBox;

  private org.example.Calculation serverCalc;
  private ServerListener calcUpdateListener;
  private static boolean isSideOn = true;

  @FXML
  public void initialize() throws IOException {
    ThemeManager.applyStyleSheet(root, getClass().getResource("/org/example/client/stylesheets/style.css"));
    ThemeManager.applyTheme(root);

    var connection = SocketManager.getInstance();


    var airportRunwayListener = new ServerListener() {
      @Override
      public void onMessageReceived(Data message) {
        if (message.getMessage().equals(Protocols.AIRPORTS_RETRIEVED)) {
          Platform.runLater(() -> receivedAirports((List<Airport>)message.getValue()));
        }
        if (message.getMessage().equals(Protocols.RUNWAYS_RETRIEVED)) {
          Platform.runLater(() -> receivedRunways((List<org.example.Runway>)message.getValue()));
        }
        if (message.getMessage().equals(Protocols.AIRPORTS_RETRIEVAL_FAILED)) {
          AdminViewController.showErrorMsg("Airport retrieval failed");
        }
        if (message.getMessage().equals(Protocols.RUNWAYS_RETRIEVAL_FAILED)) {
          AdminViewController.showErrorMsg("Runway retrieval failed");
        }

      }

      @Override
      public void onError(Throwable error) {}
    };

    connection.send(new Data(Protocols.GET_AIRPORTS, null));
    connection.addListener(airportRunwayListener);


    prefillAirportSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal == null) return;
      try {
            connection.send(new Data(Protocols.GET_RUNWAYS, newVal.getCode()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
    prefillRunwaySelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
     if (newVal == null) return;

      calc.getUiAsda().setValue(newVal.getOriginalASDA());
      calc.getUiLda().setValue(newVal.getOriginalLDA());
      calc.getUiToda().setValue(newVal.getOriginalTODA());
      calc.getUiTora().setValue(newVal.getOriginalTORA());

      thresholdVal.setText(String.valueOf(newVal.getDisplacedThreshold()));
      calc.setThreshold(newVal.getDisplacedThreshold());

      updateCalculations();
    });


    //Blank testing for the no runway option
    calc = new Calculation(
            0, 0, 0, 0,
            0, 0, 0,
            0, 0, 0, 0, 0
    ,'e');
    tableView.setCalculation(calc);

    // add obstacles
    obstacleSelector.addObstacles(
            new Obstacle("Rock", new ImageView(
                    new Image(getClass().getResourceAsStream("/org/example/client/rock2.png"))), 20, 20,10),
            new Obstacle("Plane", new ImageView(
                    new Image(getClass().getResourceAsStream("/org/example/client/plane.png"))), 200, 130,100),
            new Obstacle("Bird", new ImageView(
                    new Image(getClass().getResourceAsStream("/org/example/client/bird2.png"))), 20, 20, 1)
    );
    planesSelector.addPlane(
            new Planes("Beechcraft King Air", new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/Beechcraft_King_air_B200_per.png"))), 41.75, 25, 350),
            new Planes("Boeing 747 100", new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/Boeing-747-100_com.png"))),167 , 100, 450),
            new Planes("Learjet 60", new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/Learjet-60-com.png"))), 33.4, 20, 300),
            new Planes("Boeing C-17", new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/C-17_cargo.png"))), 208.75, 125, 450),
            new Planes("Airbus A320", new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/norebbo-a320-com.png"))),250.5 , 150, 500)
            );
    threshold.textProperty().bind(thresholdno);
    runwayComponent.thresholdIdent().bind(thresholdno);
    //listener for change in selected runway
    runwaySelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      selectedRunway = newVal;
      String[] options=selectedRunway.split("/");
      LRSelector.getItems().clear();
      LRSelector.getItems().addAll(options);
      LRSelector.setValue(options[0]);
      thresholdno.set(options[0]);
      //updateThreshold();
    });
    LRSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      try {
        thresholdno2.set(newVal.toString());
      } catch (NullPointerException e) {
        System.err.println("Null value encountered in LRSelector");
      }
    });

    path.textProperty().bind(xmlfilename);

    runwaySelector.getSelectionModel().selectFirst();


    // this input was annoying asf to setup. good luck if u make changes
    TextFormatter<BigDecimal> formatter = (TextFormatter<BigDecimal>) obstacleLTDist.getTextFormatter();
    formatter.valueProperty()
            .addListener((obs, oldVal, newVal) -> obstaclePosition.set(newVal == null ? 0 : newVal.doubleValue()));
    obstaclePosition.addListener(
            (obs, oldVal, newVal) -> Platform.runLater(() -> formatter.setValue(BigDecimal.valueOf(newVal.doubleValue()))));

    runwayComponent.obstaclePositionProperty().bindBidirectional(obstaclePosition);

    runwayComponent.setVisibility(false);


    //listener for the ComboBox for the side-on or top-down view
    topsideComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null && newValue.equals("Side On View")) {
                    runwayComponent.setVisibility(false);
                    isSideOn = true;
                } else {
                    runwayComponent.setVisibility(true);
                    runwayComponent.obstacleWrapperToFront();
                    isSideOn = false;
                    //runwayComponent.setTDMeasurements(calc.getToda());
                }
            }
        });

    planesSelector.selectedPlaneProperty().addListener((obs, oldPlane, newPlane) -> {updatePlaneVisualization(); });
    aircraftStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      runwayComponent.setTakeOffLabel(newVal.toString());
      updatePlaneVisualization();
    });
    LRSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {updatePlaneVisualization();});

  }

  private void receivedRunways(List<org.example.Runway> value) {
    prefillRunwaySelector.getItems().clear();
    prefillRunwaySelector.getItems().addAll(value);
  }

  private void receivedAirports(List<Airport> value) {
    prefillAirportSelector.getItems().clear();
    prefillAirportSelector.getItems().addAll(value);
  }

  @FXML
  private void saveToPng() throws IOException {
    Stage stage = (Stage) obstacleSelector.getScene().getWindow();
    WritableImage writableImage = new WritableImage((int)stage.getWidth(),(int)stage.getHeight());
    runwayComponent.snapshot(new SnapshotParameters(), writableImage);
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("png files (*.png)", "*.png"));
    File file = fc.showSaveDialog(null);
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
    if(file != null){
        ImageIO.write(bufferedImage,"png",file);
        alert.setHeaderText("Saved!");
        alert.setContentText("Visualisation saved to: "+file.getName());
        alert.showAndWait();
    }

  }
  
  @FXML
  private void exportToXml() {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));
    File file = fc.showSaveDialog(null);
    
    if(file != null) {
      try {
        CalcInst calcInst = new CalcInst();
        calcInst.setName("Exported_" + System.currentTimeMillis());
        
        String rawStatus = aircraftStatus.getValue() != null ? aircraftStatus.getValue().toString() : "Landing";
        System.out.println("Raw aircraft status from UI: " + rawStatus);
        
        String status;
        if (rawStatus.equals("Takeoff")) {
          status = "Taking Off"; // Schema expects "Taking Off", not "Takeoff"
          System.out.println("Mapped 'Takeoff' to 'Taking Off' for XML schema");
        } else {
          status = rawStatus; // "Landing" is already correct
          System.out.println("Using original status: " + status);
        }
        
        calcInst.setStatus(status);
        System.out.println("Final status set on CalcInst: " + calcInst.getStatus());

        calcInst.setRunwayPair(runwaySelector.getValue());
        calcInst.setLRrunway(LRSelector.getValue() != null ? LRSelector.getValue().toString() : "");
        calcInst.setCalc(calc);
        

        Obstacle obstacle = obstacleSelector.getSelectedObstacle();
        if (obstacle != null) {
          calcInst.setObstacle(obstacle);
          calcInst.setEobjdist(getdistances(obstacleLTDist));
          calcInst.setWobjdist(getdistances(obstacleUTDist));
          calcInst.setCentredist(getdistances(obstacleCLDist));
        } else {
          // no-arg constructor to avoid NullPointerException
          Obstacle defaultObstacle = new Obstacle();
          defaultObstacle.setName("Default");
          defaultObstacle.setPhysicalheight(getdistances(obstacleHeight));
          calcInst.setObstacle(defaultObstacle);
          calcInst.setEobjdist(getdistances(obstacleLTDist));
          calcInst.setWobjdist(getdistances(obstacleUTDist));
          calcInst.setCentredist(getdistances(obstacleCLDist));
        }
        
        // Set plane information
        Planes plane = planesSelector.getSelectedPlane();
        if (plane != null) {
          calcInst.setPlane(plane);
        } else {
          // same
          Planes defaultPlane = new Planes();
          defaultPlane.setName("Default");
          defaultPlane.setBlastProt(0);
          defaultPlane.setWidth(0);
          defaultPlane.setHeight(0);
          calcInst.setPlane(defaultPlane);
        }
        
        System.out.println("about to write XML with status: " + calcInst.getStatus());
        boolean success = XMLParser.writeToXML(calcInst, file);
        
        if (success) {
          alert.setHeaderText("Saved!");
          alert.setContentText("data exported to XML: " + file.getName());
        } else {
          alert.setHeaderText("export Failed");
          alert.setContentText("could not export data to XML file.");
        }
        alert.showAndWait();
      } catch (Exception e) {
        System.err.println("XML Export error: " + e.getMessage());
        Throwable cause = e.getCause();
        if (cause != null) {
          System.err.println("Caused by: " + cause.getMessage());
          cause.printStackTrace();
        }
        
        alert.setHeaderText("Export Error");
        alert.setContentText("An error occurred during export: " + e.getMessage());
        alert.showAndWait();
        e.printStackTrace();
      }
    }
  }
  
  @FXML
  private void exportToPdf() {
    try {
      Stage stage = (Stage) obstacleSelector.getScene().getWindow();
      WritableImage writableImage = new WritableImage((int)stage.getWidth(),(int)stage.getHeight());
      runwayComponent.snapshot(new SnapshotParameters(), writableImage);
      
      // prob there is a better way
      BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
      ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "png", imgBytes);
      
      FileChooser fc = new FileChooser();
      fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
      File file = fc.showSaveDialog(null);
      
      if(file != null) {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        
        // super simple
        Paragraph title = new Paragraph("Runway Calculation Report", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));
        
        com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(imgBytes.toByteArray());
        float pageWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
        float pageHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
        
        float imgWidth = pdfImage.getWidth();
        float imgHeight = pdfImage.getHeight();
        
        float ratio = Math.min(pageWidth / imgWidth, pageHeight / (imgHeight * 0.6f));
        pdfImage.scalePercent(ratio * 100);
        pdfImage.setAlignment(Element.ALIGN_CENTER);
        document.add(pdfImage);
        document.add(new Paragraph(" ")); // Empty line for spacing
        
        PdfPTable pdfTable = new PdfPTable(5); // 5 columns for TORA, TODA, ASDA, LDA, and Displacement
        pdfTable.setWidthPercentage(100);
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        String[] headers = {"Parameter", "Original (m)", "New (m)", "Difference (m)", "Explanation"};
        for (String header : headers) {
          PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
          cell.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
          cell.setPadding(5);
          pdfTable.addCell(cell);
        }
        
        SimpleTableView.TableData tableData = tableView.getData();
        if (tableData != null) {
          addTableRow(pdfTable, "TORA", tableData.getOriginalTORA(), tableData.getCalculatedTORA(), 
              tableData.getDiffTORA(), tableData.getExplanationTORA());
          addTableRow(pdfTable, "TODA", tableData.getOriginalTODA(), tableData.getCalculatedTODA(), 
              tableData.getDiffTODA(), tableData.getExplanationTODA());
          addTableRow(pdfTable, "ASDA", tableData.getOriginalASDA(), tableData.getCalculatedASDA(), 
              tableData.getDiffASDA(), tableData.getExplanationASDA());
          addTableRow(pdfTable, "LDA", tableData.getOriginalLDA(), tableData.getCalculatedLDA(), 
              tableData.getDiffLDA(), tableData.getExplanationLDA());
        }
        
        document.add(pdfTable);
        document.close();
        
        alert.setHeaderText("PDF Exported!");
        alert.setContentText("Report saved to: " + file.getName());
        alert.showAndWait();
      }
    } catch (Exception e) {
      e.printStackTrace();
      alert.setHeaderText("Export Error");
      alert.setContentText("An error occurred during PDF export: " + e.getMessage());
      alert.showAndWait();
    }
  }
  
  private void addTableRow(PdfPTable table, String parameter, int original, int calculated, int diff, String explanation) {
    table.addCell(parameter);
    table.addCell(String.valueOf(original));
    table.addCell(String.valueOf(calculated));
    table.addCell(String.valueOf(diff));
    table.addCell(explanation != null ? explanation : "");
  }

  public void updatePlaneVisualization() {
    boolean isLanding = "Landing".equals(aircraftStatus.getValue());
    boolean isTakeoff = "Takeoff".equals(aircraftStatus.getValue());
    Object selectedLR = LRSelector.getValue();
    Planes selectedPlane = planesSelector.getSelectedPlane();

    if ((isLanding || isTakeoff) && selectedPlane != null && selectedLR != null && ("Side On View".equals(topsideComboBox.getValue())) || topsideComboBox.getValue() == null) {
      String runway = selectedLR.toString();
      String runwayNumber = runway.substring(0, 2);
      char direction = runwayNumber.equals("09") ? 'e' : 'w';
      int thresholdInt = Integer.parseInt(thresholdVal.getText());
      if (isLanding) {
        if (direction == 'e') {
          runwayComponent.displayPlaneLE(selectedPlane, direction);
        } else {
          runwayComponent.displayPlaneLW(selectedPlane, direction);
        }
      } else if (isTakeoff) {
        if (direction == 'e') {
          runwayComponent.displayPlaneTE(selectedPlane, direction);
        } else {
          runwayComponent.displayPlaneTW(selectedPlane, direction);
        }
        runwayComponent.updatePlanePosition(thresholdInt);
        runwayComponent.updateLDA(thresholdInt);
      }
    }
  }


  public int getdistances(TextField val) {
    try {
      return Math.round(Float.parseFloat(val.getText()));
    } catch (NumberFormatException | NullPointerException e) {
        return 0;
    }
  }

  public int getLtdist() {
    return ltdist;
  }

  int ltdist = getdistances(obstacleLTDist);

  @FXML
  private void onAddClick() {
    Obstacle obj = obstacleSelector.getSelectedObstacle();//.getHeight();
    updatePlaneVisualization();
    String runthresh = threshold.getText().substring(0,threshold.getText().length()-1);
    if (obj != null) {
      int height = getdistances(obstacleHeight);//obj.getPhysicalheight();
      int ltdist = getdistances(obstacleLTDist);
      int utdist = getdistances(obstacleUTDist);
      int cldist = getdistances(obstacleCLDist);
      if ((height > 0) && (ltdist >= 0) && (utdist >= 0)){
      switch (runthresh){
        case "09":  {calc.calc(ltdist, utdist, cldist, height, 'e'); break;}
        case "27":  {calc.calc(ltdist, utdist, cldist, height, 'w'); break;}
      }
        char dir = runthresh.equals("09") ? 'e' : 'w';
        boolean isLanding = "Landing".equals(aircraftStatus.getValue());
      runwayComponent.updateObstacleVisualization(obj, dir, isLanding, obstaclePosition.get());
      updateCalculations();}
      else {
        alert.setHeaderText("Negative values");
        alert.setContentText("LT/UT Distances cannot be negative. Height must be greater than 0m.");
        alert.showAndWait();
      }
    }
    else {
      alert.setHeaderText("No obstacle selected");
      alert.setContentText("Please select an obstacle");
      alert.showAndWait();}
  }
  @FXML
  private void onImportClick() {

    if (xmlfile != null) {
      try {
        System.out.println("Attempting to read in XML file...");
        ImportData importData = XMLParser.readInXML(xmlfile);
        System.out.println("XML unmarshalling successful!");

        if (importData == null) {
          System.err.println("Unmarshalling failed, 'importData' is null.");
          alert.setHeaderText("Error");
          alert.setContentText("Failed to load the data from XML.");
          alert.showAndWait();
        } else {
          //refresh ui!!
          importRefresh(importData);
        }
      } catch (JAXBException e) {
        Throwable cause = e.getLinkedException();
        if (cause instanceof org.xml.sax.SAXParseException) {
        } else {
          alert.setHeaderText("Error during file processing");
          alert.setContentText("An error occurred while processing the file: " + e.getMessage());
          alert.showAndWait();
        }
      } catch (NullPointerException e) {
        alert.setHeaderText("No file");
        alert.setContentText("Please select a file.");
        alert.showAndWait();
      } catch (SAXException e) {
        alert.setHeaderText("Invalid file format");
        alert.setContentText("XML format cannot be read. Please refer to the XML schema.");
        alert.showAndWait();
      } catch (Exception e) {
        alert.setHeaderText("Invalid format");
        alert.setContentText(
            "Could not parse the file. Please ensure the file has a .xml extension");
        alert.showAndWait();
      }
    } else {
      alert.setHeaderText("No file selected");
      alert.setContentText("Please select a file before importing");
      alert.showAndWait();
    }
  }

  private void importRefresh(ImportData importData){
    try {
      calc = importData.getCalculation();
      calc.setBlastprot(importData.getPlanes().getBlastProt());
      Platform.runLater(calc::refreshUI);
      tableView.setCalculation(calc);
      
      flightStatus = importData.getStatus();
      aircraftStatus.setValue(flightStatus);
      LRrunway = importData.getLRrunway();
      LRSelector.setValue(LRrunway);
      runwayPair = importData.getRunwayPair();
      runwaySelector.setValue(runwayPair);
      thresholdVal.setText(String.valueOf(calc.getThreshold()));
      obstacleSelector.setValue(importData.getObstacle().getName());
      obstacleHeight.setText(String.valueOf(importData.getObstacle().getPhysicalheight()));
      obstacleCLDist.setText(String.valueOf(importData.getCentredist()));
      obstacleLTDist.setText(String.valueOf(importData.getEobjdist()));
      obstacleUTDist.setText(String.valueOf(importData.getWobjdist()));
      planesSelector.setValue(importData.getPlanes().getName());
    }
    catch (Exception e ){
      System.out.println(e);
    }
    //obstacleSelector.setValue(importData.getObstacle().getName());
  }

  @FXML
  private void onChooseFileClick(){
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open XML File");
    FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("xml","*.xml");
    fileChooser.getExtensionFilters().add(extensionFilter);
    fileChooser.setSelectedExtensionFilter(extensionFilter);
    xmlfile = (fileChooser.showOpenDialog(obstacleSelector.getScene().getWindow()));
    if (xmlfile!=null) {
      xmlfilename.set(xmlfile.getName());
    }
  }
  @FXML
  private void onAddThreshClick(){
    updateThreshold();
  }

  public void  updateThreshold() {//Updates the threshold with what the user inputs and does the object calculation when pressed the add button
    try {
      runwayPair = runwaySelector.getValue();
      LRrunway = LRSelector.getValue().toString();
      boolean isLanding = "Landing".equals(aircraftStatus.getValue());
      boolean isTakeoff = "Takeoff".equals(aircraftStatus.getValue());
      int thresholdInt = Integer.parseInt(thresholdVal.getText());
      if (thresholdInt < 0) {
        alert.setHeaderText("Invalid threshold");
        alert.setContentText("Threshold cannot be negative");
        alert.showAndWait();
        return;
      }
      if (thresholdInt > calc.getElength()) {
        alert.setHeaderText("Invalid threshold");
        alert.setContentText("Threshold exceeds runway length");
        alert.showAndWait();
        return;
      }
      updatePlaneVisualization();
      runwayComponent.updatePlanePosition(thresholdInt);
      runwayComponent.updateLDA(thresholdInt);
      if (LRrunway.equals("27R")) {
        calc = new Calculation(
            78, 0, 0, 0,
            3884, 3902, thresholdInt,
            60, 240, 300, 3884, 3902
            , 'w');
        if (isTakeoff) {
          updateCalculations();
          tableView.getItems().setAll(calc);
          runwayComponent.setMeasurementsTW(calc.getTora(), calc.getResa(), calc.getStrip(), calc.getEstopway(), calc.getToda(), calc.getAsda(), calc.getEclearway(), calc.getLda(), calc.getElength());
        } else if (isLanding) {
          updateCalculations();
          tableView.getItems().setAll(calc);
          runwayComponent.setMeasurementsLW(calc.getTora(), calc.getResa(),  calc.getStrip(), calc.getEstopway(), calc.getToda(), calc.getAsda(), calc.getEclearway(), calc.getLda(), calc.getElength());
        }
      } else if (LRrunway.equals("09R")) {
        calc = new Calculation(
            0, 0, 0, 0,
            3660, 3660, thresholdInt,
            60, 240, 300, 3660, 3660,
            'e');
        if (isTakeoff) {
          updateCalculations();
          tableView.getItems().setAll(calc);
          runwayComponent.setMeasurementsTE(calc.getTora(), calc.getResa(),  calc.getStrip(), calc.getEstopway(), calc.getToda(), calc.getAsda(), calc.getEclearway(), calc.getLda(), calc.getElength());
        } else if (isLanding) {
          updateCalculations();
          tableView.getItems().setAll(calc);
          runwayComponent.setMeasurementsLE(calc.getTora(), calc.getResa(),  calc.getStrip(), calc.getEstopway(), calc.getToda(), calc.getAsda(), calc.getEclearway(), calc.getLda(), calc.getElength());
        }
      } else if (LRrunway.equals("27L")) {
        calc = new Calculation(
            0, 0, 0, 0,
            3660, 3660, thresholdInt,
            60, 240, 300, 3660, 3660,
            'w');
        if (isTakeoff) {
          updateCalculations();
          tableView.getItems().setAll(calc);
          runwayComponent.setMeasurementsTW(calc.getTora(), calc.getResa(), calc.getStrip(), calc.getEstopway(), calc.getToda(), calc.getAsda(), calc.getEclearway(), calc.getLda(), calc.getElength());
        } else if (isLanding) {
          updateCalculations();
          tableView.getItems().setAll(calc);
          runwayComponent.setMeasurementsLW(calc.getTora(), calc.getResa(),  calc.getStrip(), calc.getEstopway(), calc.getToda(), calc.getAsda(), calc.getEclearway(), calc.getLda(), calc.getElength());
        }
      } else {
        calc = new Calculation(
            100, 50, 0, 0,
            3902, 3884, thresholdInt,
            60, 240, 300, 3902, 3884
            , 'e');
        if (isTakeoff) {
          updateCalculations();
          tableView.getItems().setAll(calc);
          runwayComponent.setMeasurementsTE(calc.getTora(), calc.getResa(),  calc.getStrip(), calc.getEstopway(), calc.getToda(), calc.getAsda(), calc.getEclearway(), calc.getLda(), calc.getElength());
        } else if (isLanding) {
          updateCalculations();
          tableView.getItems().setAll(calc);
          runwayComponent.setMeasurementsLE(calc.getTora(), calc.getResa(),  calc.getStrip(), calc.getEstopway(), calc.getToda(), calc.getAsda(), calc.getEclearway(), calc.getLda(), calc.getElength());
        }
      }
    } catch (NumberFormatException e) {
      System.err.println("Invalid threshold value: " + thresholdVal.getText());
      alert.setHeaderText("Invalid Input");
      alert.setContentText("Enter a valid number for threshold");
      alert.showAndWait();
    } catch (IllegalArgumentException e) {
      System.err.println("Threshold error: " + e.getMessage() + " - Value: " + thresholdVal.getText());
      alert.setHeaderText("Invalid Threshold");
      alert.setContentText(e.getMessage());
      alert.showAndWait();
    }
  }

  public void setServerCalculation(org.example.Calculation serverCalc) {
    this.serverCalc = serverCalc;

    var socket = SocketManager.getInstance();
    calcUpdateListener = new ServerListener() {
      @Override
      public void onMessageReceived(Data message) {
        if (message.getMessage().equals(Protocols.CALC_UPDATED)) {
          Platform.runLater(() -> receivedUpdatedCalculations((org.example.Calculation)message.getValue()));
        }
      }

      @Override
      public void onError(Throwable error) {}
    };
    socket.addListener(calcUpdateListener);
    try {
      socket.send(new Data(Protocols.JOIN_CALC_ROOM, serverCalc));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void updateCalculations() {
    // Instead of creating a new calculation object, use the existing serverCalc
    serverCalc.setNewASDA(calc.getUiAsda().getValue());
    serverCalc.setNewLDA(calc.getUiLda().getValue());
    serverCalc.setNewTODA(calc.getUiToda().getValue());
    serverCalc.setNewTORA(calc.getUiTora().getValue());

    System.out.println("Sending updated calculation - LDA: " + serverCalc.getNewLDA());

    try {
      // Create a new Data object to ensure a fresh instance is sent each time
      Data dataToSend = new Data(Protocols.CALC_UPDATED, serverCalc);
      SocketManager.getInstance().send(dataToSend);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void receivedUpdatedCalculations(org.example.Calculation updatedCalc) {
    System.out.println("Received updated calculations");
    System.out.println("Received updated calculation - LDA: " + updatedCalc.getNewLDA()); // Added log
    calc.getUiAsda().setValue(updatedCalc.getNewASDA());
    calc.getUiLda().setValue(updatedCalc.getNewLDA());
    calc.getUiToda().setValue(updatedCalc.getNewTODA());
    calc.getUiTora().setValue(updatedCalc.getNewTORA());
    serverCalc = updatedCalc;
    tableView.setCalculation(calc);
  }

  private void onSceneLeave() {
    var socket = SocketManager.getInstance();
    socket.removeListener(calcUpdateListener);
    try {
      System.out.println("Preparing to leave calculation room for ID: " + serverCalc.getId());
      Data leaveMessage = new Data(Protocols.LEAVE_CALC_ROOM, serverCalc);
      System.out.println("Sending LEAVE_CALC_ROOM message...");
      socket.send(leaveMessage);
      System.out.println("LEAVE_CALC_ROOM message sent successfully");
      
      // Give a small delay to ensure the message is processed, I know sounds insane but I spent 3 hours fixing this and there is no better way without rewriting the entire codebase
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    } catch (IOException e) {
      System.err.println("Error sending LEAVE_CALC_ROOM message: " + e.getMessage());
      e.printStackTrace(); 
      throw new RuntimeException(e);
    }
  }

  @FXML
  private void onBackAction() throws IOException {
    onSceneLeave();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/calcSelectScene.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) LRSelector.getScene().getWindow();

    stage.setScene(new Scene(root, 400, 300)); // Adjust size as needed
    stage.show();
  }

  public static Boolean isSideOn() {
    return isSideOn;
  }
}