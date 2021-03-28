package com.example.air_quality_app.airqualityindex;

public class stIndexLevel {
    private int id;
    private String indexLevelName;

    public int getId() { return id; }

    public String getIndexLevelName() { return indexLevelName; }

    @Override
    public String toString() {
        return "stIndexLevel{" +
                "id=" + id +
                ", indexLevelName='" + indexLevelName + '\'' +
                '}';
    }
}
