package se.magnulund.dev.movementlog.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import se.magnulund.dev.movementlog.contracts.TripLogContract;
import se.magnulund.dev.movementlog.contracts.RawDataContract;
import se.magnulund.dev.movementlog.databases.TripLogDatabase;
import se.magnulund.dev.movementlog.databases.tables.RawDataTable;
import se.magnulund.dev.movementlog.databases.tables.TripsTable;

public class TripLogProvider extends ContentProvider {

    public static final String TAG = "TripLogProvider";

    public static final String AUTHORITY = "se.magnulund.dev.movementlog.providers";

    private static final UriMatcher uriMatcher;
    private static final int RAWDATA = 1;
    private static final int RAWDATA_ENTRY = 2;
    private static final int TRIPS = 3;
    private static final int TRIP_ENTRY = 4;


    private TripLogDatabase dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new TripLogDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case RAWDATA_ENTRY:
                qb.setTables(RawDataTable.TABLE);
                qb.setProjectionMap(RawDataTable.projectionMap);

                if (uri.getPathSegments() != null) {
                    qb.appendWhere(
                            RawDataContract.Columns._ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case RAWDATA:
                qb.setTables(RawDataTable.TABLE);
                qb.setProjectionMap(RawDataTable.projectionMap);
                break;
            case TRIP_ENTRY:
                qb.setTables(TripsTable.TABLE);
                qb.setProjectionMap(TripsTable.projectionMap);

                if (uri.getPathSegments() != null) {
                    qb.appendWhere(
                            TripLogContract.Columns._ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case TRIPS:
                qb.setTables(TripsTable.TABLE);
                qb.setProjectionMap(TripsTable.projectionMap);
                break;
            default:
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            switch (uriMatcher.match(uri)) {
                case RAWDATA_ENTRY:
                case RAWDATA:
                    orderBy = RawDataContract.DEFAULT_SORT_ORDER;
                    break;
                case TRIPS:
                case TRIP_ENTRY:
                    orderBy = TripLogContract.DEFAULT_SORT_ORDER;
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
                return RawDataContract.CONTENT_TYPE;
            // get a particular entry
            case RAWDATA_ENTRY:
                return RawDataContract.CONTENT_ITEM_TYPE;
            case TRIPS:
                return TripLogContract.CONTENT_TYPE;
            // get a particular entry
            case TRIP_ENTRY:
                return TripLogContract.CONTENT_ITEM_TYPE;
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
                if (!contentValues.containsKey(RawDataContract.Columns.TIMESTAMP)) {
                    throw new SQLException("Timestamp must be specified");
                }
                if (!contentValues.containsKey(RawDataContract.Columns.ACTIVITY_TYPE)) {
                    throw new SQLException("Activity type must be specified");
                }
                if (!contentValues.containsKey(RawDataContract.Columns.CONFIDENCE)) {
                    throw new SQLException("Confidence must be specified");
                }
                if (!contentValues.containsKey(RawDataContract.Columns.CONFIDENCE_RANK)) {
                    throw new SQLException("Confidence rank must be specified");
                }

                dbTable = RawDataTable.TABLE;

                break;
            case TRIPS:
                if (!contentValues.containsKey(TripLogContract.Columns.START_TIME)) {
                    throw new SQLException("Start time must be specified");
                }
                if (!contentValues.containsKey(TripLogContract.Columns.TRIP_TYPE)) {
                    throw new SQLException("Trip type must be specified");
                }

                dbTable = TripsTable.TABLE;

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
                        RawDataTable.TABLE,
                        selection,
                        selectionArgs);
                break;
            case RAWDATA_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            RawDataTable.TABLE,
                            RawDataContract.Columns._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            case TRIPS:
                count = db.delete(
                        TripsTable.TABLE,
                        selection,
                        selectionArgs);
                break;
            case TRIP_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            TripsTable.TABLE,
                            TripLogContract.Columns._ID + " = "
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
                        RawDataTable.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case RAWDATA_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.update(
                            RawDataTable.TABLE,
                            values,
                            RawDataContract.Columns._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            case TRIPS:
                count = db.update(
                        TripsTable.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TRIP_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.update(
                            TripsTable.TABLE,
                            values,
                            TripLogContract.Columns._ID + " = "
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
        uriMatcher.addURI(AUTHORITY, RawDataContract.URI_PART_ALL_CONTENT, RAWDATA);
        uriMatcher.addURI(AUTHORITY, RawDataContract.URI_PART_SINGLE_ITEM, RAWDATA_ENTRY);
        uriMatcher.addURI(AUTHORITY, TripLogContract.URI_PART_ALL_CONTENT, TRIPS);
        uriMatcher.addURI(AUTHORITY, TripLogContract.URI_PART_SINGLE_ITEM, TRIP_ENTRY);
    }


}
