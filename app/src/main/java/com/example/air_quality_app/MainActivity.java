package com.example.air_quality_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.air_quality_app.airdata.AirData;
import com.example.air_quality_app.airqualityindex.AirQuality;
import com.example.air_quality_app.sensors.Sensors;
import com.example.air_quality_app.stations.Station;
import com.example.air_quality_app.weather.Weather;
import com.example.air_quality_app.weather.WeatherAPI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marcinmoskala.arcseekbar.ArcSeekBar;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements RvAdapter.ItemClickListener {
    private final int REQUEST_LOCATION_PERMISSION = 1;
    ArrayList<Station> stationsArrayList = new ArrayList<Station>();
    private static ArrayList<AirData> measurementsList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private double[] user_localization;
    private static ArrayList<String> SUGGESTIONS=new ArrayList<String>();
    private ArrayList<Sensors> sensorsList = new ArrayList<>();
    private AirQuality qualityIndex = new AirQuality();
    private Hashtable<String,String> polutantNameValue = new Hashtable<>();
    private String qualityName;
    int currentCityStationId;
    AutoCompleteTextView editTextCity;
    TextView cityName;
    TextView stationName;
    TextView polutantValue;
    TextView weatherTemp;
    TextView windSpeed;
    TextView airQualityLiteral;
    ProgressBar polutantBar;
    ArcSeekBar seekArc;
    View view;
    RvAdapter adapter;

    private double userLatitude;
    private double userLongitude;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCity = findViewById(R.id.editTextCity);
        cityName = findViewById(R.id.cityName);
        seekArc = findViewById(R.id.seekArc);
        stationName = findViewById(R.id.stationName);
        weatherTemp = findViewById(R.id.weatherTemp);
        windSpeed = findViewById(R.id.windSpeed);
        airQualityLiteral = findViewById(R.id.airQualityLiteral);
        polutantValue = findViewById(R.id.polutantValue);
        fillMainScreen(); //need to change name


        //arc seek bar gradient
        int[] intArray = getResources().getIntArray(R.array.progressGradientColors);
        seekArc.setProgressGradient(intArray);


    }
    private void polutantItems() {
        Log.v("attaching this","yep");
        int not_null_index = -1;
        int key =0;
        for (AirData o : measurementsList) {
            for (int i = 0; i < o.getValues().size(); i++) {
                if (o.getValues().get(i).getValue() != 0.0) {
                    not_null_index = i;
                    break;
                }

            }
            polutantNameValue.put(o.getKey(),o.getValues().get(not_null_index).getValue()+"");

            key++;
        }
        Log.v("pol__", String.valueOf(polutantNameValue));

        RecyclerView recyclerView = findViewById(R.id.polutantContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        adapter = new RvAdapter(this, polutantNameValue);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

    }


    private void fillMainScreen(){
        getStationsFromAPI();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,SUGGESTIONS);
        editTextCity.setAdapter(adapter);
        editTextCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                View v = getCurrentFocus();
                if(v != null){
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                String name = editTextCity.getText().toString();
                String[] namesSplit= editTextCity.getText().toString().split(",");
                cityName.setText(namesSplit[0]);
                stationName.setText(name);

                //
                for(Station s: stationsArrayList){
                    if(s.getStationName().contentEquals(editTextCity.getText())){
                        currentCityStationId = s.getId();
                        getWeatherFromAPI(s.getGegrLat(),s.getGegrLon()); //call weather and set the card
                        //Log.v("Lat:",String.valueOf(s.getGegrLat()));
                        //Log.v("Lon:",String.valueOf(s.getGegrLon()));
                        //Log.v("city_found:", String.valueOf(currentCityStationId));
                    }
                }

                getDataFromAPI();


            }
        });

    }
    private void getStationsFromAPI(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GIOSservice.SERVICE_ENDPOINT)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GIOSservice gioSservice = retrofit.create(GIOSservice.class);

        Disposable disposable = gioSservice.getPost()
                .subscribeOn(Schedulers.io())
                .map( response -> {
                    stationsArrayList.addAll(response);
                    populateSuggestions(); //do it when array is already filled
                    //Log.v("stations: ",stationsArrayList.toString());
                    return stationsArrayList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<Station>>(){
                    @Override
                    public void onNext(@NonNull ArrayList<Station> stations) { }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        //Log.i("testy", "throwable = " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(MainActivity.this,"ready", Toast.LENGTH_LONG).show();
                    }
                });


    }
    private void populateSuggestions(){
        for(Station s : stationsArrayList){
            SUGGESTIONS.add(s.getStationName());
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.v("clicked","yep");
    }

    @SuppressLint("MissingPermission")
    public void intentActivityFromGps(View view) {

        //Intent intent = new Intent(this, AirQualityActivity.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if(requestLocationPermission()){
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            //Log.i("testy",stationsArrayList.toString());
                            userLatitude = location.getLatitude();
                            userLongitude = location.getLongitude();
                            Log.v("latlon:",userLatitude+":"+userLongitude);
                            currentCityStationId = findClosestStation(location.getLatitude(), location.getLongitude());

                            for(Station s: stationsArrayList){
                                if(s.getId() == currentCityStationId){
                                    Log.v("1__foundthis: ",s.getCity().getName());
                                    cityName.setText(s.getCity().getName());
                                    stationName.setText(s.getStationName());
                                    editTextCity.setText("");
                                }else{
                                    Log.v("1__didnt find nothing: ","");
                                }
                            }
                            getDataFromAPI();
                            getWeatherFromAPI(userLatitude,userLongitude); //call weather and set the card

                            InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            View v = getCurrentFocus();
                            if(v != null){
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                inputManager.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                            }




                            //Bundle bundle = new Bundle();
                           // bundle.putSerializable("stations",stationsArrayList);
                           // intent.putExtras(bundle);
                           // startActivity(intent);
                            if (location != null) {
                                //currentCityStationId = findClosestStation(location.getLatitude(), location.getLongitude());
                                //Toast.makeText(this, "Whoops, something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
    }




    public void intentActivityFromText(String query) {
        Intent intent = new Intent(this, AirQualityActivity.class);
        intent.putExtra("key", query);
        Bundle bundle = new Bundle();
        bundle.putSerializable("stations",stationsArrayList);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    int num = 0;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public Boolean requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
            return false;
        }
    }


    private void getWeatherFromAPI(Double lat, Double lon){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherAPI weatherAPI = retrofit.create(WeatherAPI.class);
        Call<Weather> call = weatherAPI.getPost(lat,lon,"metric","749561a315b14523a8f5f1ef95e45864");

        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                weatherTemp.setText(response.body().getMain().get("temp").toString()+"°C");
                windSpeed.setText(response.body().getWind().get("speed").toString()+" Km/h");
                //Log.i("testy",response.body().getWind().get("speed").toString()+" Km/h");
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Log.i("testy",t.getMessage());
            }
        });
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

        Disposable disposable = gioSservice.getSensors(String.valueOf(currentCityStationId))
                .subscribeOn(Schedulers.io())
                .flatMap((Function<List<Sensors>, ObservableSource<AirQuality>>) response2 -> {
                    sensorsList.addAll(response2);
                    return gioSservice.getAirQuality(String.valueOf(currentCityStationId));
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
                    public void onComplete(){
                        Log.v("completed","getdatafromapi");
                    }
                });
    }


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
                                changeTexts();
                                polutantItems();
                            });
                        },
                        (Consumer<Throwable>) e -> Log.e("testy", "Throwable: " + e)
                );
    }

    private void changeTexts(){
        qualityName = qualityIndex.getStIndexLevel().getIndexLevelName();
        airQualityLiteral.setText(" "+qualityName);
        changeSeekBarValues(qualityName);
        //Log.v("change","succ");
    }

    private void changeSeekBarValues(String qualityName){
        switch(qualityName.toLowerCase()){
            case "bardzo dobry":
                seekArc.setProgress(100);
                polutantValue.setText(String.valueOf(seekArc.getProgress())+" %");
                break;
            case "dobry":
                seekArc.setProgress(80);
                polutantValue.setText(String.valueOf(seekArc.getProgress())+" %");
                break;
            case "umiarkowany":
                seekArc.setProgress(60);
                polutantValue.setText(String.valueOf(seekArc.getProgress())+" %");
                break;
            case "dostateczny":
                seekArc.setProgress(40);
                polutantValue.setText(String.valueOf(seekArc.getProgress())+" %");
                break;
            case "zły":
                seekArc.setProgress(20);
                polutantValue.setText(String.valueOf(seekArc.getProgress())+" %");
                break;
            case "bardzo zły":
                seekArc.setProgress(10);
                polutantValue.setText(String.valueOf(seekArc.getProgress())+" %");

            case "brak indeksu":
                seekArc.setProgress(0);
                polutantValue.setText(String.valueOf(seekArc.getProgress())+" %");
                break;

        }
    }

    private int findClosestStation(double lat, double lon){
        double station_lat = 0;
        double station_lon = 0;
        int closestStation = -1;
        double closestDistance = -1;
        double distance;
        for (Station s : stationsArrayList) {
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