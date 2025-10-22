package org.example.client.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.DefaultProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;

@DefaultProperty("value")
public class ObstacleSelector extends ComboBox<String> {

  private final Map<String, Obstacle> obstacleMap = new HashMap<>();
  private final SimpleObjectProperty<Obstacle> selectedObstacle = new SimpleObjectProperty<>(null);

  public ObstacleSelector() {
    initComponent();
    setupListeners();
  }

  private void initComponent() {
    obstacleMap.put("None", null);
    getItems().addAll("None");

    setPromptText("Obstacles");
  }

  public void addObstacles(Obstacle... obstacles) {
    // optimise ts idc at this stage
    Arrays.stream(obstacles).forEach((obstacle -> {
      obstacleMap.put(obstacle.getName(), obstacle);
      getItems().add(obstacle.getName());
    }));

  }

  private void setupListeners() {
    this.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      selectedObstacle.set(obstacleMap.get(newVal));
    });
  }

  public SimpleObjectProperty<Obstacle> selectedObstacleProperty() {
    return selectedObstacle;
  }

  public Obstacle getSelectedObstacle() {
    return selectedObstacle.get();
  }
}
