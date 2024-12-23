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
        List<Read> sortedReads = new ArrayList<>(reads);
        sortedReads.sort(Comparator.comparingInt(Read::getOrder));
        return sortedReads;
    }

    public static void sortBuildings(List<Building> buildings) {
        buildings.sort((b1, b2) -> {
            int completeComparison = Boolean.compare(b1.isComplete(), b2.isComplete());
            if (completeComparison != 0) {
                return completeComparison;
            }
            int addressComparison = b1.getAddress().compareToIgnoreCase(b2.getAddress());
            if (addressComparison != 0) {
                return addressComparison;
            }
            return Integer.compare(b1.getBuildingNumber(), b2.getBuildingNumber());
        });
    }

}
