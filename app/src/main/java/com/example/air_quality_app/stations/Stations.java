package com.example.air_quality_app.stations;

import java.util.List;

public class Stations {
    private List<Station> stations;

    public List<Station> getStations(){
        return stations;
    }

    @Override
    public String toString() {
        return "Stations{" +
                "stations=" + stations +
                '}';
    }
}
