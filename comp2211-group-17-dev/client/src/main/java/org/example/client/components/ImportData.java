package org.example.client.components;

import org.example.client.Calculation;

public class ImportData {
    private String name;
    private String runwayPair;
    private String LRrunway;
    private Calculation calculation;
    private int eobjdist;
    private int wobjdist;
    private int centredist;
    private Obstacle obstacle;

    private String status;

    private Planes planes;

    public ImportData(String name, String status, String runwayPair, String LRrunway, Calculation calculation,int eobjdist, int wobjdist, int centredist, Obstacle obstacle, Planes planes){
        this.name = name;
        this.status = status;
        this.runwayPair = runwayPair;
        this.LRrunway = LRrunway;
        this.calculation=calculation;
        this.eobjdist = eobjdist;
        this.wobjdist = wobjdist;
        this.centredist = centredist;
        this.obstacle = obstacle;
        this.planes = planes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCentredist() {
        return centredist;
    }

    public void setCentredist(int centredist) {
        this.centredist = centredist;
    }

    public int getEobjdist() {
        return eobjdist;
    }

    public void setEobjdist(int eobjdist) {
        this.eobjdist = eobjdist;
    }

    public int getWobjdist() {
        return wobjdist;
    }

    public void setWobjdist(int wobjdist) {
        this.wobjdist = wobjdist;
    }

    public String getName() {
        return name;
    }

    public Calculation getCalculation() {
        return calculation;
    }

    public String getLRrunway() {
        return LRrunway;
    }

    public String getRunwayPair() {
        return runwayPair;
    }

    public Obstacle getObstacle() {
        return obstacle;
    }
    public Planes getPlanes() {
        return planes;
    }
    public void setPlanes(Planes planes){
        System.out.println("planes set!!!");
        this.planes = planes;
    }
}
