package se.magnulund.dev.movementlog;

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

public class MovementContentProvider extends ContentProvider {

    public static final String TAG = "MovementContentProvider";

    public static final String PROVIDER_NAME = "se.magnulund.dev.movementlog.provider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);

    /**
     * DATABASE STUFF
     */

    public static final String _ID = "_id";
    // ADD MORE DB COLUMN NAMES!

    private SQLiteDatabase movementDataDB;
    private static final String DATABASE_NAME = "MovementData";
    private static final String DATABASE_TABLE = "data";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE +
                    "(_id integer primary key autoincrement);";

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

    private static final int SINGLE = 1;
    private static final int LIST = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "data", SINGLE);
        uriMatcher.addURI(PROVIDER_NAME, "data/#", LIST);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {

        /*
         * Creates a new helper object. This method always returns quickly.
         * Notice that the database itself isn't created or opened
         * until SQLiteOpenHelper.getWritableDatabase is called
         */
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);

        switch (uriMatcher.match(uri)) {
            case SINGLE:
                if (uri.getPathSegments() != null) {
                    sqlBuilder.appendWhere(
                            _ID + " = " + uri.getPathSegments().get(1));
                }
                break;
            case LIST:
            default:
        }

        if (sortOrder == null || sortOrder.equals(""))
            sortOrder = _ID;

        Cursor cursor = sqlBuilder.query(
                movementDataDB,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

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
            case LIST:
                return "vnd.android.cursor.dir/vnd.magnulund.movementdata ";
            // get a particular entry
            case SINGLE:
                return "vnd.android.cursor.item/vnd.magnulund.movementdata ";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }


    // create/open the database
    private void getDatabase() {

        movementDataDB = dbHelper.getWritableDatabase();

        if (movementDataDB == null) {
            throw new SQLException("Failed to create movementDB -" + TAG);
        }

    }

    // Implements the provider's insert method
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        getDatabase();

        // add a new entry
        long rowID = movementDataDB.insert(
                DATABASE_TABLE, "", values);

        // if added successfully
        if (rowID == 0) {
            throw new SQLException("Failed to insert row into " + uri + " - " + TAG);
        }

        // Get uri and notify about change
        Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(_uri, null);
        }
        return _uri;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        getDatabase();

        int count = 0;
        switch (uriMatcher.match(uri)) {
            case LIST:
                count = movementDataDB.delete(
                        DATABASE_TABLE,
                        selection,
                        selectionArgs);
                break;
            case SINGLE:
                if (uri.getPathSegments() != null) {
                    count = movementDataDB.delete(
                            DATABASE_TABLE,
                            _ID + " = "
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

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case LIST:
                count = movementDataDB.update(
                        DATABASE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case SINGLE:
                if (uri.getPathSegments() != null) {
                    count = movementDataDB.update(
                            DATABASE_TABLE,
                            values,
                            _ID + " = "
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
        if (getContext() != null && count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }


}
