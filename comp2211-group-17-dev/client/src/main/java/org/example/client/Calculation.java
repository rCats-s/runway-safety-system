package org.example.client;

import static java.lang.Math.abs;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import org.example.client.theme.ThemedAlert;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "etora", "etoda", "easda", "elda", "wtora", "wtoda", "wasda", "wlda", 
    "strip", "resa", "wclearway", "wstopway", "eclearway", "estopway", "dt",
    "initelda", "initeasda", "initetora", "initetoda", "initwlda", "initwasda", 
    "initwtora", "initwtoda", "blastprot"
})
public class Calculation {
@XmlElement
  private  int strip;
  @XmlElement
  private  int eclearway;
  @XmlElement
  private  int estopway;
  @XmlElement
  private  int wclearway;
  @XmlElement
  private  int wstopway;
  @XmlElement
  private int elength;
  @XmlTransient
  private  IntegerProperty uilda = new SimpleIntegerProperty();
  @XmlTransient
  private  IntegerProperty uitora = new SimpleIntegerProperty();
  @XmlTransient
  private  IntegerProperty uitoda = new SimpleIntegerProperty();
  @XmlTransient
  private  IntegerProperty uiasda = new SimpleIntegerProperty();
  @XmlTransient
  // um idk how to implement this but yeah
  private final IntegerProperty time = new SimpleIntegerProperty(0);
  @XmlElement
   private int dt;
  @XmlElement
   private  int resa;
  @XmlElement
   private  int blastprot;
  @XmlElement
   private int etora;
  @XmlElement
   private int etoda;
  @XmlElement
   private int easda;
  @XmlElement
   private int elda;
  @XmlElement
   private int wtora;
  @XmlElement
   private int wtoda;
  @XmlElement
   private int wasda;
  @XmlElement
   private int wlda;
  @XmlElement
   private int initwlda;
  @XmlElement
   private int initetora;
  @XmlElement
   private int initetoda;
  @XmlElement
   private int initeasda;
  @XmlElement
   private int initelda;
  @XmlElement
   private int initwtora;
  @XmlElement
   private int initwtoda;
  @XmlElement
   private int initwasda;

  @XmlTransient
  private Alert alert = new ThemedAlert(Alert.AlertType.ERROR);;

  //dt = displacementthreshold
    /*
    Calculation class
    Takes params:
        -  eclearway: clearway for planes taking off/landing from eastern side (m)
        -  estopway: stopway for planes taking off/landing from eastern side (m)
        -  wclearway: clearway for planes taking off/landing from western side (m)
        -  wclearway: clearway for planes taking off/landing from western side (m)
        -   elength: length of runway from east (m)
        -   wlength: length of runway from west (m)
        - dt: displacement threshold (m) - can be used for takeoff but not landing
        - strip: strip end length (m)
        - resa: clearing before obstacle on runway (m)
        - blastprot: between 300-500m for obstacle clearance behind, depending on type of plane
        - initelda: initial LDA from eastern side of runway (m)
        - initwlda: initial LDA from western side of runway (m)
     */

  public Calculation(){
  }
  public Calculation(int eclearway, int estopway, int wclearway, int wstopway, int elength,
                     int wlength, int dt, int strip, int resa, int blastprot, int initelda, int initwlda, char dir) {
    this.elength = elength;
    this.dt = dt;
    this.blastprot = blastprot;
    this.resa = resa;
    this.strip = strip;
    this.eclearway = eclearway;
    this.estopway = estopway;
    this.wclearway = wclearway;
    this.wstopway = wstopway;
    initetora = etora = elength;
    initetoda = etoda = etora + eclearway;
    initeasda = easda = etora + estopway;
    initwtora = wtora = wlength;
    initwtoda = wtoda = wtora + wclearway;
    initwasda = wasda = etora + wstopway;
    this.initelda = elda = initelda - dt;
    this.initwlda = wlda = initwlda;
    displayInits(dir);
  }


  public int getEtora() {
    return etora;
  }
  public void setEtora(int etora) {
    this.etora = etora;
  }

   public int getEtoda() {
    return etoda;
  }

  public void setEtoda(int etoda) {
    this.etoda = etoda;
  }

   public int getEasda() {
    return easda;
  }

  public void setEasda(int easda) {
    this.easda = easda;
  }

   public int getElda() {
    return elda;
  }

  public void setElda(int elda) {
    this.elda = elda;
  }

   public int getWtora() {
    return wtora;
  }

  public void setWtora(int wtora) {
    this.wtora = wtora;
  }

   public int getWtoda() {
    return wtoda;
  }

  public void setWtoda(int wtoda) {
    this.wtoda = wtoda;
  }

   public int getWasda() {
    return wasda;
  }

  public void setWasda(int wasda) {
    this.wasda = wasda;
  }

   public int getWlda() {
    return wlda;
  }

  public void setWlda(int wlda) {
    this.wlda = wlda;
  }

   public int getStrip() {
    return strip;
  }

  public void setStrip(int strip) {
    this.strip = strip;
  }

   public int getResa() {
    return resa;
  }

  public void setResa(int resa) {
    this.resa = resa;
  }

   public int getWclearway() {
    return wclearway;
  }

  public void setWclearway(int wclearway) {
    this.wclearway = wclearway;
  }
  public int getElength() {return elength;}

  // Setter
  public void setElength(int elength) {
    this.elength = elength;
  }

   public int getWstopway() {
    return wstopway;
  }

  public void setWstopway(int wstopway) {
    this.wstopway = wstopway;
  }

   public int getEclearway() {
    return eclearway;
  }

  public void setEclearway(int eclearway) {
    this.eclearway = eclearway;
  }

   public int getEstopway() {
    return estopway;
  }

  public void setEstopway(int estopway) {
    this.estopway = estopway;
  }

   public int getThreshold() {
    return dt;
  }

  public void setThreshold(int threshold) {
    this.dt = threshold;
  }

   public int getInitelda() {
    return initelda;
  }

  public void setInitelda(int initelda) {
    this.initelda = initelda;
  }

   public int getIniteasda() {
    return initeasda;
  }

  public void setIniteasda(int initeasda) {
    this.initeasda = initeasda;
  }

   public int getInitetora() {
    return initetora;
  }

  public void setInitetora(int initetora) {
    this.initetora = initetora;
  }

   public int getInitetoda() {
    return initetoda;
  }

  public void setInitetoda(int initetoda) {
    this.initetoda = initetoda;
  }

   public int getInitwlda() {
    return initwlda;
  }

  public void setInitwlda(int initwlda) {
    this.initwlda = initwlda;
  }

   public int getInitwasda() {
    return initwasda;
  }

  public void setInitwasda(int initwasda) {
    this.initwasda = initwasda;
  }

   public int getInitwtora() {
    return initwtora;
  }

  public void setInitwtora(int initwtora) {
    this.initwtora = initwtora;
  }

   public int getInitwtoda() {
    return initwtoda;
  }

  public void setInitwtoda(int initwtoda) {
    this.initwtoda = initwtoda;
  }
   public int getBlastprot() {
    return blastprot;
  }

  public void setBlastprot(int blastprot) {
    this.blastprot = blastprot;
  }





  public void refreshUI(){
    uiasda.set(wasda);
    uitora.set(wtora);
    uitoda.set(wtoda);
    uilda.set(wlda);
  }

  /*
  Takes params:
      - eobjdist: distance of obstacle from eastern threshold - negative is in stopway/clearway, positive towards the western end
      - wobjdist: distance of obstacle from western threshold - negative is in stopway/clearway, positive towards the eastern end
      - centerdist: distance of obstacle from centreline - -ve is south of the centreline, +ve is north of the centreline
      - height: height of the obstacle
   */
  public void calc(int eobjdist, int wobjdist, int centredist, int height, char dir) {
    //In case of having an obstacle within 75m from the centreline and 60m from the runway, the runway parameters need to be re--‐declared.
    //Otherwise, no re--‐declaration is required
    int edistfromthresh = eobjdist;
    if (initetoda == 0 || initwtoda == 0 ){
      alert.setHeaderText("Runway not set");
      alert.setContentText("Calculations cannot occur without selecting a runway");
      alert.showAndWait();
      return;
    }
    if (validateDistances(eobjdist,wobjdist, centredist)){
      alert.setHeaderText("Invalid Calculation");
      alert.setContentText("Distance values cannot be greater than the length of the runway. Please try again. TODA is "+initetoda);
      alert.showAndWait();
      return;
    }
    if (eobjdist >= -60 && wobjdist >= -60 && abs(centredist) <= 75) {
      //obj before eastern threshold or too close to take off/land
      if (eobjdist <= 0 || eobjdist <= (height * 50 + 300)) {
        recalculateTakingOffAway(edistfromthresh, 'e');
        recalculateLandingOver(edistfromthresh, height, strip, 'e');
        recalculateTakingOffTowards(wobjdist, height, strip, resa, 'w');
        recalculateLandingTowards(wobjdist, resa, strip, 'w');
      }
      //obj allows for take off/ landing towards it from east
      if (eobjdist > (height * 50 + 300)) {
        recalculateTakingOffAway(wobjdist, 'w');
        recalculateLandingOver(wobjdist, height, strip, 'w');
        recalculateTakingOffTowards(edistfromthresh, height, strip, resa,
                'e');
        recalculateLandingTowards(edistfromthresh, resa, strip, 'e');
      }
      if (validateOut()) {
        switch (dir) {
          case 'e':
            uilda.set(elda);
            uiasda.set(easda);
            uitora.set(etora);
            uitoda.set(etoda);
            break;
          case 'w':
            uilda.set(wlda);
            uiasda.set(wasda);
            uitora.set(wtora);
            uitoda.set(wtoda);
            break;
        }
      }
      //Error handling for stupid calcs, shouldn't have a negative output.
      else {
        alert.setHeaderText("Invalid Calculation");
        alert.setContentText("Check input vals for typos - negative calculations should not occur");
        alert.showAndWait();
      }
    }
    //if out of range reset vals as if object doesn't exist
    else {
      displayInits(dir);

    }
  }

  private boolean validateDistances(int eobjdist, int wobjdist, int centredist){
    return ((eobjdist > initetoda) || (wobjdist > initwtoda));
  }



  private boolean isPositive(int val){
    return val>=0;
  }
  private boolean validateOut(){
    return (isPositive(elda) && isPositive(wlda) && isPositive(etoda) && isPositive(wtoda) && isPositive(etora) && isPositive(wtora) && isPositive(wasda) && isPositive(easda));
  }

  private void displayInits(char dir){
    switch (dir) {
      case 'e':
      uilda.set(initelda);
      uiasda.set(initeasda);
      uitora.set(initetora);
      uitoda.set(initetoda);
      break;
      case 'w':
        uilda.set(initwlda);
        uiasda.set(initwasda);
        uitora.set(initwtora);
        uitoda.set(initwtoda);
        break;
    }
  }

  /*
  Takes params:
      - distfromthresh: location of the object in relation to e/w threshold (m)
      - height: object height (m)
      - strip: strip end length (m)
      - dir: indicates 'e' or 'w' depending on which runway threshold
   */
  private void recalculateLandingOver(int distfromthresh, int height, int strip, char dir) {
    switch (dir) {
      case 'e':
        elda = elda - distfromthresh - (height * 50) - strip;
        break;
      case 'w':
        wlda = wlda - (height * 50) - distfromthresh - strip;
        break;
    }
  }

  public void reset(){
    uilda.set(initelda);
    uiasda.set(initeasda);
    uitora.set(initetora);
    uitoda.set(initetoda);
  }
  /*
  Takes params:
      - distfromthresh: location of the object in relation to e/w threshold (m)
      - resa: clearing before obstacle on runway (m)
      - strip: strip end length (m)
      - dir: indicates 'e' or 'w' depending on which runway threshold
   */
  private void recalculateLandingTowards(int distfromthresh, int resa, int strip, char dir) {
    switch (dir) {
      case 'e':
        elda = distfromthresh - resa - strip;
        break;
      case 'w':
        wlda = distfromthresh - resa - strip;
        break;
    }
  }

  /*
  Takes params:
      - distfromthresh: location of the object in relation to e/w threshold (m)
      - dir: indicates 'e' or 'w' depending on which runway threshold
   */
  private void recalculateTakingOffAway(int distfromthresh, char dir) {
    switch (dir) {
      case 'e':
        etora = initetora - distfromthresh - blastprot - dt;
        etoda = etora + eclearway;
        easda = etora + estopway;
        break;
      case 'w':
        wtora = initwtora - strip - resa - distfromthresh;
        wasda = wtora + wstopway;
        wtoda = wtora + wclearway;
        break;
    }
  }

  /*
  Takes params:
      - distfromthresh: location of the object in relation to e/w threshold (m)
      - height: object height (m)
      - strip: strip end length (m)
      - resa: clearing before obstacle on runway (m)
      - dir: indicates 'e' or 'w' depending on which runway threshold
   */
  private void recalculateTakingOffTowards(int distfromthresh, int height, int strip, int resa,
                                           char dir) {
    switch (dir) {
      case 'e':
        etora = distfromthresh + dt - (height * 50) - strip;
        easda = etora;
        etoda = etora;
        elda = distfromthresh - strip - resa;
        break;
      case 'w':
        wtora = distfromthresh - (height * 50) - strip;
        wtoda = wtora;
        wasda = wtora;
        wlda = distfromthresh - resa - strip;
        break;
    }
  }

  public IntegerProperty getUiAsda() {
    return uiasda;
  }

  public IntegerProperty getUiTora() {
    return uitora;
  }

  public IntegerProperty getUiToda() {
    return uitoda;
  }

  public IntegerProperty getUiLda() {
    return uilda;
  }

  public IntegerProperty getTime() {
    return time;
  }

  public int getAsda() {
    return easda;
  }

  public int getTora() {
    return etora;
  }

  public int getToda() {
    return etoda;
  }

  public int getLda() {
    return elda;
  }
}