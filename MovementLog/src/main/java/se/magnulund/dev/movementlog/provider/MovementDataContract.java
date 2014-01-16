package se.magnulund.dev.movementlog.provider;// Created by Gustav on 12/01/2014.


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Calendar;

import se.magnulund.dev.movementlog.DetectedMovement;
import se.magnulund.dev.movementlog.Trip;

/**
 * A provider of movement raw data (MovementDataContract.RawData) and
 * driving/biking data (MovementDataContract.Trips)
 */
public class MovementDataContract {
    /**
     * Authority string for this provider.
     */
    public static final String AUTHORITY = "se.magnulund.dev.movementlog.provider";
    /**
     * The content:// style URL for this provider
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private static final String TAG = "MovementDataContract";

    /**
     * Contains raw data from activity recognition.
     */
    public static class RawData implements BaseColumns {
        public static final String URI_PART_ALL_CONTENT = "rawdata";

        public static final String URI_PART_SINGLE_ITEM = URI_PART_ALL_CONTENT + "/#";

        /**
         * The MIME type of {@link #CONTENT_URI} providing the full raw data log.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.magnulund.rawdata";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single rawdata log entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.magnulund.rawdata";
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/" + URI_PART_ALL_CONTENT);
        public static final String _ID = BaseColumns._ID;
        /**
         * The timestamp column. The timestamp for the detected activity in milliseconds.
         * <p>TYPE: INTEGER</p>
         */
        public static final String TIMESTAMP = "timestamp";
        /**
         * The activity type column. An integer corresponding to the detected activity (walking, driving, etc...)
         * according to [com.google.android.gms.location.DetectedActivity].
         * <p>TYPE: INTEGER</p>
         */
        // --------IMPLEMENT OWN MAPPING BASED ON DetectedActivity SO WE CAN SHARE WITH APPS -------
        // -----------------------------WITHOUT PLAY SERVICES DEPENDENCIES????----------------------
        public static final String ACTIVITY_TYPE = "activity_type";
        /**
         * The confidence column. A value between 0 and 100. Higher values imply higher confidence.
         * <p>TYPE: INTEGER</p>
         */
        public static final String CONFIDENCE = "confidence";
        public static final String DEFAULT_SORT_ORDER = TIMESTAMP + " DESC, " + CONFIDENCE + " DESC";
        /**
         * The confidence rank column. Ranking of activities for a certain timestamp with 0 being the highest rank.
         * <p>TYPE: INTEGER</p>
         */
        public static final String CONFIDENCE_RANK = "confidence_rank";
        public static final String[] DEFAULT_PROJECTION = {_ID, TIMESTAMP, ACTIVITY_TYPE, CONFIDENCE, CONFIDENCE_RANK};

        /**
         * Adds an entry to the rawdata log, with the given timestamp, activity type and confidence.
         *
         * @param context          the current application context
         * @param detectedMovement the detected movement
         */
        public static Uri addEntry(Context context, DetectedMovement detectedMovement) {
            final ContentResolver resolver = context.getContentResolver();

            final int COLUMN_COUNT = 4;
            ContentValues values = new ContentValues(COLUMN_COUNT);

            values.put(TIMESTAMP, detectedMovement.getTimestamp());
            values.put(ACTIVITY_TYPE, detectedMovement.getType());
            values.put(CONFIDENCE, detectedMovement.getConfidence());
            values.put(CONFIDENCE_RANK, detectedMovement.getRank());

            return resolver.insert(CONTENT_URI, values);
        }

        /**
         * Retrieves a specific entry from the rawdata log using the id of that entry
         *
         * @param context the current application context
         * @param id      the id of the entry
         */
        public static Cursor getEntryByID(Context context, long id) {
            final ContentResolver resolver = context.getContentResolver();

            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

            if (uri != null) {
                return resolver.query(uri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
            }

            return null;
        }

        /**
         * Retrieves a specific entry from the rawdata log using the URI for that entry
         *
         * @param context        the current application context
         * @param singleEntryUri the id of the entry
         */
        public static Cursor getEntry(Context context, Uri singleEntryUri) {
            final ContentResolver resolver = context.getContentResolver();

            if (singleEntryUri != null) {
                return resolver.query(singleEntryUri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
            }

            return null;
        }

        /**
         * Deletes all entries from the raw data database.
         *
         * @param context the current application context
         */
        public static int deleteAllEntries(Context context) {
            final ContentResolver resolver = context.getContentResolver();

            return resolver.delete(CONTENT_URI, null, null);
        }

        /**
         * Deletes entries older than a specified time from the raw data database.
         *
         * @param context               the current application context
         * @param maxAllowedEntryTime   the maximum allowed raw data timestamp in milliseconds
         */
        public static int deleteOldEntries(Context context, int maxAllowedEntryTime) {
            final ContentResolver resolver = context.getContentResolver();

            String selection = TIMESTAMP + " < ?";

            String[] selectionArgs = {Integer.toString(maxAllowedEntryTime)};

            return resolver.delete(CONTENT_URI, selection, selectionArgs);
        }


    }

    //* ADD update methods

    /**
     * Contains data for detected trips.
     */
    public static class Trips implements BaseColumns {
        public static final String URI_PART_ALL_CONTENT = "trips";

        public static final String URI_PART_SINGLE_ITEM = URI_PART_ALL_CONTENT + "/#";

        /**
         * The MIME type of {@link #CONTENT_URI} providing the full trip log.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.magnulund.trips";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single trip log entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.magnulund.trips";
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/" + URI_PART_ALL_CONTENT);
        public static final String _ID = BaseColumns._ID;
        /**
         * The START_TIME column. The timestamp for the start of the trip in milliseconds.
         * <p>TYPE: INTEGER</p>
         */
        public static final String START_TIME = "start_time";
        /**
         * The END_TIME column. The timestamp for the end of the trip in milliseconds.
         * <p>TYPE: INTEGER</p>
         */
        public static final String END_TIME = "end_time";
        public static final String DEFAULT_SORT_ORDER = END_TIME + " DESC";
        /**
         * The trip type column. An integer corresponding to the detected trip (1 = DRIVING, 2 = BIKING).
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
        public static final String[] DEFAULT_PROJECTION = {_ID, START_TIME, END_TIME, TRIP_TYPE, START_LATITUDE, START_LONGITUDE, END_LATITUDE, END_LONGITUDE};

        /**
         * Adds an entry to the trip database.
         *
         * @param context the current application context
         * @param trip    the trip
         * @return the Uri of the inserted entry
         */
        public static Uri addTrip(Context context, Trip trip) {
            final ContentResolver resolver = context.getContentResolver();

            final int COLUMN_COUNT = 7;
            ContentValues values = new ContentValues(COLUMN_COUNT);

            values.put(START_TIME, trip.getStartTime());
            values.put(END_TIME, trip.getEndTime());
            values.put(TRIP_TYPE, trip.getType());

            Location start = trip.getStartLocation();
            if (start != null) {
                values.put(START_LATITUDE, Location.convert(start.getLatitude(), Location.FORMAT_DEGREES));
                values.put(START_LONGITUDE, Location.convert(start.getLongitude(), Location.FORMAT_DEGREES));
            }

            Location end = trip.getStartLocation();
            if (end != null) {
                values.put(END_LATITUDE, Location.convert(end.getLatitude(), Location.FORMAT_DEGREES));
                values.put(END_LONGITUDE, Location.convert(end.getLongitude(), Location.FORMAT_DEGREES));
            }

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
            values.put(START_LATITUDE, Location.convert(startLocation.getLatitude(), Location.FORMAT_DEGREES));
            values.put(START_LONGITUDE, Location.convert(startLocation.getLongitude(), Location.FORMAT_DEGREES));

            return uri != null && resolver.update(uri, values, null, null) > 0;
        }

        /**
         * Sets the end time of a specific trip
         *
         * @param context the current application context
         * @param id      the id of the trip
         * @param endTime the end timestamp of the trip
         */
        public static boolean updateTripEndTime(Context context, long id, int endTime) {
            final ContentResolver resolver = context.getContentResolver();

            Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

            int COLUMN_COUNT = 1;
            ContentValues values = new ContentValues(COLUMN_COUNT);
            values.put(END_TIME, Integer.toString(endTime));

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
            values.put(END_LATITUDE, Location.convert(endLocation.getLatitude(), Location.FORMAT_DEGREES));
            values.put(END_LONGITUDE, Location.convert(endLocation.getLongitude(), Location.FORMAT_DEGREES));

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
        public static int deleteOldTrips(Context context, int maxAllowedTripAge) {
            final ContentResolver resolver = context.getContentResolver();

            String selection = END_TIME + " < ?";

            Calendar now = Calendar.getInstance();

            final int dateCutoff = (int) now.getTimeInMillis() - maxAllowedTripAge;

            String[] selectionArgs = {Integer.toString(dateCutoff)};

            return resolver.delete(CONTENT_URI, selection, selectionArgs);
        }


    }
}