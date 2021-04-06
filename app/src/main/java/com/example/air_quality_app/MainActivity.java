package com.example.air_quality_app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.Manifest;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.air_quality_app.sensors.Sensors;
import com.example.air_quality_app.stations.Station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_LOCATION_PERMISSION = 1;
    ArrayList<Station> stationsArrayList = new ArrayList<Station>();
    private FusedLocationProviderClient fusedLocationClient;
    private double[] user_localization;
    EditText editTextCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextCity = findViewById(R.id.editTextCity);
        getStationsFromAPI();
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
                    return stationsArrayList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<Station>>(){
                    @Override
                    public void onNext(@NonNull ArrayList<Station> stations) { }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.i("testy", "throwable = " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(MainActivity.this,"ready", Toast.LENGTH_LONG).show();
                    }
                });
    }




    @SuppressLint("MissingPermission")
    public void intentActivityFromGps(View view) {
        Intent intent = new Intent(this, AirQualityActivity.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if(requestLocationPermission()){
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            Log.i("testy",stationsArrayList.toString());
                            intent.putExtra("lat", location.getLatitude());
                            intent.putExtra("lon", location.getLongitude());
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("stations",stationsArrayList);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            if (location != null) {
                                //Toast.makeText(this, "Whoops, something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
    }

    public void intentActivityFromText(View view) {
        String city = editTextCity.getText().toString();
        Intent intent = new Intent(this, AirQualityActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("stations",stationsArrayList);
        intent.putExtras(bundle);
        startActivity(intent);
    }

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
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
            return false;
        }
    }



}