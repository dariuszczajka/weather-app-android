package com.example.air_quality_app.airdata;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface DataAPI {
    @GET("{sensorID}")
    Observable<AirData> getPost(@Path("sensorID") String url);
}
