package se.magnulund.dev.movementlog.contracts;// Created by Gustav on 12/01/2014.


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Arrays;
import java.util.Calendar;

import se.magnulund.dev.movementlog.providers.MovementDataProvider;
import se.magnulund.dev.movementlog.rawdata.RawData;
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

    public static final String[] DEFAULT_PROJECTION = {Columns._ID, Columns.START_TIME, Columns.END_TIME, Columns.TRIP_TYPE, Columns.START_LATITUDE, Columns.START_LONGITUDE, Columns.END_LATITUDE, Columns.END_LONGITUDE, Columns.CONFIRMED};
    public static final String DEFAULT_SORT_ORDER = Columns.START_TIME + " DESC, " + Columns.END_TIME + " DESC";

    /**
     * Adds an entry to the trip database.
     *
     * @param context the current application context
     * @param trip    the trip
     * @return the Uri of the inserted entry
     */
    public static Uri addTrip(Context context, Trip trip) {
        final ContentResolver resolver = context.getContentResolver();

        final int COLUMN_COUNT = 8;
        ContentValues values = new ContentValues(COLUMN_COUNT);

        values.put(Columns.START_TIME, trip.getStartTime());
        values.put(Columns.END_TIME, trip.getEndTime());
        values.put(Columns.TRIP_TYPE, trip.getType());

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

        values.put(Columns.CONFIRMED, (trip.getConfirmed()) ? 1 : 0);

        return resolver.insert(CONTENT_URI, values);
    }

    /**
     * Retrieves a specific trip from the trips log using the id of that trip
     *
     * @param context the current application context
     * @param id      the id of the trip
     */
    public static Cursor getTripByID(Context context, long id) {
        final ContentResolver resolver = context.getContentResolver();

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

        if (uri != null) {
            return resolver.query(uri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
        }

        return null;
    }

    /**
     * Retrieves a specific trip from the trips log using the URI of that trip
     *
     * @param context        the current application context
     * @param singleEntryUri the Uri of the trip
     */
    public static Cursor getTrip(Context context, Uri singleEntryUri) {
        final ContentResolver resolver = context.getContentResolver();

        if (singleEntryUri != null) {
            return resolver.query(singleEntryUri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
        }

        return null;
    }

    /**
     * Returns a cursor to the full Columns table.
     *
     * @param context the current application context
     */
    public static Cursor getCursor(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        return resolver.query(CONTENT_URI, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
    }

    /**
     * Retrieves a specific trip from the trips log using the URI of that trip
     *
     * @param context the current application context
     */
    public static Trip getLatestUnfinishedTrip(Context context) {
        final ContentResolver resolver = context.getContentResolver();

        String selection = Columns.END_TIME + " = ? AND " + Columns.CONFIRMED + " = ?";

        String[] selectionArgs = {Integer.toString(0), Integer.toString(1)};

        String sortOrderWithLimit = DEFAULT_SORT_ORDER + " LIMIT 1";

        Cursor c = resolver.query(CONTENT_URI, DEFAULT_PROJECTION, selection, selectionArgs, sortOrderWithLimit);

        Trip trip;

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            try {
                trip = Trip.fromCursor(c);
            } catch (Exception e) {
                trip = null;
                e.printStackTrace();
            }
        } else {
            trip = null;
        }

        return trip;
    }

    /**
     * Updates the stored information about a specific trip
     *
     * @param resolver the resolver
     * @param trip     the trip
     */
    public static boolean updateTrip(ContentResolver resolver, Trip trip) {

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, trip.getID());

        final int COLUMN_COUNT = 8;
        ContentValues values = new ContentValues(COLUMN_COUNT);

        values.put(Columns.START_TIME, trip.getStartTime());
        values.put(Columns.END_TIME, trip.getEndTime());
        values.put(Columns.TRIP_TYPE, trip.getType());

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

        values.put(Columns.CONFIRMED, (trip.getConfirmed()) ? 1 : 0);

        return uri != null && resolver.update(uri, values, null, null) > 0;
    }

    /**
     * Sets the start location of a specific trip
     *
     * @param context       the current application context
     * @param id            the id of the trip
     * @param startLocation the start location of the trip
     */
    public static boolean updateTripStartLocation(Context context, long id, Location startLocation) {
        final ContentResolver resolver = context.getContentResolver();

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

        int COLUMN_COUNT = 2;
        ContentValues values = new ContentValues(COLUMN_COUNT);
        values.put(Columns.START_LATITUDE, Location.convert(startLocation.getLatitude(), Location.FORMAT_DEGREES));
        values.put(Columns.START_LONGITUDE, Location.convert(startLocation.getLongitude(), Location.FORMAT_DEGREES));

        return uri != null && resolver.update(uri, values, null, null) > 0;
    }

    /**
     * Sets the end time of a specific trip
     *
     * @param context the current application context
     * @param id      the id of the trip
     * @param endTime the end timestamp of the trip
     */
    public static boolean updateTripEndTime(Context context, long id, long endTime) {
        final ContentResolver resolver = context.getContentResolver();

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

        int COLUMN_COUNT = 1;
        ContentValues values = new ContentValues(COLUMN_COUNT);
        values.put(Columns.END_TIME, Long.toString(endTime));

        return uri != null && resolver.update(uri, values, null, null) > 0;
    }

    /**
     * Sets the end location of a specific trip
     *
     * @param context     the current application context
     * @param id          the id of the trip
     * @param endLocation the end location of the trip
     */
    public static boolean updateTripEndLocation(Context context, long id, Location endLocation) {
        final ContentResolver resolver = context.getContentResolver();

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

        int COLUMN_COUNT = 2;
        ContentValues values = new ContentValues(COLUMN_COUNT);
        values.put(Columns.END_LATITUDE, Location.convert(endLocation.getLatitude(), Location.FORMAT_DEGREES));
        values.put(Columns.END_LONGITUDE, Location.convert(endLocation.getLongitude(), Location.FORMAT_DEGREES));

        return uri != null && resolver.update(uri, values, null, null) > 0;
    }

    /**
     * Deletes an entry with a specific id from the Trip database.
     *
     * @param context the current application context
     * @param tripID  the ID of the trip to be deleted
     */
    public static int deleteTripByID(Context context, long tripID) {
        final ContentResolver resolver = context.getContentResolver();

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, tripID);

        if (uri != null) {
            return resolver.delete(uri, null, null);
        }
        return -1;
    }

    /**
     * Deletes all entries from the Trip database.
     *
     * @param context the current application context
     */
    public static int deleteAllTrips(Context context) {
        final ContentResolver resolver = context.getContentResolver();

        return resolver.delete(CONTENT_URI, null, null);
    }

    /**
     * Deletes entries older than a specified age from the trip database.
     *
     * @param context           the current application context
     * @param maxAllowedTripAge the maximum allowed trip age in milliseconds
     */
    public static int deleteOldTrips(Context context, long maxAllowedTripAge) {
        final ContentResolver resolver = context.getContentResolver();

        String selection = Columns.END_TIME + " < ?";

        Calendar now = Calendar.getInstance();

        final long dateCutoff = now.getTimeInMillis() - maxAllowedTripAge;

        String[] selectionArgs = {Long.toString(dateCutoff)};

        return resolver.delete(CONTENT_URI, selection, selectionArgs);
    }

    /**
     * Checks if a cursor has all columns for a full Trip entry.
     *
     * @param cursor the cursor to check
     */
    public static boolean isValidCursor(Cursor cursor) {
        String[] validColumns = {
                Columns._ID,
                Columns.START_TIME,
                Columns.END_TIME,
                Columns.TRIP_TYPE,
                Columns.START_LATITUDE,
                Columns.START_LONGITUDE,
                Columns.END_LATITUDE,
                Columns.END_LONGITUDE,
                Columns.CONFIRMED
        };

        return cursor.getColumnNames() != null && Arrays.equals(cursor.getColumnNames(), validColumns);
    }

    /**
     * Contains data for detected trips.
     */
    public static class Columns implements BaseColumns {

        public static final String _ID = BaseColumns._ID;

        /**
         * The START_TIME column. The timestamp for the start of the trip in milliseconds.
         * <p>TYPE: INTEGER</p>
         */
        public static final String START_TIME = "start_time";

        /**
         * The START_TIME_FROM_ID column. The id of the raw data entry that set the start time.
         * <p>TYPE: INTEGER</p>
         */
        public static final String START_TIME_FROM_ID = "start_time_from_id";

        /**
         * The START_TIME_FROM_DATA_TYPE column. The data type of the entry that set the end time.
         * <p>TYPE: INTEGER</p>
         */
        public static final String START_TIME_FROM_DATA_TYPE = "start_time_from_data_type";

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
        public static final String END_TIME_FROM_ID = "end_time_from_id";

        /**
         * The END_TIME_FROM_DATA_TYPE column. The data type of the entry that set the end time.
         * <p>TYPE: INTEGER</p>
         */
        public static final String END_TIME_FROM_DATA_TYPE = "end_time_from_data_type";

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

        /**
         * The confirmed column. An boolean indicating if a trip is finished and confirmed.
         * <p>TYPE: STRING</p>
         */
        public static final String CONFIRMED = "confirmed";

    }
}