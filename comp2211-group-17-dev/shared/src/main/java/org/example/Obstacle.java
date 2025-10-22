package org.example;

import java.io.Serializable;

public class Obstacle implements Serializable {
    private Integer id;
    private String name;
    private Integer height;
    private Integer distanceFromThreshold;
    private Integer offsetFromCenterLine;
    private String description;
}
