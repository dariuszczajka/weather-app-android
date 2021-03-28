package com.example.air_quality_app.stations;

public class Commune {
    private String communeName;
    private String districtName;
    private String provinceName;

    public String getCommuneName() { return communeName; }
    public String getDistrictName() { return districtName; }
    public String getProvinceName() { return provinceName; }

    @Override
    public String toString() {
        return "Commune{" +
                "communeName='" + communeName + '\'' +
                ", districtName='" + districtName + '\'' +
                ", provinceName='" + provinceName + '\'' +
                '}';
    }
}
