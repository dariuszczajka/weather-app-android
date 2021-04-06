package com.example.air_quality_app.stations;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Station implements Serializable {
    private int id;
    private String stationName;
    private double gegrLat;
    private double gegrLon;
    private City city;
    private String addressStreet;

    protected Station(Parcel in) {
        id = in.readInt();
        stationName = in.readString();
        gegrLat = in.readDouble();
        gegrLon = in.readDouble();
        addressStreet = in.readString();
    }

    public int getId() { return id; }
    public String getStationName() { return stationName; }
    public double getGegrLat() { return gegrLat; }
    public double getGegrLon() { return gegrLon; }
    public City getCity() { return city; }
    public String getAddressStreet() { return addressStreet; }

    @Override
    public String toString() {
        return "Station{" +
                "id=" + id +
                ", stationName='" + stationName + '\'' +
                ", gegrLat=" + gegrLat +
                ", gegrLon=" + gegrLon +
                ", city=" + city +
                ", addressStreet='" + addressStreet + '\'' +
                '}';
    }

}
