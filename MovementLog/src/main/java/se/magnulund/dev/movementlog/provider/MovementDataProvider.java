package se.magnulund.dev.movementlog.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public class MovementDataProvider extends ContentProvider {

    public static final String TAG = "MovementDataProvider";

    public static final String AUTHORITY = MovementDataContract.AUTHORITY;

    public static final Uri CONTENT_URI = MovementDataContract.CONTENT_URI;
    private static final String DATABASE_NAME = "movement_data.db";
    private static final String RAWDATA_TABLE_NAME = "rawdata";
    private static final String TRIPS_TABLE_NAME = "trips";
    private static final String DATABASE_CREATE =
            "create table " + RAWDATA_TABLE_NAME + "("
                    + MovementDataContract.RawData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MovementDataContract.RawData.TIMESTAMP + " INTEGER, "
                    + MovementDataContract.RawData.ACTIVITY_TYPE + " INTEGER, "
                    + MovementDataContract.RawData.CONFIDENCE + "INTEGER"
                    + ");"
                    + "create table " + TRIPS_TABLE_NAME + "("
                    + MovementDataContract.Trips._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MovementDataContract.Trips.START_TIME + " INTEGER, "
                    + MovementDataContract.Trips.END_TIME + " INTEGER, "
                    + MovementDataContract.Trips.TRIP_TYPE + " INTEGER,"
                    + MovementDataContract.Trips.START_LATITUDE + " TEXT, "
                    + MovementDataContract.Trips.START_LONGITUDE + " TEXT, "
                    + MovementDataContract.Trips.END_LATITUDE + " TEXT, "
                    + MovementDataContract.Trips.END_LONGITUDE + " TEXT)"
                    + ");";
    private static final int DATABASE_VERSION = 1;
    private static final UriMatcher uriMatcher;
    private static final int RAWDATA = 1;
    private static final int RAWDATA_ENTRY = 2;
    private static final int TRIPS = 3;
    private static final int TRIP_ENTRY = 4;
    private static HashMap<String, String> rawdataProjectionMap;
    private static HashMap<String, String> tripsProjectionMap;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {

        /*
         * Creates a new helper object. This method always returns quickly.
         * Notice that the database itself isn't created or opened
         * until SQLiteOpenHelper.getWritableDatabase is called
         */
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case RAWDATA_ENTRY:
                qb.setTables(RAWDATA_TABLE_NAME);
                qb.setProjectionMap(rawdataProjectionMap);
                if (uri.getPathSegments() != null) {
                    qb.appendWhere(
                            MovementDataContract.RawData._ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case RAWDATA:
                qb.setTables(RAWDATA_TABLE_NAME);
                qb.setProjectionMap(rawdataProjectionMap);
                break;
            case TRIP_ENTRY:
                qb.setTables(TRIPS_TABLE_NAME);
                qb.setProjectionMap(tripsProjectionMap);
                if (uri.getPathSegments() != null) {
                    qb.appendWhere(
                            MovementDataContract.Trips._ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case TRIPS:
                qb.setTables(RAWDATA_TABLE_NAME);
                qb.setProjectionMap(tripsProjectionMap);
                break;
            default:
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            switch (uriMatcher.match(uri)) {
                case RAWDATA_ENTRY:
                case RAWDATA:
                    orderBy = MovementDataContract.RawData.DEFAULT_SORT_ORDER;
                    break;
                case TRIPS:
                case TRIP_ENTRY:
                    orderBy = MovementDataContract.Trips.DEFAULT_SORT_ORDER;
                    break;
                default:
                    orderBy = "";
            }
        } else {
            orderBy = sortOrder;
        }

        Cursor cursor = qb.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                orderBy);

        //---register to watch a content URI for changes---
        if (cursor != null && getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            // get all entries
            case RAWDATA:
                return MovementDataContract.RawData.CONTENT_TYPE;
            // get a particular entry
            case RAWDATA_ENTRY:
                return MovementDataContract.RawData.CONTENT_ITEM_TYPE;
            case TRIPS:
                return MovementDataContract.Trips.CONTENT_TYPE;
            // get a particular entry
            case TRIP_ENTRY:
                return MovementDataContract.Trips.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // create/open the database
    private void getDatabase() {

        db = dbHelper.getWritableDatabase();

        if (db == null) {
            throw new SQLException("Failed to create or open movementDB -" + TAG);
        }

    }

    // Implements the provider's insert method
    @Override
    public Uri insert(Uri uri, ContentValues startValues) {

        final int match = uriMatcher.match(uri);

        // Validate the requested uri
        if (match != RAWDATA && match != TRIPS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues contentValues;
        if (startValues != null) {
            contentValues = new ContentValues(startValues);
        } else {
            contentValues = new ContentValues();
        }

        String dbTable = "";

        switch (match) {
            case RAWDATA:
                if (contentValues.containsKey(MovementDataContract.RawData.TIMESTAMP) == false) {
                    throw new SQLException("Timestamp must be specified");
                }
                if (contentValues.containsKey(MovementDataContract.RawData.ACTIVITY_TYPE) == false) {
                    throw new SQLException("Activity type must be specified");
                }
                if (contentValues.containsKey(MovementDataContract.RawData.CONFIDENCE) == false) {
                    throw new SQLException("Confidence must be specified");
                }

                dbTable = RAWDATA_TABLE_NAME;

                break;
            case TRIPS:
                if (contentValues.containsKey(MovementDataContract.Trips.START_TIME) == false) {
                    throw new SQLException("Start time must be specified");
                }
                if (contentValues.containsKey(MovementDataContract.Trips.TRIP_TYPE) == false) {
                    throw new SQLException("Trip type must be specified");
                }
                if (contentValues.containsKey(MovementDataContract.Trips.START_LATITUDE) == false) {
                    throw new SQLException("Start latitude must be specified");
                }
                if (contentValues.containsKey(MovementDataContract.Trips.START_LONGITUDE) == false) {
                    throw new SQLException("Start longitude must be specified");
                }

                if (contentValues.containsKey(MovementDataContract.Trips.END_TIME) == false) {
                    contentValues.put(MovementDataContract.Trips.END_TIME, (String) null);
                }
                if (contentValues.containsKey(MovementDataContract.Trips.END_LATITUDE) == false) {
                    contentValues.put(MovementDataContract.Trips.END_LATITUDE, (String) null);
                }
                if (contentValues.containsKey(MovementDataContract.Trips.END_LONGITUDE) == false) {
                    contentValues.put(MovementDataContract.Trips.END_LONGITUDE, (String) null);
                }

                dbTable = TRIPS_TABLE_NAME;

                break;
            default:
        }

        getDatabase();

        // add a new entry
        long rowID = db.insert(dbTable, null, contentValues);

        // if added successfully
        if (rowID > 0) {
            // Get uri and notify about change
            Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(insertUri, null);
            }
            return insertUri;
        }

        throw new SQLException("Failed to insert row into " + uri + " - " + TAG);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        getDatabase();

        int count = 0;
        switch (uriMatcher.match(uri)) {
            case RAWDATA:
                count = db.delete(
                        RAWDATA_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case RAWDATA_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            RAWDATA_TABLE_NAME,
                            MovementDataContract.RawData._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            case TRIPS:
                count = db.delete(
                        TRIPS_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TRIP_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            TRIPS_TABLE_NAME,
                            MovementDataContract.Trips._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown URI " + selection);
        }
        if (getContext() != null && count > 0) { // can count be <= 0 if item/s were deleted???
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case RAWDATA:
                count = db.delete(
                        RAWDATA_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case RAWDATA_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            RAWDATA_TABLE_NAME,
                            MovementDataContract.RawData._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            case TRIPS:
                count = db.delete(
                        TRIPS_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TRIP_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            TRIPS_TABLE_NAME,
                            MovementDataContract.Trips._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown URI " + selection);
        }
        if (getContext() != null && count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MovementDataContract.RawData.URI_PART_ALL_CONTENT, RAWDATA);
        uriMatcher.addURI(AUTHORITY, MovementDataContract.RawData.URI_PART_SINGLE_ITEM, RAWDATA_ENTRY);
        uriMatcher.addURI(AUTHORITY, MovementDataContract.Trips.URI_PART_ALL_CONTENT, TRIPS);
        uriMatcher.addURI(AUTHORITY, MovementDataContract.Trips.URI_PART_SINGLE_ITEM, TRIP_ENTRY);

        rawdataProjectionMap = new HashMap<String, String>();
        rawdataProjectionMap.put(MovementDataContract.RawData._ID, MovementDataContract.RawData._ID);
        rawdataProjectionMap.put(MovementDataContract.RawData.TIMESTAMP, MovementDataContract.RawData.TIMESTAMP);
        rawdataProjectionMap.put(MovementDataContract.RawData.ACTIVITY_TYPE, MovementDataContract.RawData.ACTIVITY_TYPE);
        rawdataProjectionMap.put(MovementDataContract.RawData.CONFIDENCE, MovementDataContract.RawData.CONFIDENCE);
        rawdataProjectionMap.put(MovementDataContract.RawData.CONFIDENCE_RANK, MovementDataContract.RawData.CONFIDENCE_RANK);

        tripsProjectionMap = new HashMap<String, String>();
        tripsProjectionMap.put(MovementDataContract.Trips._ID, MovementDataContract.Trips._ID);
        tripsProjectionMap.put(MovementDataContract.Trips.START_TIME, MovementDataContract.Trips.START_TIME);
        tripsProjectionMap.put(MovementDataContract.Trips.END_TIME, MovementDataContract.Trips.END_TIME);
        tripsProjectionMap.put(MovementDataContract.Trips.TRIP_TYPE, MovementDataContract.Trips.TRIP_TYPE);
        tripsProjectionMap.put(MovementDataContract.Trips.START_LATITUDE, MovementDataContract.Trips.START_LATITUDE);
        tripsProjectionMap.put(MovementDataContract.Trips.START_LONGITUDE, MovementDataContract.Trips.START_LONGITUDE);
        tripsProjectionMap.put(MovementDataContract.Trips.END_LATITUDE, MovementDataContract.Trips.END_LATITUDE);
        tripsProjectionMap.put(MovementDataContract.Trips.END_LONGITUDE, MovementDataContract.Trips.END_LONGITUDE);

    }

    /**
     * Helper class that actually creates and manages the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            Log.w("Content provider database",
                    "Upgrading database from version " +
                            oldVersion + " to " + newVersion +
                            ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS titles");
            onCreate(db);
        }
    }

}
