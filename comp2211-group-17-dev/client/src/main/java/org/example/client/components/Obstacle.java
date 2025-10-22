package org.example.client.components;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Obstacle {

  private  ImageView image;

  private  String name;

  private int physicalheight = 0;

  private String path;


  public Obstacle(){
    name = new String();
    image = new ImageView();
  }


  public Obstacle(String name, ImageView image) {
    this.name = name;
    this.image = image;
  }

  public Obstacle(String name, ImageView image, double width, double height, int physicalheight) {
    this.name = name;
    this.image = image;
    image.setFitHeight(height);
    image.setFitWidth(width);
    this.physicalheight = physicalheight;
  }

  @XmlTransient
  public String getPath() {
    return path;
  }
  public void setPath(String path){
    this.path = path;
  }
  @XmlElement
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  @XmlElement
  public int getPhysicalheight() { return physicalheight;}
  public void setPhysicalheight(int physicalheight){
    this.physicalheight = physicalheight;
  }

  @XmlTransient
  public ImageView getImage(){
    return image;
  }
  @XmlTransient
  public void setImage(String path){
    image = new ImageView(new Image(getClass().getResourceAsStream(path)));
  }

}
