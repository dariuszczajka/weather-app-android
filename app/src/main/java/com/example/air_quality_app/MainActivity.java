package com.example.air_quality_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.marcinmoskala.arcseekbar.ArcSeekBar;
import com.xw.repo.BubbleSeekBar;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private double[] user_localization;
    SearchView editTextCity;
    TextView cityInput;
    TextView polutantValue;
    SeekBar seekBar;
    ProgressBar polutantBar;
    ArcSeekBar arcSeekBar;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextCity = findViewById(R.id.editTextCity);
        cityInput = findViewById(R.id.citySearch);
        arcSeekBar = findViewById(R.id.seekArc);
        editTextCity.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length() != 0){
                    Log.v("city:query:  ",query);
                    cityInput.setText(query);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //arc seek bar gradient
        int[] intArray = getResources().getIntArray(R.array.progressGradientColors);
        arcSeekBar.setProgressGradient(intArray);

    }

    @SuppressLint("MissingPermission")
    public void intentActivityFromGps(View view) {
        Intent intent = new Intent(this, AirQualityActivity.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if(requestLocationPermission()){
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            intent.putExtra("lat", location.getLatitude());
                            intent.putExtra("lon", location.getLongitude());
                            startActivity(intent);
                            if (location != null) {
                                //Toast.makeText(this, "Whoops, something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
    }

    public void updateCircleBar(){

        polutantBar.setProgress(10);
        polutantValue.setText(String.valueOf(polutantBar.getProgress()));
    }

    public void intentActivityFromText(String query) {
        Intent intent = new Intent(this, AirQualityActivity.class);
        intent.putExtra("key", query);
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
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
            return false;
        }
    }

}