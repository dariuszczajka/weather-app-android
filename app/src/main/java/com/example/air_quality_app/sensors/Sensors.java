package com.example.air_quality_app.sensors;

public class Sensors {
    private int id;
    private int stationId;
    private Param param;

    public int getId() { return id; }
    public int getStationId() { return stationId; }
    public Param getParam() { return param; }

    @Override
    public String toString() {
        return "Sensors{" +
                "id=" + id +
                ", stationId=" + stationId +
                ", param=" + param +
                '}';
    }
}
