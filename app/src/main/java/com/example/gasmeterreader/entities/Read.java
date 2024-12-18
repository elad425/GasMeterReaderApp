package com.example.gasmeterreader.entities;

public class Read {
    private final int user_id;
    private final String city;
    private final String street;
    private final int house_number;
    private final String entry;
    private final int order;
    private final String user_name;
    private final String user_status;
    private final int apartment;
    private final int meter_id;
    private final double last_read;
    private double current_read;
    private final int center;
    private final String comment;
    private boolean isRead;

    public Read(int user_id, String city, String street, int house_number,
                String entry, int order, String user_name, String user_status, int apartment,
                int meter_id, double last_read, double current_read, int center, String comment) {
        this.user_id = user_id;
        this.city = city;
        this.street = street;
        this.house_number = house_number;
        this.entry = entry;
        this.order = order;
        this.user_name = user_name;
        this.user_status = user_status;
        this.apartment = apartment;
        this.meter_id = meter_id;
        this.last_read = last_read;
        this.current_read = current_read;
        this.center = center;
        this.comment = comment;
        this.isRead = false;
    }

    public int getCenter() {
        return center;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public int getHouse_number() {
        return house_number;
    }

    public int getOrder() {
        return order;
    }

    public String getEntry() {
        return entry;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_status() {
        return user_status;
    }

    public int getApartment() {
        return apartment;
    }

    public int getMeter_id() {
        return meter_id;
    }

    public double getLast_read() {
        return last_read;
    }

    public double getCurrent_read() {
        return current_read;
    }

    public void setCurrent_read(double current_read) {
        this.current_read = current_read;
    }

    public String getComment() {
        return comment;
    }

    public boolean isRead() {
        return isRead;
    }

    public void wasRead() {
        isRead = true;
    }

}
