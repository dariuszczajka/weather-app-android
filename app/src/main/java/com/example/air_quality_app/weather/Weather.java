package com.example.air_quality_app.weather;

import java.util.Map;

public class Weather {

    private Map<String,Double> main;
    private Map<String,Double> wind;

    public Map<String,Double> getMain() {
        return main;
    }

    public Map<String, Double> getWind() {
        return wind;
    }
}
