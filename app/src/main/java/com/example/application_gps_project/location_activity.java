package com.example.application_gps_project;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.application_gps_project.databinding.ActivityLocationBinding;

import java.util.List;
import java.util.Locale;

public class location_activity extends AppCompatActivity implements  LocationListener, SensorEventListener { //
    SharedPreferences sharedPreferences;
    private ActivityLocationBinding binding;
    LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor mStepCounter;
    private boolean isCountrtSensorPresent;
    private int stepCounter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivityLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        getStepler();
        getLocation();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(location_activity.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            },1);
        }

        binding.btnMaps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(location_activity.this, GoogleMaps_Activity.class));
                }
            });
    }

    public void getStepler(){
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null){
            mStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isCountrtSensorPresent = true;
        }else{
            binding.TVsteper.setText("Counter Sensor is not Present");
            isCountrtSensorPresent=false;
        }
    }

    public void getLocation () {
        try {
            //Toast.makeText(location_activity.this, "KIWI", Toast.LENGTH_LONG).show();

            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        //Toast.makeText(this, ""+location.getLatitude()+", "+location.getLongitude(), Toast.LENGTH_LONG).show();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            binding.TVlocation.setText(address);
            Toast.makeText(this, ""+address, Toast.LENGTH_LONG).show();

            // Store a string
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lat", String.valueOf(location.getLatitude()));
            editor.putString("lng", String.valueOf(location.getLongitude()));
            editor.putString("address", String.valueOf(address));
            editor.apply();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == mStepCounter){
            stepCounter = (int) sensorEvent.values[0];
            binding.TVsteper.setText(String.valueOf(stepCounter));
            double stepLengthMeters = 0.0007; // Długość kroku w metrach
            double distance = stepCounter * stepLengthMeters;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");

            binding.TVdistance.setText(String.valueOf(decimalFormat.format(distance)));

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null){
            sensorManager.registerListener(this, mStepCounter, sensorManager.SENSOR_DELAY_NORMAL);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null){
            sensorManager.unregisterListener(this, mStepCounter);

        }

    }
}