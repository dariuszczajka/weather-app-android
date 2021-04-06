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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AirQualityActivity extends AppCompatActivity {
    private static ArrayList<Station> stationsList = new ArrayList<>();
    private ArrayList<Sensors> sensorsList = new ArrayList<>();
    private static ArrayList<AirData> measurementsList = new ArrayList<>();
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


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GIOSservice.SERVICE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();


        GIOSservice gioSservice = retrofit.create(GIOSservice.class);

        Disposable disposable = gioSservice.getPost()
                .subscribeOn(Schedulers.io())
                .flatMap((Function<List<Station>, ObservableSource<List<Sensors>>>) response1 -> {
                    Log.i("testy","callback 1");
                    stationsList.addAll(response1);
                    return gioSservice.getSensors(444+"");
                })
                .flatMap((Function<List<Sensors>, ObservableSource<AirData>>) response2 -> {
                    Log.i("testy","callback 2");
                    sensorsList.addAll(response2);
                    return gioSservice.getData(3070 +"");
                })
                .flatMap((Function<AirData, ObservableSource<AirQuality>>) response3 -> {
                    Log.i("testy","callback 3");
                    measurementsList.add(response3);
                    return gioSservice.getAirQuality(444+"");
                })
                .map(response4 -> {
                    Log.i("testy","callback 4");
                    qualityIndex = response4;
                    return response4;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<AirQuality>() {
                    @Override
                    public void onNext(@NonNull AirQuality o) {
                        Log.i("testy", "wszystko gra");
                        Log.i("testy", stationsList.toString());
                        Log.i("testy", sensorsList.toString());
                        Log.i("testy",measurementsList.toString());
                        Log.i("testy",qualityIndex.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.i("testy", "throwable = " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        setValues();

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
        for (Station s : stationsList) {
            station_lat = s.getGegrLat();
            station_lon = s.getGegrLon();
            distance = Math.sqrt(Math.pow(Math.abs(station_lat-lat),2)+Math.pow(Math.abs(station_lon-lon),2));
            if(distance < closestDistance || closestDistance == -1){
                closestDistance = distance;
                closestStation = s.getId();
            }
        }
        return closestStation;
    }

}