package org.example.client.components;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import javafx.scene.image.ImageView;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"name", "width", "height", "blastprot"})
public class Planes {

    @XmlTransient
    private ImageView image = null;
    @XmlElement
    private String name = null;
    @XmlElement
    private int width = 0;
    // I might be tweaking but order here has to correspond with propOrder in reverse
    @XmlElement
    private int height = 0;
    @XmlElement
    private int blastprot = 0;

    public Planes() {}

    public Planes(String name, ImageView image) {
        this.name = name;
        this.image = image;
    }

    public Planes(String name, ImageView image, double width, double height, int blastProtVal) {
        this.name = name;
        this.image = image;
        image.setFitHeight(height);
        image.setFitWidth(width);
        this.blastprot = blastProtVal;
    }

    public ImageView getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBlastProt() {
        return blastprot;
    }

    public void setBlastProt(int blastProt) {
        this.blastprot = blastProt;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
