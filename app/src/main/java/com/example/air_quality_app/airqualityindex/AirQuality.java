package com.example.air_quality_app.airqualityindex;

public class AirQuality {
    private int id;
    private String stCalcDate;
    private stIndexLevel stIndexLevel;
    private String stSourceDataDate;
    private String so2CalcDate;
    private stIndexLevel so2IndexLevel;
    private String so2SourceDataDate;
    //private int no2CalcDate;
    private stIndexLevel no2IndexLevel;
    private String no2SourceDataDate;
    private String coCalcDate;
    private stIndexLevel coIndexLevel;
    private String coSourceDataDate;
    private String pm10CalcDate;
    private stIndexLevel pm10IndexLevel;
    private String pm10SourceDataDate;
    private String pm25CalcDate;
    private String pm25IndexLevel;
    private String pm25SourceDataDate;
    private String o3CalcDate;
    private stIndexLevel o3IndexLevel;
    private String o3SourceDataDate;
    private String c6h6CalcDate;
    private stIndexLevel c6h6IndexLevel;
    private String c6h6SourceDataDate;
    private boolean stIndexStatus;
    private String stIndexCrParam;

    public int getId() { return id; }
    public String getStCalcDate() { return stCalcDate; }
    public stIndexLevel getStIndexLevel(){ return stIndexLevel; }

    public String getStSourceDataDate() {
        return stSourceDataDate;
    }

    @Override
    public String toString() {
        return "AirQuality{" +
                "id=" + id +
                ", stCalcDate='" + stCalcDate + '\'' +
                ", stIndexLevel=" + stIndexLevel +
                ", stSourceDataDate='" + stSourceDataDate + '\'' +
                ", so2CalcDate='" + so2CalcDate + '\'' +
                ", so2IndexLevel=" + so2IndexLevel +
                ", so2SourceDataDate='" + so2SourceDataDate + '\'' +
                ", no2IndexLevel=" + no2IndexLevel +
                ", no2SourceDataDate='" + no2SourceDataDate + '\'' +
                ", coCalcDate='" + coCalcDate + '\'' +
                ", coIndexLevel=" + coIndexLevel +
                ", coSourceDataDate='" + coSourceDataDate + '\'' +
                ", pm10CalcDate='" + pm10CalcDate + '\'' +
                ", pm10IndexLevel=" + pm10IndexLevel +
                ", pm10SourceDataDate='" + pm10SourceDataDate + '\'' +
                ", pm25CalcDate='" + pm25CalcDate + '\'' +
                ", pm25IndexLevel='" + pm25IndexLevel + '\'' +
                ", pm25SourceDataDate='" + pm25SourceDataDate + '\'' +
                ", o3CalcDate='" + o3CalcDate + '\'' +
                ", o3IndexLevel=" + o3IndexLevel +
                ", o3SourceDataDate='" + o3SourceDataDate + '\'' +
                ", c6h6CalcDate='" + c6h6CalcDate + '\'' +
                ", c6h6IndexLevel=" + c6h6IndexLevel +
                ", c6h6SourceDataDate='" + c6h6SourceDataDate + '\'' +
                ", stIndexStatus=" + stIndexStatus +
                ", stIndexCrParam='" + stIndexCrParam + '\'' +
                '}';
    }
}

