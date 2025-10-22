package org.example.client.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.DefaultProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;

@DefaultProperty("value")
public class PlanesSelector extends ComboBox<String> {

    private final Map<String, Planes> PlanesMap = new HashMap<>();
    private final SimpleObjectProperty<Planes> selectedPlane = new SimpleObjectProperty<>(null);

    public PlanesSelector() {
        initComponent();
        setupListeners();
    }

    private void initComponent() {
        PlanesMap.put("None", null);
        getItems().addAll("None");

        setPromptText("Planes");
    }

    public void addPlane(Planes... planes) {
        Arrays.stream(planes).forEach(plane -> {
            PlanesMap.put(plane.getName(), plane);
            getItems().add(plane.getName());
        });
    }
    private void setupListeners() {
        this.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedPlane.set(PlanesMap.get(newVal));
        });
    }

    public SimpleObjectProperty<Planes> selectedPlaneProperty() {
        return selectedPlane;
    }

    public Planes getSelectedPlane() {
        return selectedPlane.get();
    }
}
