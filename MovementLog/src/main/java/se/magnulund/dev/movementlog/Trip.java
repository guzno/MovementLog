package se.magnulund.dev.movementlog;// Created by Gustav on 16/01/2014.

import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.provider.MovementDataContract;

public class Trip {
    private static final String TAG = "Trip";
    long startTime;
    int type;
    long endTime;
    long _id;
    Location startLocation;
    Location endLocation;

    public Trip(long startTime, int tripType, long endTime, Location startLocation, Location endLocation) {
        this.startTime = startTime;
        this.type = tripType;
        this.endTime = endTime;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public Trip(long startTime, int tripType) {
        this.startTime = startTime;
        this.type = tripType;
    }

    public static Trip fromCursor(Cursor cursor) throws Exception {
        if (!MovementDataContract.Trips.isValidCursor(cursor) ) {
            throw new Exception("Invalid cursor - does not point to a full Trip entry.");
        }

        Trip trip;

        final long _id = cursor.getLong(cursor.getColumnIndex(MovementDataContract.Trips._ID));

        final int tripType = cursor.getInt(cursor.getColumnIndex(MovementDataContract.Trips.TRIP_TYPE));

        final long startTime = cursor.getLong(cursor.getColumnIndex(MovementDataContract.Trips.START_TIME));

        trip = new Trip(startTime, tripType);

        trip.setID(_id);

        final long endTime = cursor.getLong(cursor.getColumnIndex(MovementDataContract.Trips.END_TIME));

        trip.setEndTime(endTime);

        if (!cursor.isNull(cursor.getColumnIndex(MovementDataContract.Trips.START_LATITUDE)) &&
                !cursor.isNull(cursor.getColumnIndex(MovementDataContract.Trips.START_LONGITUDE))
                ) {
            Location startLocation = new Location("PLACEHOLDER");

            double startLatitude = Location.convert(
                    cursor.getString(cursor.getColumnIndex(MovementDataContract.Trips.START_LATITUDE))
            );
            startLocation.setLatitude(startLatitude);

            double startLongitude = Location.convert(
                    cursor.getString(cursor.getColumnIndex(MovementDataContract.Trips.START_LONGITUDE))
            );
            startLocation.setLongitude(startLongitude);

            trip.setStartLocation(startLocation);
        }

        if (!cursor.isNull(cursor.getColumnIndex(MovementDataContract.Trips.END_LATITUDE)) &&
                !cursor.isNull(cursor.getColumnIndex(MovementDataContract.Trips.END_LONGITUDE))
                ) {
            Location endLocation = new Location("PLACEHOLDER");

            double endLatitude = Location.convert(
                    cursor.getString(cursor.getColumnIndex(MovementDataContract.Trips.END_LATITUDE))
            );
            endLocation.setLatitude(endLatitude);

            double endLongitude = Location.convert(
                    cursor.getString(cursor.getColumnIndex(MovementDataContract.Trips.END_LONGITUDE))
            );
            endLocation.setLongitude(endLongitude);

            trip.setEndLocation(endLocation);
        }

        return trip;
    }

    public long getID() {
        return _id;
    }

    public void setID(long id) {
        this._id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getType() {
        return type;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
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

    public String getTripTypeName(){
        return getNameFromType(this.getType());
    }

    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
        }
        return "unknown";
    }
}
