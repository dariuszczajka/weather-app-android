package com.example.air_quality_app.stations;

import com.example.air_quality_app.stations.Commune;

public class City {
    private int id;
    private String name;
    private Commune commune;

    public int getId() { return id; }
    public Commune getCommune() { return commune; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", commune=" + commune +
                '}';
    }
}
