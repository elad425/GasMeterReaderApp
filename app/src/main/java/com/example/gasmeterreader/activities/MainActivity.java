package com.example.gasmeterreader.activities;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.adapters.BuildingAdapter;
import com.example.gasmeterreader.viewModels.MainViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private BuildingAdapter buildingListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExtendedFloatingActionButton uploadFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration config = getResources().getConfiguration();
        config.fontScale = 1.0f;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Setup RecyclerView
        RecyclerView lstBuildings = findViewById(R.id.lstBuildings);
        buildingListAdapter = new BuildingAdapter(null, this);
        lstBuildings.setAdapter(buildingListAdapter);
        lstBuildings.setLayoutManager(new LinearLayoutManager(this));

        // Setup SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.reloadBuildings();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Setup FAB
        uploadFab = findViewById(R.id.uploadFab);
        uploadFab.setOnClickListener(v -> {
            // Add animation
            uploadFab.extend();
            viewModel.updateAllReadings();
            // Shrink after delay
            uploadFab.postDelayed(uploadFab::shrink, 2000);
        });

        // Observe buildings data
        viewModel.getBuildings().observe(this, buildings -> {
            buildingListAdapter.updateBuildings(buildings);
            swipeRefreshLayout.setRefreshing(false);
        });

        // Hide/Show FAB on scroll
        lstBuildings.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && uploadFab.isExtended())
                    uploadFab.shrink();
                else if (dy < 0 && !uploadFab.isExtended())
                    uploadFab.extend();
            }
        });
    }

}