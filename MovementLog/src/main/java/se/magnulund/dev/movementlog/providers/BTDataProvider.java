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

import se.magnulund.dev.movementlog.contracts.BTContract;
import se.magnulund.dev.movementlog.databases.BTDatabase;
import se.magnulund.dev.movementlog.databases.tables.BTConnectionEventsTable;
import se.magnulund.dev.movementlog.databases.tables.BTDevicesTable;

public class BTDataProvider extends ContentProvider {

    public static final String TAG = "BTDataProvider";

    public static final String AUTHORITY = "se.magnulund.dev.movementlog.providers.bt";

    private static final UriMatcher uriMatcher;
    private static final int BT_DEVICES = 1;
    private static final int BT_DEVICES_ENTRY = 2;
    private static final int BT_CONNECTION_EVENTS = 3;
    private static final int BT_CONNECTION_EVENTS_ENTRY = 4;


    private BTDatabase dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new BTDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case BT_DEVICES_ENTRY:
                qb.setTables(BTDevicesTable.TABLE);
                qb.setProjectionMap(BTDevicesTable.projectionMap);

                if (uri.getPathSegments() != null) {
                    qb.appendWhere(
                            BTContract.Devices.Columns._ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case BT_DEVICES:
                qb.setTables(BTDevicesTable.TABLE);
                qb.setProjectionMap(BTDevicesTable.projectionMap);
                break;
            case BT_CONNECTION_EVENTS_ENTRY:
                qb.setTables(BTConnectionEventsTable.TABLE);
                qb.setProjectionMap(BTConnectionEventsTable.projectionMap);

                if (uri.getPathSegments() != null) {
                    qb.appendWhere(
                            BTContract.ConnectionEvents.Columns._ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case BT_CONNECTION_EVENTS:
                qb.setTables(BTConnectionEventsTable.TABLE);
                qb.setProjectionMap(BTConnectionEventsTable.projectionMap);
                break;
            default:
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            switch (uriMatcher.match(uri)) {
                case BT_DEVICES_ENTRY:
                case BT_DEVICES:
                    orderBy = BTContract.Devices.DEFAULT_SORT_ORDER;
                    break;
                /*case BT_CONNECTION_EVENTS:
                case BT_CONNECTION_EVENTS_ENTRY:
                    orderBy = BTContract.ConnectionEvents.DEFAULT_SORT_ORDER;
                    break;*/
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
            case BT_DEVICES:
                return BTContract.Devices.CONTENT_TYPE;
            // get a particular entry
            case BT_DEVICES_ENTRY:
                return BTContract.Devices.CONTENT_ITEM_TYPE;
            case BT_CONNECTION_EVENTS:
                return BTContract.ConnectionEvents.CONTENT_TYPE;
            // get a particular entry
            case BT_CONNECTION_EVENTS_ENTRY:
                return BTContract.ConnectionEvents.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // Implements the provider's insert method
    @Override
    public Uri insert(Uri uri, ContentValues startValues) {

        final int match = uriMatcher.match(uri);

        // Validate the requested uri
        if (match != BT_DEVICES && match != BT_CONNECTION_EVENTS) {
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
            case BT_DEVICES:
                if (!contentValues.containsKey(BTContract.Devices.Columns.ADDRESS)) {
                    throw new SQLException("ADDRESS must be specified");
                }
                if (!contentValues.containsKey(BTContract.Devices.Columns.NAME)) {
                    throw new SQLException("NAME must be specified");
                }
                if (!contentValues.containsKey(BTContract.Devices.Columns.MAJOR_CLASS)) {
                    throw new SQLException("MAJOR_CLASS must be specified");
                }
                if (!contentValues.containsKey(BTContract.Devices.Columns.SUBCLASS)) {
                    throw new SQLException("SUBCLASS must be specified");
                }

                dbTable = BTDevicesTable.TABLE;

                break;
            case BT_CONNECTION_EVENTS:
                if (!contentValues.containsKey(BTContract.ConnectionEvents.Columns.TIMESTAMP)) {
                    throw new SQLException("TIMESTAMP must be specified");
                }
                if (!contentValues.containsKey(BTContract.ConnectionEvents.Columns.DEVICE_ID)) {
                    throw new SQLException("DEVICE_ID must be specified");
                }
                if (!contentValues.containsKey(BTContract.ConnectionEvents.Columns.CONNECTION_STATE)) {
                    throw new SQLException("CONNECTION_STATE must be specified");
                }

                dbTable = BTConnectionEventsTable.TABLE;

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
            case BT_DEVICES:
                count = db.delete(
                        BTDevicesTable.TABLE,
                        selection,
                        selectionArgs);
                break;
            case BT_DEVICES_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            BTDevicesTable.TABLE,
                            BTContract.Devices.Columns._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            case BT_CONNECTION_EVENTS:
                count = db.delete(
                        BTConnectionEventsTable.TABLE,
                        selection,
                        selectionArgs);
                break;
            case BT_CONNECTION_EVENTS_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.delete(
                            BTConnectionEventsTable.TABLE,
                            BTContract.ConnectionEvents.Columns._ID + " = "
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
            case BT_DEVICES:
                count = db.update(
                        BTDevicesTable.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case BT_DEVICES_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.update(
                            BTDevicesTable.TABLE,
                            values,
                            BTContract.Devices.Columns._ID + " = "
                                    + uri.getPathSegments().get(1)
                                    + (!TextUtils.isEmpty(selection) ?
                                    " AND (" + selection + ')'
                                    : ""),
                            selectionArgs);
                }

                break;
            case BT_CONNECTION_EVENTS:
                count = db.update(
                        BTConnectionEventsTable.TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case BT_CONNECTION_EVENTS_ENTRY:
                if (uri.getPathSegments() != null) {
                    count = db.update(
                            BTConnectionEventsTable.TABLE,
                            values,
                            BTContract.ConnectionEvents.Columns._ID + " = "
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
        uriMatcher.addURI(AUTHORITY, BTContract.Devices.URI_PART_ALL_CONTENT, BT_DEVICES);
        uriMatcher.addURI(AUTHORITY, BTContract.Devices.URI_PART_SINGLE_ITEM, BT_DEVICES_ENTRY);
        uriMatcher.addURI(AUTHORITY, BTContract.ConnectionEvents.URI_PART_ALL_CONTENT, BT_CONNECTION_EVENTS);
        uriMatcher.addURI(AUTHORITY, BTContract.ConnectionEvents.URI_PART_SINGLE_ITEM, BT_CONNECTION_EVENTS_ENTRY);
    }


}
