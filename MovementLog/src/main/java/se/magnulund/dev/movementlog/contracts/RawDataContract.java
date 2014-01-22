package se.magnulund.dev.movementlog.contracts;// Created by Gustav on 22/01/2014.

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Arrays;

import se.magnulund.dev.movementlog.providers.MovementDataProvider;
import se.magnulund.dev.movementlog.rawdata.RawData;

public class RawDataContract {
    private static final String TAG = "RawDataContract";
    /**
     * Authority string for this provider.
     */
    public static final String AUTHORITY = MovementDataProvider.AUTHORITY;

    public static final String URI_PART_ALL_CONTENT = "rawdata";
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + URI_PART_ALL_CONTENT);
    public static final String URI_PART_SINGLE_ITEM = URI_PART_ALL_CONTENT + "/#";
    /**
     * The MIME type of {@link #CONTENT_URI} providing the full raw data log.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.magnulund.rawdata";
    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single rawdata log entry.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.magnulund.rawdata";
    public static final String[] DEFAULT_PROJECTION = {Columns._ID, Columns.TIMESTAMP, Columns.ACTIVITY_TYPE, Columns.CONFIDENCE, Columns.CONFIDENCE_RANK};
    public static final String DEFAULT_SORT_ORDER = Columns.TIMESTAMP + " DESC, " + Columns.CONFIDENCE + " DESC";

    /**
     * Adds an entry to the rawdata log, with the given timestamp, activity type and confidence.
     *
     * @param context   the current application context
     * @param rawData   the detected activity rawdata
     */
    // TODO add "confirmed" int (1|0)
    public static Uri addEntry(Context context, RawData rawData) {
        final ContentResolver resolver = context.getContentResolver();

        final int COLUMN_COUNT = 4;
        ContentValues values = new ContentValues(COLUMN_COUNT);

        values.put(Columns.TIMESTAMP, rawData.getTimestamp());
        values.put(Columns.ACTIVITY_TYPE, rawData.getType());
        values.put(Columns.CONFIDENCE, rawData.getConfidence());
        values.put(Columns.CONFIDENCE_RANK, rawData.getRank());

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
     * Returns a cursor to the full Columns table.
     *
     * @param context the current application context
     */
    public static Cursor getCursor(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        return resolver.query(CONTENT_URI, DEFAULT_PROJECTION, null, null, DEFAULT_SORT_ORDER);
    }

    /**
     * Updates the stored information about a specific trip
     *
     * @param context          the current application context
     * @param id               the id of the entry to be updated
     * @param rawData the Columns object containing the data to be stored
     */
    public static boolean updateEntry(Context context, long id, RawData rawData) {
        final ContentResolver resolver = context.getContentResolver();

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

        final int COLUMN_COUNT = 4;
        ContentValues values = new ContentValues(COLUMN_COUNT);

        values.put(Columns.TIMESTAMP, rawData.getTimestamp());
        values.put(Columns.ACTIVITY_TYPE, rawData.getType());
        values.put(Columns.CONFIDENCE, rawData.getConfidence());
        values.put(Columns.CONFIDENCE_RANK, rawData.getRank());

        return uri != null && resolver.update(uri, values, null, null) > 0;
    }

    /**
     * Deletes an entry with a specific id from the Columns database.
     *
     * @param context the current application context
     * @param id      the ID of the entry to be deleted
     */
    public static int deleteEntryByID(Context context, long id) {
        final ContentResolver resolver = context.getContentResolver();

        Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);

        if (uri != null) {
            return resolver.delete(uri, null, null);
        }
        return -1;
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
     * @param context             the current application context
     * @param maxAllowedEntryTime the maximum allowed raw data timestamp in milliseconds
     */
    public static int deleteOldEntries(Context context, long maxAllowedEntryTime) {
        final ContentResolver resolver = context.getContentResolver();

        String selection = Columns.TIMESTAMP + " < ?";

        String[] selectionArgs = {Long.toString(maxAllowedEntryTime)};

        return resolver.delete(CONTENT_URI, selection, selectionArgs);
    }

    /**
     * Checks if a cursor has all columns for a full Columns entry.
     *
     * @param cursor the cursor to check
     */
    public static boolean isValidCursor(Cursor cursor) {
        String[] validColumns = {
                Columns._ID,
                Columns.TIMESTAMP,
                Columns.ACTIVITY_TYPE,
                Columns.CONFIDENCE,
                Columns.CONFIDENCE_RANK
        };

        return cursor.getColumnNames() != null && Arrays.equals(cursor.getColumnNames(), validColumns);
    }

    /**
     * Contains raw data from activity recognition.
     */
    public static class Columns implements BaseColumns {

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
        // TODO implement mapping to DetectedActivity type-names?
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

    }
}
