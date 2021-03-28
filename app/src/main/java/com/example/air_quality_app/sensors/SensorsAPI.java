package com.example.air_quality_app.sensors;

import com.example.air_quality_app.stations.Station;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface SensorsAPI {
    @GET
    Call<List<Sensors>> getPost(@Url String url);
}
