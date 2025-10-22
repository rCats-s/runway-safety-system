package org.example;

import java.io.Serializable;

public class Airport implements Serializable {
    private int id;
    private String name;
    private String code;
    private String location;

    // Default constructor required for ORMLite
    public Airport() {
    }

    public Airport(String name, String code, String location) {
        this.name = name;
        this.code = code;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
