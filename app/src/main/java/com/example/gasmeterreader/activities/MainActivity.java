package com.example.gasmeterreader.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.gasmeterreader.R;
import com.example.gasmeterreader.adapters.BuildingAdapter;
import com.example.gasmeterreader.entities.Building;
import com.example.gasmeterreader.viewModels.MainViewModel;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private BuildingAdapter buildingListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExtendedFloatingActionButton uploadFab;
    private MaterialCheckBox filterCheckbox;  // Add this
    private MaterialTextView itemCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        setupRecyclerView();
        setupObservers();
    }

    private void initializeUI() {
        // Set font scale and status bar
        Configuration config = getResources().getConfiguration();
        config.fontScale = 1.0f;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Setup Toolbar
        setSupportActionBar(findViewById(R.id.topAppBar));

        // Setup SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        filterCheckbox = findViewById(R.id.filterCheckbox);
        itemCounter = findViewById(R.id.itemCounter);

        swipeRefreshLayout.setOnRefreshListener(this::handleRefresh);

        filterCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBuildingsList(Objects.requireNonNull(viewModel.getBuildings().getValue()));
        });

        // Setup FAB
        setupFloatingActionButton();
    }

    private void setupRecyclerView() {
        RecyclerView lstBuildings = findViewById(R.id.lstBuildings);
        buildingListAdapter = new BuildingAdapter(null, this);

        lstBuildings.setAdapter(buildingListAdapter);
        lstBuildings.setLayoutManager(new LinearLayoutManager(this));
        lstBuildings.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                handleScroll(dy);
            }
        });
    }

    private void setupFloatingActionButton() {
        uploadFab = findViewById(R.id.uploadFab);
        uploadFab.setOnClickListener(v -> handleFabClick());
    }

    private void setupObservers() {
        viewModel.getBuildings().observe(this, this::updateBuildingsList);
    }

    private void handleRefresh() {
        viewModel.reloadBuildings();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void handleFabClick() {
        uploadFab.extend();
        viewModel.updateAllReadings();
        uploadFab.postDelayed(uploadFab::shrink, 2000);
    }

    private void handleScroll(int dy) {
        if (dy > 0 && uploadFab.isExtended()) {
            uploadFab.shrink();
        } else if (dy < 0 && !uploadFab.isExtended()) {
            uploadFab.extend();
        }
    }

    private void updateBuildingsList(List<Building> buildings) {
        if (buildings == null) return;

        List<Building> displayList;
        if (filterCheckbox.isChecked()) {
            // Filter incomplete buildings
            List<Building> filteredList = new ArrayList<>();
            for (Building building : buildings) {
                if (!building.isComplete()) {
                    filteredList.add(building);
                }
            }
            displayList = filteredList;
        } else {
            // Show all buildings
            displayList = buildings;
        }

        // Update the counter
        itemCounter.setText(displayList.size() + " מרכזיות");

        // Update the adapter
        buildingListAdapter.updateBuildings(displayList);
        swipeRefreshLayout.setRefreshing(false);
    }
}