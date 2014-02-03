package se.magnulund.dev.movementlog.databases.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

import se.magnulund.dev.movementlog.contracts.TripLogContract;

public class TripsTable {

    public static final String TABLE = "trips";
    private static final String TABLE_CREATE =
            "create table " + TABLE + "("
                    + TripLogContract.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TripLogContract.Columns.TRIP_TYPE + " INTEGER, "
                    + TripLogContract.Columns.START_TIME + " INTEGER, "
                    + TripLogContract.Columns.STARTED_BY_ID + " INTEGER, "
                    + TripLogContract.Columns.STARTED_BY_TYPE + " INTEGER, "
                    + TripLogContract.Columns.START_CONFIRMED_BY_ID + " INTEGER, "
                    + TripLogContract.Columns.START_COORDS + " TEXT, "
                    + TripLogContract.Columns.END_TIME + " INTEGER, "
                    + TripLogContract.Columns.ENDED_BY_ID + " INTEGER, "
                    + TripLogContract.Columns.ENDED_BY_TYPE + " INTEGER, "
                    + TripLogContract.Columns.END_CONFIRMED_BY_ID + " INTEGER, "
                    + TripLogContract.Columns.END_COORDS + " TEXT, "
                    + TripLogContract.Columns.CONFIRMED_AS + " INTEGER"
                    + ");";


    public static final HashMap<String, String> projectionMap;

    static {
        projectionMap = new HashMap<>();
        projectionMap.put(TripLogContract.Columns._ID, TripLogContract.Columns._ID);
        projectionMap.put(TripLogContract.Columns.TRIP_TYPE, TripLogContract.Columns.TRIP_TYPE);
        projectionMap.put(TripLogContract.Columns.START_TIME, TripLogContract.Columns.START_TIME);
        projectionMap.put(TripLogContract.Columns.STARTED_BY_ID, TripLogContract.Columns.STARTED_BY_ID);
        projectionMap.put(TripLogContract.Columns.STARTED_BY_TYPE, TripLogContract.Columns.STARTED_BY_TYPE);
        projectionMap.put(TripLogContract.Columns.START_CONFIRMED_BY_ID, TripLogContract.Columns.START_CONFIRMED_BY_ID);
        projectionMap.put(TripLogContract.Columns.START_COORDS, TripLogContract.Columns.START_COORDS);
        projectionMap.put(TripLogContract.Columns.END_TIME, TripLogContract.Columns.END_TIME);
        projectionMap.put(TripLogContract.Columns.ENDED_BY_ID, TripLogContract.Columns.ENDED_BY_ID);
        projectionMap.put(TripLogContract.Columns.ENDED_BY_TYPE, TripLogContract.Columns.ENDED_BY_TYPE);
        projectionMap.put(TripLogContract.Columns.END_CONFIRMED_BY_ID, TripLogContract.Columns.END_CONFIRMED_BY_ID);
        projectionMap.put(TripLogContract.Columns.END_COORDS, TripLogContract.Columns.END_COORDS);
        projectionMap.put(TripLogContract.Columns.CONFIRMED_AS, TripLogContract.Columns.CONFIRMED_AS);
    }

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("Content provider database", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
