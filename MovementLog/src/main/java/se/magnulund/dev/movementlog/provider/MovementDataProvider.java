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

import se.magnulund.dev.movementlog.databases.MovementsDatabase;
import se.magnulund.dev.movementlog.tables.RawData;
import se.magnulund.dev.movementlog.tables.Trips;

public class MovementDataProvider extends ContentProvider {

    public static final String TAG = "MovementDataProvider";

    public static final String AUTHORITY = MovementDataContract.AUTHORITY;

    private static final UriMatcher uriMatcher;
    private static final int RAWDATA = 1;
    private static final int RAWDATA_ENTRY = 2;
    private static final int TRIPS = 3;
    private static final int TRIP_ENTRY = 4;

    private MovementsDatabase dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new MovementsDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case RAWDATA_ENTRY:
                qb.setTables(RawData.TABLE);
                qb.setProjectionMap(RawData.projectionMap);

                if (uri.getPathSegments() != null) {
                    qb.appendWhere(
                            MovementDataContract.RawDataLog._ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case RAWDATA:
                qb.setTables(RawData.TABLE);
                qb.setProjectionMap(RawData.projectionMap);
                break;
            case TRIP_ENTRY:
                qb.setTables(Trips.TABLE);
                qb.setProjectionMap(Trips.projectionMap);

                if (uri.getPathSegments() != null) {
                    qb.appendWhere(
                            MovementDataContract.TripLog._ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case TRIPS:
                qb.setTables(Trips.TABLE);
                qb.setProjectionMap(Trips.projectionMap);
                break;
            default:
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            switch (uriMatcher.match(uri)) {
                case RAWDATA_ENTRY:
                case RAWDATA:
                    orderBy = MovementDataContract.RawDataLog.DEFAULT_SORT_ORDER;
                    break;
                case TRIPS:
                case TRIP_ENTRY:
                    orderBy = MovementDataContract.TripLog.DEFAULT_SORT_ORDER;
                    break;
                default:
                    orderBy = "";
            }
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        assert db != null;

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
                return MovementDataContract.RawDataLog.CONTENT_TYPE;
            // get a particular entry
            case RAWDATA_ENTRY:
                return MovementDataContract.RawDataLog.CONTENT_ITEM_TYPE;
            case TRIPS:
                return MovementDataContract.TripLog.CONTENT_TYPE;
            // get a particular entry
            case TRIP_ENTRY:
                return MovementDataContract.TripLog.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
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
                if (!contentValues.containsKey(MovementDataContract.RawDataLog.TIMESTAMP)) {
                    throw new SQLException("Timestamp must be specified");
                }
                if (!contentValues.containsKey(MovementDataContract.RawDataLog.ACTIVITY_TYPE)) {
                    throw new SQLException("Activity type must be specified");
                }
                if (!contentValues.containsKey(MovementDataContract.RawDataLog.CONFIDENCE)) {
                    throw new SQLException("Confidence must be specified");
                }
                if (!contentValues.containsKey(MovementDataContract.RawDataLog.CONFIDENCE_RANK)) {
                    throw new SQLException("Confidence rank must be specified");
                }

                dbTable = RawData.TABLE;

                break;
            case TRIPS:
                if (!contentValues.containsKey(MovementDataContract.TripLog.START_TIME)) {
                    throw new SQLException("Start time must be specified");
                }
                if (!contentValues.containsKey(MovementDataContract.TripLog.TRIP_TYPE)) {
                    throw new SQLException("Trip type must be specified");
                }

                dbTable = Trips.TABLE;

                break;
            default:
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assert db != null;

        // add a new entry
        long rowID = db.insert(dbTable, null, contentValues);

        // if added successfully
        if (rowID > 0) {
            // Get uri and notify about change
            Uri insertUri = ContentUris.withAppendedId(uri, rowID);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(insertUri, null);
            }
            return insertUri;
        }

        throw new SQLException("Failed to insert row into " + uri + " - " + TAG);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assert db != null;

        int count = 0;
        switch (uriMatcher.match(uri)) {
            case RAWDATA:
                count = db.delete(
                        RawData.TABLE,
                        selection,
                        selectionArgs);
                break;
            case RAWDATA_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            RawData.TABLE,
                            MovementDataContract.RawDataLog._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            case TRIPS:
                count = db.delete(
                        Trips.TABLE,
                        selection,
                        selectionArgs);
                break;
            case TRIP_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            Trips.TABLE,
                            MovementDataContract.TripLog._ID + " = "
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
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assert db != null;

        int count = 0;
        switch (uriMatcher.match(uri)) {
            case RAWDATA:
                count = db.update(
                        RawData.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case RAWDATA_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.update(
                            RawData.TABLE,
                            values,
                            MovementDataContract.RawDataLog._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            case TRIPS:
                count = db.update(
                        Trips.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TRIP_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.update(
                            Trips.TABLE,
                            values,
                            MovementDataContract.TripLog._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown URI " + uri);
        }

        dbHelper.getWritableDatabase();

        if (getContext() != null && count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MovementDataContract.RawDataLog.URI_PART_ALL_CONTENT, RAWDATA);
        uriMatcher.addURI(AUTHORITY, MovementDataContract.RawDataLog.URI_PART_SINGLE_ITEM, RAWDATA_ENTRY);
        uriMatcher.addURI(AUTHORITY, MovementDataContract.TripLog.URI_PART_ALL_CONTENT, TRIPS);
        uriMatcher.addURI(AUTHORITY, MovementDataContract.TripLog.URI_PART_SINGLE_ITEM, TRIP_ENTRY);
    }


}
