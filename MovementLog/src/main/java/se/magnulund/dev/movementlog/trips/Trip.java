package se.magnulund.dev.movementlog.trips;// Created by Gustav on 16/01/2014.

import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.contracts.TripLogContract;

public class Trip {
    private static final String TAG = "Trip";

    long _id;
    int type;

    long startTime;
    int startConfirmedByID = -1;
    int startedByID = -1;
    int startedByType = -1;

    long endTime;
    int endConfirmedByID = -1;
    int endedByID = -1;
    int endedByType = -1;

    int confirmed;

    Location startLocation;
    Location endLocation;

    public Trip(long startTime, int tripType) {
        this.startTime = startTime;
        this.type = tripType;
        this.confirmed = 0;
    }

    public static Trip fromCursor(Cursor cursor) throws Exception {

        final int tripType = cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.TRIP_TYPE));

        final long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(TripLogContract.Columns.START_TIME));

        Trip trip = new Trip(startTime, tripType);

        trip.setID(cursor.getLong(cursor.getColumnIndexOrThrow(TripLogContract.Columns._ID)));

        trip.setStartedByID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.STARTED_BY_ID)));
        trip.setStartedByType(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.STARTED_BY_TYPE)));

        trip.setStartConfirmedByID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.START_CONFIRMED_BY_ID)));

        if (!cursor.isNull(cursor.getColumnIndexOrThrow(TripLogContract.Columns.START_LATITUDE)) &&
                !cursor.isNull(cursor.getColumnIndexOrThrow(TripLogContract.Columns.START_LONGITUDE))
                ) {
            Location startLocation = new Location("PLACEHOLDER");

            final double startLatitude = Location.convert(cursor.getString(cursor.getColumnIndex(TripLogContract.Columns.START_LATITUDE)));
            startLocation.setLatitude(startLatitude);

            final double startLongitude = Location.convert(cursor.getString(cursor.getColumnIndex(TripLogContract.Columns.START_LONGITUDE)));
            startLocation.setLongitude(startLongitude);

            trip.setStartLocation(startLocation);
        }

        trip.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(TripLogContract.Columns.END_TIME)));

        trip.setEndedByID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.ENDED_BY_ID)));
        trip.setEndedByType(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.ENDED_BY_TYPE)));

        trip.setEndConfirmedByID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.END_CONFIRMED_BY_ID)));

        if (!cursor.isNull(cursor.getColumnIndexOrThrow(TripLogContract.Columns.END_LATITUDE)) && !cursor.isNull(cursor.getColumnIndexOrThrow(TripLogContract.Columns.END_LONGITUDE))) {
            Location endLocation = new Location("PLACEHOLDER");

            final double endLatitude = Location.convert(cursor.getString(cursor.getColumnIndex(TripLogContract.Columns.END_LATITUDE)));
            endLocation.setLatitude(endLatitude);

            final double endLongitude = Location.convert(cursor.getString(cursor.getColumnIndex(TripLogContract.Columns.END_LONGITUDE)));
            endLocation.setLongitude(endLongitude);

            trip.setEndLocation(endLocation);
        }

        return trip;
    }

    public static String getTripTypeName(int tripType) {
        switch (tripType) {
            case DetectedActivity.IN_VEHICLE:
                return "in car";
            case DetectedActivity.ON_BICYCLE:
                return "on bicycle";
            default:
                return "unknown";
        }
    }

    public int getStartConfirmedByID() {
        return startConfirmedByID;
    }

    public void setStartConfirmedByID(int startConfirmedByID) {
        this.startConfirmedByID = startConfirmedByID;
    }

    public int getEndConfirmedByID() {
        return endConfirmedByID;
    }

    public void setEndConfirmedByID(int endConfirmedByID) {
        this.endConfirmedByID = endConfirmedByID;
    }

    public boolean isStartConfirmed() {
        return startConfirmedByID > -1;
    }

    public int getStartedByID() {
        return startedByID;
    }

    public void setStartedByID(int startedByID) {
        this.startedByID = startedByID;
    }

    public int getStartedByType() {
        return startedByType;
    }

    public void setStartedByType(int startedByType) {
        this.startedByType = startedByType;
    }

    public boolean isEndConfirmed() {
        return endConfirmedByID > -1;
    }

    public int getEndedByID() {
        return endedByID;
    }

    public void setEndedByID(int endedByID) {
        this.endedByID = endedByID;
    }

    public int getEndedByType() {
        return endedByType;
    }

    public void setEndedByType(int endedByType) {
        this.endedByType = endedByType;
    }

    public boolean isConfirmed() {
        return startConfirmedByID > -1 && endConfirmedByID > -1;
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

    /**
     * Map detected activity types to string resources
     *
     * @return the string resource id for the name of the type
     */
    public int getTripTypeNameResourceID() {
        return getResourceFromType(this.type);
    }

    /**
     * Map detected activity types to string resources
     *
     * @param tripType The detected activity type
     * @return the string resource id for the name of the type
     */
    private int getResourceFromType(int tripType) {
        switch (tripType) {
            case DetectedActivity.IN_VEHICLE:
                return R.string.trip_type_in_vehicle;
            case DetectedActivity.ON_BICYCLE:
                return R.string.trip_type_on_bike;
            default:
                return -1;
        }
    }
}
