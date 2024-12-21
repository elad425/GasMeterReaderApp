package com.example.gasmeterreader.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gasmeterreader.entities.Building;

import java.util.List;
@Dao
public interface BuildingDao {
    @Update
    void update(Building building);

    @Query("SELECT * FROM building WHERE center = :center")
    Building getBuildingByCenter(int center);

    @Query("SELECT * FROM building")
    List<Building> getAllBuildings();

    @Query("SELECT * FROM building")
    LiveData<List<Building>> getAllBuildingsLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<Building> building);

    @Query("DELETE FROM building")
    void clear();
}
