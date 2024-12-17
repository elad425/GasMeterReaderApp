package com.example.gasmeterreader.utils;

import com.example.gasmeterreader.entities.Building;
import com.example.gasmeterreader.entities.Read;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityUtils {
    public static List<Building> assignReadsToBuildings(List<Read> reads) {
        List<Building> buildings = new ArrayList<>();
        boolean found;
        for (Read read : reads) {
            found = false;
            for (Building building : buildings) {
                if (read.getCenter() == building.getCenter()) {
                    found = true;
                    building.addRead(read);
                    break;
                }
            }
            if (!found){
                Building building = new Building(read.getStreet(),read.getCity(),read.getHouse_number(),read.getCenter());
                building.addRead(read);
                buildings.add(building);
            }
        }
        return buildings;
    }

    public static List<Read> sortReadsByOrder(List<Read> reads) {
        // Create a new list to avoid modifying the original
        List<Read> sortedReads = new ArrayList<>(reads);
        // Sort the list using a comparator
        sortedReads.sort(Comparator.comparingInt(Read::getOrder));
        return sortedReads;
    }

}
