package com.example.air_quality_app.sensors;

public class Param {
    private String paramName;
    private String paramFormula;
    private String paramCode;
    private int idParam;

    public int getIdParam() { return idParam; }
    public String getParamCode() { return paramCode; }
    public String getParamFormula() { return paramFormula; }
    public String getParamName() { return paramName; }

    @Override
    public String toString() {
        return "Param{" +
                "paramName='" + paramName + '\'' +
                ", paramFormula='" + paramFormula + '\'' +
                ", paramCode='" + paramCode + '\'' +
                ", idParam=" + idParam +
                '}';
    }
}
