package com.example.gasmeterreader.viewModels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.gasmeterreader.room.BuildingRepository;
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
        buildingRepository.getAllBuildingsLive().observeForever(buildingsLiveData::setValue);
    }

    public void reloadBuildings() {
        buildingRepository.reloadBuilding();
    }

    public void updateAllReadings() {
        buildingRepository.updateAllReadings();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        buildingRepository.getAllBuildingsLive().removeObserver(buildings -> {});
    }
}