package com.example.air_quality_app.airqualityindex;

import com.example.air_quality_app.airdata.AirData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface AirQualityAPI{
    @GET
    Call<AirQuality> getPost(@Url String url);
}
