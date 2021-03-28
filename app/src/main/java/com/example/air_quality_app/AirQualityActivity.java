package com.example.air_quality_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.air_quality_app.airdata.AirData;
import com.example.air_quality_app.airdata.DataAPI;
import com.example.air_quality_app.sensors.Sensors;
import com.example.air_quality_app.sensors.SensorsAPI;
import com.example.air_quality_app.stations.Station;
import com.example.air_quality_app.stations.StationAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AirQualityActivity extends AppCompatActivity {

    HashMap<Integer, Station> stationHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);
        if(stationHashMap.isEmpty()){
            getAllStationsFromApi();
        }

        //development
        getDataFromSensor(92);
    }

    private void getAllStationsFromApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.gios.gov.pl/pjp-api/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StationAPI stationAPI = retrofit.create(StationAPI.class);
        Call<List<Station>> call = stationAPI.getPost();

        call.enqueue(new Callback<List<Station>>() {

            @Override
            public void onResponse(Call<List<Station>> call, Response<List<Station>> response) {
                for (int i=0;i<response.body().size();i++) {
                    stationHashMap.put(response.body().get(i).getId(),response.body().get(i));
                }
            }
            @Override
            public void onFailure(Call<List<Station>> call, Throwable t) {
                Log.i("testy",t.getMessage());
            }
        });
    }

    private ArrayList<Sensors> getSensorsFromStation(int stationID){
        ArrayList<Sensors> resp = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.gios.gov.pl/pjp-api/rest/station/sensors/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SensorsAPI sensorsAPI = retrofit.create(SensorsAPI.class);
        Call<List<Sensors>> call = sensorsAPI.getPost(String.valueOf(stationID));

        call.enqueue(new Callback<List<Sensors>>() {
            @Override
            public void onResponse(Call<List<Sensors>> call, Response<List<Sensors>> response) {
                for(int i=0;i<response.body().size();i++){
                    resp.add(response.body().get(i));
                }

            }

            @Override
            public void onFailure(Call<List<Sensors>> call, Throwable t) {
                Log.i("testy",t.getMessage());
                //TODO: try/catch block
            }
        });
        return resp;
    }

    private LinkedList<AirData> getDataFromSensor(int sensorID){
        LinkedList<AirData> ad = new LinkedList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.gios.gov.pl/pjp-api/rest/data/getData/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DataAPI dataAPI = retrofit.create(DataAPI.class);
        Call<AirData> call = dataAPI.getPost(String.valueOf(sensorID));

        call.enqueue(new Callback<AirData>() {
            @Override
            public void onResponse(Call<AirData> call, Response<AirData> response) {
                   for(int i=0;i<response.body().getValues().size();i++){
                       Log.i("testy",response.body().getValues().get(i).toString());
                   }
            }

            @Override
            public void onFailure(Call<AirData> call, Throwable t) {
                Log.i("testy",t.getMessage());
                //TODO: try/catch block
            }
        });
        return ad;
    }

}