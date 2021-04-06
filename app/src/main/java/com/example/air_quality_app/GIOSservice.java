package com.example.air_quality_app;

import com.example.air_quality_app.airdata.AirData;
import com.example.air_quality_app.airqualityindex.AirQuality;
import com.example.air_quality_app.sensors.Sensors;
import com.example.air_quality_app.stations.Station;

import java.util.List;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface GIOSservice {
    String SERVICE_ENDPOINT = "https://api.gios.gov.pl/pjp-api/rest/";
    @Headers({"Accept: application/json"})
    @GET("station/findAll")
    Observable<List<Station>> getPost();

    @Headers({"Accept: application/json"})
    @GET("station/sensors/{stationID}")
    Observable<List<Sensors>> getSensors(@Path("stationID") String stationID);

    @Headers({"Accept: application/json"})
    @GET("data/getData/{sensorID}")
    Observable<AirData> getData(@Path("sensorID") String sensorID);

    @Headers({"Accept: application/json"})
    @GET("aqindex/getIndex/{stationID}")
    Observable<AirQuality> getAirQuality(@Path("stationID") String stationID);
}
