package com.example.gasmeterreader.utils;

import androidx.room.TypeConverter;

import com.example.gasmeterreader.entities.Read;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromReadList(List<Read> readList) {
        if (readList == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Read>>() {}.getType();
        return gson.toJson(readList, type);
    }

    @TypeConverter
    public static List<Read> toReadList(String readListString) {
        if (readListString == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Read>>() {}.getType();
        return gson.fromJson(readListString, type);
    }
}