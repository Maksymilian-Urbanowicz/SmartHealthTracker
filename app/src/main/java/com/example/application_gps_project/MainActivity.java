package com.example.application_gps_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.application_gps_project.databinding.ActivityLocationBinding;

public class MainActivity extends AppCompatActivity {

//    private ActivityLocationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(MainActivity.this, location_activity.class));

        Button btn = findViewById(R.id.btnToNext);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
//        binding = ActivityLocationBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

    }
}