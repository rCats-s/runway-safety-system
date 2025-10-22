package org.example;

import java.io.Serializable;

public class Runway implements Serializable {
    private int id;
    private String designation;
    private Integer airportId; // Please use Integer instead of int to allow null values!
    private int originalTORA;
    private int originalTODA;
    private int originalASDA;
    private int originalLDA;
    private int displacedThreshold;
    private int clearway;
    private int stopway;

    public Runway() {
    }

    public Runway(String designation, Integer airportId, int originalTORA, int originalTODA, int originalASDA, int originalLDA, 
                 int displacedThreshold, int clearway, int stopway) {
        this.designation = designation;
        this.airportId = airportId;
        this.originalTORA = originalTORA;
        this.originalTODA = originalTODA;
        this.originalASDA = originalASDA;
        this.originalLDA = originalLDA;
        this.displacedThreshold = displacedThreshold;
        this.clearway = clearway;
        this.stopway = stopway;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Integer getAirportId() {
        return airportId;
    }

    public void setAirportId(Integer airportId) {
        this.airportId = airportId;
    }

    public int getOriginalTORA() {
        return originalTORA;
    }

    public void setOriginalTORA(int originalTORA) {
        this.originalTORA = originalTORA;
    }

    public int getOriginalTODA() {
        return originalTODA;
    }

    public void setOriginalTODA(int originalTODA) {
        this.originalTODA = originalTODA;
    }

    public int getOriginalASDA() {
        return originalASDA;
    }

    public void setOriginalASDA(int originalASDA) {
        this.originalASDA = originalASDA;
    }

    public int getOriginalLDA() {
        return originalLDA;
    }

    public void setOriginalLDA(int originalLDA) {
        this.originalLDA = originalLDA;
    }

    public int getDisplacedThreshold() {
        return displacedThreshold;
    }

    public void setDisplacedThreshold(int displacedThreshold) {
        this.displacedThreshold = displacedThreshold;
    }

    public int getClearway() {
        return clearway;
    }

    public void setClearway(int clearway) {
        this.clearway = clearway;
    }

    public int getStopway() {
        return stopway;
    }

    public void setStopway(int stopway) {
        this.stopway = stopway;
    }

    @Override
    public String toString() {
        return designation + " (" + originalTORA + "m)";
    }
}
