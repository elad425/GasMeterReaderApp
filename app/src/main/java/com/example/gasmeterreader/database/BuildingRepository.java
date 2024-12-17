package com.example.gasmeterreader.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.gasmeterreader.api.Api;
import com.example.gasmeterreader.entities.Building;
import com.example.gasmeterreader.entities.Read;

import java.util.ArrayList;
import java.util.List;

public class BuildingRepository {
    private final AppDatabase db;
    private final Api api;

    public BuildingRepository(Application application) {
        db = AppDatabase.getInstance(application);
        api = new Api(db.buildingDao(),application.getApplicationContext());
    }

    public void updateBuilding(Building updatedBuilding) {
        db.buildingDao().update(updatedBuilding);
    }

    public void reloadBuilding(){
        db.buildingDao().clear();
        api.getReads();
    }

    public LiveData<List<Building>> getAllBuildingsLive() {
        return db.buildingDao().getAllBuildingsLive();
    }

    public Building getBuildingByCenter(int center) {
        return db.buildingDao().getBuildingByCenter(center);
    }

    public void updateRead(Read read){
        api.updateRead(read.getUser_id(),read);
    }

    public void updateAllReadings(){
        for (Building building : db.buildingDao().getAllBuildings()) {
            for (Read read : building.getReadList()) {
                api.updateRead(read.getUser_id(), read);
            }
        }
    }

}