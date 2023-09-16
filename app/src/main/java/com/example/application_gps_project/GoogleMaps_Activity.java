package com.example.application_gps_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.application_gps_project.databinding.ActivityLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GoogleMaps_Activity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;

    private ActivityLocationBinding binding;

    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean isFollowLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        binding = ActivityLocationBinding.inflate(getLayoutInflater());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(GoogleMaps_Activity.this);

        FloatingActionButton FAB_FolowLocalisation = findViewById(R.id.FAB_FolowLocalisation);
        FAB_FolowLocalisation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set ON / OFF following current location
                isFollowLocation=!isFollowLocation;
                Toast.makeText(GoogleMaps_Activity.this, "Tracing localisation: " + ((isFollowLocation == true)? "ON": "OFF"), Toast.LENGTH_SHORT).show();

                //set icon
                int nowyObrazSrc = (isFollowLocation==true)?R.drawable.baseline_near_me :R.drawable.baseline_near_me_disabled;
                Drawable drawable = getResources().getDrawable(nowyObrazSrc);
                FAB_FolowLocalisation.setImageDrawable(drawable);
            }
        });


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        // Sprawdź uprawnienia do dostępu do lokalizacji
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mMap.setMyLocationEnabled(true);
//
//            // Dodaj LocationListener do śledzenia zmian lokalizacji użytkownika
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
//        } else {
//            // Jeśli brak uprawnień, poproś użytkownika o nie
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
//        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Sprawdź uprawnienia do dostępu do lokalizacji
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Ustaw odpowiedni zoom
                    float zoomLevel = 21.0f; // Możesz dostosować wartość zoomu
                    LatLng markerPosition = new LatLng(latitude, longitude);
                    //mMap.addMarker(new MarkerOptions().position(markerPosition).title("Current location marker"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, zoomLevel));

                    FloatingActionButton FAB_ShareLocation = findViewById(R.id.FAB_ShareLocation);
                    FAB_ShareLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Tworzymy link URL na podstawie współrzędnych

                            String locationUrl = "https://maps.google.com/?q=" + String.valueOf(latitude) + "," + String.valueOf(longitude);

                            // Tworzymy Intencję do udostępnienia linku URL
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, locationUrl);

                            // Uruchamiamy menu systemowe do udostępniania
                            startActivity(Intent.createChooser(intent, "Udostępnij lokalizację"));
                        }
                    });

                }
            }).addOnFailureListener(this, e -> {
                // Obsłuż błąd, jeśli nie udało się pobrać lokalizacji.
                Toast.makeText(this, "Nie udało się pobrać lokalizacji.", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Jeśli brak uprawnień, poproś użytkownika o nie
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //Toast.makeText(this, "" + isFollowLocation, Toast.LENGTH_LONG).show();
        // Aktualizacja lokalizacji użytkownika
        if (isFollowLocation == true) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Ustaw odpowiedni zoom
            float zoomLevel = 19.0f; // Możesz dostosować wartość zoomu
            LatLng markerPosition = new LatLng(latitude, longitude);
            mMap.clear(); // Wyczyść wszystkie markery na mapie
            //mMap.addMarker(new MarkerOptions().position(markerPosition).title("Current location marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, zoomLevel));
            //Toast.makeText(GoogleMaps_Activity.this, "SiemA DZIAŁAM W GOOGLEMAPS_Activity", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Uprawnienia zostały przyznane, możesz teraz uaktualnić mapę
                recreate(); // Restartuje aktywność, aby załadować mapę z uprawnieniami
            } else {
                // Uprawnienia nie zostały przyznane, obsłuż to tutaj (np. informacja dla użytkownika).
                Toast.makeText(this, "Brak uprawnień do dostępu do lokalizacji.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Obsłuż wyłączenie dostawcy lokalizacji, jeśli to konieczne
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Obsłuż włączenie dostawcy lokalizacji, jeśli to konieczne
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Obsłuż zmiany statusu dostawcy lokalizacji, jeśli to konieczne
    }
}