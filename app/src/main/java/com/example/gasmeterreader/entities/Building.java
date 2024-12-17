package com.example.gasmeterreader.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "building")
public class Building {
    private final String address;
    private final String city;
    private final int buildingNumber;
    @PrimaryKey
    private final int center;
    private List<Read> readList;
    private boolean isComplete;

    public Building(String address, String city, int buildingNumber, int center) {
        this.address = address;
        this.city = city;
        this.buildingNumber = buildingNumber;
        this.center = center;
        this.readList = new ArrayList<>();
        this.isComplete = false;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public int getBuildingNumber() {
        return buildingNumber;
    }

    public int getCenter() {
        return center;
    }

    public List<Read> getReadList() {
        return readList;
    }

    public void setReadList(List<Read> readList) {
        this.readList = readList;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public void addRead(Read read){
        this.readList.add(read);
    }

    public int getLeftToDo(){
        int count = 0;
        for (Read read: readList){
            if(read.getCurrent_read() != 0){
                count += 1;
            }
        }
        return count;
    }

}
