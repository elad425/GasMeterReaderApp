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
    private final MutableLiveData<List<Building>> buildingsLiveData;

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
        // Observe the repository's LiveData and update our MutableLiveData
        buildingRepository.getAllBuildingsLive().observeForever(buildings -> {
            buildingsLiveData.setValue(buildings);
        });
    }

    public void reloadBuildings() {
        buildingRepository.reloadBuilding();
        // No need to call loadBuildings() as we're already observing changes
    }

    public void updateAllReadings() {
        buildingRepository.updateAllReadings();
        // No need to call loadBuildings() as we're already observing changes
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up the observer if needed
        buildingRepository.getAllBuildingsLive().removeObserver(buildings -> {});
    }
}