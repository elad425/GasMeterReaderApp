package com.example.gasmeterreader.activities;

import static com.example.gasmeterreader.utils.EntityUtils.sortBuildings;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.gasmeterreader.R;
import com.example.gasmeterreader.adapters.BuildingAdapter;
import com.example.gasmeterreader.entities.Building;
import com.example.gasmeterreader.viewModels.MainViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private BuildingAdapter buildingListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Chip filterCheckbox;
    private MaterialTextView itemCounter;
    private TextView totalRemaining;
    private TextView totalCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialWindow();
        initializeUI();
        setupRecyclerView();
        setupObservers();
    }

    private void initialWindow(){
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Configuration config = getResources().getConfiguration();
        config.fontScale = 1.0f;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        setContentView(R.layout.activity_main);
    }

    private void initializeUI() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        filterCheckbox = findViewById(R.id.filterCheckbox);
        itemCounter = findViewById(R.id.itemCounter);
        totalRemaining = findViewById(R.id.totalRemaining);
        totalCompleted = findViewById(R.id.totalCompleted);

        swipeRefreshLayout.setOnRefreshListener(this::handleRefresh);

        filterCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateBuildingsList(Objects.requireNonNull(viewModel.getBuildings().getValue())));

        ImageButton uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> viewModel.updateAllReadings());
    }

    private void setupRecyclerView() {
        RecyclerView lstBuildings = findViewById(R.id.lstBuildings);
        buildingListAdapter = new BuildingAdapter(null, this);
        lstBuildings.setAdapter(buildingListAdapter);
        lstBuildings.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupObservers() {
        viewModel.getBuildings().observe(this, this::updateBuildingsList);
    }

    private void handleRefresh() {
        viewModel.reloadBuildings();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateBuildingsList(List<Building> buildings) {
        if (buildings == null) {
            buildingListAdapter.updateBuildings(new ArrayList<>());
            itemCounter.setText("0 מרכזיות");
            return;
        }

        List<Building> displayList = buildings;
        sortBuildings(displayList);
        if (filterCheckbox != null && filterCheckbox.isChecked()) {
            displayList = buildings.stream()
                    .filter(building -> !building.isComplete())
                    .collect(Collectors.toList());
        }

        int completed = 0;
        int total = 0;
        for(Building building : buildings){
            completed += building.getCompleted();
            total += building.getReadList().size();
        }

        itemCounter.setText(String.format(Locale.ENGLISH,"%d מרכזיות", displayList.size()));
        buildingListAdapter.updateBuildings(displayList);
        swipeRefreshLayout.setRefreshing(false);
        totalRemaining.setText(String.valueOf(total));
        totalCompleted.setText(String.valueOf(completed));
    }
}