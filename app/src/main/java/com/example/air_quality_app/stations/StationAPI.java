package com.example.air_quality_app.stations;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StationAPI {
    @GET("station/findAll")
    Call<List<Station>> getPost();
}
