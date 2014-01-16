package se.magnulund.dev.movementlog;// Created by Gustav on 16/01/2014.

import android.location.Location;

public class Trip {
    private static final String TAG = "Trip";
    int startTime;
    int type;
    int endTime;
    Location startLocation;
    Location endLocation;

    public Trip(int startTime, int tripType, int endTime, Location startLocation, Location endLocation) {
        this.startTime = startTime;
        this.type = tripType;
        this.endTime = endTime;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public Trip(int startTime, int tripType) {
        this.startTime = startTime;
        this.type = tripType;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getType() {
        return type;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }
}
