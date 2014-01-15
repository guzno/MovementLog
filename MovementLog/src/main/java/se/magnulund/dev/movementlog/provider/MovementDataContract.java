package se.magnulund.dev.movementlog.provider;// Created by Gustav on 12/01/2014.


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Calendar;

import se.magnulund.dev.movementlog.DetectedMovement;

/**
 * A provider of movement raw data (MovementDataContract.RawData) and
 * driving/biking data (MovementDataContract.Trips)
 */
public class MovementDataContract {
    private static final String TAG = "MovementDataContract";
    /**
     * Authority string for this provider.
     */
    public static final String AUTHORITY = "se.magnulund.dev.movementlog.provider";

    /**
     * The content:// style URL for this provider
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

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
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/" + URI_PART_ALL_CONTENT);
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single rawdata log entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.magnulund.rawdata";

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
        /**
         * The confidence rank column. Ranking of activities for a certain timestamp with 0 being the highest rank.
         * <p>TYPE: INTEGER</p>
         */
        public static final String CONFIDENCE_RANK = "confidence_rank";

        public static final String DEFAULT_SORT_ORDER = TIMESTAMP + " DESC, " + CONFIDENCE + " DESC";

        public static final String[] DEFAULT_PROJECTION = {_ID, TIMESTAMP, ACTIVITY_TYPE, CONFIDENCE, CONFIDENCE_RANK};

        /**
         * Adds an entry to the rawdata log, with the given timestamp, activity type and confidence.
         *
         * @param context           the current application context
         * @param detectedMovement  the detected movement
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

        public static Cursor getEntryByID(Context context, int id) {
            final ContentResolver resolver = context.getContentResolver();

            Uri uri = CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
            Log.e(TAG, "getEntryByID - "+uri);
            if (uri != null){
                return resolver.query(uri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
            }

            return null;
        }

        public static Cursor getEntry(Context context, Uri singleEntryUri) {
            final ContentResolver resolver = context.getContentResolver();

            if (singleEntryUri != null){
                return resolver.query(singleEntryUri, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
            }

            return null;
        }


    }

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
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/" + URI_PART_ALL_CONTENT);
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single trip log entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.magnulund.trips";

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

        public static final String DEFAULT_SORT_ORDER = END_TIME + " DESC";

        /**
         * Adds an entry to the trip database.
         *
         * @param context       the current application context
         * @param startTime     the timestamp of when the trip started
         * @param endTime       the timestamp of when the trip ended (optional). int timestamp or null
         * @param tripType      the trip type
         * @param startLocation the start location
         * @param endLocation   the end location (optional). end Location or null.
         */
        protected static Uri addTrip(Context context, int startTime,
                                     Integer endTime, int tripType,
                                     Location startLocation,
                                     Location endLocation) {
            final ContentResolver resolver = context.getContentResolver();

            final int COLUMN_COUNT = 7;
            ContentValues values = new ContentValues(COLUMN_COUNT);

            values.put(START_TIME, startTime);
            values.put(END_TIME, endTime);
            values.put(TRIP_TYPE, tripType);
            values.put(START_LATITUDE,
                    Location.convert(startLocation.getLatitude(), Location.FORMAT_DEGREES));
            values.put(START_LONGITUDE,
                    Location.convert(startLocation.getLongitude(), Location.FORMAT_DEGREES));
            values.put(END_LATITUDE,
                    endLocation == null ? null :
                            Location.convert(endLocation.getLatitude(), Location.FORMAT_DEGREES));
            values.put(END_LONGITUDE,
                    endLocation == null ? null :
                            Location.convert(endLocation.getLongitude(), Location.FORMAT_DEGREES));
            return resolver.insert(CONTENT_URI, values);
        }

        /**
         * Deletes an entry with a specific id from the Trip database.
         *
         * @param context the current application context
         * @param tripID  the ID of the trip to be deleted
         */
        protected static int deleteTripByID(Context context, int tripID) {
            final ContentResolver resolver = context.getContentResolver();

            String selection = _ID + " = ?";

            String[] selectionArgs = {Integer.toString(tripID)};

            Uri uri = Uri.parse("content://" + AUTHORITY + URI_PART_SINGLE_ITEM);

            return resolver.delete(uri, selection, selectionArgs);
        }

        /**
         * Deletes all entries from the Trip database.
         *
         * @param context the current application context
         */
        protected static int deleteAllTrips(Context context) {
            final ContentResolver resolver = context.getContentResolver();

            String selection = "*";

            String[] selectionArgs = {};

            Uri uri = Uri.parse("content://" + AUTHORITY + URI_PART_ALL_CONTENT);

            return resolver.delete(uri, selection, selectionArgs);
        }

        /**
         * Deletes entries older than a specified age from the trip database.
         *
         * @param context           the current application context
         * @param maxAllowedTripAge the maximum allowed trip age in milliseconds
         */
        protected static int deleteOldTrips(Context context, int maxAllowedTripAge) {
            final ContentResolver resolver = context.getContentResolver();

            String selection = END_TIME + " < ?";

            Calendar now = Calendar.getInstance();

            final int dateCutoff = (int) now.getTimeInMillis() - maxAllowedTripAge;

            String[] selectionArgs = {Integer.toString(dateCutoff)};

            Uri uri = Uri.parse("content://" + AUTHORITY + URI_PART_ALL_CONTENT);

            return resolver.delete(uri, selection, selectionArgs);
        }


    }
}