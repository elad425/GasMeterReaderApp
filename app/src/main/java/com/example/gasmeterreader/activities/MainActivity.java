package com.example.gasmeterreader.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.adapters.BuildingAdapter;
import com.example.gasmeterreader.viewModels.MainViewModel;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private BuildingAdapter buildingListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration config = getResources().getConfiguration();
        config.fontScale = 1.0f;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        RecyclerView lstBuildings = findViewById(R.id.lstBuildings);
        buildingListAdapter = new BuildingAdapter(null, this);
        lstBuildings.setAdapter(buildingListAdapter);
        lstBuildings.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getBuildings().observe(this, buildings ->
                buildingListAdapter.updateBuildings(buildings));

        ImageButton refreshButton = findViewById(R.id.refresh);
        refreshButton.setOnClickListener(v -> viewModel.reloadBuildings());

        ImageButton uploadButton = findViewById(R.id.upload);
        uploadButton.setOnClickListener(v -> viewModel.updateAllReadings());
    }
}