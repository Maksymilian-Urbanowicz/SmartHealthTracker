package com.example.application_gps_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.application_gps_project.databinding.ActivityLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.List;

public class GoogleMaps_Activity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private LatLng fromLatLng = null;
    private LatLng toLatLng = null;
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
        mapFragment.getMapAsync(this);

        Button btnFindRoute = findViewById(R.id.btnFind_Route);
        btnFindRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout LLfindRoute = findViewById(R.id.LLfind_Route);
                LLfindRoute.setVisibility(View.VISIBLE);
            }
        });

        Button btnSetRoute = findViewById(R.id.btnSet_Route);
        btnSetRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Geocoder geocoder = new Geocoder(GoogleMaps_Activity.this);
                List<Address> addresses;

                try {
                    EditText TEdestFrom = findViewById(R.id.TEdest_From);
                    EditText TEdestTo = findViewById(R.id.TEdest_TO);
                    addresses = geocoder.getFromLocationName(String.valueOf(TEdestFrom.getText()), 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        fromLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    }

                    addresses = geocoder.getFromLocationName(String.valueOf(TEdestTo.getText()), 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        toLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    }

                    drawRouteOnMap();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        FloatingActionButton FABFollowLocation = findViewById(R.id.FAB_FolowLocalisation);
        FABFollowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set ON / OFF following current location
                isFollowLocation = !isFollowLocation;
                Toast.makeText(GoogleMaps_Activity.this, "Tracing localisation: " + ((isFollowLocation == true) ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();

                //set icon
                int nowyObrazSrc = (isFollowLocation == true) ? R.drawable.baseline_near_me : R.drawable.baseline_near_me_disabled;
                Drawable drawable = getResources().getDrawable(nowyObrazSrc);
                FABFollowLocation.setImageDrawable(drawable);
            }
        });
    }

    private void drawRouteOnMap() {
        if (mMap != null && fromLatLng != null && toLatLng != null) {
            String googleMapsApiKey = BuildConfig.API_KEY;
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey(googleMapsApiKey)
                    .build();

            try {
                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING) // Możesz zmienić tryb na inny, np. TravelMode.WALKING
                        .origin(new com.google.maps.model.LatLng(fromLatLng.latitude, fromLatLng.longitude))
                        .destination(new com.google.maps.model.LatLng(toLatLng.latitude, toLatLng.longitude))
                        .await();

                if (result.routes != null && result.routes.length > 0) {
                    // Uzyskaj szczegółowe punkty trasy
                    List<com.google.maps.model.LatLng> path = result.routes[0].overviewPolyline.decodePath();

                    // Utwórz obiekt do rysowania trasy
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(15) // Grubość trasy
                            .color(Color.RED); // Kolor trasy

                    // Dodaj punkty trasy do obiektu PolylineOptions
                    for (com.google.maps.model.LatLng point : path) {
                        polylineOptions.add(new LatLng(point.lat, point.lng));
                    }

                    // Dodaj trasę do mapy
                    mMap.clear();
                    mMap.addPolyline(polylineOptions);

                    // Przesuń kamerę na środek trasy
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(fromLatLng);
                    builder.include(toLatLng);
                    LatLngBounds bounds = builder.build();
                    int padding = 100; // Odstęp od krawędzi mapy
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.moveCamera(cu);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

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
                    float zoomLevel = 19.0f; // Możesz dostosować wartość zoomu
                    LatLng markerPosition = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, zoomLevel));

                    FloatingActionButton FABShareLocation = findViewById(R.id.FAB_ShareLocation);
                    FABShareLocation.setOnClickListener(new View.OnClickListener() {
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
        // Aktualizacja lokalizacji użytkownika
        if (isFollowLocation) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Ustaw odpowiedni zoom
            float zoomLevel = 19.0f; // Możesz dostosować wartość zoomu
            LatLng markerPosition = new LatLng(latitude, longitude);
            mMap.clear(); // Wyczyść wszystkie markery na mapie
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, zoomLevel));
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