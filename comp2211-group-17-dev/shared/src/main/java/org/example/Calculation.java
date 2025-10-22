package org.example;

import java.io.Serializable;

public class Calculation implements Serializable {
    private Integer id;
    private String name; // not in schema
    private long calculationTimestamp;
    private Integer newTORA;
    private Integer newTODA;
    private Integer newASDA;
    private Integer newLDA;
    private String details;

    public Calculation() {}

    public Calculation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNewTORA() {
        return newTORA;
    }

    public Integer getNewASDA() {
        return newASDA;
    }

    public Integer getNewLDA() {
        return newLDA;
    }

    public Integer getNewTODA() {
        return newTODA;
    }

    public void setNewASDA(Integer newASDA) {
        this.newASDA = newASDA;
    }

    public void setNewLDA(Integer newLDA) {
        this.newLDA = newLDA;
    }

    public void setNewTODA(Integer newTODA) {
        this.newTODA = newTODA;
    }

    public void setNewTORA(Integer newTORA) {
        this.newTORA = newTORA;
    }

    @Override
    public String toString() {
        return name;
    }
}
