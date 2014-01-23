package se.magnulund.dev.movementlog.contracts;// Created by Gustav on 12/01/2014.


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Calendar;

import se.magnulund.dev.movementlog.providers.MovementDataProvider;
import se.magnulund.dev.movementlog.trips.Trip;

/**
 * A provider of detected activity raw data (TripLogContract.Columns) and
 * driving/biking data (TripLogContract.Columns)
 */
public class TripLogContract {
    private static final String TAG = "TripLogContract";

    /**
     * Authority string for this provider.
     */
    public static final String AUTHORITY = MovementDataProvider.AUTHORITY;
    public static final String URI_PART_ALL_CONTENT = "trips";
    public static final String URI_PART_SINGLE_ITEM = URI_PART_ALL_CONTENT + "/#";

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + URI_PART_ALL_CONTENT);

    /**
     * The MIME type of {@link #CONTENT_URI} providing the full trip log.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.magnulund.trips";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single trip log entry.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.magnulund.trips";

    public static final String[] DEFAULT_PROJECTION = {
            Columns._ID,
            Columns.TRIP_TYPE,
            Columns.START_TIME,
            Columns.STARTED_BY_ID,
            Columns.STARTED_BY_TYPE,
            Columns.START_CONFIRMED_BY_ID,
            Columns.START_LATITUDE,
            Columns.START_LONGITUDE,
            Columns.END_TIME,
            Columns.ENDED_BY_ID,
            Columns.ENDED_BY_TYPE,
            Columns.END_CONFIRMED_BY_ID,
            Columns.END_LATITUDE,
            Columns.END_LONGITUDE
    };
    public static final String DEFAULT_SORT_ORDER = Columns.START_TIME + " DESC, " + Columns.END_TIME + " DESC";

    /**
     * Adds an entry to the trip database.
     *
     * @param resolver the content resolver
     * @param trip     the trip
     * @return the Uri of the inserted entry
     */
    public static Uri addTrip(ContentResolver resolver, Trip trip) {
        ContentValues values = getTripContentValues(trip);

        return resolver.insert(CONTENT_URI, values);
    }

    /**
     * Retrieves a specific trip from the trips log using the URI of that trip
     *
     * @param resolver the content resolver
     * @param uri      the Uri of the trip
     */
    public static Cursor getTrip(ContentResolver resolver, Uri uri) {

        if (uri != null) {
            return resolver.query(uri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
        } else {
            return null;
        }
    }

    /**
     * Returns a cursor to the full Columns table.
     *
     * @param resolver the content resolver
     */
    public static Cursor getCursor(ContentResolver resolver) {
        return resolver.query(CONTENT_URI, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
    }

    /**
     * Retrieves a specific trip from the trips log using the URI of that trip
     *
     * @param resolver the content resolver
     */
    public static Trip getLatestUnfinishedTrip(ContentResolver resolver) {

        String selection = Columns.END_CONFIRMED_BY_ID + " < ?";

        String[] selectionArgs = {Integer.toString(0)};

        String sortOrderWithLimit = DEFAULT_SORT_ORDER + " LIMIT 1";

        Cursor c = resolver.query(CONTENT_URI, DEFAULT_PROJECTION, selection, selectionArgs, sortOrderWithLimit);

        if (c != null && c.getCount() > 0) {

            c.moveToFirst();

            try {
                return Trip.fromCursor(c);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Updates the stored information about a specific trip
     *
     * @param resolver the content resolver
     * @param trip     the trip
     */
    public static boolean updateTrip(ContentResolver resolver, Trip trip) {

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, trip.getID());

        ContentValues values = getTripContentValues(trip);

        return uri != null && resolver.update(uri, values, null, null) > 0;
    }

    /**
     * Deletes an entry with a specific id from the Trip database.
     *
     * @param resolver the content resolver
     * @param trip     the trip
     */
    public static int deleteTrip(ContentResolver resolver, Trip trip) {

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, trip.getID());

        if (uri != null) {
            return resolver.delete(uri, null, null);
        }
        return -1;
    }

    /**
     * Deletes all entries from the Trip database.
     *
     * @param resolver the content resolver
     */
    public static int deleteAllTrips(ContentResolver resolver) {

        return resolver.delete(CONTENT_URI, null, null);
    }

    /**
     * Deletes entries older than a specified age from the trip database.
     *
     * @param resolver          the content resolver
     * @param maxAllowedTripAge the maximum allowed trip age in milliseconds
     */
    public static int deleteOldTrips(ContentResolver resolver, long maxAllowedTripAge) {

        String selection = Columns.END_TIME + " < ?";

        Calendar now = Calendar.getInstance();

        final long dateCutoff = now.getTimeInMillis() - maxAllowedTripAge;

        String[] selectionArgs = {Long.toString(dateCutoff)};

        return resolver.delete(CONTENT_URI, selection, selectionArgs);
    }

    private static ContentValues getTripContentValues(Trip trip) {

        ContentValues values = new ContentValues(Columns.CONTENT_VALUE_COLUMN_COUNT);

        values.put(Columns.START_TIME, trip.getStartTime());
        values.put(Columns.TRIP_TYPE, trip.getType());
        values.put(Columns.END_TIME, trip.getEndTime());

        values.put(Columns.STARTED_BY_ID, trip.getStartedByID());
        values.put(Columns.STARTED_BY_TYPE, trip.getStartedByType());
        values.put(Columns.START_CONFIRMED_BY_ID, trip.getStartConfirmedByID());
        values.put(Columns.ENDED_BY_ID, trip.getEndedByID());
        values.put(Columns.ENDED_BY_TYPE, trip.getEndedByType());
        values.put(Columns.END_CONFIRMED_BY_ID, trip.getEndConfirmedByID());

        Location start = trip.getStartLocation();
        if (start != null) {
            values.put(Columns.START_LATITUDE, Location.convert(start.getLatitude(), Location.FORMAT_DEGREES));
            values.put(Columns.START_LONGITUDE, Location.convert(start.getLongitude(), Location.FORMAT_DEGREES));
        }

        Location end = trip.getStartLocation();
        if (end != null) {
            values.put(Columns.END_LATITUDE, Location.convert(end.getLatitude(), Location.FORMAT_DEGREES));
            values.put(Columns.END_LONGITUDE, Location.convert(end.getLongitude(), Location.FORMAT_DEGREES));
        }

        return values;
    }

    /**
     * Contains data for detected trips.
     */
    public static class Columns implements BaseColumns {

        public static final String _ID = BaseColumns._ID;

        public static final int CONTENT_VALUE_COLUMN_COUNT = 13;

        /**
         * The START_TIME column. The timestamp for the start of the trip in milliseconds.
         * <p>TYPE: INTEGER</p>
         */
        public static final String START_TIME = "start_time";

        /**
         * The START_TIME_FROM_ID column. The id of the raw data entry that set the start time.
         * <p>TYPE: INTEGER</p>
         */
        public static final String STARTED_BY_ID = "start_time_from_id";

        /**
         * The START_TIME_FROM_DATA_TYPE column. The data type of the entry that set the end time.
         * <p>TYPE: INTEGER</p>
         */
        public static final String STARTED_BY_TYPE = "start_time_from_data_type";

        /**
         * The START_CONFIRMED_ID column. The id of the raw data entry that confirmed the start of the trip.
         * <p>TYPE: INTEGER</p>
         */
        public static final String START_CONFIRMED_BY_ID = "start_confirmed_id";

        /**
         * The END_TIME column. The timestamp for the end of the trip in milliseconds.
         * <p>TYPE: INTEGER</p>
         */
        public static final String END_TIME = "end_time";

        /**
         * The END_TIME_FROM_ID column. The id of the raw data entry that set the end time.
         * <p>TYPE: INTEGER</p>
         */
        public static final String ENDED_BY_ID = "end_time_from_id";

        /**
         * The END_TIME_FROM_DATA_TYPE column. The data type of the entry that set the end time.
         * <p>TYPE: INTEGER</p>
         */
        public static final String ENDED_BY_TYPE = "end_time_from_data_type";

        /**
         * The END_CONFIRMED_BY_ID column. The id of the raw data entry that confirmed the end of the trip.
         * <p>TYPE: INTEGER</p>
         */
        public static final String END_CONFIRMED_BY_ID = "end_confirmed_id";

        /**
         * The TRIP_TYPE column. An integer corresponding to the detected trip (1 = DRIVING, 2 = BIKING).
         * <p>TYPE: INTEGER</p>
         */
        public static final String TRIP_TYPE = "trip_type";

        /**
         * The start latitude column. A coordinate representing the latitude at which the trip started.
         * <p>TYPE: STRING</p>
         */
        public static final String START_LATITUDE = "start_latitude";

        /**
         * The start longitude column. A coordinate representing the longitude at which the trip started.
         * <p>TYPE: STRING</p>
         */
        public static final String START_LONGITUDE = "start_longitude";

        /**
         * The start latitude column. A coordinate representing the latitude at which the trip ended.
         * <p>TYPE: STRING</p>
         */
        public static final String END_LATITUDE = "end_latitude";

        /**
         * The start longitude column. A coordinate representing the longitude at which the trip ended.
         * <p>TYPE: STRING</p>
         */
        public static final String END_LONGITUDE = "end_longitude";

    }
}