package se.magnulund.dev.movementlog.trips;// Created by Gustav on 16/01/2014.

import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.contracts.TripLogContract;

public class Trip {
    private static final String TAG = "Trip";
    long startTime;
    int type;
    long endTime;
    long _id;
    Location startLocation;
    Location endLocation;
    boolean confirmed;

    public Trip(long startTime, int tripType, long endTime, Location startLocation, Location endLocation, boolean confirmed) {
        this.startTime = startTime;
        this.type = tripType;
        this.endTime = endTime;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.confirmed = confirmed;
    }

    public Trip(long startTime, int tripType) {
        this.startTime = startTime;
        this.type = tripType;
        this.confirmed = false;
    }

    public static Trip fromCursor(Cursor cursor) throws Exception {
        if (!TripLogContract.isValidCursor(cursor)) {
            throw new Exception("Invalid cursor - does not point to a full Trip entry.");
        }

        Trip trip;

        final long _id = cursor.getLong(cursor.getColumnIndex(TripLogContract.Columns._ID));

        final int tripType = cursor.getInt(cursor.getColumnIndex(TripLogContract.Columns.TRIP_TYPE));

        final long startTime = cursor.getLong(cursor.getColumnIndex(TripLogContract.Columns.START_TIME));

        trip = new Trip(startTime, tripType);

        trip.setID(_id);

        final long endTime = cursor.getLong(cursor.getColumnIndex(TripLogContract.Columns.END_TIME));

        trip.setEndTime(endTime);

        if (!cursor.isNull(cursor.getColumnIndex(TripLogContract.Columns.START_LATITUDE)) &&
                !cursor.isNull(cursor.getColumnIndex(TripLogContract.Columns.START_LONGITUDE))
                ) {
            Location startLocation = new Location("PLACEHOLDER");

            double startLatitude = Location.convert(
                    cursor.getString(cursor.getColumnIndex(TripLogContract.Columns.START_LATITUDE))
            );
            startLocation.setLatitude(startLatitude);

            double startLongitude = Location.convert(
                    cursor.getString(cursor.getColumnIndex(TripLogContract.Columns.START_LONGITUDE))
            );
            startLocation.setLongitude(startLongitude);

            trip.setStartLocation(startLocation);
        }

        if (!cursor.isNull(cursor.getColumnIndex(TripLogContract.Columns.END_LATITUDE)) &&
                !cursor.isNull(cursor.getColumnIndex(TripLogContract.Columns.END_LONGITUDE))
                ) {
            Location endLocation = new Location("PLACEHOLDER");

            double endLatitude = Location.convert(
                    cursor.getString(cursor.getColumnIndex(TripLogContract.Columns.END_LATITUDE))
            );
            endLocation.setLatitude(endLatitude);

            double endLongitude = Location.convert(
                    cursor.getString(cursor.getColumnIndex(TripLogContract.Columns.END_LONGITUDE))
            );
            endLocation.setLongitude(endLongitude);

            trip.setEndLocation(endLocation);
        }

        trip.setConfirmed(cursor.getInt(cursor.getColumnIndex(TripLogContract.Columns.CONFIRMED)) == 1);

        return trip;
    }

    public boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
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
    public int getTripTypeNameID() {
        return getNameIDFromType(this.getType());
    }

    /**
     * Map detected activity types to string resources
     *
     * @param activityType The detected activity type
     * @return the string resource id for the name of the type
     */
    private int getNameIDFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return R.string.trip_type_in_vehicle;
            case DetectedActivity.ON_BICYCLE:
                return R.string.trip_type_on_bike;
        }
        return -1;
    }
}
