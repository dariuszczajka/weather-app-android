package com.example.air_quality_app.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPI {
    @GET("data/2.5/weather")
    Call<Weather> getPost(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("units") String units,
            @Query("APPID") String APPID
    );
}
