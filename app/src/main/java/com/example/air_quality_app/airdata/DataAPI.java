package com.example.air_quality_app.airdata;

import com.example.air_quality_app.sensors.Sensors;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface DataAPI {
    @GET
    Call<AirData> getPost(@Url String url);
}
