package com.example.gasmeterreader.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gasmeterreader.database.BuildingRepository;
import com.example.gasmeterreader.entities.Building;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final BuildingRepository buildingRepository;
    private LiveData<List<Building>> buildingsLiveData;

    public MainViewModel(@NonNull Application application) {
        super(application);
        buildingRepository = new BuildingRepository(application);
        buildingsLiveData = new MutableLiveData<>();
        loadBuildings();
    }

    public LiveData<List<Building>> getBuildings() {
        return buildingsLiveData;
    }

    private void loadBuildings() {
        buildingsLiveData = buildingRepository.getAllBuildingsLive();
    }

    public void reloadBuildings() {
        buildingRepository.reloadBuilding();
        loadBuildings();
    }

    public void updateAllReadings() {
        buildingRepository.updateAllReadings();
        loadBuildings();
    }
}