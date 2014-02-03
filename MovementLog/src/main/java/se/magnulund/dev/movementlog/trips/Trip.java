package se.magnulund.dev.movementlog.trips;// Created by Gustav on 16/01/2014.

import android.database.Cursor;

import com.google.android.gms.location.DetectedActivity;

import se.magnulund.dev.movementlog.R;
import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.location.TripCoords;

public class Trip {
    private static final String TAG = "Trip";

    public static final int FIELD_NOT_SET = -1;

    public static final boolean CONFIRMED = true;
    public static final boolean UNCONFIRMED = false;

    public static final int TRIP_CONFIRMED_AS_CORRECT = 1;
    public static final int TRIP_UNCONFIRMED = 0;
    public static final int TRIP_CONFIRMED_AS_INCORRECT = -1;

    int _id;
    int type;

    long startTime;
    int startConfirmedByID;
    int startedByID;
    int startedByType;

    long endTime;
    int endConfirmedByID;
    int endedByID;
    int endedByType;

    int confirmedAs;

    TripCoords startCoords;
    TripCoords endCoords;

    boolean endCoordsSet;
    boolean startCoordsSet;

    public Trip(long startTime, int tripType) {

        this.startTime = startTime;

        this.type = tripType;

        this.confirmedAs = TRIP_UNCONFIRMED;

        this._id = FIELD_NOT_SET;

        this.startConfirmedByID = FIELD_NOT_SET;
        this.startedByID = FIELD_NOT_SET;
        this.startedByType = FIELD_NOT_SET;

        this.endConfirmedByID = FIELD_NOT_SET;
        this.endedByID = FIELD_NOT_SET;
        this.endedByType = FIELD_NOT_SET;

        this.startCoords = null;
        this.endCoords = null;

        this.endCoordsSet = false;
        this.startCoordsSet = false;
    }

    public static Trip fromCursor(Cursor cursor) throws Exception {

        final int tripType = cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.TRIP_TYPE));

        final long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(TripLogContract.Columns.START_TIME));

        Trip trip = new Trip(startTime, tripType);

        trip.setID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns._ID)));

        trip.setStartedByID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.STARTED_BY_ID)));

        trip.setStartedByType(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.STARTED_BY_TYPE)));

        trip.setStartConfirmedByID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.START_CONFIRMED_BY_ID)));

        if (!cursor.isNull(cursor.getColumnIndexOrThrow(TripLogContract.Columns.START_COORDS))) {

            TripCoords startCoords = TripCoords.fromJSONstring(cursor.getString(cursor.getColumnIndexOrThrow(TripLogContract.Columns.START_COORDS)));
            trip.setStartCoords(startCoords);
        }

        trip.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(TripLogContract.Columns.END_TIME)));

        trip.setEndedByID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.ENDED_BY_ID)));

        trip.setEndedByType(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.ENDED_BY_TYPE)));

        trip.setEndConfirmedByID(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.END_CONFIRMED_BY_ID)));

        if (!cursor.isNull(cursor.getColumnIndexOrThrow(TripLogContract.Columns.END_COORDS))) {

            TripCoords endCoords = TripCoords.fromJSONstring(cursor.getString(cursor.getColumnIndexOrThrow(TripLogContract.Columns.END_COORDS)));
            trip.setEndCoords(endCoords);

        }

        trip.setConfirmedAs(cursor.getInt(cursor.getColumnIndexOrThrow(TripLogContract.Columns.CONFIRMED_AS)));

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
        if (isStartConfirmed() && isEndConfirmed()) {
            confirmedAs = TRIP_CONFIRMED_AS_CORRECT;
        }
    }

    public int getEndConfirmedByID() {
        return endConfirmedByID;
    }

    public void setEndConfirmedByID(int endConfirmedByID) {
        this.endConfirmedByID = endConfirmedByID;
        if (isStartConfirmed() && isEndConfirmed()) {
            confirmedAs = TRIP_CONFIRMED_AS_CORRECT;
        }
    }

    public void setConfirmedAs(int confirmedType) {
        this.confirmedAs = confirmedType;
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

    public int getConfirmedAs() {
        return confirmedAs;
    }

    public int getID() {
        return _id;
    }

    public void setID(int id) {
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

    public TripCoords getStartCoords() {
        return startCoords;
    }

    public void setStartCoords(TripCoords startCoords) {
        this.startCoords = startCoords;
        this.startCoordsSet = startCoords != null;
    }

    public boolean hasStartCoords(){
        return startCoordsSet;
    }

    public TripCoords getEndCoords() {
        return endCoords;
    }

    public void setEndCoords(TripCoords endCoords) {
        this.endCoords = endCoords;
        this.endCoordsSet = endCoords != null;
    }

    public boolean hasEndCoords(){
        return endCoordsSet;
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
