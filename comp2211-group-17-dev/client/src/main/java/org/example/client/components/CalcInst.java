package org.example.client.components;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;
import org.example.client.Calculation;

@XmlRootElement(name = "CalcInst")
@XmlType(propOrder = { "name", "status","runwayPair", "LRrunway","calculation", "eobjdist","wobjdist","centredist","obstacle","planes"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CalcInst {
    @XmlElement
    private String name;
    @XmlElement
    private String status;
    @XmlElement
    private String runwayPair;
    @XmlElement
    private String LRrunway;
    @XmlElement(name = "Calculation")
    private Calculation calculation;
    @XmlElement
    private int eobjdist;
    @XmlElement
    private int wobjdist;
    @XmlElement
    private int centredist;
    @XmlElement(name = "Obstacle")
    private Obstacle obstacle;
    @XmlElement(name="Planes")
    private Planes planes;
    public CalcInst(){
        this.calculation = new Calculation();

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

    public void setWobjdist(int wobjdist) {
        this.wobjdist = wobjdist;
    }

    public int getWobjdist() {
        return wobjdist;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
       public Calculation getCalculation() {
        return calculation;
    }
    public String getRunwayPair(){
        return runwayPair;
    }
    public void setRunwayPair(String runwayPair){
        this.runwayPair = runwayPair;
    }
    public String getLRrunway(){
        return LRrunway;
    }
    public void setLRrunway(String LRrunway){
        this.LRrunway = LRrunway;
    }
    public void setCalc(Calculation calculation) {
        this.calculation = calculation;
    }
    public Obstacle getObstacle() {
        return obstacle;
    }
    public void setObstacle(Obstacle obstacle) {
        this.obstacle = obstacle;
    }

    public void setPlane(Planes planes){ this.planes = planes;}
    public Planes getPlane(){ return planes;}


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
