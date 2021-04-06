package com.example.air_quality_app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.air_quality_app.airdata.AirData;
import com.example.air_quality_app.airqualityindex.AirQuality;
import com.example.air_quality_app.sensors.Sensors;
import com.example.air_quality_app.stations.Station;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AirQualityActivity extends AppCompatActivity {
    private static ArrayList<Station> stationsList = new ArrayList<>();
    private ArrayList<Sensors> sensorsList = new ArrayList<>();
    private static ArrayList<AirData> measurementsList = new ArrayList<>();
    private AirQuality qualityIndex = new AirQuality();
    private double userLatitude;
    private double userLongitude;
    private int closestStationID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        stationsList = (ArrayList<Station>) bundle.getSerializable("stations");
        Log.i("testy", String.valueOf(stationsList.toString()));
        userLatitude = intent.getDoubleExtra("lat",0);
        userLongitude = intent.getDoubleExtra("lon",0);

        closestStationID = findClosestStation(userLatitude,userLongitude);

        getDataFromAPI();
    }

    private void getDataFromAPI(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GIOSservice.SERVICE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        GIOSservice gioSservice = retrofit.create(GIOSservice.class);

        Disposable disposable = gioSservice.getSensors(String.valueOf(closestStationID))
                .subscribeOn(Schedulers.io())
                .flatMap((Function<List<Sensors>, ObservableSource<AirQuality>>) response2 -> {
                    sensorsList.addAll(response2);
                    return gioSservice.getAirQuality(String.valueOf(closestStationID));
                })
                .map(response4 -> {
                    qualityIndex = response4;
                    return response4;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<AirQuality>() {
                    @Override
                    public void onNext(@NonNull AirQuality o) {
                        getMeasurements(retrofit);
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


    @SuppressLint("CheckResult")
    private void getMeasurements(Retrofit retrofit){
        GIOSservice gioSservice = retrofit.create(GIOSservice.class);

        List<Observable<AirData>> requests = new ArrayList<>();
        for(int i = 0; i<sensorsList.size();i++){
            requests.add(gioSservice.getData(sensorsList.get(i).getId()+""));
        }

        Observable.zip(
                requests,
                (Function<Object[], List<AirData>>) objects -> {
                    List<AirData> airDataList = new ArrayList<>();
                    for(Object obj : objects){
                        airDataList.add((AirData) obj );
                    }
                    return airDataList;
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe(
                        (Consumer<List<AirData>>) airDataList -> {
                            runOnUiThread(() -> {
                                measurementsList.addAll(airDataList);
                                setValues();
                            });
                        },
                        (Consumer<Throwable>) e -> Log.e("testy", "Throwable: " + e)
                );
    }

    private void setValues(){
        TextView tv = findViewById(R.id.testing);
        tv.setText("lat = " + userLatitude + " " + "lon = " + userLongitude + " \n STACJA: " + closestStationID);
        TextView city = findViewById(R.id.text_city);
        for(Station o : stationsList){
            if(o.getId() == closestStationID){
                city.setText(o.getStationName());
                break;
            }
        }

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

             TextView air_quality = findViewById(R.id.text_quality);
             air_quality.setText(qualityIndex.getStIndexLevel().getIndexLevelName());
        }

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