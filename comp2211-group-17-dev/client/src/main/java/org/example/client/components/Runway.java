package org.example.client.components;

import java.io.IOException;

import org.example.client.MainSceneController;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Path;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Scale;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

public class Runway extends StackPane {
  private final SimpleStringProperty thresholdIdent = new SimpleStringProperty("T");

  private SimpleDoubleProperty scaleFactor = new SimpleDoubleProperty(1.0);
  private SimpleDoubleProperty runwayStartX = new SimpleDoubleProperty(0);

  @FXML
  Label testLabel2, clearedLabel, ldaLabel, stripLabel, resaLabel, stopwayLabel, clearwayLabel, takeofflabel, runwayId;

  @FXML
  Rectangle topDownView;

  @FXML
  Rectangle topDownRunway, topDownClearWay, topDownStopWay, toraRect, resaRect, stripEndRect, stopWayRect, todaRect, asdaRect, clearRect, ldaRect, runwayRect, TRect, THRect, THRect2, resaRect2, resaRect3, THRect3, THRect31, THRect4, resaRect4,asdas,todas, THRect5, resaRect5, THRect6, resaRect6, topDownCentreLine, topDownLda, stripEnd, topDownResa, topDownToda;

  @FXML
  Path clearedAndGraded;
  @FXML
  Label toraId, resaId, todaId, asdaId, ldaId, stopId, clearId, stripId, tId, thId, blastId;

  @FXML
  HBox resaBig;

  private Line hypoLine;
  private Obstacle obstacle;
  private Polygon hypoTriangle;
  private Line vLine;
  private Line hLine;



  // Properties
  private final SimpleDoubleProperty obstaclePosition = new SimpleDoubleProperty();
  private final ObjectProperty<ObstacleSelector> obstacleSelector = new SimpleObjectProperty<>(
      null);
  private final SimpleDoubleProperty planesPosition = new SimpleDoubleProperty();
  private final ObjectProperty<PlanesSelector> planesSelector = new SimpleObjectProperty<>(
          null);
  @FXML
  private StackPane obstacleWrapper;
  @FXML
  private StackPane planeWrapper;
  private final ChangeListener<Obstacle> selectedObstacleListener = (obs, oldObs, newObs) -> {
    updateObstacle(newObs);
  };
  private final ChangeListener<Planes> selectedPlanesListener = (planes, oldPlanes, newPlanes) -> {
    displayPlaneLE(newPlanes, 'e');
  };

  private final Scale scaleTransform = new Scale(1.0,1.0);

  public Runway() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("runway-component.fxml"));
    loader.setRoot(this);
    loader.setController(this);
    loader.load();
    getTransforms().add(scaleTransform);
    setUpZoom();
  }

  @FXML
  private void initialize() {
    setupBindings();
    setupListeners();

  }
  public SimpleDoubleProperty scaleFactorProperty() {
    return scaleFactor;
  }
  public SimpleStringProperty thresholdIdent() {
    return thresholdIdent;
  }

  public void setThresholdIdent(String value) {
    thresholdIdent.set(value);
  }
  private void setupBindings() {
    // position obstacle image
    System.out.println("entered setUpBindings");
    {tId.textProperty().bind(thresholdIdent);}

    double baseOffset = -890;
    obstacleWrapper.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(baseOffset));
    TRect.translateXProperty().bind(planePosition);
    THRect2.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-60));
    resaRect.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-60));
    THRect3.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-60));
    resaRect2.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-60));
    THRect31.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-60));
    resaRect3.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-60));
    THRect4.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-695));
    resaRect4.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-695));
    THRect5.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-695));
    resaRect5.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-695));
    THRect6.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-695));
    resaRect6.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-695));
    tId.translateXProperty().bind(planePosition);

  }

  private void setupListeners() {
    System.out.println("entered setUpListeners");
    obstacleSelector.addListener((obs, oldSelector, newSelector) -> {
      if (oldSelector != null) {
        oldSelector.selectedObstacleProperty().removeListener(selectedObstacleListener);
      }
      if (newSelector != null) {
        newSelector.selectedObstacleProperty().addListener(selectedObstacleListener);
        updateObstacle(newSelector.getSelectedObstacle());
      } else {
        updateObstacle(null);
      }
    });
    planesSelector.addListener((obs, oldSelector, newSelector) -> {
      if (oldSelector != null) {
        oldSelector.selectedPlaneProperty().removeListener(selectedPlanesListener);
      }
      if (newSelector != null) {
        newSelector.selectedPlaneProperty().addListener(selectedPlanesListener);
        displayPlaneLE(newSelector.getSelectedPlane(), 'e');
      } else {
        displayPlaneLE(null, 'e');
      }
    });
  }
  Line leftVertical = new Line();
  Line leftHorizontal = new Line();
  Line leftHypo = new Line();
  Line rightVertical = new Line();
  Line rightHorizontal = new Line();
  Line rightHypo = new Line();
  private void updateObstacle(Obstacle original) {;
    obstacleWrapper.getChildren().clear();
    if (original != null && original.getImage().getImage() != null) {
      ImageView copy = new ImageView(original.getImage().getImage());
      copy.setFitWidth(original.getImage().getFitWidth());
      copy.setFitHeight(original.getImage().getFitHeight());
      copy.setPreserveRatio(original.getImage().isPreserveRatio());

      int physicalHeight = original.getPhysicalheight();
      double scaledHeight = physicalHeight * scaleFactor.get();
      double scaledHorizontal = 50 * physicalHeight * scaleFactor.get();

      leftVertical.setStroke(Color.RED);
      leftVertical.setStrokeWidth(2);
      double leftX = -copy.getFitWidth() / 2 - 5;
      leftVertical.setStartX(leftX);
      leftVertical.setStartY(0);
      leftVertical.setEndX(leftX);
      leftVertical.setEndY(-scaledHeight);

      leftHorizontal.setStroke(Color.RED);
      leftHorizontal.setStrokeWidth(2);
      leftHorizontal.setStartX(leftX);
      leftHorizontal.setStartY(0);
      leftHorizontal.setEndX(leftX - scaledHorizontal);
      leftHorizontal.setEndY(0);

      leftHypo.setStroke(Color.RED);
      leftHypo.setStrokeWidth(2);
      leftHypo.setStartX(leftX);
      leftHypo.setStartY(-scaledHeight);
      leftHypo.setEndX(leftX - scaledHorizontal);
      leftHypo.setEndY(0);

      rightVertical.setStroke(Color.GREEN);
      rightVertical.setStrokeWidth(2);
      double rightX = copy.getFitWidth() / 2 + 5;
      rightVertical.setStartX(rightX);
      rightVertical.setStartY(0);
      rightVertical.setEndX(rightX);
      rightVertical.setEndY(-scaledHeight);

      rightHorizontal.setStroke(Color.GREEN);
      rightHorizontal.setStrokeWidth(2);
      rightHorizontal.setStartX(rightX);
      rightHorizontal.setStartY(0);
      rightHorizontal.setEndX(rightX + scaledHorizontal);
      rightHorizontal.setEndY(0);

      rightHypo.setStroke(Color.GREEN);
      rightHypo.setStrokeWidth(2);
      rightHypo.setStartX(rightX);
      rightHypo.setStartY(-scaledHeight);
      rightHypo.setEndX(rightX + scaledHorizontal);
      rightHypo.setEndY(0);

      obstacleWrapper.getChildren().addAll(copy, leftVertical, leftHorizontal, leftHypo, rightVertical, rightHorizontal, rightHypo);
    }
  }
  private ImageView currentPlaneImage;

  public void displayPlaneLE(Planes plane, char direction) {
    planeWrapper.getChildren().clear();
    if (plane != null && plane.getImage() != null) {
     //ImageView copy = new ImageView(plane.getImage().getImage());
     ImageView copy;
      if(!MainSceneController.isSideOn()) {
        copy = new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/plane_311452-Photoroom.png")));
        copy.setRotate(90);
      } else {
        copy = new ImageView(plane.getImage().getImage());
      }
      copy.setFitWidth(plane.getImage().getFitWidth());
      copy.setFitHeight(plane.getImage().getFitHeight());
      copy.setPreserveRatio(true);
      copy.setTranslateY(20);
      copy.setTranslateX(0);
      copy.setEffect(new DropShadow(5, Color.BLACK));
      copy.setScaleX(-1);
      planeWrapper.getChildren().add(copy);
      currentPlaneImage = copy;
    }
  }
  public void displayPlaneLW(Planes plane, char direction) {
    planeWrapper.getChildren().clear();
    if (plane != null && plane.getImage() != null) {
      //ImageView copy = new ImageView(plane.getImage().getImage());
      ImageView copy;
      if(!MainSceneController.isSideOn()) {
        copy = new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/plane_311452-Photoroom.png")));
        copy.setRotate(90);
      } else {
        copy = new ImageView(plane.getImage().getImage());
      }
      copy.setFitWidth(plane.getImage().getFitWidth());
      copy.setFitHeight(plane.getImage().getFitHeight());
      copy.setPreserveRatio(true);
      copy.setTranslateY(20);
      copy.setTranslateX(0);
      copy.setEffect(new DropShadow(5, Color.BLACK));
      planeWrapper.getChildren().add(copy);
      currentPlaneImage = copy;
    }
  }
  private final SimpleDoubleProperty planePosition = new SimpleDoubleProperty(0);

  public SimpleDoubleProperty planePositionProperty() {
    return planePosition;
  }

  public void setPlanePosition(double position) {
    planePosition.set(position);
  }
  public double getPlanePosition() {
    return planePosition.get();
  }
  public void updatePlanePosition(double thresholdD) {
    double scaledPosition = thresholdD * scaleFactor.get();
    double baseOffset = 0;
    planePosition.set(baseOffset + scaledPosition);
  }
  public void updateLDA(double thresholdD) {
      THRect.setWidth(thresholdD * scaleFactor.doubleValue());
  }

  public void displayPlaneTE(Planes plane, char direction) {
    planeWrapper.getChildren().clear();
    if (plane != null && plane.getImage() != null) {
      ImageView copy;
      if(!MainSceneController.isSideOn()) {
        copy = new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/plane_311452-Photoroom.png")));
        copy.setRotate(90);
      } else {
        copy = new ImageView(plane.getImage().getImage());
      }
      //ImageView copy = new ImageView(plane.getImage().getImage());
      copy.setFitWidth(plane.getImage().getFitWidth());
      copy.setFitHeight(plane.getImage().getFitHeight());
      copy.setPreserveRatio(true);
      copy.setTranslateY(0);
      copy.setEffect(new DropShadow(5, Color.BLACK));
      copy.setRotate(-20);
      copy.setScaleX(-1);
      planeWrapper.getChildren().add(copy);
      currentPlaneImage = copy;
    }
  }
  public void displayPlaneTW(Planes plane, char direction) {
    planeWrapper.getChildren().clear();
    if (plane != null && plane.getImage() != null) {
      ImageView copy;
      if(!MainSceneController.isSideOn()) {
        copy = new ImageView(new Image(getClass().getResourceAsStream("/org/example/client/plane_311452-Photoroom.png")));
      } else {
        copy = new ImageView(plane.getImage().getImage());
      }
      copy.setFitWidth(plane.getImage().getFitWidth());
      copy.setFitHeight(plane.getImage().getFitHeight());
      copy.setPreserveRatio(true);
      copy.setTranslateY(0);
      copy.setEffect(new DropShadow(5, Color.BLACK));
      copy.setRotate(20);
      planeWrapper.getChildren().add(copy);
      currentPlaneImage = copy;
    }
  }

  public SimpleDoubleProperty obstaclePositionProperty() {
    return obstaclePosition;
  }

  public Double getObstaclePosition() {
    return obstaclePosition.get();
  }

  public void setObstaclePosition(Double position) {
    obstaclePosition.set(position);
  }

  public ObjectProperty<ObstacleSelector> obstacleSelectorProperty() {
    return obstacleSelector;
  }

  public ObstacleSelector getObstacleSelector() {
    return obstacleSelector.get();
  }

  public void setObstacleSelector(ObstacleSelector length) {
    obstacleSelector.set(length);
  }
  public ObjectProperty<PlanesSelector> planesSelectorProperty() {
    return planesSelector;
  }

  public PlanesSelector getPlanesSelector() {
    return planesSelector.get();
  }

  public void setPlanesSelector(PlanesSelector selector) {
    planesSelector.set(selector);
  }
  public void setVisibility(Boolean choice) {
    topDownView.setVisible(choice);
    topDownRunway.setVisible(choice);
    topDownClearWay.setVisible(choice);
    topDownStopWay.setVisible(choice);
    clearedAndGraded.setVisible(choice);
    clearedLabel.setVisible(choice);
    topDownCentreLine.setVisible(choice);
    topDownLda.setVisible(choice);
    topDownResa.setVisible(choice);
    stripEnd.setVisible(choice);
    ldaLabel.setVisible(choice);
    stripLabel.setVisible(choice);
    resaLabel.setVisible(choice);
    stopwayLabel.setVisible(choice);
    clearwayLabel.setVisible(choice);
    topDownToda.setVisible(false);
    runwayRect.setVisible(!choice);
    runwayId.setVisible(!choice);
  }
  private int currentEl;

  public void setMeasurementsTE(int tora, int resa, int stripEnd, int stopway, int toda, int asda, int cway, int lda, int el) {
    double scaleFactor = 1000.0 / el;
    this.currentEl = el;
    leftVertical.setVisible(false);
    leftHorizontal.setVisible(false);
    leftHypo.setVisible(false);
    rightHorizontal.setVisible(false);
    rightHypo.setVisible(false);
    rightVertical.setVisible(false);
    THRect2.setVisible(false);
    resaRect.setVisible(false);
    THRect3.setVisible(false);
    resaRect2.setVisible(false);
    resaRect3.setVisible(false);
    THRect31.setVisible(false);
    if (obstaclePosition.get() > el/2) {
      resaRect.setWidth(scaleFactor * resa);
      THRect2.setVisible(true);
      resaRect.setVisible(true);
      THRect3.setVisible(true);
      resaRect2.setWidth(scaleFactor * resa);
      resaRect2.setVisible(true);
      resaRect3.setWidth(scaleFactor * resa);
      resaRect3.setVisible(true);
      THRect31.setVisible(true);
      planeWrapper.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-150));
      rightHorizontal.setVisible(false);
      rightHypo.setVisible(false);
      rightVertical.setVisible(false);
      leftVertical.setVisible(true);
      leftHorizontal.setVisible(true);
      leftHypo.setVisible(true);
    } else {
      leftVertical.setVisible(false);
      leftHorizontal.setVisible(false);
      leftHypo.setVisible(false);
      rightHorizontal.setVisible(false);
      rightHypo.setVisible(false);
      rightVertical.setVisible(false);
      THRect2.setVisible(false);
      resaRect.setVisible(false);
      THRect3.setVisible(false);
      resaRect2.setVisible(false);
      resaRect3.setVisible(false);
      THRect31.setVisible(false);
      planeWrapper.translateXProperty().unbind();
      planeWrapper.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-130));
      if (currentPlaneImage != null) {
        currentPlaneImage.setRotate(0);
        currentPlaneImage.setTranslateY(200);
      }

    }
    this.scaleFactor.set(scaleFactor);
    TRect.setVisible(false);
    tId.setVisible(true);
    THRect.setVisible(true);
    runwayRect.setWidth(1000);
    toraRect.setWidth(tora * scaleFactor);
    toraRect.setVisible(true);
    toraId.setVisible(true);
    stopWayRect.setWidth(stopway * scaleFactor);
    if (stopway == 0) {
      stopWayRect.setVisible(false);
      stopId.setVisible(false);
    } else {
      stopWayRect.setVisible(true);
      stopId.setVisible(true);
    }
    todaRect.setWidth(toda * scaleFactor);
    todaRect.setVisible(true);
    todaId.setVisible(true);
    clearRect.setWidth(cway * scaleFactor);
    if (cway == 0) {
      clearRect.setVisible(false);
      clearId.setVisible(false);
    } else {
      clearRect.setVisible(true);
      clearId.setVisible(true);
    }
    ldaRect.setWidth(tora * scaleFactor);
    ldaRect.setVisible(false);
    ldaId.setVisible(false);
    asdaRect.setWidth(asda * scaleFactor);
    asdaId.setVisible(true);
    asdaRect.setVisible(true);
  }

  public void setMeasurements(int tora) {
    toraRect.setWidth(tora*0.256);
    resaRect.setWidth(240*0.256);
    stripEndRect.setWidth(60*0.56);

    //here
    stripEnd.setWidth(60*0.56);
    topDownResa.setWidth(240*0.256);
    //topDownLda.setWidth(tora*0.256);
  }

  public void setMeasurementsTW(int tora, int resa, int stripEnd, int stopway, int toda, int asda, int cway, int lda, int el) {
    double scaleFactor = 1000.0 / el;
    this.currentEl = el;
    leftVertical.setVisible(false);
    leftHorizontal.setVisible(false);
    leftHypo.setVisible(false);
    rightHorizontal.setVisible(false);
    rightHypo.setVisible(false);
    rightVertical.setVisible(false);
    THRect2.setVisible(false);
    resaRect.setVisible(false);
    THRect3.setVisible(false);
    resaRect2.setVisible(false);
    resaRect3.setVisible(false);
    THRect31.setVisible(false);
    todas.setTranslateX(-(cway * scaleFactor));
    todas.setVisible(true);
    asdas.setTranslateX(-(stopway * scaleFactor));
    asdas.setVisible(true);
    if (obstaclePosition.get() > el/2) {
      leftVertical.setVisible(false);
      leftHorizontal.setVisible(false);
      leftHypo.setVisible(false);
      rightHorizontal.setVisible(false);
      rightHypo.setVisible(false);
      rightVertical.setVisible(false);
      THRect2.setVisible(true);
      resaRect.setVisible(true);
      resaRect.setWidth(resa * scaleFactor);
      THRect3.setVisible(true);
      resaRect2.setVisible(true);
      resaRect2.setWidth(resa * scaleFactor);
      resaRect3.setVisible(true);
      resaRect3.setWidth(resa * scaleFactor);
      THRect31.setVisible(true);
      planeWrapper.translateXProperty().unbind();
      planeWrapper.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-130));
      if (currentPlaneImage != null) {
        currentPlaneImage.setRotate(0);
        currentPlaneImage.setTranslateY(200);
      }
      THRect4.setVisible(false);
      resaRect4.setVisible(false);
      THRect5.setVisible(false);
      resaRect5.setWidth(scaleFactor * resa);
      resaRect5.setVisible(false);
      resaRect6.setWidth(scaleFactor * resa);
      resaRect6.setVisible(false);
      THRect6.setVisible(false);
    } else if  (obstaclePosition.get() > 0) {
      leftVertical.setVisible(false);
      leftHorizontal.setVisible(false);
      leftHypo.setVisible(false);
      rightHorizontal.setVisible(true);
      rightHypo.setVisible(true);
      rightVertical.setVisible(true);
      resaRect4.setWidth(scaleFactor * resa);
      THRect4.setVisible(true);
      resaRect4.setVisible(true);
      THRect5.setVisible(true);
      resaRect5.setWidth(scaleFactor * resa);
      resaRect5.setVisible(true);
      resaRect6.setWidth(scaleFactor * resa);
      resaRect6.setVisible(true);
      THRect6.setVisible(true);
      THRect2.setVisible(false);
      resaRect.setVisible(false);
      resaRect.setWidth(resa * scaleFactor);
      THRect3.setVisible(false);
      resaRect2.setVisible(false);
      resaRect2.setWidth(resa * scaleFactor);
      resaRect3.setVisible(false);
      resaRect3.setWidth(resa * scaleFactor);
      THRect31.setVisible(false);
      double offset = -220;
      if (currentPlaneImage != null) {
        currentPlaneImage.setTranslateX(250);
        currentPlaneImage.setTranslateY(0);
      }
    }
    this.scaleFactor.set(scaleFactor);
    TRect.setVisible(false);
    tId.setVisible(true);
    THRect.setVisible(false);
    runwayRect.setWidth(1000);
    toraRect.setWidth(tora * scaleFactor);
    toraRect.setVisible(true);
    toraId.setVisible(true);
    stopWayRect.setWidth(stopway * scaleFactor);
    if (stopway == 0) {
      stopWayRect.setVisible(false);
      stopId.setVisible(false);
    } else {
      stopWayRect.setVisible(true);
      stopId.setVisible(true);
    }
    todaRect.setWidth(tora * scaleFactor);
    todaRect.setVisible(true);
    todaId.setVisible(true);
    clearRect.setWidth(cway * scaleFactor);
    if (cway == 0) {
      clearRect.setVisible(false);
      clearId.setVisible(false);
    } else {
      clearRect.setVisible(true);
      clearId.setVisible(true);
    }
    ldaRect.setWidth(tora * scaleFactor);
    ldaRect.setVisible(false);
    ldaId.setVisible(false);
    asdaRect.setWidth(tora * scaleFactor);
    asdaId.setVisible(true);
    asdaRect.setVisible(true);
  }
  public void setMeasurementsLE(int tora, int resa, int stripEnd, int stopway, int toda, int asda, int cway, int lda, int el) {
    double scaleFactor = 1000.0 / el;
    this.scaleFactor.set(scaleFactor);
    resaRect.setVisible(false);
    THRect3.setVisible(false);
    resaRect2.setVisible(false);
    resaRect3.setVisible(false);
    THRect31.setVisible(false);
    leftVertical.setVisible(false);
    leftHorizontal.setVisible(false);
    leftHypo.setVisible(false);
    rightHorizontal.setVisible(false);
    rightHypo.setVisible(false);
    rightVertical.setVisible(false);
    if (obstaclePosition.get() > el/2) {
      leftVertical.setVisible(false);
      leftHorizontal.setVisible(false);
      leftHypo.setVisible(false);
      rightHorizontal.setVisible(false);
      rightHypo.setVisible(false);
      rightVertical.setVisible(false);
      resaRect.setWidth(scaleFactor * resa);
      THRect2.setVisible(true);
      resaRect.setVisible(true);
      THRect3.setVisible(false);
      resaRect2.setWidth(scaleFactor * resa);
      resaRect2.setVisible(false);
      resaRect3.setWidth(scaleFactor * resa);
      resaRect3.setVisible(false);
      THRect31.setVisible(false);
      THRect4.setVisible(false);
      resaRect4.setVisible(false);
      resaRect4.setWidth(scaleFactor * resa);
      asdaRect.setVisible(false);
      asdaId.setVisible(false);
      todaRect.setVisible(false);
      todaId.setVisible(false);
      toraId.setVisible(false);
      todaRect.setVisible(false);
      if (currentPlaneImage != null) {
        currentPlaneImage.setTranslateX(-550);
      }
    } else if  (obstaclePosition.get() > 0) {
      leftVertical.setVisible(false);
      leftHorizontal.setVisible(false);
      leftHypo.setVisible(false);
      rightHorizontal.setVisible(true);
      rightHypo.setVisible(true);
      rightVertical.setVisible(true);
      THRect2.setVisible(false);
      resaRect.setVisible(false);
      THRect3.setVisible(false);
      resaRect2.setVisible(false);
      resaRect3.setVisible(false);
      THRect31.setVisible(false);
      resaRect4.setWidth(scaleFactor * resa);
      THRect4.setVisible(true);
      resaRect4.setVisible(true);
      asdaRect.setVisible(false);
      asdaId.setVisible(false);
      todaRect.setVisible(false);
      todaId.setVisible(false);
      toraId.setVisible(false);
      todaRect.setVisible(false);
      planeWrapper.translateXProperty().unbind();
      planeWrapper.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-130));
      if (currentPlaneImage != null) {
        currentPlaneImage.setTranslateY(0);
      }

    }
    asdaRect.setWidth(asda * scaleFactor);
    TRect.setVisible(true);
    THRect.setVisible(true);
    tId.setVisible(true);
    runwayRect.setWidth(1000);
    runwayRect.setVisible(true);
    asdaRect.setVisible(false);
    asdaId.setVisible(false);
    todaRect.setWidth(toda * scaleFactor);
    todaRect.setVisible(false);
    todaId.setVisible(false);
    toraId.setVisible(false);
    todaRect.setVisible(false);
    toraRect.setWidth(tora * scaleFactor);
    toraRect.setVisible(false);
    stopWayRect.setWidth(stopway * scaleFactor);
    clearRect.setWidth(cway * scaleFactor);
    ldaRect.setWidth(tora * scaleFactor);
    ldaRect.setVisible(true);
    ldaId.setVisible(true);
    if (cway == 0) {
      clearRect.setVisible(false);
      clearId.setVisible(false);
    } else {
      clearRect.setVisible(true);
      clearId.setVisible(true);
    }
    if (stopway == 0) {
      stopWayRect.setVisible(false);
      stopId.setVisible(false);
    } else {
      stopWayRect.setVisible(true);
      stopId.setVisible(true);
    }
  }

  public void setMeasurementsLW(int tora, int resa, int stripEnd, int stopway, int toda, int asda, int cway, int lda, int el) {
    double scaleFactor = 1000.0 / el;
    this.scaleFactor.set(scaleFactor);
    resaRect.setVisible(false);
    THRect3.setVisible(false);
    resaRect2.setVisible(false);
    resaRect3.setVisible(false);
    THRect31.setVisible(false);
    leftVertical.setVisible(false);
    leftHorizontal.setVisible(false);
    leftHypo.setVisible(false);
    rightHorizontal.setVisible(false);
    rightHypo.setVisible(false);
    rightVertical.setVisible(false);
    if (obstaclePosition.get() > el/2) {
      leftVertical.setVisible(true);
      leftHorizontal.setVisible(true);
      leftHypo.setVisible(true);
      rightHorizontal.setVisible(false);
      rightHypo.setVisible(false);
      rightVertical.setVisible(false);
      THRect2.setVisible(true);
      resaRect.setVisible(true);
      resaRect.setWidth(scaleFactor * resa);
      THRect3.setVisible(false);
      resaRect2.setVisible(false);
      resaRect2.setWidth(resa * scaleFactor);
      resaRect3.setVisible(false);
      THRect31.setVisible(false);
      resaRect4.setWidth(scaleFactor * resa);
      THRect4.setVisible(false);
      resaRect4.setVisible(false);
      asdaRect.setVisible(false);
      asdaId.setVisible(false);
      todaRect.setVisible(false);
      todaId.setVisible(false);
      toraId.setVisible(false);
      todaRect.setVisible(false);
      planeWrapper.translateXProperty().unbind();
      planeWrapper.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-130));
      if (currentPlaneImage != null) {
        currentPlaneImage.setTranslateY(0);
      }
    } else if  (obstaclePosition.get() > 0) {
      leftVertical.setVisible(false);
      leftHorizontal.setVisible(false);
      leftHypo.setVisible(false);
      rightHorizontal.setVisible(false);
      rightHypo.setVisible(false);
      rightVertical.setVisible(false);
      resaRect5.setWidth(scaleFactor * resa);
      THRect5.setVisible(false);
      resaRect5.setVisible(false);
      THRect3.setVisible(false);
      resaRect2.setWidth(scaleFactor * resa);
      resaRect2.setVisible(false);
      resaRect3.setWidth(scaleFactor * resa);
      resaRect3.setVisible(false);
      THRect31.setVisible(false);
      THRect4.setVisible(true);
      resaRect4.setVisible(true);
      resaRect4.setWidth(scaleFactor * resa);
      asdaRect.setVisible(false);
      asdaId.setVisible(false);
      todaRect.setVisible(false);
      todaId.setVisible(false);
      toraId.setVisible(false);
      todaRect.setVisible(false);
      if (currentPlaneImage != null) {
        currentPlaneImage.setTranslateX(550);
      }
    }
    asdaRect.setWidth(asda * scaleFactor);
    TRect.setVisible(true);
    THRect.setVisible(true);
    tId.setVisible(true);
    runwayRect.setWidth(1000);
    runwayRect.setVisible(true);
    asdaRect.setVisible(false);
    asdaId.setVisible(false);
    todaRect.setWidth(toda * scaleFactor);
    todaRect.setVisible(false);
    todaId.setVisible(false);
    toraId.setVisible(false);
    todaRect.setVisible(false);
    toraRect.setWidth(tora * scaleFactor);
    toraRect.setVisible(false);
    stopWayRect.setWidth(stopway * scaleFactor);
    clearRect.setWidth(cway * scaleFactor);
    ldaRect.setWidth(tora * scaleFactor);
    ldaRect.setVisible(true);
    ldaId.setVisible(true);
    if (cway == 0) {
      clearRect.setVisible(false);
      clearId.setVisible(false);
    } else {
      clearRect.setVisible(true);
      clearId.setVisible(true);
    }
    if (stopway == 0) {
      stopWayRect.setVisible(false);
      stopId.setVisible(false);
    } else {
      stopWayRect.setVisible(true);
      stopId.setVisible(true);
    }
  }



  public void updateObstacleVisualization(Obstacle obstacle, char direction, boolean isLanding, double position) {
    System.out.println("entered updateObstacleVisuaisation2");
    this.obstaclePosition.set(position);
    updateObstacle(obstacle, direction, isLanding);
  }
  private char currentDirection = 'e';
  private boolean isCurrentlyLanding = true;

  private void updateObstacle(Obstacle original, char direction, boolean isLanding) {

    obstacleWrapper.getChildren().clear();
    if (original != null && original.getImage().getImage() != null) {
      ImageView copy = new ImageView(original.getImage().getImage());
      copy.setFitWidth(original.getImage().getFitWidth());
      copy.setFitHeight(original.getImage().getFitHeight());
      copy.setPreserveRatio(original.getImage().isPreserveRatio());
      copy.setLayoutY(-copy.getFitHeight());

      obstacleWrapper.getChildren().addAll(copy);
    }
  }
  private void setUpZoom() {
    this.addEventHandler(ScrollEvent.SCROLL, event -> {
      double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
      zoom(zoomFactor, event.getX(), event.getY());
      event.consume();
    });

    this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      if (event.getButton() == MouseButton.SECONDARY) {
        Bounds bounds = getLayoutBounds();
        scaleTransform.setPivotX(bounds.getWidth() / 2);
        scaleTransform.setPivotY(bounds.getHeight() / 2);
        scaleTransform.setX(1.0);
        scaleTransform.setY(1.0);
        event.consume();
      }
    });
  }

  public void zoom(Double factor, double pivotX, double pivotY) {
    double currentScale = scaleTransform.getX();
    double newScale = currentScale * factor;
    newScale = Math.max(1.0, Math.min(newScale, 5.0));
    scaleTransform.setPivotX(pivotX);
    scaleTransform.setPivotY(pivotY);
    scaleTransform.setX(newScale);
    scaleTransform.setY(newScale);
  }

  public void obstacleWrapperToFront() {
    obstacleWrapper.toFront();
    planeWrapper.toFront();
    takeofflabel.toFront();
  }

  public void setTakeOffLabel(String newtakeofflabel) {
    takeofflabel.setText(newtakeofflabel);
  }

  // public void setTDMeasurementsTE(int el) {
  //   double scaleFactor = 1000.0 / el;
  //   this.currentEl = el;
  //   todas.setTranslateX(-(cway * scaleFactor));
  //   todas.setVisible(true);
  //   if (obstaclePosition.get() > el/2) {
  //   resa.setVisible(true);
  //   planeWrapper.translateXProperty().unbind();
  //     planeWrapper.translateXProperty().bind(obstaclePosition.multiply(scaleFactor).add(-130));
  //     if (currentPlaneImage != null) {
  //       currentPlaneImage.setRotate(0);
  //       currentPlaneImage.setTranslateY(200);
  //     }

  // }

  // public void setTDMeasurements(int toda) {
  //   double topDownResaLength = obstaclePosition.get();
  //   double resaLength2 = runwayLength - topDownResaLength;
  //   double resaScaledLength = resaLength2 * scaleFactor;
  //   topDownResa.setWidth(resaScaledLength);
  //   topDownResa.setTranslateX(topDownResaLength * scaleFactor);
  //   topDownToda.setVisible(true);
  //   topDownToda.setWidth(toda);
  //   topDownToda.setTranslateX(0);
  // }
}



