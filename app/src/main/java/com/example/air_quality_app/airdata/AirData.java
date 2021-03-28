package com.example.air_quality_app.airdata;

import java.util.List;

public class AirData {
    private String key;
    private List<Measurement> values;

    public List<Measurement> getValues() { return values; }
    public String getKey() { return key; }

    @Override
    public String toString() {
        return "AirData{" +
                "key='" + key + '\'' +
                ", values=" + values +
                '}';
    }
}
