package com.example.air_quality_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    EditText editTextCity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextCity = findViewById(R.id.editTextCity);
    }

    public void intentActivityFromGps(View view) {
        Intent intent = new Intent(this, AirQualityActivity.class);
        intent.putExtra("key", "");
        startActivity(intent);
    }

    public void intentActivityFromText(View view) {
        String city = editTextCity.getText().toString();
        Intent intent = new Intent(this, AirQualityActivity.class);
        intent.putExtra("key", city);
        startActivity(intent);
    }
}