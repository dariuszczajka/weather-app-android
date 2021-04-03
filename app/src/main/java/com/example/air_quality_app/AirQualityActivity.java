package com.example.air_quality_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import com.example.air_quality_app.airdata.AirData;
import com.example.air_quality_app.airdata.DataAPI;
import com.example.air_quality_app.airdata.Measurement;
import com.example.air_quality_app.airqualityindex.AirQuality;
import com.example.air_quality_app.airqualityindex.AirQualityAPI;
import com.example.air_quality_app.sensors.Sensors;
import com.example.air_quality_app.sensors.SensorsAPI;
import com.example.air_quality_app.stations.Station;
import com.example.air_quality_app.stations.StationAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AirQualityActivity extends AppCompatActivity {
    private static HashMap<Integer, Station> stationHashMap = new HashMap<>();
    private LinkedList<Sensors> sensorsList = new LinkedList<>();
    private static LinkedList<AirData> measurementsList = new LinkedList<>();
    private AirQuality qualityIndex = new AirQuality();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);

        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat",0);
        double lon = intent.getDoubleExtra("lon",0);
        int closestID = 0;
        if(lat != 0 && lon != 0){
            TextView tv = findViewById(R.id.testing);
            closestID = findClosestStation(lat,lon);
            tv.setText("lat = " + lat + " " + "lon = " + lon + " \n STACJA: " + closestID);
        }

        getAllStationsFromApi(lon, lat);

    }

    private void getAllStationsFromApi(double lon, double lat) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.gios.gov.pl/pjp-api/rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StationAPI stationAPI = retrofit.create(StationAPI.class);
        Call<List<Station>> call = stationAPI.getPost();
        call.enqueue(new Callback<List<Station>>() {

            @Override
            public void onResponse(Call<List<Station>> call, Response<List<Station>> response) {
                for (int i = 0; i < response.body().size(); i++) {
                    stationHashMap.put(response.body().get(i).getId(), response.body().get(i));
                }

                int closestStationID = stationHashMap.get(findClosestStation(lat,lon)).getId();
                Log.i("testy","najbliza stacja = " + closestStationID);
                TextView city = findViewById(R.id.text_city);
                city.setText(stationHashMap.get(closestStationID).getStationName());


                Retrofit retrofit1 = new Retrofit.Builder()
                        .baseUrl("https://api.gios.gov.pl/pjp-api/rest/station/sensors/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                SensorsAPI sensorsAPI = retrofit1.create(SensorsAPI.class);
                Call<List<Sensors>> call1 = sensorsAPI.getPost(String.valueOf(closestStationID));

                call1.enqueue(new Callback<List<Sensors>>() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onResponse(Call<List<Sensors>> call, Response<List<Sensors>> response) {
                        for (int i = 0; i < response.body().size(); i++) {
                            sensorsList.add(response.body().get(i));
                        }

                        Retrofit retrofit2 = new Retrofit.Builder()
                                .baseUrl("https://api.gios.gov.pl/pjp-api/rest/data/getData/")
                                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        DataAPI dataAPI = retrofit2.create(DataAPI.class);

                        List<Observable<AirData>> requests = new ArrayList<>();

                        for(int i=0;i<sensorsList.size();i++){
                            requests.add(dataAPI.getPost(sensorsList.get(i).getId()+""));
                        }

                        Observable.zip(
                                requests,
                                (Function<Object[], List<AirData>>) objects -> {
                                    List<AirData> airDataList = new ArrayList<>();
                                    for(Object o : objects){
                                        airDataList.add((AirData) o);
                                    }
                                    return airDataList;
                        })
                        .subscribe(
                                (Consumer<List<AirData>>) airDataList -> {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            getQualityIndexFromStation(closestStationID);
                                            measurementsList.addAll(airDataList);
                                            setValues();
                                        }
                                    });

                                },

                                (Consumer<Throwable>) e -> Log.e("testy", "Throwable: " + e)
                        );

                    }

                    @Override
                    public void onFailure(Call<List<Sensors>> call, Throwable t) {
                        Log.i("testy", t.getMessage());
                        //TODO: try/catch block
                    }
                });
            }
            @Override
            public void onFailure(Call<List<Station>> call, Throwable t) {
                Log.i("testy",t.getMessage());
            }
        });
    }


    private void setValues(){
        TextView so2 = findViewById(R.id.text_so2);
        TextView no2 = findViewById(R.id.text_no2);
        TextView o3 = findViewById(R.id.text_o3);
        TextView pm10 = findViewById(R.id.text_pm10);

        int not_null_index = -1;

        for(AirData o : measurementsList){
            for(int i = 0; i < o.getValues().size(); i++){
                if(o.getValues().get(i).getValue() != 0.0){
                    not_null_index = i;
                    break;
                }
            }

            switch(o.getKey()){
                case "SO2":
                    so2.setText(o.getValues().get(not_null_index).getValue()+"");
                    break;
                case "NO2":
                    no2.setText(o.getValues().get(not_null_index).getValue()+"");
                    break;
                case "O3":
                    o3.setText(o.getValues().get(not_null_index).getValue()+"");
                    break;
                case "PM10":
                    pm10.setText(o.getValues().get(not_null_index).getValue()+"");
                    break;
                default:
                    Log.i("testy", "218 linijka switch case, coś poszło nie tak :c");

            }
        }

    }

    private void getQualityIndexFromStation(int stationID){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.gios.gov.pl/pjp-api/rest/aqindex/getIndex/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AirQualityAPI airQualityAPI = retrofit.create(AirQualityAPI.class);
        Call<AirQuality> call = airQualityAPI.getPost(String.valueOf(stationID));

        call.enqueue(new Callback<AirQuality>() {
            @Override
            public void onResponse(Call<AirQuality> call, Response<AirQuality> response) {
                qualityIndex = response.body();
                TextView air_quality = findViewById(R.id.text_quality);
                Log.i("testy", qualityIndex.getStIndexLevel().getIndexLevelName() + "");
                air_quality.setText(qualityIndex.getStIndexLevel().getIndexLevelName());
            }

            @Override
            public void onFailure(Call<AirQuality> call, Throwable t) {
                //Log.i("testy",t.getMessage());
            }
        });
    }


    private int findClosestStation(double lat, double lon){
        double station_lat = 0;
        double station_lon = 0;
        int closestStation = -1;
        double closestDistance = -1;
        double distance;
        for (int i: stationHashMap.keySet()) {
            station_lat = stationHashMap.get(i).getGegrLat();
            station_lon = stationHashMap.get(i).getGegrLon();
            distance = Math.sqrt(Math.pow(Math.abs(station_lat-lat),2)+Math.pow(Math.abs(station_lon-lon),2));
            if(distance < closestDistance || closestDistance == -1){
                closestDistance = distance;
                closestStation = i;
            }
        }
        return closestStation;
    }

}