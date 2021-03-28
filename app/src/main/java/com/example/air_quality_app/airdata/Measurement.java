package com.example.air_quality_app.airdata;

public class Measurement {
    private String date;
    private double value;

    public double getValue() { return value; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        return "Measurement{" +
                "date='" + date + '\'' +
                ", value=" + value +
                '}';
    }
}
