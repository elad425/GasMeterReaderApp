package com.example.gasmeterreader.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.gasmeterreader.entities.Building;
import com.example.gasmeterreader.utils.Converters;

@Database(entities = {Building.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    static AppDatabase instance;
    public abstract BuildingDao buildingDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, "buildingDb")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}